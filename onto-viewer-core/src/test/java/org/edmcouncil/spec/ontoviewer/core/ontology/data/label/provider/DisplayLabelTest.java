package org.edmcouncil.spec.ontoviewer.core.ontology.data.label.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.xml.sax.SAXException;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
public class DisplayLabelTest extends BasicOntologyLoader {

  @TempDir
  Path tempDir;

  private LabelProvider labelProviderTest;

  @BeforeEach
  public void setUp() throws URISyntaxException, IOException, OWLOntologyCreationException, ParserConfigurationException, XPathExpressionException, SAXException {
    ViewerCoreConfiguration viewerCoreConfiguration = new ViewerCoreConfiguration();

    OntologyManager ontologyManager = prepareOntology(tempDir);
    BooleanItem displayLabel = new BooleanItem();
    displayLabel.setType(ConfigItemType.BOOLEAN);
    displayLabel.setValue(false);
    viewerCoreConfiguration.addConfigElement(ConfigKeys.DISPLAY_LABEL, displayLabel);

    labelProviderTest = new LabelProvider(viewerCoreConfiguration);
    labelProviderTest.setOntologyManager(ontologyManager);
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
