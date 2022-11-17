package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual;

import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.model.graph.viewer.ViewerGraphFactory;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisGraph;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.RestrictionGraphDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.CopyrightHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.DataTypeHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IndividualHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataTypeHandler.class);
  private EntitiesCacheService entitiesCacheService;
  private LabelProvider labelProvider;
  private AxiomsHandler axiomsHandler;
  private OntologyManager ontologyManager;
  private QnameHandler qnameHandler;
  private LicenseHandler licenseHandler;
  private CopyrightHandler copyrightHandler;
  private AnnotationsDataHandler annotationsDataHandler;
  private RestrictionGraphDataHandler graphDataHandler;

  public IndividualHandler(EntitiesCacheService entitiesCacheService, LabelProvider labelProvider,
      AxiomsHandler axiomsHandler, OntologyManager ontologyManager, QnameHandler qnameHandler,
      LicenseHandler licenseHandler, CopyrightHandler copyrightHandler,
      AnnotationsDataHandler annotationsDataHandler, RestrictionGraphDataHandler graphDataHandler) {
    this.entitiesCacheService = entitiesCacheService;
    this.labelProvider = labelProvider;
    this.axiomsHandler = axiomsHandler;
    this.ontologyManager = ontologyManager;
    this.qnameHandler = qnameHandler;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.annotationsDataHandler = annotationsDataHandler;
    this.graphDataHandler = graphDataHandler;
  }

  public OwlListDetails handle(OWLNamedIndividual individual) {
    var ontology = ontologyManager.getOntology();
    var iri = individual.getIRI();

    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(individual.getIRI()));

      OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handle(individual, ontology);

      OwlDetailsProperties<PropertyValue> annotations =
          annotationsDataHandler.handleAnnotations(individual.getIRI(), ontology, resultDetails);
      OntologyGraph ontologyGraph = graphDataHandler.handleGraph(individual, ontology, 0, 0);
      if (ontologyGraph.isEmpty()) {
        resultDetails.setGraph(null);
      } else {
        VisGraph vgj = new ViewerGraphFactory().convertToVisGraph(ontologyGraph);
        resultDetails.setGraph(vgj);
      }
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      var qname = qnameHandler.getQName(iri);
      resultDetails.setqName(qname);
      if (!copyrightHandler.isCopyrightExist(annotations)) {
        resultDetails.addAllProperties(copyrightHandler.getCopyright(iri));
      }
      if (!licenseHandler.isLicenseExist(annotations)) {
        resultDetails.addAllProperties(licenseHandler.getLicense(iri));
      }
    } catch (Exception ex) {
      LOG.warn("Unable to handle individual " + iri + ". Details: " + ex.getMessage(), ex);
    }
    return resultDetails;
  }

  public OwlListDetails handle(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.INDIVIDUAL);

    try {
      if (entityEntry.isPresent()) {
        var individual = entityEntry.getEntityAs(OWLNamedIndividual.class);

        resultDetails = handle(individual);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle individual {}. Details: {}", iri, ex.getMessage(), ex);
    }
    return resultDetails;
  }
}
