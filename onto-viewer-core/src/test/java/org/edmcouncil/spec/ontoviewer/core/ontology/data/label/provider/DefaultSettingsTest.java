package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.MemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
public class DefaultSettingsTest extends BasicOntologyLoader {

  @TempDir
  Path tempDir;

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws IOException, OWLOntologyCreationException, URISyntaxException {
    var configurationService = new MemoryBasedConfigurationService();
    OntologyManager ontologyManager = prepareOntology(tempDir);
    labelProviderTest = new LabelProvider(configurationService, ontologyManager);
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