package org.edmcouncil.spec.fibo.view.configuration;

import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.springframework.context.annotation.Bean;

/**
 * TODO: do that correctly.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
/*@Configuration
@ComponentScan(basePackages = {
  "org.edmcouncil.spec.fibo.view",
  "org.edmcouncil.spec.fibo.weasel",
  "org.edmcouncil.spec.fibo.config"
})*/
public class Config {

  @Bean
  public FileSystemManager fileSystemManager() {
    return null;
  }
}
