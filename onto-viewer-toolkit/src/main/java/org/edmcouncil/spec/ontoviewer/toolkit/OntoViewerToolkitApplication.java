package org.edmcouncil.spec.ontoviewer.toolkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OntoViewerToolkitApplication {

  public static void main(String[] args) {
    try {
      SpringApplication.run(OntoViewerToolkitApplication.class, args);
      System.exit(0);
    }
    catch (Exception exception) {
      System.out.println(exception.getStackTrace());
      System.exit(1);}
  }
}
