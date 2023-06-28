package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlMemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.visitor.ContainsVisitors;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.OntologyVisitors;
import org.edmcouncil.spec.ontoviewer.core.utils.OntologyUtils;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.cache.support.NoOpCacheManager;

class AxiomsHandlerTest {

  private static final String ONTOLOGY_IRI_STRING = "http://trojczak.pl/ontology/sparseDisplayExample/";
  private static final IRI CLASS_P_IRI = IRI.create(ONTOLOGY_IRI_STRING + "ClassP_some");

  private AxiomsHandler axiomsHandler;
  private OntologyManager ontologyManager;

  @BeforeEach
  void setUp() throws OWLOntologyCreationException {
    this.ontologyManager = new OntologyManager();
    ontologyManager.updateOntology(prepareOntology());

    var applicationConfigurationService = new YamlMemoryBasedConfigurationService();
    var labelProvider = new LabelProvider(applicationConfigurationService, ontologyManager);
    var scopeIriOntology = new ScopeIriOntology();
    var ontologyUtils = new OntologyUtils(ontologyManager);
    var cacheManager = new NoOpCacheManager();
    var deprecatedHandler = new DeprecatedHandler(cacheManager, ontologyUtils, ontologyManager);
    var parser = new Parser(labelProvider, scopeIriOntology, deprecatedHandler);
    var ontologyVisitors = new OntologyVisitors(labelProvider);
    var owlUtils = new OwlUtils(ontologyVisitors);
    var axiomsHelper = new AxiomsHelper(owlUtils, parser);
    this.axiomsHandler = new AxiomsHandler(axiomsHelper, new ContainsVisitors());
  }

  @Test
  void shouldReturnDefinedSubClassOfProperties() {
    var classP = ontologyManager.getOntology().classesInSignature(Imports.INCLUDED)
        .filter(owlClass -> owlClass.getIRI().equals(CLASS_P_IRI))
        .findFirst()
        .orElseThrow();

    OwlDetailsProperties<PropertyValue> actualResult = axiomsHandler.handle(classP, ontologyManager.getOntology());

    assertEquals(2, actualResult.getProperties().get("@viewer.axiom.SubClassOf").size());
  }

  private OWLOntology prepareOntology() throws OWLOntologyCreationException {
    var resourceAsStream = getClass().getResourceAsStream("/ontology/sparseDisplayExample1.owl");
    var owlOntologyManager = OWLManager.createOWLOntologyManager();
    return owlOntologyManager.loadOntologyFromOntologyDocument(resourceAsStream);
  }
}