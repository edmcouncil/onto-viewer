package org.edmcouncil.spec.ontoviewer.webapp.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class ModelBuilderFactory {

  @Value("${build.version}")
  private String version;

  public ModelBuilder getInstance(Model model) {
    var modelBuilder = new ModelBuilder(model);
    modelBuilder.setVersion(version);
    return modelBuilder;
  }
}