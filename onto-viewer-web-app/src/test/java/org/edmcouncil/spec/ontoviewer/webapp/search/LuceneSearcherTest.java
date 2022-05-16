package org.edmcouncil.spec.ontoviewer.webapp.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;

class LuceneSearcherTest {

  private static final String EXAMPLE_ONTOLOGY_PATH = "/ontology/search_example_ontology.rdf";

  private LuceneSearcher luceneSearcher;

  private Path tempHomePath;

  @BeforeEach
  void setUp() throws OWLException, IOException {
    tempHomePath = Files.createTempDirectory("onto-viewer-tests");

    var appProperties = new AppProperties();
    appProperties.setDefaultHomePath(tempHomePath.toString());
    appProperties.setConfigPath("config");
    appProperties.setSearch(Map.of("reindexOnStart", "true"));

    var fileSystemManager = new FileSystemManager(appProperties);
    var applicationConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    applicationConfigurationService.init();

    var ontologyManager = prepareOntologyManager();
    this.luceneSearcher = new LuceneSearcher(
        appProperties, fileSystemManager, ontologyManager, applicationConfigurationService);
    this.luceneSearcher.init();
    this.luceneSearcher.populateIndex();
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(tempHomePath.toFile());
  }

  private OntologyManager prepareOntologyManager() throws OWLException {
    var exampleOntologyPath = getClass().getResourceAsStream(EXAMPLE_ONTOLOGY_PATH);
    if (exampleOntologyPath == null) {
      throw new IllegalStateException(
          String.format("Example ontology in path '%s' not found.", EXAMPLE_ONTOLOGY_PATH));
    }
    var owlOntologyManager = OWLManager.createOWLOntologyManager();
    var ontology = owlOntologyManager.loadOntologyFromOntologyDocument(exampleOntologyPath);

    var ontologyManager = new OntologyManager();
    ontologyManager.updateOntology(ontology);
    return ontologyManager;
  }

  // Basic search

  @Test
  void shouldReturnTheBestMatchForSpecificTerm() {
    var actualResult = luceneSearcher.search("mortgage", true);

    assertThat(actualResult.size(), equalTo(3));
    assertThat(actualResult.get(0).getIri(), equalTo("http://www.example.com/Mortgage"));
  }

  @Test
  void shouldReturnTheBestMatchForSpecificMultiWordsTerm() {
    var actualResult = luceneSearcher.search("sponsored loan", true);

    assertThat(actualResult.size(), equalTo(1));
    assertThat(actualResult.get(0).getIri(),
        equalTo("http://www.example.com/GovernmentSponsoredLoan"));
  }

  @Test
  void shouldReturnTheBestMatchForWhenSearchingWithinRdfsLabelSubAnnotation() {
    var actualResult = luceneSearcher.search("government", true);

    assertThat(actualResult.size(), equalTo(1));
    assertThat(actualResult.get(0).getIri(),
        equalTo("http://www.example.com/GovernmentSponsoredLoan"));
  }

  @Test
  void shouldReturnResultsWhenFuzzyMatchExists() {
    var actualResult = luceneSearcher.search("mortgagee", true);

    assertThat(actualResult.size(), equalTo(3));
  }

  @Test
  void shouldReturnEmptyResultsWhenThereIsNoMatch() {
    var actualResult = luceneSearcher.search("foobarbaz", true);

    assertTrue(actualResult.isEmpty());
  }

  @Test
  void shouldReturnCorrectHighlightForSearchResult() {
    var actualResult = luceneSearcher.search("reverse", true);

    assertThat(actualResult.size(), equalTo(1));
    assertThat(actualResult.get(0).getHighlights().get(0).getHighlightedText(),
        equalTo("<B>reverse</B> mortgage"));
  }

  @Test
  void shouldReturnTheBestMatchForSpecificTermWithoutHighlighting() {
    var actualResult = luceneSearcher.search("reverse", false);

    assertThat(actualResult.size(), equalTo(1));
    assertTrue(actualResult.get(0).getHighlights().isEmpty());
  }

  // Advance search
  @Test
  void shouldReturnTheBestMatchForSpecificTermWithAdvanceMode() {
    var properties = List.of("rdfs_label", "skos_definition");
    var actualResult = luceneSearcher.searchAdvance("contract", properties, true);

    assertThat(actualResult.size(), equalTo(2));
  }

  @Test
  void shouldReturnTheBestMatchForSpecificTermWithAdvanceModeWithoutHighlighting() {
    var properties = List.of("rdfs_label", "skos_definition");
    var actualResult = luceneSearcher.searchAdvance("contract", properties, false);

    assertThat(actualResult.size(), equalTo(2));
    assertTrue(actualResult.get(0).getHighlights().isEmpty());
  }

  @Test
  void shouldReturnEmptyResultWhenTermDoesNotOccurForSpecificTermWithAdvanceMode() {
    var properties = List.of("rdfs_label");
    var actualResult = luceneSearcher.searchAdvance("contract", properties, true);

    assertThat(actualResult.size(), equalTo(0));
  }

  // Find properties
  @Test
  void shouldReturnListOfSupportedFindProperties() {
    var actualResult = luceneSearcher.getFindProperties();

    assertThat(actualResult.size(), equalTo(6));
    assertThat(actualResult.get(0), equalTo(
        new FindProperty(
            "RDFS Label",
            "rdfs_label",
            "http://www.w3.org/2000/01/rdf-schema#label")));
  }
}