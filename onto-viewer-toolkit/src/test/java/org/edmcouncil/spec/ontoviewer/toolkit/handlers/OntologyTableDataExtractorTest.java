package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlMemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.EntityData;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.toolkit.OntoViewerToolkitApplication;
import org.edmcouncil.spec.ontoviewer.toolkit.OntoViewerToolkitCommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootTest
@AutoConfigureMockMvc
class OntologyTableDataExtractorTest {

  private static final String TEST_FILTER_PATTERN = "test1";
  private static final int NUMBER_OF_ENTITIES = 5;

  @Autowired
  private OntologyTableDataExtractor ontologyTableDataExtractor;
  @Autowired
  private OntologyManager ontologyManager;

  @MockBean
  private OntoViewerToolkitCommandLine ontoViewerToolkitCommandLine;

  @BeforeEach
  void setUp() throws OWLOntologyCreationException {
    var ontologyInputStream = getClass().getResourceAsStream("/ontologies/test1.rdf");
    ontologyManager.updateOntology(readOntology(ontologyInputStream));
  }

  @Test
  void shouldExtractCorrectEntityDataForClass() {
    var expectedEntityData = createEntityData("http://example.com/test1/A",
        "A",
        "Class",
        "'A' Ontology",
        "'A' synonym",
        "'A' definition",
        "A is a kind of Thing.",
        "'A' example",
        "'A' explanatory note",
        "NOT_SET");

    var actualResult = ontologyTableDataExtractor.extractEntityData();

    assertEquals(NUMBER_OF_ENTITIES, actualResult.size());
    assertEquals(expectedEntityData, getEntityDataWithIri(actualResult, "http://example.com/test1/A"));
  }

  @Test
  void shouldExtractCorrectEntityDataForIndividual() {
    var expectedEntityData = createEntityData("http://example.com/test1/A1",
        "A1",
        "Individual",
        "'A' Ontology",
        "",
        "'A1' definition",
        "",
        "",
        "",
        "NOT_SET");

    var actualResult = ontologyTableDataExtractor.extractEntityData();

    assertEquals(NUMBER_OF_ENTITIES, actualResult.size());
    assertEquals(expectedEntityData, getEntityDataWithIri(actualResult, "http://example.com/test1/A1"));
  }

  @Test
  void shouldExtractCorrectEntityDataForObjectProperty() {
    var expectedEntityData = createEntityData("http://example.com/test1/B1",
        "'B1' object property label",
        "Object Property",
        "'A' Ontology",
        "",
        "'B1' definition",
        "'B1' object property label is a kind of topObjectProperty.",
        "",
        "",
        "NOT_SET");

    var actualResult = ontologyTableDataExtractor.extractEntityData();

    assertEquals(NUMBER_OF_ENTITIES, actualResult.size());
    assertEquals(expectedEntityData, getEntityDataWithIri(actualResult, "http://example.com/test1/B1"));
  }

  @Test
  void shouldExtractCorrectEntityDataForDataProperty() {
    var expectedEntityData = createEntityData("http://example.com/test1/C1",
        "'C1' data property label",
        "Data Property",
        "'A' Ontology",
        "",
        "'C1' definition",
        "'C1' data property label is a kind of topDataProperty.",
        "",
        "'C1' explanatory note",
        "NOT_SET");

    var actualResult = ontologyTableDataExtractor.extractEntityData();

    assertEquals(NUMBER_OF_ENTITIES, actualResult.size());
    assertEquals(expectedEntityData, getEntityDataWithIri(actualResult, "http://example.com/test1/C1"));
  }

  private EntityData getEntityDataWithIri(List<EntityData> entityDataList, String entityIri) {
    for (EntityData entityData : entityDataList) {
      if (entityData.getIri().equals(entityIri)) {
        return entityData;
      }
    }

    throw new IllegalStateException(String.format("EntityData with IRI '%s' not found.", entityIri));
  }

  private OWLOntology readOntology(InputStream ontologyInputStream) throws OWLOntologyCreationException {
    var owlOntologyManager = OWLManager.createOWLOntologyManager();
    return owlOntologyManager.loadOntologyFromOntologyDocument(ontologyInputStream);
  }

  private EntityData createEntityData(String iri, String termLabel, String typeLabel, String ontology, String synonyms,
      String definition, String generatedDescription, String examples, String explanations, String maturity) {
    var entityData = new EntityData();
    entityData.setIri(iri);
    entityData.setTermLabel(termLabel);
    entityData.setTypeLabel(typeLabel);
    entityData.setOntology(ontology);
    entityData.setSynonyms(synonyms);
    entityData.setDefinition(definition);
    entityData.setGeneratedDefinition(generatedDescription);
    entityData.setExamples(examples);
    entityData.setExplanations(explanations);
    entityData.setMaturity(maturity);
    return entityData;
  }

  @ComponentScan(basePackageClasses = OntoViewerToolkitApplication.class)
  @Configuration
  static class IntegrationTestsConfiguration {

    @Primary
    @Bean
    ApplicationConfigurationService applicationConfigurationService() {
      var configurationService = new YamlMemoryBasedConfigurationService();

      var glossaryGroup = configurationService.getConfigurationData().getGroupsConfig()
          .getGroups()
          .get("Glossary");
      glossaryGroup.addAll(List.of(
          "https://www.omg.org/spec/Commons/AnnotationVocabulary/synonym",
          "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym",
          "http://www.w3.org/2004/02/skos/core#definition",
          "http://www.w3.org/2004/02/skos/core#example",
          "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/explanatoryNote"));

      configurationService.getConfigurationData().getToolkitConfig().setFilterPattern(TEST_FILTER_PATTERN);

      return configurationService;
    }
  }
}