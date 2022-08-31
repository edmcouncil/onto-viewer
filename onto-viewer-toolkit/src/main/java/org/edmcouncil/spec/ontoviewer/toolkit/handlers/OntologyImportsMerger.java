package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.stereotype.Service;

@Service
public class OntologyImportsMerger {

  private final OntologyManager ontologyManager;
  private final ApplicationConfigurationService applicationConfigurationService;

  public OntologyImportsMerger(OntologyManager ontologyManager,
      ApplicationConfigurationService applicationConfigurationService) {
    this.ontologyManager = ontologyManager;
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public OWLOntology mergeImportOntologies(String ontologyIri, String ontologyVersionIri) {
    try {
      var owlOntologyManager = OWLManager.createOWLOntologyManager();
      var outputOntology = owlOntologyManager.createOntology(IRI.create(ontologyIri));

      var changeOntologyId = new SetOntologyID(
          outputOntology,
          new OWLOntologyID(IRI.create(ontologyIri), IRI.create(ontologyVersionIri)));
      owlOntologyManager.applyChange(changeOntologyId);

      var inputOntology = ontologyManager.getOntology();
      mergeOntologies(outputOntology, inputOntology.imports());

      addAnnotations(outputOntology);

      return outputOntology;
    } catch (Exception ex) {
      throw new OntoViewerToolkitRuntimeException(
          "Exception thrown while merging ontologies. Details: " + ex.getMessage());
    }
  }

  private void mergeOntologies(OWLOntology outputOntology, Stream<OWLOntology> imports) {
    var ontologies = imports.collect(Collectors.toList());
    for (OWLOntology ontology : ontologies) {
      outputOntology
          .getOWLOntologyManager()
          .addAxioms(outputOntology, ontology.axioms(Imports.INCLUDED));
    }
  }

  private void addAnnotations(OWLOntology outputOntology) {
    Map<IRI, OWLOntology> iriToOntology = new HashMap<>();
    ontologyManager.getOntologyWithImports().forEach(owlOntology -> {
      var ontologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();
      ontologyIriOptional.ifPresent(iri -> iriToOntology.put(iri, owlOntology));
    });

    var configurationData = applicationConfigurationService.getConfigurationData();
    for (String path : configurationData.getOntologiesConfig().getPaths()) {
      addAnnotationForSpecificOntologyLocation(outputOntology, iriToOntology, path);
    }
    for (String url : configurationData.getOntologiesConfig().getUrls()) {
      addAnnotationForSpecificOntologyLocation(outputOntology, iriToOntology, url);
    }
  }

  private void addAnnotationForSpecificOntologyLocation(OWLOntology outputOntology, Map<IRI, OWLOntology> iriToOntology,
      String location) {
    var ontologyIri = ontologyManager.getLocationToIriMapping().get(location);
    if (ontologyIri != null) {
      var owlOntology = iriToOntology.get(ontologyIri);
      owlOntology.annotations().forEach(owlAnnotation -> addOntologyAnnotation(outputOntology, owlAnnotation));
    }
  }

  private void addOntologyAnnotation(OWLOntology outputOntology, OWLAnnotation owlAnnotation) {
    var owlOntologyManager = outputOntology.getOWLOntologyManager();
    var addOntologyAnnotation = new AddOntologyAnnotation(outputOntology, owlAnnotation);
    owlOntologyManager.applyChange(addOntologyAnnotation);
  }
}
