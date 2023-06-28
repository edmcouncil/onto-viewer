package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.MissingLanguageAction;
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
class MissingLanguageActionTest extends BaseTest {

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException {
    var fileSystemManager = prepareFileSystem();
    var configurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    configurationService.init();

    configurationService.getConfigurationData().getLabelConfig().setLabelLang("pl");
    configurationService.getConfigurationData().getLabelConfig().setForceLabelLang(false);
    configurationService.getConfigurationData().getLabelConfig().setMissingLanguageAction(MissingLanguageAction.FIRST);

    OntologyManager ontologyManager = prepareOntology(tempHomeDir);
    labelProviderTest = new LabelProvider(configurationService, ontologyManager);
  }

  @Test
  void testLabelProviderMissingLanguageAction() {
    if (labelProviderTest == null) {
      fail("Label provider is null");
    }
    Map<String, String> expectedResult = new HashMap<>();
    expectedResult.put("http://example.com/Class5Test", "Class_5_Test_fr");
    expectedResult.put("http://example.com/SubClass3Test", "SubClass_3_Test_pl");

    for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
      String result = labelProviderTest.getLabelOrDefaultFragment(IRI.create(entry.getKey()));
      assertEquals(entry.getValue(), result);
    }
  }
}