package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;

class OntologyImportsMergerTest {

  private static final String NEW_ONTOLOGY_IRI = "http://example.com/New/";
  private static final String UMBRELLA_ONTOLOGY_IRI = "http://example.com/Umbrella/";
  private static final String A_ONTOLOGY_IRI = "http://example.com/A/";
  private static final String B_ONTOLOGY_IRI = "http://example.com/B/";
  private static final String C_ONTOLOGY_IRI = "http://example.com/C/";

  private OntologyManager ontologyManager;
  private OntologyImportsMerger ontologyImportsMerger;
  private OWLDataFactory dataFactory;
  private OWLOntologyManager owlOntologyManager;

  @BeforeEach
  void setUp() {
    this.ontologyManager = new OntologyManager();
    this.ontologyImportsMerger = new OntologyImportsMerger(ontologyManager);
    this.dataFactory = OWLManager.getOWLDataFactory();
    this.owlOntologyManager = OWLManager.createOWLOntologyManager();
  }

  @Test
  void shouldMergeTwoSimpleOntologies() {
    var owlOntology = createOntologyWithImportedOntologies();
    ontologyManager.updateOntology(owlOntology);

    var mergedOntology = ontologyImportsMerger.mergeImportOntologies(NEW_ONTOLOGY_IRI);

    assertEquals(NEW_ONTOLOGY_IRI, mergedOntology.getOntologyID().getOntologyIRI().orElseThrow().toString());
    assertEquals(5, mergedOntology.classesInSignature(Imports.EXCLUDED).count());
  }

  @Test
  void shouldMergeTwoOntologiesWithAnnotationsIgnored() {
    var owlOntology = createOntologyWithImportedOntologiesWithAnnotations();
    ontologyManager.updateOntology(owlOntology);

    var mergedOntology = ontologyImportsMerger.mergeImportOntologies(NEW_ONTOLOGY_IRI);

    assertEquals(NEW_ONTOLOGY_IRI, mergedOntology.getOntologyID().getOntologyIRI().orElseThrow().toString());
    assertEquals(8, mergedOntology.classesInSignature(Imports.EXCLUDED).count());
    assertEquals(0, owlOntology.annotations().count());
  }

  @Test
  void shouldMergeTwoOntologiesWithNestedImportedOntology() {
    var owlOntology = createOntologyWithNestedOntologies();
    ontologyManager.updateOntology(owlOntology);

    var mergedOntology = ontologyImportsMerger.mergeImportOntologies(NEW_ONTOLOGY_IRI);

    assertEquals(NEW_ONTOLOGY_IRI, mergedOntology.getOntologyID().getOntologyIRI().orElseThrow().toString());
    assertEquals(10, mergedOntology.classesInSignature(Imports.EXCLUDED).count());
  }

  private OWLOntology createOntologyWithImportedOntologies() {
    try {
      createOntology(A_ONTOLOGY_IRI, "A", 3);
      createOntology(B_ONTOLOGY_IRI, "B", 2);

      var umbrellaOntology = owlOntologyManager.createOntology(IRI.create(UMBRELLA_ONTOLOGY_IRI));
      addImport(umbrellaOntology, A_ONTOLOGY_IRI);
      addImport(umbrellaOntology, B_ONTOLOGY_IRI);

      return umbrellaOntology;
    } catch (Exception ex) {
      throw new OntoViewerToolkitRuntimeException(
          String.format("Exception thrown while creating ontology. Details: %s", ex.getMessage()),
          ex);
    }
  }

  private OWLOntology createOntologyWithImportedOntologiesWithAnnotations() {
    try {
      var aOntology = createOntology(A_ONTOLOGY_IRI, "A", 5);
      var aOntologyLabel = dataFactory.getRDFSLabel(dataFactory.getOWLLiteral("A Ontology"));
      owlOntologyManager.applyChange(new AddOntologyAnnotation(aOntology, aOntologyLabel));

      var bOntology = createOntology(B_ONTOLOGY_IRI, "B", 3);
      var bOntologyLabel = dataFactory.getRDFSLabel(dataFactory.getOWLLiteral("B Ontology"));
      owlOntologyManager.applyChange(new AddOntologyAnnotation(bOntology, bOntologyLabel));

      var umbrellaOntology = owlOntologyManager.createOntology(IRI.create(UMBRELLA_ONTOLOGY_IRI));
      addImport(umbrellaOntology, A_ONTOLOGY_IRI);
      addImport(umbrellaOntology, B_ONTOLOGY_IRI);

      return umbrellaOntology;
    } catch (Exception ex) {
      throw new OntoViewerToolkitRuntimeException(
          String.format("Exception thrown while creating ontology. Details: %s", ex.getMessage()),
          ex);
    }
  }

  private OWLOntology createOntologyWithNestedOntologies() {
    try {
      var aOntology = createOntology(A_ONTOLOGY_IRI, "A", 5);
      createOntology(B_ONTOLOGY_IRI, "B", 3);
      createOntology(C_ONTOLOGY_IRI, "C", 2);

      var umbrellaOntology = owlOntologyManager.createOntology(IRI.create(UMBRELLA_ONTOLOGY_IRI));
      addImport(aOntology, C_ONTOLOGY_IRI);
      addImport(umbrellaOntology, A_ONTOLOGY_IRI);
      addImport(umbrellaOntology, B_ONTOLOGY_IRI);

      return umbrellaOntology;
    } catch (Exception ex) {
      throw new OntoViewerToolkitRuntimeException(
          String.format("Exception thrown while creating ontology. Details: %s", ex.getMessage()),
          ex);
    }
  }

  private OWLOntology createOntology(String ontologyIri, String classPrefix, int numberOfClasses) {
    try {
      var ontology = owlOntologyManager.createOntology(IRI.create(ontologyIri));

      for (int i = 1; i <= numberOfClasses; i++) {
        addClass(ontology, ontologyIri, classPrefix + i);
      }

      return ontology;
    } catch (Exception ex) {
      throw new OntoViewerToolkitRuntimeException(
          String.format("Exception thrown while creating ontology. Details: %s", ex.getMessage()),
          ex);
    }
  }

  private void addClass(OWLOntology ontology, String ontologyIri, String className) {
    var owlClass = dataFactory.getOWLClass(ontologyIri, className);
    var owlDeclarationAxiom = dataFactory.getOWLDeclarationAxiom(owlClass);
    owlOntologyManager.applyChange(new AddAxiom(ontology, owlDeclarationAxiom));
  }

  private void addImport(OWLOntology umbrellaOntology, String ontologyIri) {
    var aOntologyImport = dataFactory.getOWLImportsDeclaration(IRI.create(ontologyIri));
    owlOntologyManager.applyChange(new AddImport(umbrellaOntology, aOntologyImport));
  }
}