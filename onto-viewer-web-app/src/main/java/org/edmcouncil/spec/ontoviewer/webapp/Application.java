package org.edmcouncil.spec.ontoviewer.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ServletComponentScan
@ComponentScan(basePackages = {
    "org.edmcouncil.spec.ontoviewer.core",
    "org.edmcouncil.spec.ontoviewer.configloader",
    "org.edmcouncil.spec.ontoviewer.webapp"
})
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}