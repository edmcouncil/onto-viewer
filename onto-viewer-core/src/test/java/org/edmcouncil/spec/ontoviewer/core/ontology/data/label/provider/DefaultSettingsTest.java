package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.BaseTest;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
class DefaultSettingsTest extends BaseTest {

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws IOException, OWLOntologyCreationException, URISyntaxException {
    var fileSystemManager = prepareFileSystem();
    var configurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    configurationService.init();

    var ontologyManager = prepareOntology(tempHomeDir);
    labelProviderTest = new LabelProvider(configurationService, ontologyManager);
  }

  @Test
  void getLabelOrDefaultFragmentTest() {
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
  void getLabelOrDefaultFragmentIriTest() {
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
