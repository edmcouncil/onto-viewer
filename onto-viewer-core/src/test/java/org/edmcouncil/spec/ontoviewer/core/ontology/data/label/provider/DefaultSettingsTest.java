package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.SAXException;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
public class DefaultSettingsTest {

  @TempDir
  Path tempDir;

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException, ParserConfigurationException, XPathExpressionException, SAXException {

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    OntologyManager ontologyManager = new OntologyManager();

    var ontologyFileName = "LabelProviderTest.owl";
    prepareOntologyFiles(ontologyFileName);

    OWLOntology ontology = owlOntologyManager
        .loadOntologyFromOntologyDocument(
            tempDir
                .resolve("LabelProviderTest.owl")
                .toFile());
    ontologyManager.updateOntology(ontology);

    ViewerCoreConfiguration viewerCoreConfiguration = new ViewerCoreConfiguration();
    labelProviderTest = new LabelProvider(viewerCoreConfiguration);
    labelProviderTest.setOntologyManager(ontologyManager);

  }

  private void prepareOntologyFiles(String ontologyFileName) throws IOException, URISyntaxException {
    var ontologyLocationPath = getClass().getResource("/integartion_test/tests_ontology");
    if (ontologyLocationPath == null) {
      fail("Unable to find ontology files.");
    }
    var ontologyLocation = Path.of(ontologyLocationPath.toURI());
    Files.copy(ontologyLocation.resolve(ontologyFileName),
        tempDir.resolve(ontologyFileName));
  }

  @Test
  public void getLabelOrDefaultFragmentTest() {
    if (labelProviderTest == null) {
      fail("Label provider is null");
    }
    Map<String, String> expectedResult = new HashMap<>();

    expectedResult.put("http://example.com/Class1Test", "Class_1_Test");
    expectedResult.put("http://example.com/Class4Test", "Class_4_Test");
    expectedResult.put("http://example.com/SubClass4Test", "SubClass_4_Test");
    expectedResult.put("http://example.com/Class5Test", "Class_5_Test_en");
    expectedResult.put("http://example.com/SubClass5Test", "SubClass_5_Test");
    for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
      String result = labelProviderTest.getLabelOrDefaultFragment(IRI.create(entry.getKey()));
      assertEquals(entry.getValue(), result);

    }
  }

  @Test
  public void getLabelOrDefaultFragmentIriTest() {
    Map<String, String> expectedResult = new HashMap<>();
    expectedResult.put("http://example.com/Class2Test", "Class2Test");
    expectedResult.put("http://example.com/SubClass2Test", "SubClass2Test");
    expectedResult.put("http://example.com/Class3Test", "Class3Test");
    expectedResult.put("http://example.com/SubClass1Test", "SubClass1Test");
    for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
      String result = labelProviderTest.getLabelOrDefaultFragment(IRI.create(entry.getKey()));
      assertEquals(entry.getValue(), result);

    }
  }
}
