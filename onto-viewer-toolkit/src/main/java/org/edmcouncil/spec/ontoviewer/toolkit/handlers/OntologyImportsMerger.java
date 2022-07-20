package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.stereotype.Service;

@Service
public class OntologyImportsMerger {

  private final OntologyManager ontologyManager;

  public OntologyImportsMerger(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
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
}
