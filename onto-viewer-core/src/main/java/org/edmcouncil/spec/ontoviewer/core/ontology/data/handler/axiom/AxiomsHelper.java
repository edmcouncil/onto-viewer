package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AxiomsHelper {

  private static final Logger LOG = LoggerFactory.getLogger(AxiomsHelper.class);
  private static final String INVERSE_OF_SUBJECT = "InverseOf";
  private static final Set<String> SUBJECTS_TO_HIDE = ImmutableSet.of("SubClassOf", "Domain", "Range",
      "SubPropertyOf:",
      "Range:", "Functional:", "Transitive:", "Symmetric:", "Asymmetric", "Reflexive",
      "Irreflexive");
  private final OwlUtils owlUtils;
  private final Parser parser;
  private final OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
  private final Set<String> unwantedTypes = new HashSet<>();
  public final Set<String> unwantedEndOfLeafIri = new HashSet<>();

  {
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#Thing");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topObjectProperty");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topDataProperty");

    unwantedTypes.add("^^anyURI");
    unwantedTypes.add("^^dateTime");
  }

  public AxiomsHelper(OwlUtils owlUtils, Parser parser) {
    this.owlUtils = owlUtils;
    this.parser = parser;
  }

  //TODO: refactor this method
  private <T extends OWLAxiom> void processingAxioms(
      T axiom,
      Boolean fixRenderedIri,
      String iriFragment,
      String splitFragment,
      OwlAxiomPropertyValue opv,
      String renderedVal,
      int startCountingArgs) {
    String argPattern = "/arg%s/";
    String[] splitted = renderedVal.split(" ");
    String openingBrackets = "(";
    String closingBrackets = ")";
    String openingCurlyBrackets = "{";
    String closingCurlyBrackets = "}";
    String comma = ",";
    Iterator<OWLEntity> iterator = axiom.signature().iterator();
    LOG.trace("Rendered Val: {}", renderedVal);

    while (iterator.hasNext()) {
      OWLEntity next = iterator.next();
      String eSignature = rendering.render(next);
      eSignature = fixRenderedIri && iriFragment.equals(eSignature) ? splitFragment : eSignature;
      String key = null;
      LOG.debug("Processing Item: {}", next);
      LOG.trace("OWL Entity splitted: {}", Arrays.asList(splitted));

      for (int countingArg = startCountingArgs; countingArg < startCountingArgs + splitted.length;
          countingArg++) {
        int fixedIValue = countingArg - startCountingArgs;
        String string = splitted[fixedIValue].trim();
        LOG.trace("Splitted string i: '{}', str: '{}'", fixedIValue, string);
        //more than 1 because when it's 1, it's a number
        Boolean hasOpeningBrackets = string.length() > 1 ? string.contains("(") : false;
        int countOpeningBrackets = StringUtils.countLetter(string, '(');

        Boolean hasClosingBrackets =
            string.length() > 1 ? string.endsWith(closingBrackets) : false;
        int countClosingBrackets = StringUtils.countLetter(string, ')');

        Boolean hasOpeningCurlyBrackets = string.length() > 1 ? string.contains("{") : false;
        int countOpeningCurlyBrackets = StringUtils.countLetter(string, '{');

        Boolean hasClosingCurlyBrackets = string.length() > 1 ? string.contains("}") : false;
        int countClosingCurlyBrackets = StringUtils.countLetter(string, '}');

        Boolean hasComma = string.length() > 1 ? string.contains(",") : false;
        int countComma = StringUtils.countLetter(string, ',');

        if (hasOpeningBrackets) {
          String newString = string.substring(countOpeningBrackets);
          LOG.trace("Old string: '{}', new string '{}', count opening parenthesis '{}'", string,
              newString,
              countOpeningBrackets);
          string = newString;
        }
        if (hasClosingBrackets) {
          String newString = string.substring(0, string.length() - countClosingBrackets);
          LOG.trace("Old string: '{}', new string '{}', count closing parenthesis '{}'", string,
              newString,
              countClosingBrackets);

          string = newString;
        }
        if (hasOpeningCurlyBrackets) {
          String newString = string.substring(countOpeningCurlyBrackets);
          LOG.trace("Old string: '{}', new string '{}', count opening curly brackets '{}'", string,
              newString,
              countOpeningCurlyBrackets);
          string = newString;
        }
        if (hasClosingCurlyBrackets) {
          String newString = string.substring(0, string.length() - countClosingCurlyBrackets);
          LOG.trace("Old string: '{}', new string '{}', count closing curly brackets '{}'", string,
              newString,
              countClosingCurlyBrackets);

          string = newString;
        }
        if (hasComma) {
          String newString = string.substring(0, string.length() - countComma);
          LOG.trace("Old string: '{}', new string '{}', count comma '{}'", string,
              newString,
              countComma);

          string = newString;
        }
        if (string.equals(eSignature)) {
          LOG.trace("Find match for processing item {}", string);
          String generatedKey = String.format(argPattern, countingArg);
          key = generatedKey;
          String textToReplace = generatedKey;
          if (hasOpeningBrackets) {
            String prefix = String.join("",
                Collections.nCopies(countOpeningBrackets, openingBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingBrackets) {
            String postfix = String.join("",
                Collections.nCopies(countClosingBrackets, closingBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasOpeningCurlyBrackets) {
            String prefix = String.join("",
                Collections.nCopies(countOpeningCurlyBrackets, openingCurlyBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingCurlyBrackets) {
            String postfix = String.join("",
                Collections.nCopies(countClosingCurlyBrackets, closingCurlyBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasComma) {
            String postfix = String.join("", Collections.nCopies(countComma, comma));
            textToReplace = textToReplace + postfix;
          }
          LOG.trace("Prepared text: {} for: {}", textToReplace, splitted[fixedIValue]);
          splitted[fixedIValue] = textToReplace;
          String eIri = next.getIRI().toString();

          parser.parseToIri(argPattern, opv, key, splitted, fixedIValue, generatedKey, eIri,
              countOpeningBrackets, countClosingBrackets, countComma);
        }
        opv.setLastId(countingArg);
      }
    }

    parser.checkAndParseUriInLiteral(splitted, argPattern, opv);

    String value = String.join(" ", splitted).trim();

    LOG.debug("Prepared value for axiom : {}", value);
    opv.setValue(value);
    String fullRenderedString = parser.parseRenderedString(opv);
    opv.setFullRenderedString(fullRenderedString);
    LOG.debug("Full Rendered String: {}", fullRenderedString);
  }

  public <T extends OWLAxiom> OwlAxiomPropertyValue prepareAxiomPropertyValue(
      T axiom,
      String iriFragment,
      String splitFragment,
      Boolean fixRenderedIri,
      String key,
      int startCountingArgs,
      boolean bypassClass
  ) {
    String value = rendering.render(axiom);
    LOG.debug("Rendered default value: {}", value);
    for (String unwantedType : unwantedTypes) {
      value = value.replaceAll(unwantedType, "");
    }

    if (bypassClass) {
      value = fixRenderedValue(value, iriFragment, splitFragment, fixRenderedIri);
    }
    OwlAxiomPropertyValue axiomPropertyValue = new OwlAxiomPropertyValue();
    axiomPropertyValue.setValue(value);
    axiomPropertyValue.setType(OwlType.AXIOM);

    boolean isRestriction = owlUtils.isRestriction(axiom);
    if (!isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
      axiomPropertyValue.setType(OwlType.TAXONOMY);
    }

    processingAxioms(axiom, fixRenderedIri, iriFragment, splitFragment, axiomPropertyValue, value,
        startCountingArgs);

    return axiomPropertyValue;
  }

  private String fixRenderedValue(String axiomString, String iriFragment, String splitFragment,
      Boolean fixRenderedIri) {
    String[] axiomParts = axiomString.split(" ");
    LOG.debug("Split fixRenderedValue: {}", Arrays.asList(axiomParts));
    Boolean axiomIsInverseOf = INVERSE_OF_SUBJECT.contains(axiomParts[1]);
    Boolean axiomSubject = axiomParts[0].contains(iriFragment);

    if (axiomIsInverseOf) {
      if (axiomSubject) {
        axiomParts[0] = "";
        axiomParts[1] = "";
      } else {
        axiomParts[1] = "";
        axiomParts[2] = "";
      }
    }
    if (SUBJECTS_TO_HIDE.contains(axiomParts[1])) {
      axiomParts[0] = "";
      axiomParts[1] = "";
    }
    if (fixRenderedIri) {
      int iriFragmentIndex = -1;
      for (int i = 0; i < axiomParts.length; i++) {
        String sString = axiomParts[i];
        if (iriFragment.contains(sString)) {
          iriFragmentIndex = i;
          break;
        }
      }
      if (iriFragmentIndex != -1) {
        axiomParts[iriFragmentIndex] = splitFragment;
      }
    }
    axiomString = String.join(" ", axiomParts);
    return axiomString;
  }
}
