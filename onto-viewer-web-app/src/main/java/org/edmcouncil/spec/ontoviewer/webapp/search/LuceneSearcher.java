package org.edmcouncil.spec.ontoviewer.webapp.search;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;
import static org.semanticweb.owlapi.vocab.Namespaces.SKOS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.TextSearcherConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.exception.RequestHandlingException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
public class LuceneSearcher {

  private static final Logger LOGGER = getLogger(LuceneSearcher.class);
  private static final String LUCENE_INDEX_NAME = "lucene_index";
  private static final String IRI_FIELD = "iri";
  private static final Map<String, String> IRI_TO_SHORT_ID = new HashMap<>();
  private static final String TYPE_FIELD = "type";
  private static final String QUERY_FIELD_DELIMITER = " OR ";
  private static final int DEFAULT_FUZZY_DISTANCE = 2;

  static {
    IRI_TO_SHORT_ID.put(RDFS.NAMESPACE, "rdfs");
    IRI_TO_SHORT_ID.put(SKOS.getPrefixIRI(), "skos");
    IRI_TO_SHORT_ID.put("http://purl.org/dc/terms/", "purl");
  }

  private final AppProperties appProperties;
  private final FileSystemManager fileSystemManager;
  private final OntologyManager ontologyManager;
  private final OWLDataFactory dataFactory;
  private final ConfigurationService configurationService;

  private int fuzzyDistance;

  private StandardAnalyzer analyzer;
  private FSDirectory indexDirectory;
  private Path indexPath;
  private IndexWriterConfig indexWriterConfig;
  private Set<OWLAnnotationProperty> allSearchProperties;
  private Set<String> allFieldNames;
  private Set<String> basicFieldNames;
  private Map<String, FindProperty> findPropertiesMap = new HashMap<>();
  private String rdfsLabelFieldName;

  public LuceneSearcher(AppProperties appProperties, FileSystemManager fileSystemManager,
      OntologyManager ontologyManager, ConfigurationService configurationService) {
    this.appProperties = appProperties;
    this.fileSystemManager = fileSystemManager;
    this.ontologyManager = ontologyManager;
    this.dataFactory = OWLManager.getOWLDataFactory();
    this.configurationService = configurationService;

    this.fuzzyDistance = getTextSearcherConfig().getFuzzyDistance();
    if (this.fuzzyDistance == -1) {
      this.fuzzyDistance = Integer.parseInt(
          getSearchProperty("fuzzyDistance", DEFAULT_FUZZY_DISTANCE));
      LOGGER.info(
          "'fuzzyDistance' not set in the configuration. Using default value: {}",
          this.fuzzyDistance);
    }
  }

