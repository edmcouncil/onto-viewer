package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClassDataHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassDataHelper.class);

  private final OntologyManager ontologyManager;
  private final LabelProvider labelProvider;
  private final StringIdentifier stringIdentifier;

  public ClassDataHelper(OntologyManager ontologyManager, LabelProvider labelProvider,
      StringIdentifier stringIdentifier) {
    this.ontologyManager = ontologyManager;
    this.labelProvider = labelProvider;
    this.stringIdentifier = stringIdentifier;
  }

  public List<PropertyValue> getSubclasses(OWLClass clazz) {
    List<OWLClassExpression> subClasses = EntitySearcher
        .getSuperClasses(clazz, ontologyManager.getOntologyWithImports())
        .collect(Collectors.toList());

    List<PropertyValue> result = new LinkedList<>();
    for (OWLClassExpression subClassExpression : subClasses) {
      Optional<OWLEntity> entityOptional = subClassExpression.signature().findFirst();

      if (subClassExpression.getClassExpressionType() != ClassExpressionType.OWL_CLASS ||
          entityOptional.isEmpty() ||
          entityOptional.get().getIRI().equals(clazz.getIRI())) {
        continue;
      }

      var entityIri = entityOptional.get().getIRI();

      String key = StringUtils.getIdentifier(entityIri);

      OwlAxiomPropertyValue axiomPropertyValue = new OwlAxiomPropertyValue();
      axiomPropertyValue.setType(OwlType.TAXONOMY);
      axiomPropertyValue.setValue(key);

      OwlAxiomPropertyEntity propertyEntity = new OwlAxiomPropertyEntity(
          entityIri.toString(),
          labelProvider.getLabelOrDefaultFragment(entityIri));
      axiomPropertyValue.addEntityValues(key, propertyEntity);
      result.add(axiomPropertyValue);
    }
    return result.stream().distinct().collect(Collectors.toList());
  }

  public List<PropertyValue> getSubclasses(OwlDetailsProperties<PropertyValue> axioms) {
    return axioms
        .getProperties()
        .getOrDefault(stringIdentifier.subClassOfIriString, new ArrayList<>(0));
  }

  public List<PropertyValue> filterSubclasses(List<PropertyValue> subclasses) {
    List<PropertyValue> result = subclasses.stream()
        .filter((pv) -> (!pv.getType().equals(OwlType.TAXONOMY)))
        .collect(Collectors.toList());
    return result;
  }

  /**
   * This method is used to display SubClassOf
   *
   * @param clazz Clazz are all properties of direct Subclasses.
   * @return Properties of direct Subclasses.
   */
  public OwlDetailsProperties<PropertyValue> handleDirectSubclasses(OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var subClasses = EntitySearcher
        .getSubClasses(clazz, ontologyManager.getOntologyWithImports())
        .collect(Collectors.toSet());

    for (OWLClassExpression subClass : subClasses) {
      if (subClass.isOWLClass()) {
        IRI iri = subClass.asOWLClass().getIRI();
        OwlDirectedSubClassesProperty subClassProperty = new OwlDirectedSubClassesProperty();
        subClassProperty.setType(OwlType.DIRECT_SUBCLASSES);
        subClassProperty.setValue(
            new Pair(labelProvider.getLabelOrDefaultFragment(iri), iri.toString()));
        String key = ViewerIdentifierFactory.createId(
            ViewerIdentifierFactory.Type.function,
            OwlType.DIRECT_SUBCLASSES.name().toLowerCase());
        result.addProperty(key, subClassProperty);
      }
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  public List<PropertyValue> getSuperElements(OWLEntity entity, OWLOntology ontology,
      OwlType type) {
    Stream<OWLProperty> propertyStream = null;
    OWLProperty prop = null;
    switch (type) {
      case AXIOM_CLASS:
        return getSubclasses(entity.asOWLClass());
      case AXIOM_DATA_PROPERTY:
        prop =
            entity.asOWLDataProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;
      case AXIOM_OBJECT_PROPERTY:
        prop = entity.asOWLObjectProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;
      case AXIOM_ANNOTATION_PROPERTY:
        prop = entity.asOWLAnnotationProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;

    }
    List<PropertyValue> resultProperties = new LinkedList<>();

    for (OWLProperty owlProperty : propertyStream.collect(Collectors.toSet())) {
      LOGGER.trace("{} Sub Property Of {}", StringUtils.getIdentifier(entity.getIRI()),
          StringUtils.getIdentifier(owlProperty.getIRI()));
      IRI subClazzIri = entity.getIRI();
      IRI superClazzIri = owlProperty.getIRI();

      OwlAxiomPropertyValue pv = new OwlAxiomPropertyValue();
      OwlAxiomPropertyEntity entitySubClass = new OwlAxiomPropertyEntity();
      OwlAxiomPropertyEntity entitySuperClass = new OwlAxiomPropertyEntity();
      entitySubClass.setIri(subClazzIri.getIRIString());
      entitySubClass.setLabel(labelProvider.getLabelOrDefaultFragment(subClazzIri));
      entitySuperClass.setIri(superClazzIri.getIRIString());
      entitySuperClass.setLabel(labelProvider.getLabelOrDefaultFragment(superClazzIri));

      pv.setType(OwlType.TAXONOMY);
      pv.addEntityValues(labelProvider.getLabelOrDefaultFragment(subClazzIri), entitySubClass);
      pv.addEntityValues(labelProvider.getLabelOrDefaultFragment(superClazzIri), entitySuperClass);
      resultProperties.add(pv);
    }
    return resultProperties;
  }
}
