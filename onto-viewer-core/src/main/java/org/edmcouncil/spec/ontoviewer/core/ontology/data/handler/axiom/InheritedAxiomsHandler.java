package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlLabeledMultiAxiom;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Component;

@Component
public class InheritedAxiomsHandler {

  private final Parser parser;
  private final LabelProvider labelProvider;
  private final OwlUtils owlUtils;
  private final AxiomsHandler axiomsHandler;
  private final DeprecatedHandler deprecatedHandler;

  public InheritedAxiomsHandler(
      Parser parser,
      LabelProvider labelProvider,
      OwlUtils owlUtils,
      AxiomsHandler axiomsHandler,
      DeprecatedHandler deprecatedHandler) {
    this.parser = parser;
    this.labelProvider = labelProvider;
    this.owlUtils = owlUtils;
    this.axiomsHandler = axiomsHandler;
    this.deprecatedHandler = deprecatedHandler;
  }

  //  /**
  //   * This method is used to handle Inherited Axioms
  //   *
  //   * @param ontology Paramter which loaded ontology.
  //   * @param clazz Clazz are all properties of Inherited Axioms.
  //   * @return Class and properties of Inherited Axioms.
  //   */
  public OwlDetailsProperties<PropertyValue> handle(OWLOntology ontology, OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String subClassOfKey =
        ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.axiom, "SubClassOf");
    String equivalentClassKey =
        ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.axiom, "EquivalentClasses");
    String key =
        ViewerIdentifierFactory.createId(
            ViewerIdentifierFactory.Type.function, OwlType.ANONYMOUS_ANCESTOR.name().toLowerCase());

    Set<OWLClassExpression> alreadySeen = new HashSet<>();
    Set<OWLClass> rset = owlUtils.getSuperClasses(clazz, ontology, alreadySeen);
    Map<IRI, Set<OwlAxiomPropertyValue>> values = new HashMap<>();

    rset.stream()
        .forEachOrdered(
            (c) -> {
              OwlDetailsProperties<PropertyValue> handleAxioms = axiomsHandler.handle(c, ontology);
              for (Map.Entry<String, List<PropertyValue>> entry :
                  handleAxioms.getProperties().entrySet()) {

                if (entry.getKey().equals(subClassOfKey)
                    || entry.getKey().equals(equivalentClassKey)) {

                  for (PropertyValue propertyValue : entry.getValue()) {
                    if (propertyValue.getType() != OwlType.TAXONOMY) {

                      if (entry.getKey().equals(equivalentClassKey)) {
                        OwlAxiomPropertyValue opv = (OwlAxiomPropertyValue) propertyValue;
                        String val = opv.getValue();
                        String[] value = val.split(" ");
                        value[0] =  "";
                        val = String.join(" ", value);
                        opv.setValue(val);
                      }
                      OwlAxiomPropertyValue opv = (OwlAxiomPropertyValue) propertyValue;

                      Set<OwlAxiomPropertyValue> owlAxiomPropertyValues =
                          values.getOrDefault(c.getIRI(), new LinkedHashSet<>());

                      owlAxiomPropertyValues.add(opv);
                      values.put(c.getIRI(), owlAxiomPropertyValues);
                    }
                  }
                }
              }
            });

    for (Map.Entry<IRI, Set<OwlAxiomPropertyValue>> entry : values.entrySet()) {

      OwlLabeledMultiAxiom owlLabeledMultiAxiom = new OwlLabeledMultiAxiom();
      owlLabeledMultiAxiom.setType(OwlType.OWL_LABELED_MULTI_AXIOM);
      owlLabeledMultiAxiom.setValue(List.copyOf(entry.getValue()));

      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      prop.setDeprecated(deprecatedHandler.getDeprecatedForEntity(entry.getKey()));
      owlLabeledMultiAxiom.setEntityLabel(prop);

      String fullRenderedString = parser.parseRenderedString(owlLabeledMultiAxiom);
      owlLabeledMultiAxiom.setFullRenderedString(fullRenderedString);

      result.addProperty(key, owlLabeledMultiAxiom);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }
}
