package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.BaseTest;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
class DisplayLabelTest extends BaseTest {

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException {
    var fileSystemManager = prepareFileSystem();
    var applicationConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    applicationConfigurationService.init();
    var configuration = applicationConfigurationService.getConfigurationData();
    configuration.getLabelConfig().setDisplayLabel(false);

    OntologyManager ontologyManager = prepareOntology(tempHomeDir);
    labelProviderTest = new LabelProvider(applicationConfigurationService, ontologyManager);
  }

  @Test
  void testDisplayLabel() {
    if (labelProviderTest == null) {
      fail("Label provider is null");
    }
    Map<String, String> expectedResult = new HashMap<>();
    expectedResult.put("http://example.com/Class5Test", "Class5Test");
    expectedResult.put("http://example.com/SubClass3Test", "SubClass3Test");
    expectedResult.put("http://example.com/Class4Test", "Class4Test");
    expectedResult.put("http://example.com/SubClass4Test", "SubClass4Test");
    expectedResult.put("http://example.com/SubClass5Test", "SubClass5Test");
    for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
      String result = labelProviderTest.getLabelOrDefaultFragment(IRI.create(entry.getKey()));
      assertEquals(entry.getValue(), result);
    }
  }
}