  @PostConstruct
  void init() {
    this.indexPath = fileSystemManager.getViewerHomeDir().resolve(LUCENE_INDEX_NAME);
    this.analyzer = new StandardAnalyzer();
    this.indexWriterConfig = new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE_OR_APPEND);
  }

  @PreDestroy
  void close() {
    try {
      this.indexDirectory.close();
    } catch (IOException ex) {
      LOGGER.error("Unable to close index while destroying {}. Details: {}",
          this.getClass().getName(),
          ex.getMessage(),
          ex);
    }
  }

  public void populateIndex() {
    LOGGER.info("Starting to populate the Lucene index located on path '{}'...", indexPath);

    Set<OWLAnnotationProperty> rdfsLabelAndItsSubAnnotations = EntitySearcher.getSubProperties(
            dataFactory.getRDFSLabel(),
            ontologyManager.getOntologyWithImports())
        .collect(Collectors.toSet());
    rdfsLabelAndItsSubAnnotations.add(dataFactory.getRDFSLabel());

    basicFieldNames = rdfsLabelAndItsSubAnnotations.stream()
        .map(this::getFieldName)
        .collect(Collectors.toSet());

    var configuredFindProperties = getFindProperties();
    findPropertiesMap = configuredFindProperties.stream()
        .collect(Collectors.toMap(FindProperty::getIdentifier, findProperty -> findProperty));

    var findAnnotationProperties = configuredFindProperties.stream().map(findProperty ->
        dataFactory.getOWLAnnotationProperty(findProperty.getIri())
    ).collect(Collectors.toSet());

    allSearchProperties = new HashSet<>();
    allSearchProperties.addAll(rdfsLabelAndItsSubAnnotations);
    allSearchProperties.addAll(findAnnotationProperties);

    allFieldNames = allSearchProperties.stream()
        .map(this::getFieldName)
        .collect(Collectors.toSet());
    rdfsLabelFieldName = getFieldName(dataFactory.getRDFSLabel());

    boolean shouldReindexOnStart;
    try {
      if (getTextSearcherConfig().isReindexOnStart() != null) {
        shouldReindexOnStart = getTextSearcherConfig().isReindexOnStart();
      } else {
        shouldReindexOnStart = Boolean.parseBoolean(getSearchProperty("reindexOnStart", false));
        LOGGER.info(
            "'reindexOnStart' not set in the configuration. Using application property value '{}'.",
            shouldReindexOnStart);
      }
      if (shouldReindexOnStart) {
        LOGGER.info("Search configuration property 'reindexOnStart' is on. "
            + "Deleting the old index directory.");
        FileUtils.deleteDirectory(indexPath.toFile());
      }

      indexDirectory = FSDirectory.open(indexPath);
      var indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
      indexEntities(indexWriter);
    } catch (IOException ex) {
      LOGGER.error("Exception thrown while indexing entities. Details: {}", ex.getMessage(), ex);
    }
  }

  public List<FindResult> search(String query) {
    try {
      var queryParser = new QueryParser(getFieldName(dataFactory.getRDFSLabel()), analyzer);
      var parsedQuery = queryParser.parse(prepareQueryString(query));

      return searchWithQuery(parsedQuery);
    } catch (IOException | ParseException ex) {
      var errorMessage = String.format(
          "Error occurred when handling search request. Details: %s.", ex.getMessage());
      LOGGER.error(errorMessage, ex);

      throw new RequestHandlingException(errorMessage);
    }
  }

  public List<FindResult> searchAdvance(String query, List<String> findProperties) {
    try {
      var queryParser = new QueryParser(getFieldName(dataFactory.getRDFSLabel()), analyzer);
      var parsedQuery = queryParser.parse(prepareQueryString(query, findProperties));

      return searchWithQuery(parsedQuery);
    } catch (IOException | ParseException ex) {
      var errorMessage = String.format(
          "Error occurred when handling advance search request. Details: %s", ex.getMessage());
      LOGGER.error(errorMessage, ex);

      throw new RequestHandlingException(errorMessage);
    }
  }

  public List<FindProperty> getFindProperties() {
    return getTextSearcherConfig().getFindProperties();
  }

  private List<FindResult> searchWithQuery(Query query) throws IOException {
    try (var indexReader = DirectoryReader.open(indexDirectory)) {
      var indexSearcher = new IndexSearcher(indexReader);
      var queryScorer = new QueryScorer(query);
      var highlighter = new Highlighter(queryScorer);
      var fragmenter = new SimpleSpanFragmenter(queryScorer);
      highlighter.setTextFragmenter(fragmenter);

      var findResults = new ArrayList<FindResult>();

      var topDocs = indexSearcher.search(query, 10000);
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        var luceneDocument = indexSearcher.doc(scoreDoc.doc);
        var score = scoreDoc.score;
        var iri = luceneDocument.getField(IRI_FIELD).stringValue();
        var type = luceneDocument.getField(TYPE_FIELD).stringValue();

        var rdfsLabelField = luceneDocument.getField(rdfsLabelFieldName);
        String printableLabel = null;
        if (rdfsLabelField != null) {
          printableLabel = rdfsLabelField.stringValue();
        }
        var highlight = getHighlight(highlighter, luceneDocument);

        findResults.add(new FindResult(iri, type, printableLabel, highlight, score));
      }

      return findResults;
    }
  }

  private String getHighlight(Highlighter highlighter, Document document) {
    for (String fieldName : allFieldNames) {
      try {
        var field = document.getField(fieldName);
        if (field != null) {
          var bestFragment = highlighter.getBestFragment(analyzer, fieldName, field.stringValue());
          if (bestFragment != null) {
            return bestFragment;
          }
        }
      } catch (IOException | InvalidTokenOffsetsException ex) {
        LOGGER.warn(
            "Exception thrown while getting highlighting for document {} for field {}. Details: {}",
            document, fieldName, ex.getMessage(), ex);
      }
    }

    return null;
  }

  private void indexEntities(IndexWriter indexWriter) {
    var ontology = ontologyManager.getOntology();

    var documents = new HashSet<Document>();

    documents.addAll(
        prepareDocuments(ontology.classesInSignature(INCLUDED), OwlType.CLASS));
    documents.addAll(
        prepareDocuments(ontology.individualsInSignature(INCLUDED), OwlType.INDIVIDUAL));
    documents.addAll(
        prepareDocuments(ontology.objectPropertiesInSignature(INCLUDED), OwlType.OBJECT_PROPERTY));
    documents.addAll(
        prepareDocuments(ontology.dataPropertiesInSignature(INCLUDED), OwlType.DATA_PROPERTY));
    documents.addAll(
        prepareDocuments(
            ontology.annotationPropertiesInSignature(INCLUDED), OwlType.ANNOTATION_PROPERTY));

    try {
      for (Document document : documents) {
        var updateTerm = new Term(IRI_FIELD, document.getField(IRI_FIELD).stringValue());
        indexWriter.updateDocument(updateTerm, document);
      }
      indexWriter.commit();

      LOGGER.info("Successfully indexed {} documents.", documents.size());
    } catch (IOException ex) {
      LOGGER.warn("Unable to index entities. Details: {}", ex.getMessage(), ex);
    }
  }

  private Set<Document> prepareDocuments(Stream<? extends OWLEntity> entities, OwlType type) {
    return entities.map(owlEntity -> {
          var document = new Document();

          allSearchProperties.forEach(owlAnnotationProperty -> {
            var owlAnnotations = EntitySearcher.getAnnotations(owlEntity,
                ontologyManager.getOntologyWithImports(),
                owlAnnotationProperty);

            owlAnnotations.forEach(annotationProperty -> {
              var annotationValue = annotationProperty.getValue();
              annotationValue.asLiteral().ifPresent(owlLiteral ->
                  document.add(
                      new TextField(
                          getFieldName(owlAnnotationProperty),
                          owlLiteral.getLiteral(),
                          Store.YES)));
            });
          });

          document.add(new StringField(IRI_FIELD, owlEntity.getIRI().toString(), Store.YES));
          document.add(new StringField(TYPE_FIELD, type.name(), Store.YES));

          return document;
        })
        .collect(Collectors.toSet());
  }

  private String getFieldName(OWLAnnotationProperty owlAnnotationProperty) {
    var namespace = owlAnnotationProperty.getIRI().getNamespace();
    var fragment = owlAnnotationProperty.getIRI().getFragment();
    // We need to replace ':' because query parser isn't able to parse it correctly
    var prefix = IRI_TO_SHORT_ID.computeIfAbsent(namespace, key -> key.replace(":", "_"));
    return String.format("%s#%s", prefix, fragment);
  }

  private String prepareQueryString(String term) {
    var fuzzyDelimiter = "~" + fuzzyDistance + " ";
    var termWithFuzzing = String.join(fuzzyDelimiter, term.split("\\s+")).trim() + fuzzyDelimiter;

    return basicFieldNames.stream()
        .map(fieldName -> String.format("%s:%s", fieldName, termWithFuzzing))
        .collect(Collectors.joining(QUERY_FIELD_DELIMITER));
  }

  private String prepareQueryString(String query, List<String> findProperties) {
    var fuzzyDelimiter = "~" + fuzzyDistance + " ";
    var termWithFuzzing = String.join(fuzzyDelimiter, query.split("\\s+")).trim() + fuzzyDelimiter;

    return findProperties.stream()
        .map(findPropertyIdentifier -> {
          var findProperty = findPropertiesMap.get(findPropertyIdentifier);
          return getFieldName(dataFactory.getOWLAnnotationProperty(findProperty.getIri()));
        })
        .map(fieldName -> String.format("%s:%s", fieldName, termWithFuzzing))
        .collect(Collectors.joining(QUERY_FIELD_DELIMITER));
  }

  private String getSearchProperty(String name, Object defaultValue) {
    return appProperties.getSearch().getOrDefault(name, defaultValue).toString();
  }

  private TextSearcherConfig getTextSearcherConfig() {
    return configurationService.getCoreConfiguration().getTextSearcherConfig();
  }
}
