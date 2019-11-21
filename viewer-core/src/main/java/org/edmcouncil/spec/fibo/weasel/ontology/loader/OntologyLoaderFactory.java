package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntologyLoaderFactory {

  public OntologyLoader getInstance(ViewerCoreConfiguration viewerCoreConfiguration, FileSystemManager fsm) {
    if (viewerCoreConfiguration.isOntologyLocationSet()) {
      if (viewerCoreConfiguration.isOntologyLocationURL()) {
        return new UrlOntologyLoader();
      } else {
        return new FileOntologyLoader(fsm);
      }
    } else {
      return new FileOntologyLoader(fsm);
    }
  }

}
