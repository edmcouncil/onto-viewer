package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.onto.OntologyResources;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdateJobIdGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdaterOperation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Component;

@Component
public class MetadataHandler {

  private final LabelProvider labelProvider;
  private final ModuleHelper moduleHelper;
  private final OntologyManager ontologyManager;
  private final AnnotationsDataHandler annotationsDataHandler;
  private final QnameHandler qnameHandler;
  private final ModuleHandler moduleHandler;
  private final MetadataHelper metadataHelper;

  public MetadataHandler(LabelProvider labelProvider, ModuleHelper moduleHelper,
      OntologyManager ontologyManager, AnnotationsDataHandler annotationsDataHandler,
      QnameHandler qnameHandler, ModuleHandler moduleHandler, MetadataHelper metadataHelper) {
    this.labelProvider = labelProvider;
    this.moduleHelper = moduleHelper;
    this.ontologyManager = ontologyManager;
    this.annotationsDataHandler = annotationsDataHandler;
    this.qnameHandler = qnameHandler;
    this.moduleHandler = moduleHandler;
    this.metadataHelper = metadataHelper;
  }

  private final Map<IRI, OntologyResources> resources = new HashMap<>();

  private long numberOfResourcesUpdates = 0;

  public OwlListDetails handle(IRI iri) {
    
    OwlListDetails ontologyDetails = new OwlListDetails();
    OwlDetailsProperties<PropertyValue> metadata = handle(iri, ontologyDetails);
    if (metadata != null && !metadata.getProperties().keySet().isEmpty()) {
      ontologyDetails.addAllProperties(metadata);
      ontologyDetails.setIri(iri.toString());
      ontologyDetails.setLabel(labelProvider.getLabelOrDefaultFragment(iri));
      ontologyDetails.setLocationInModules(moduleHelper.getElementLocationInModules(iri));
      return ontologyDetails;
    }
    return null;
  }

  public OwlDetailsProperties<PropertyValue> handle(IRI iri, OwlListDetails details) {
    
    OWLOntology ontology = ontologyManager.getOntology();
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OwlDetailsProperties<PropertyValue> annotations = null;

    for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
      var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();
      if (ontologyIriOptional.isPresent()) {
        var currentOntologyIri = ontologyIriOptional.get();
        if (currentOntologyIri.equals(iri)
            || currentOntologyIri.equals(
            IRI.create(iri.getIRIString().substring(0, iri.getIRIString().length() - 1)))) {
          annotations = annotationsDataHandler.handleOntologyAnnotations(
              currentOntology.annotations(), details);

          var qName = qnameHandler.getQnameOntology(currentOntology);
          details.setqName(qName);
          OntologyResources ontologyResources = getOntologyResources(currentOntologyIri);
          if (ontologyResources != null) {
            for (Map.Entry<String, List<PropertyValue>> entry : ontologyResources.getResources()
                .entrySet()) {
              for (PropertyValue propertyValue : entry.getValue()) {
                annotations.addProperty(entry.getKey(), propertyValue);
              }
            }
          }
          details.setMaturityLevel(moduleHandler.getMaturityLevelForModule(currentOntologyIri));
          break;
        }
      }
    }
    return annotations;
  }

  private OntologyResources getOntologyResources(IRI ontologyIri) {

    clearResourcesAfterUpdate();

    if (!resources.containsKey(ontologyIri)) {
      var owlOntologyOptional = ontologyManager.getOntologyWithImports()
          .filter(owlOntology -> {
            var owlOntologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();
            return owlOntologyIriOptional.map(iri -> iri.equals(ontologyIri)).orElse(false);
          })
          .findFirst();

      if (owlOntologyOptional.isPresent()) {
        var ontologyResources = metadataHelper.extractOntologyResources(owlOntologyOptional.get());
        resources.put(ontologyIri, ontologyResources);
      } else {
        resources.put(ontologyIri, new OntologyResources());
      }
    }
    return resources.get(ontologyIri);
  }

  private void clearResourcesAfterUpdate() {
    final long numberOfOntologyUpdates = UpdateJobIdGenerator.getCurrentID();
    final UpdateJobStatus statusOfLastUpdate = UpdaterOperation.getLastStatusFromLastJob();
    if (numberOfOntologyUpdates > numberOfResourcesUpdates && UpdateJobStatus.DONE.equals(statusOfLastUpdate)) {
      resources.clear();
      numberOfResourcesUpdates = numberOfOntologyUpdates;
    }
  }
}