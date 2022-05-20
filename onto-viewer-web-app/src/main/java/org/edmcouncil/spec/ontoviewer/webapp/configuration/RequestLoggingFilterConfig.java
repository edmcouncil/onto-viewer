package org.edmcouncil.spec.ontoviewer.webapp.configuration;

import java.util.function.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

  @Bean
  public CommonsRequestLoggingFilter loggingFilter() {
    var loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(10000);
    loggingFilter.setHeaderPredicate(header -> !header.toLowerCase().equals("x-api-key"));
    loggingFilter.setHeaderPredicate(header -> !header.toLowerCase().equals("authorization"));
    loggingFilter.setHeaderPredicate(header -> !header.toLowerCase().equals("proxy-authorization"));

    return loggingFilter;
  }
}
