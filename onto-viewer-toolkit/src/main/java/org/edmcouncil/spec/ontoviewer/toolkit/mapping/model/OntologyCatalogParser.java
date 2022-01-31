package org.edmcouncil.spec.ontoviewer.toolkit.mapping.model;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.simpleframework.xml.core.Persister;

public class OntologyCatalogParser {

  public Catalog readOntologyMapping(String ontologyMappingPath) throws OntoViewerToolkitException {
    if (Files.notExists(Path.of(ontologyMappingPath))) {
      var message = "Ontology mapping file doesn't exists in path '" + ontologyMappingPath + "'.";
      throw new OntoViewerToolkitException(message);
    }

    try (var ontologyMappingReader = new FileReader(ontologyMappingPath)) {
      var persister = new Persister();
      return persister.read(Catalog.class, ontologyMappingReader);
    } catch (Exception ex) {
      throw new OntoViewerToolkitException("Unable to load catalog with ontology mappings.", ex);
    }
  }
} 