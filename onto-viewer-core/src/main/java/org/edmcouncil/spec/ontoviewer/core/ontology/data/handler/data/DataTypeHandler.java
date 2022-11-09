package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data;

import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.CopyrightHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataTypeHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataTypeHandler.class);

  private EntitiesCacheService entitiesCacheService;
  private LabelProvider labelProvider;
  private OntologyManager ontologyManager;
  private QnameHandler qnameHandler;
  private LicenseHandler licenseHandler;
  private CopyrightHandler copyrightHandler;
  private AnnotationsDataHandler annotationsDataHandler;

  public DataTypeHandler(EntitiesCacheService entitiesCacheService, LabelProvider labelProvider,
      OntologyManager ontologyManager, QnameHandler qnameHandler, LicenseHandler licenseHandler,
      CopyrightHandler copyrightHandler, AnnotationsDataHandler annotationsDataHandler) {
    this.entitiesCacheService = entitiesCacheService;
    this.labelProvider = labelProvider;
    this.ontologyManager = ontologyManager;
    this.qnameHandler = qnameHandler;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.annotationsDataHandler = annotationsDataHandler;
  }

  public OwlListDetails handleParticularDatatype(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.DATATYPE);

    try {
      if (entityEntry.isPresent()) {
        var datatype = entityEntry.getEntityAs(OWLDatatype.class);

        resultDetails = handleParticularDatatype(datatype);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle datatype {}. Details: {}", iri, ex.getMessage());
    }
    return resultDetails;
  }

  public OwlListDetails handleParticularDatatype(OWLDatatype datatype) {
    var ontology = ontologyManager.getOntology();
    var resultDetails = new OwlListDetails();
    var iri = datatype.getIRI();
    var qname = qnameHandler.getQName(iri);
    try {
      resultDetails.setqName(qname);
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(iri));
      resultDetails.addAllProperties(
          annotationsDataHandler.handleAnnotations(iri, ontology, resultDetails));
      resultDetails.addAllProperties(licenseHandler.getLicense(iri));
      resultDetails.addAllProperties(copyrightHandler.getCopyright(iri));
    } catch (Exception ex) {
      LOG.warn("Unable to handle datatype {}. Details: {}", iri, ex.getMessage());
    }
    return resultDetails;
  }
}
