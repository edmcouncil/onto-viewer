package org.edmcouncil.spec.ontoviewer.toolkit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfigProperties {

  @Value("${ov.application.name}")
  private String applicationName;
  @Value("${ov.application.version:unknown}")
  private String applicationVersion;
  @Value("${git.commit.id:unknown}")
  private String commitId;

  public String getApplicationName() {
    return applicationName;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public String getCommitId() {
    return commitId;
  }
}