package org.edmcouncil.spec.fibo.view.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class ModelBuilderFactory {

  @Value("${build.version}")
  private String version;

  public ModelBuilder getInstance(Model model) {
    ModelBuilder mb = new ModelBuilder(model);
    mb.setVersion(version);
    return mb;
  }

}
