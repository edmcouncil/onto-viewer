package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import java.util.Collections;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
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
  private final DeprecatedHandler deprecatedHandler;

  public Parser(LabelProvider labelProvider, ScopeIriOntology scopeIriOntology,
      DeprecatedHandler deprecatedHandler) {
    this.labelProvider = labelProvider;
    this.scopeIriOntology = scopeIriOntology;
    this.deprecatedHandler = deprecatedHandler;
  }

  private void parseUrl(String probablyUrl, String[] splited, int j) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(probablyUrl));
    splited[j] = label;
  }

  public void parseToIri(String probablyUrl, OwlAxiomPropertyValue opv, String key,
      String[] splited, int j, String generatedKey, String iriString, int countOpeningParenthesis,
      int countClosingParenthesis, int countComma) {
    OwlAxiomPropertyEntity axiomPropertyEntity = new OwlAxiomPropertyEntity();
    if (iriString.contains("<") && iriString.contains(">")) {
      iriString = iriString.replace("<", "").replace(">", "");
    }
    axiomPropertyEntity.setIri(iriString);
    LOG.debug("Probably iriString {}", iriString);
    var iri = IRI.create(iriString);
    String label = labelProvider.getLabelOrDefaultFragment(iri);
    axiomPropertyEntity.setLabel(label);
    axiomPropertyEntity.setDeprecated(deprecatedHandler.getDeprecatedForEntity(iri));
    opv.addEntityValues(key, axiomPropertyEntity);
    splited[j] = generatedKey;

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
    splited[j] = textToReplace;
  }

  public void checkAndParseUriInLiteral(String[] splited, String argPattern,
      OwlAxiomPropertyValue opv) {
    for (int j = 0; j < splited.length; j++) {
      String str = splited[j].trim();
      String probablyUrl = splited[j].trim();
      if (str.startsWith("<") && str.endsWith(">")) {
        int length = str.length();
        probablyUrl = str.substring(1, length - 1);
      }
      if (UrlChecker.isUrl(probablyUrl)) {
        String generatedKey = String.format(argPattern, j);
        String key = generatedKey;

        if (scopeIriOntology.scopeIri(probablyUrl)) {
          //Brace checking is not needed here, so the arguments are 0.
          parseToIri(probablyUrl, opv, key, splited, j, generatedKey, str, 0, 0, 0);
        } else {
          parseUrl(probablyUrl, splited, j);
        }
      }
    }
  }

  public String parseRenderedString(OwlAxiomPropertyValue opv) {
    String result = opv.getValue();
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : opv.getEntityMaping().entrySet()) {
      LOG.debug("parseRenderedString: {}", entry.toString());
      String key = entry.getKey();
      if (!key.contains("arg")) {
        continue;
      }
      String replecment = entry.getValue().getLabel();
      LOG.debug("replecment: {}", replecment);
      result = result.replaceAll(key, replecment);
      LOG.debug("result: {}", result);
    }
    return result;
  }
}
