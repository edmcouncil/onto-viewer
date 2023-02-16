package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import java.util.Collections;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.utils.UrlChecker;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Parser {

  private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

  private final LabelProvider labelProvider;
  private final ScopeIriOntology scopeIriOntology;

  public Parser(LabelProvider labelProvider, ScopeIriOntology scopeIriOntology) {
    this.labelProvider = labelProvider;
    this.scopeIriOntology = scopeIriOntology;
  }

  private void parseUrl(String probablyUrl, String[] splitted, int j) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(probablyUrl));
    splitted[j] = label;
  }

  public void parseToIri(OwlAxiomPropertyValue opv, String key, String[] splitted, int j, String generatedKey,
      String iriString, int countOpeningParenthesis, int countClosingParenthesis, int countComma) {
    if (iriString.contains("<") && iriString.contains(">")) {
      iriString = iriString.replace("<", "")
          .replace(">", "");
    }

    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(iriString));

    var axiomPropertyEntity = new OwlAxiomPropertyEntity(iriString, label, OntoViewerEntityType.CLASS);
    opv.addEntityValues(key, axiomPropertyEntity);
    splitted[j] = generatedKey;

    String textToReplace = generatedKey;

    if (countOpeningParenthesis > 0) {
      String prefix = String.join("", Collections.nCopies(countOpeningParenthesis, "("));
      textToReplace = prefix + textToReplace;
    }
    if (countClosingParenthesis > 0) {
      String postfix = String.join("", Collections.nCopies(countClosingParenthesis, ")"));
      textToReplace = textToReplace + postfix;
    }
    if (countComma > 0) {
      String postfix = String.join("", Collections.nCopies(countComma, ","));
      textToReplace = textToReplace + postfix;
    }
    splitted[j] = textToReplace;
  }

  public void checkAndParseUriInLiteral(String[] splitted, String argPattern, OwlAxiomPropertyValue opv) {
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
          //Brace checking is not needed here, so the arguments are 0.
          parseToIri(opv, key, splitted, j, generatedKey, str, 0, 0, 0);
        } else {
          parseUrl(probablyUrl, splitted, j);
        }
      }
    }
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
