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
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
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
public class MissingLanguageActionTest {

  @TempDir
  Path tempDir;

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException, ParserConfigurationException, XPathExpressionException, SAXException {

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
    ViewerCoreConfiguration viewerCoreConfiguration = new ViewerCoreConfiguration();
    OntologyManager ontologyManager = new OntologyManager();

    StringItem labelLang = new StringItem("pl");
    viewerCoreConfiguration.addConfigElement(ConfigKeys.LABEL_LANG, labelLang);

    BooleanItem forceLabelLang = new BooleanItem();
    forceLabelLang.setType(ConfigItemType.BOOLEAN);
    forceLabelLang.setValue(Boolean.valueOf(false));
    viewerCoreConfiguration.addConfigElement(ConfigKeys.FORCE_LABEL_LANG, forceLabelLang);

    MissingLanguageItem missingLanguageItem = new MissingLanguageItem();
    missingLanguageItem.setType(ConfigItemType.MISSING_LANGUAGE_ACTION);
    missingLanguageItem.setValue(MissingLanguageItem.Action.FIRST);
    viewerCoreConfiguration.addConfigElement(ConfigKeys.MISSING_LANGUAGE_ACTION, missingLanguageItem);

    var ontologyFileName = "LabelProviderTest.owl";
    prepareOntologyFiles(ontologyFileName);

    OWLOntology ontology = owlOntologyManager
        .loadOntologyFromOntologyDocument(
            tempDir
                .resolve("LabelProviderTest.owl")
                .toFile());
    ontologyManager.updateOntology(ontology);

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
