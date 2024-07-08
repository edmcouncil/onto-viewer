package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import java.util.Collections;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlLabeledMultiAxiom;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.utils.UrlChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.stereotype.Component;

@Component
public class Parser {

  private final LabelProvider labelProvider;
  private final ScopeIriOntology scopeIriOntology;
  private final DeprecatedHandler deprecatedHandler;

  public Parser(
      LabelProvider labelProvider,
      ScopeIriOntology scopeIriOntology,
      DeprecatedHandler deprecatedHandler) {
    this.labelProvider = labelProvider;
    this.scopeIriOntology = scopeIriOntology;
    this.deprecatedHandler = deprecatedHandler;
  }

  private void parseUrl(String probablyUrl, String[] splitted, int j) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(probablyUrl));
    splitted[j] = label;
  }

  public void parseToIri(
      OWLEntity owlEntity,
      OwlAxiomPropertyValue opv,
      String key) {
    String iriString = owlEntity.getIRI().getIRIString();
    if (iriString.contains("<") && iriString.contains(">")) {
      iriString = iriString
          .replace("<", "")
          .replace(">", "");
    }

    var iri = IRI.create(iriString);
    String label = labelProvider.getLabelOrDefaultFragment(iri);

    var axiomPropertyEntity = new OwlAxiomPropertyEntity(
        iriString,
        label,
        OntoViewerEntityType.fromEntityType(owlEntity),
        deprecatedHandler.getDeprecatedForEntity(iri));

    opv.addEntityValues(key, axiomPropertyEntity);
  }

  public void parseToIri(String anonymousId,
          OwlAxiomPropertyValue opv,
          String key) {

    String label = labelProvider.getLabelOrDefaultFragment(NodeID.getNodeID(anonymousId));
    String iriString = anonymousId;
    
    var axiomPropertyEntity = new OwlAxiomPropertyEntity(
            iriString,
            label,
            OntoViewerEntityType.fromEntityType(anonymousId),
           false);

    opv.addEntityValues(key, axiomPropertyEntity);
  }

  public void checkAndParseUriInLiteral(OWLEntity owlEntity, String[] splitted, String argPattern, OwlAxiomPropertyValue opv) {
    for (int j = 0; j < splitted.length; j++) {
      String str = splitted[j].trim();
      String probablyUrl = splitted[j].trim();
      if (str.startsWith("<") && str.endsWith(">")) {
        int length = str.length();
        probablyUrl = str.substring(1, length - 1);
      }
      if (UrlChecker.isUrl(probablyUrl)) {
        String generatedKey = String.format(argPattern, j);
        String key = generatedKey;

        if (scopeIriOntology.scopeIri(probablyUrl)) {
          // Brace checking is not needed here, so the arguments are 0.
          parseToIri(owlEntity, opv, key);
        } else {
          parseUrl(probablyUrl, splitted, j);
        }
      }
    }
  }

  public String parseRenderedString(String value, Map<String, OwlAxiomPropertyEntity> entityMapping) {
    String result = value;
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : entityMapping.entrySet()) {
      String key = entry.getKey();
      if (!key.contains("arg")) {
        continue;
      }
      String replacement = entry.getValue().getLabel();
      result = result.replaceAll(key, replacement);
    }
    return result;
  }

  public String parseRenderedString(OwlLabeledMultiAxiom multiAxiom) {
    StringBuilder result = new StringBuilder();
    result.append(multiAxiom.getEntityLabel().getLabel()).append(": ");
    for (OwlAxiomPropertyValue value : multiAxiom.getValue()) {
      String axiomPropertyValue =
          value.getFullRenderedString() == null
              ? parseRenderedString(value)
              : value.getFullRenderedString();
      result.append(axiomPropertyValue).append(", ");
    }
    return result.toString();
  }

  public String parseRenderedString(OwlAxiomPropertyValue opv) {
    String result = opv.getValue();
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : opv.getEntityMaping().entrySet()) {
        String key = entry.getKey();
      if (!key.contains("arg")) {
        continue;
      }
      String replacement = entry.getValue().getLabel();
      result = result.replaceAll(key, replacement);
    }
    return result;
  }
}
