package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.metadata;

import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.core.model.onto.OntologyResources;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetadataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataHelper.class);
  private final CustomDataFactory customDataFactory;

  public MetadataHelper(CustomDataFactory customDataFactory) {
    this.customDataFactory = customDataFactory;
  }

  public OntologyResources extractOntologyResources(OWLOntology selectedOntology) {
    OntologyResources ontologyResources = new OntologyResources();
    Optional<IRI> ontologyIriOptional = selectedOntology.getOntologyID().getOntologyIRI();
    IRI ontologyIri;
    if (ontologyIriOptional.isPresent()) {
      ontologyIri = ontologyIriOptional.get();
    } else {
      ontologyIriOptional = selectedOntology.getOntologyID().getDefaultDocumentIRI();
      if (ontologyIriOptional.isPresent()) {
        ontologyIri = ontologyIriOptional.get();
        LOG.debug("IRI for this ontology doesn't exist, use Default Document IRI {}",
            ontologyIri);
      } else {
        LOG.debug("Ontology doesn't have any iri to present... Ontology ID: {}",
            selectedOntology.getOntologyID());
        return null;
      }
    }

    selectedOntology.annotationPropertiesInSignature()
        .map(annotationProperty -> customDataFactory.createAnnotationIri(
            annotationProperty.getIRI().toString()))
        .forEachOrdered(annotationIri ->
            ontologyResources.addElement(
                selectResourceIriString(annotationIri, ontologyIri,
                    ViewerIdentifierFactory.Element.annotationProperty),
                annotationIri));

    selectedOntology.classesInSignature()
        .map(clazz -> customDataFactory.createAnnotationIri(clazz.getIRI().toString()))
        .forEachOrdered(clazzIri ->
            ontologyResources.addElement(
                selectResourceIriString(clazzIri, ontologyIri,
                    ViewerIdentifierFactory.Element.clazz),
                clazzIri));

    selectedOntology.dataPropertiesInSignature()
        .map(
            dataProperty -> customDataFactory.createAnnotationIri(dataProperty.getIRI().toString()))
        .forEachOrdered(dataPropertyIri ->
            ontologyResources.addElement(
                selectResourceIriString(dataPropertyIri, ontologyIri,
                    ViewerIdentifierFactory.Element.dataProperty),
                dataPropertyIri));

    selectedOntology.objectPropertiesInSignature()
        .map(objectProperty -> customDataFactory.createAnnotationIri(
            objectProperty.getIRI().toString()))
        .forEachOrdered(objectPropertyIri ->
            ontologyResources.addElement(
                selectResourceIriString(objectPropertyIri, ontologyIri,
                    ViewerIdentifierFactory.Element.objectProperty),
                objectPropertyIri));

    selectedOntology.individualsInSignature()
        .map(individual -> customDataFactory.createAnnotationIri(individual.getIRI().toString()))
        .forEachOrdered(individual ->
            ontologyResources.addElement(
                selectResourceIriString(individual, ontologyIri,
                    ViewerIdentifierFactory.Element.instance),
                individual));

    selectedOntology.datatypesInSignature()
        .map(datatype -> customDataFactory.createAnnotationIri(datatype.getIRI().toString()))
        .forEachOrdered(datatypeIri ->
            ontologyResources.addElement(
                selectResourceIriString(datatypeIri, ontologyIri,
                    ViewerIdentifierFactory.Element.dataType),
                datatypeIri));

    ontologyResources.sortInAlphabeticalOrder();

    return ontologyResources;
  }

  private String selectResourceIriString(OwlAnnotationIri c, IRI ontologyIri,
      ViewerIdentifierFactory.Element element) {
    String annotationIri = c.getValue().getIri();

    return annotationIri.contains(ontologyIri)
        ? ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, element)
        : ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, element);
  }
}
