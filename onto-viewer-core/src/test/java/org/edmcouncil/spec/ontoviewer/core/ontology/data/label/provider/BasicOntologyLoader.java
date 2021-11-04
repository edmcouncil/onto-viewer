package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author patrycja.miazek
 */
public abstract class BasicOntologyLoader {

  public void prepareOntologyFiles(String ontologyFileName, Path tempDir) throws IOException, URISyntaxException {
    var ontologyLocationPath = getClass().getResource("/integartion_test/tests_ontology");
    if (ontologyLocationPath == null) {
      fail("Unable to find ontology files.");
    }
    var ontologyLocation = Path.of(ontologyLocationPath.toURI());
    Files.copy(ontologyLocation.resolve(ontologyFileName),
        tempDir.resolve(ontologyFileName));
  }

  public OntologyManager prepareOntology(Path tempDir) throws IOException, URISyntaxException, OWLOntologyCreationException {
    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
    OntologyManager ontologyManager = new OntologyManager();
    var ontologyFileName = "LabelProviderTest.owl";
    prepareOntologyFiles(ontologyFileName, tempDir);

    OWLOntology ontology = owlOntologyManager
        .loadOntologyFromOntologyDocument(
            tempDir
                .resolve("LabelProviderTest.owl")
                .toFile());
    ontologyManager.updateOntology(ontology);
    return ontologyManager;
  }
}
