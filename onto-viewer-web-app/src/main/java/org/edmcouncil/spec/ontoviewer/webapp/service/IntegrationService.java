package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.Integration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.IntegrationsConfig;
import org.edmcouncil.spec.ontoviewer.core.exception.IntegrationNotConfiguredException;
import org.edmcouncil.spec.ontoviewer.core.exception.RequestHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;

@Service
public class IntegrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationService.class);
  private static final String ACCESSTOKEN_KEY = "ACCESSTOKEN";
  private static final String ACCEPT = "Accept";
  private static final String AUTHORIZATION = "Authorization";
  private static final String DATA_WORLD_DESCRIBE_ID = "dwDescribe";
  private static final String INTEGRATIONS_KEY = "INTEGRATIONS";
  private static final String QUERY_PARAMETER = "query";
  private static final String OV_KEY_PREFIX = "OV";
  private static final String URL_KEY = "URL";

  private final ApplicationConfigurationService applicationConfigurationService;
  private final ConfigurableEnvironment environment;
  private final OkHttpClient httpClient = new OkHttpClient();

  private IntegrationsConfig integrationsConfig;

  public IntegrationService(ApplicationConfigurationService applicationConfigurationService,
      ConfigurableEnvironment environment) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.environment = environment;
  }

  @PostConstruct
  public void init() {
    setIntegrationProperties(environment);
  }

  public String getDataWorldDescribe(String iri) {
    if (iri == null || iri.isBlank()) {
      throw new RequestHandlingException("IRI shouldn't be blank");
    }
    var integrationOptional = integrationsConfig.getIntegration(DATA_WORLD_DESCRIBE_ID);
    if (integrationOptional.isEmpty()) {
      throw new IntegrationNotConfiguredException("No integration configuration for data.world describe integration.");
    }
    var integration = integrationOptional.get();

    String queryBody = getQueryBody(iri);
    FormBody body = new FormBody.Builder()
        .add(QUERY_PARAMETER, queryBody)
        .build();
    Request request = new Request.Builder()
        .url(integration.getUrl())
        .post(body)
        .addHeader(ACCEPT, "application/rdf+xml")
        .addHeader(AUTHORIZATION, getAccessTokenHeaderValue())
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      var responseCode = response.code();
      if (!response.isSuccessful()) {
        throw new RequestHandlingException(String.format("Request to data.world API wasn't successful. "
            + "The response ended with code %d and message: %s",
            responseCode,
            response.body().string()));
      }

      return response.body().string();
    } catch (Exception ex) {
      throw new RequestHandlingException(
          String.format("Exception occurred while handling data.world describe query: %s", ex.getMessage()),
          ex);
    }
  }

  private String getAccessTokenHeaderValue() {
    var integrationOptional = this.integrationsConfig.getIntegrationIgnoreCase(DATA_WORLD_DESCRIBE_ID);
    if (integrationOptional.isPresent()) {
      var integration = integrationOptional.get();
      return String.format("Bearer %s", integration.getAccessToken());
    }
    return "";
  }

  private String getQueryBody(String iri) {
    return String.format("DESCRIBE <%s>", iri);
  }

  private void setIntegrationProperties(ConfigurableEnvironment environment) {
    this.integrationsConfig = applicationConfigurationService.getConfigurationData().getIntegrationsConfig();
    environment.getSystemEnvironment().forEach((key, value) -> {
      if (key.startsWith(OV_KEY_PREFIX)) {
        var splitKey = List.of(key.split("_"));
        if (splitKey.size() == 4) {
          if (splitKey.get(1).equalsIgnoreCase(INTEGRATIONS_KEY)) {
            String integrationId = splitKey.get(2);
            String propertyName = splitKey.get(3);

            Optional<Integration> integrationOptional = this.integrationsConfig.getIntegrationIgnoreCase(integrationId);
            if (integrationOptional.isPresent()) {
              var integration = integrationOptional.get();

              switch (propertyName.toUpperCase()) {
                case ACCESSTOKEN_KEY: {
                  integration.setAccessToken(value.toString());
                  break;
                }
                case URL_KEY: {
                  integration.setUrl(value.toString());
                  break;
                }
                default: {
                  LOGGER.warn("Integration property key '{}' with value '{}' not recognised.", key, value);
                }
              }
            }
          }
        } else {
          LOGGER.warn(
              "Env variable starting with 'OV' should have 4 parts combined with '_' but is '{}' with value '{}'",
              key,
              value);
        }
      }
    });
  }
}