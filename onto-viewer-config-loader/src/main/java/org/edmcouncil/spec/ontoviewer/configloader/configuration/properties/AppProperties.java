package org.edmcouncil.spec.ontoviewer.configloader.configuration.properties;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private String defaultHomePath;
  private String viewerConfigFileName;
  private String defaultOntologyFileName;
  private String configDownloadPath;
  private Map<String, Object> search;
  private String fallbackUrl;

  public String getConfigDownloadPath() {
    return configDownloadPath;
  }

  public void setConfigDownloadPath(String configPath) {
    this.configDownloadPath = configPath;
  }

  public String getDefaultHomePath() {
    return defaultHomePath;
  }

  public void setDefaultHomePath(String defaultHomePath) {
    this.defaultHomePath = defaultHomePath;
  }

  public String getViewerConfigFileName() {
    return viewerConfigFileName;
  }

  public void setViewerConfigFileName(String viewerConfigFileName) {
    this.viewerConfigFileName = viewerConfigFileName;
  }

  public String getDefaultOntologyFileName() {
    return defaultOntologyFileName;
  }

  public void setDefaultOntologyFileName(String defaultOntologyFileName) {
    this.defaultOntologyFileName = defaultOntologyFileName;
  }

  public Map<String, Object> getSearch() {
    return this.search;
  }

  public void setSearch(Map<String, Object> search) {
    this.search = search;
  }

  public String getFallbackUrl() {
    return fallbackUrl;
  }

  public void setFallbackUrl(String fallbackUrl) {
    this.fallbackUrl = fallbackUrl;
  }
}
