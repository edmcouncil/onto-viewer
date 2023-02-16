package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlMemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.OntologyVisitors;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;

class AxiomsHandlerTest {

  private static final IRI mortgageIri =
      IRI.create("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/Mortgage");

  private AxiomsHandler axiomsHandler;
  private OntologyManager ontologyManger;

  @BeforeEach
  void setUp() throws OWLOntologyCreationException {
    this.ontologyManger = new OntologyManager();
    ontologyManger.updateOntology(prepareOntology());

    var applicationConfigurationService = new YamlMemoryBasedConfigurationService();
    var labelProvider = new LabelProvider(applicationConfigurationService, ontologyManger);
    var scopeIriOntology = new ScopeIriOntology();
    var parser = new Parser(labelProvider, scopeIriOntology);
    var ontologyVisitors = new OntologyVisitors(labelProvider);
    var owlUtils = new OwlUtils(ontologyVisitors);
    var axiomsHelper = new AxiomsHelper(owlUtils, parser);
    this.axiomsHandler = new AxiomsHandler(axiomsHelper);
  }

  @Test
  void should() {
    var mortgageClass = ontologyManger.getOntology().classesInSignature(Imports.INCLUDED)
        .filter(owlClass -> owlClass.getIRI().equals(mortgageIri))
        .findFirst()
        .orElseThrow();

    var actualResult = axiomsHandler.handle(mortgageClass, ontologyManger.getOntology());

    System.out.println(actualResult);
  }

  private OWLOntology prepareOntology() throws OWLOntologyCreationException {
    var resourceAsStream = getClass().getResourceAsStream("/ontology/MortgageLoansWithoutImports.rdf");
    var owlOntologyManager = OWLManager.createOWLOntologyManager();
    return owlOntologyManager.loadOntologyFromOntologyDocument(resourceAsStream);
  }
}