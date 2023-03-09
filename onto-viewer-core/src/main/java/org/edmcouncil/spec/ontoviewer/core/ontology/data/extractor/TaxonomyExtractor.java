package org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyElementImpl;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaxonomyExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(TaxonomyExtractor.class);

  private final LabelProvider labelProvider;
  private final ClassDataHelper extractSubAndSuper;
  private final AxiomsHelper axiomsHelper;

  public TaxonomyExtractor(LabelProvider labelProvider, ClassDataHelper extractSubAndSuper,
      AxiomsHelper axiomsHelper) {
    this.labelProvider = labelProvider;
    this.extractSubAndSuper = extractSubAndSuper;
    this.axiomsHelper = axiomsHelper;
  }

  public OwlTaxonomyImpl extractTaxonomy(List<PropertyValue> subElements, IRI objIri,
      OWLOntology ontology, OwlType type) {
    return extractTaxonomy(subElements, objIri, ontology, type, 0);
  }

  private OwlTaxonomyImpl extractTaxonomy(List<PropertyValue> subElements, IRI objIri,
      OWLOntology ontology, OwlType type, int depth) {
    OwlTaxonomyImpl taxonomy = new OwlTaxonomyImpl();
    if (!subElements.isEmpty()) {
      for (PropertyValue property : subElements) {
        // TODO: Replace this hack with proper handling of circular taxonomies etc.
        if (depth > 40) {
          LOG.debug("Depth > 40 for extracting taxonomy for objIri {} (type: {}) "
              + "and current taxonomy: {}", objIri, type, taxonomy);
          continue;
        }

        if (property.getType().equals(OwlType.TAXONOMY)) {
          OwlAxiomPropertyValue axiomProperty = (OwlAxiomPropertyValue) property;
          LOG.debug("Axiom Property {}", axiomProperty);
          IRI subElementIri = extractSubElementIri(axiomProperty, objIri);

          OWLEntity entity = createEntity(ontology, subElementIri, type);

          List<PropertyValue> subTax = extractSubAndSuper.getSuperElements(entity, ontology, type);

          OwlTaxonomyImpl subCLassTax =
              extractTaxonomy(subTax, entity.getIRI(), ontology, type, depth++);

          String label = labelProvider.getLabelOrDefaultFragment(objIri);

          OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(objIri.getIRIString(), label);

          if (subCLassTax.getValue().isEmpty()) {
            List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();
            currentTax.add(taxEl);
            taxonomy.addTaxonomy(currentTax);
          } else {
            taxonomy.addTaxonomy(subCLassTax, taxEl);
          }
        }
      }
    } else {
      LOG.trace("\t\tEnd leaf on {}", StringUtils.getIdentifier(objIri));
      String label = labelProvider.getLabelOrDefaultFragment(objIri);

      OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(objIri.getIRIString(), label);
      List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();

      if (!axiomsHelper.unwantedEndOfLeafIri.contains(objIri.toString())) {
        switch (type) {
          case AXIOM_CLASS:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#Thing"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#Thing", label));
            break;
          case AXIOM_OBJECT_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#topObjectProperty",
                    label));
            break;
          case AXIOM_DATA_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#topDataProperty"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#topDataProperty", label));
            break;
          case AXIOM_ANNOTATION_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property",
                    label));
            break;
          case AXIOM_NAMED_INDIVIDUAL:
            break;
          default:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#Thing"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#Thing", label));
            break;
        }
      }
      currentTax.add(taxEl);

      taxonomy.addTaxonomy(currentTax);
    }

    return taxonomy;
  }

  private IRI extractSubElementIri(OwlAxiomPropertyValue axiomProperty, IRI objIri) {
    LOG.debug("Axiom Property SubElementIri {}", axiomProperty.toString());
    LOG.debug("extractSubElementIri -> obj {}", objIri);
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : axiomProperty.getEntityMaping()
        .entrySet()) {
      LOG.debug("Axiom Property entry element {}", entry.toString());
      if (!entry.getValue().getIri().equals(objIri.getIRIString())) {
        return IRI.create(entry.getValue().getIri());
      }
    }
    return null;
  }

  private OWLEntity createEntity(OWLOntology ontology, IRI sci, OwlType type) {
    switch (type) {
      case AXIOM_CLASS:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(sci);
      case AXIOM_DATA_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(sci);
      case AXIOM_OBJECT_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(sci);
      case AXIOM_DATATYPE:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDatatype(sci);
      case AXIOM_ANNOTATION_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(sci);
    }

    return null;
  }

  public List<PropertyValue> extractTaxonomyElements(List<PropertyValue> subclasses) {
    return subclasses
        .stream()
        .filter(pv -> (pv.getType().equals(OwlType.TAXONOMY)))
        .distinct()
        .collect(Collectors.toList());
  }

  public boolean trackingThePath(OntologyModule node, IRI ontologyIri, List<String> track,
      IRI elementIri) {
    if (node == null) {
      return false;
    }

    if (IRI.create(node.getIri()).equals(elementIri)) {
      track.add(node.getIri());
      return true;
    }

    if (node.getIri().equals(ontologyIri.toString())) {
      track.add(node.getIri());
      return true;
    }

    for (OntologyModule child : node.getSubModule()) {
      if (trackingThePath(child, ontologyIri, track, elementIri)) {
        track.add(0, node.getIri());
        return true;
      }
    }
    return false;
  }
}
