package org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.Parser;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.visitor.ContainsVisitors;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(UsageExtractor.class);
  private static final String BR_LITERAL = " <br />";

  private final ContainsVisitors containsVisitors;
  private final LabelProvider labelProvider;
  private final Parser parser;
  private final AxiomsHelper axiomsHelper;
  private final DeprecatedHandler deprecatedHandler;

  public UsageExtractor(ContainsVisitors containsVisitors,
      LabelProvider labelProvider,
      Parser parser,
      AxiomsHelper axiomsHelper,
      DeprecatedHandler deprecatedHandler) {
    this.containsVisitors = containsVisitors;
    this.labelProvider = labelProvider;
    this.parser = parser;
    this.axiomsHelper = axiomsHelper;
    this.deprecatedHandler = deprecatedHandler;
  }

  public OwlDetailsProperties<PropertyValue> extractUsageForClasses(OWLClass clazz, OWLOntology ontology) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String key = ViewerIdentifierFactory.createId(
        ViewerIdentifierFactory.Type.function,
        OwlType.USAGE_CLASSES.name().toLowerCase());

    extractUsageOwlClass(clazz, ontology, result, key);
    extractRangeOfObjectProperty(clazz, ontology, result, key);
    extractDomainOfObjectProperty(clazz, ontology, result, key);
    result.sortPropertiesInAlphabeticalOrder();

    return result;
  }

  private void extractUsageOwlClass(OWLClass clazz, OWLOntology ontology,
      OwlDetailsProperties<PropertyValue> result, String key) {
    Map<IRI, List<OwlAxiomPropertyValue>> values = new HashMap<>();
    Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      axioms.addAll(
          currentOntology.axioms(AxiomType.SUBCLASS_OF)
              .filter(el -> el.accept(containsVisitors.visitor(clazz.getIRI())))
              .collect(Collectors.toSet()));
    });

  //  int start = 0;
    for (OWLSubClassOfAxiom axiom : axioms) {
      LOG.debug("Extract Usage as String {}", axiom);
      LOG.debug("Extract Usage subCLass {}", axiom.getSubClass());

      IRI iri = null;
      if(axiom.getSubClass().isOWLClass()){
        iri = axiom.getSubClass().asOWLClass().getIRI();
        if (iri.equals(clazz.getIRI())) {
          continue;
        }
      } else {
        //bypassing GCI
        continue;
      }
      String iriFragment = iri.getFragment();
      String splitFragment = StringUtils.getIdentifier(iri);
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = axiomsHelper.prepareAxiomPropertyValue(
          axiom,
          iriFragment,
          splitFragment,
          fixRenderedIri,
          //start,
          false);
      //start = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = values.getOrDefault(iri, new LinkedList<>());
      ll.add(opv);

      values.put(iri, ll);
    }
      axiomGenerate(result, key, values );
  }

  public void axiomGenerate(OwlDetailsProperties<PropertyValue> result, String key,
      Map<IRI, List<OwlAxiomPropertyValue>> values) {
    axiomGenerate(result, key, values, true);
  }

  public void axiomGenerate(OwlDetailsProperties<PropertyValue> result, String key,
      Map<IRI, List<OwlAxiomPropertyValue>> values, boolean addClassSource) {
    StringBuilder sb = new StringBuilder();

    for (Map.Entry<IRI, List<OwlAxiomPropertyValue>> entry : values.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      if (addClassSource) {
        sb.append("%arg00%").append(BR_LITERAL);
      }
      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        if (addClassSource) {
          sb.append("- ").append(owlAxiomPropertyValue.getValue());
        } else {
          sb.append(owlAxiomPropertyValue.getValue());
        }
        if (i < entry.getValue().size()) {
          sb.append("<br />");
        }
        for (Map.Entry<String, OwlAxiomPropertyEntity> mapping :
            owlAxiomPropertyValue.getEntityMaping().entrySet()) {
          opv.addEntityValues(mapping.getKey(), mapping.getValue());
        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      prop.setDeprecated(deprecatedHandler.getDeprecatedForEntity(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sb.toString());
      opv.setType(OwlType.AXIOM);

      sb = new StringBuilder();

      String fullRenderedString = parser.parseRenderedString(opv.getValue(), opv.getEntityMaping());
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }
  }

  private void extractRangeOfObjectProperty(OWLClass clazz, OWLOntology ontology,
      OwlDetailsProperties<PropertyValue> result, String key) {
    Map<IRI, List<OwlAxiomPropertyValue>> valuesO = new HashMap<>();
    Set<OWLObjectPropertyRangeAxiom> ops = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      ops.addAll(currentOntology.axioms(AxiomType.OBJECT_PROPERTY_RANGE)
          .filter(el -> el.accept(containsVisitors.visitorObjectProperty(clazz.getIRI())))
          .collect(Collectors.toSet()));
    });

   // int startR = 0;

    LOG.debug("How many range is found for x : {}", ops.size());

    for (OWLObjectPropertyRangeAxiom axiom : ops) {
      OWLEntity rangeEntity = axiom.signature()
          .filter(entity -> !entity.getIRI()
              .equals(clazz.getIRI()))
          .findFirst().get();
      LOG.debug("OwlDataHandler -> extractUsageRangeAxiom {}", rangeEntity.getIRI());

      String iriFragment = rangeEntity.getIRI().toString();
      String splitFragment = StringUtils.getIdentifier(rangeEntity.getIRI().toString());
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = axiomsHelper.prepareAxiomPropertyValue(axiom, iriFragment, splitFragment,
          fixRenderedIri, false);
   //   startR = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = valuesO.getOrDefault(rangeEntity, new LinkedList<>());
      ll.add(opv);

      valuesO.put(rangeEntity.getIRI(), ll);
    }
  }

  private void extractDomainOfObjectProperty(OWLClass clazz, OWLOntology ontology,
      OwlDetailsProperties<PropertyValue> result, String key) {
    Map<IRI, List<OwlAxiomPropertyValue>> valuesD = new HashMap<>();
    Set<OWLObjectPropertyDomainAxiom> opd = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      opd.addAll(
          currentOntology.axioms(AxiomType.OBJECT_PROPERTY_DOMAIN)
              .filter(el -> el.accept(containsVisitors.visitorObjectProperty(clazz.getIRI())))
              .collect(Collectors.toSet()));
    });
    //int startD = 0;

    for (OWLObjectPropertyDomainAxiom axiom : opd) {
      OWLEntity domainEntity
          = axiom.signature()
          .filter(e -> !e.getIRI().equals(clazz.getIRI()))
          .findFirst().get();
      LOG.debug("OwlDataHandler -> extractUsageObjectDomainAxiom {}", domainEntity.getIRI());

      String iriFragment = domainEntity.getIRI().toString();
      String splitFragment = StringUtils.getIdentifier(domainEntity.getIRI().toString());
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = axiomsHelper.prepareAxiomPropertyValue(axiom, iriFragment, splitFragment,
          fixRenderedIri, false);
    //  startD = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = valuesD.getOrDefault(domainEntity, new LinkedList());
      ll.add(opv);

      valuesD.put(domainEntity.getIRI(), ll);
    }

    axiomGenerate(result, key, valuesD);

    StringBuilder sbd = new StringBuilder();

    for (Map.Entry<IRI, List<OwlAxiomPropertyValue>> entry : valuesD.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      sbd.append("%arg00%").append(" <br />");
      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        sbd.append("- ").append(owlAxiomPropertyValue.getValue());
        if (i < entry.getValue().size()) {
          sbd.append("<br />");
        }
        for (Map.Entry<String, OwlAxiomPropertyEntity> maping : owlAxiomPropertyValue.getEntityMaping()
            .entrySet()) {
          opv.addEntityValues(maping.getKey(), maping.getValue());
        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      prop.setDeprecated(deprecatedHandler.getDeprecatedForEntity(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sbd.toString());
      opv.setType(OwlType.AXIOM);

      LOG.debug("Generated big axiom: {}", sbd);
      sbd = new StringBuilder();
      String fullRenderedString = parser.parseRenderedString(opv.getValue(), opv.getEntityMaping());
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }
  }
}
