package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.AppConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.util.CollectionUtils;
import org.xml.sax.SAXException;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
public class LabelDefinedTest {

  @TempDir
  Path tempDir;

  private LabelProvider labelProviderTest;
  private AppConfiguration config;
  private ViewerCoreConfiguration viewerCoreConfigurationTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException, ParserConfigurationException, XPathExpressionException, SAXException {

    OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
    ViewerCoreConfiguration viewerCoreConfiguration = new ViewerCoreConfiguration();

    OntologyManager ontologyManager = new OntologyManager();

    LabelPriority labelPriorityVal = new LabelPriority();
    labelPriorityVal.setType(ConfigItemType.PRIORITY);
    labelPriorityVal.setValue(LabelPriority.Priority.USER_DEFINED);
    viewerCoreConfiguration.addConfigElement(ConfigKeys.LABEL_PRIORITY, labelPriorityVal);

    BooleanItem displayLabel = new BooleanItem();
    displayLabel.setType(ConfigItemType.BOOLEAN);
    displayLabel.setValue(true);
    viewerCoreConfiguration.addConfigElement(ConfigKeys.DISPLAY_LABEL, displayLabel);

    

    DefaultLabelItem defaultLabelItem = new DefaultLabelItem("http://example.com/SubClass3Test", "SubClass_3_Test_user_defined");
    viewerCoreConfiguration.addConfigElement(ConfigKeys.USER_DEFAULT_NAME_LIST, defaultLabelItem);

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

  @AfterEach
  public void tearDown() {
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
  void testLabelDefined() {
    if (labelProviderTest == null) {
      fail("Label provider is null");
    }
    Map<String, String> expectedResult = new HashMap<>();

    expectedResult.put("http://example.com/SubClass3Test", "SubClass_3_Test_user_defined");

    for (Map.Entry<String, String> entry : expectedResult.entrySet()) {
      String result = labelProviderTest.getLabelOrDefaultFragment(IRI.create(entry.getKey()));
      assertEquals(entry.getValue(), result);

    }
  }

}
