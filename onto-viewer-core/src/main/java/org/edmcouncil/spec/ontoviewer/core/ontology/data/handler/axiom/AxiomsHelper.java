package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.RestrictionType;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
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

 private final  OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
  private final Set<String> unwantedTypes = new HashSet<>();
  private final Set<String> unwantedEndOfLeafIri = new HashSet<>();

  public AxiomsHelper(OwlUtils owlUtils, Parser parser) {
    this.owlUtils = owlUtils;
    this.parser = parser;

    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#Thing");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topObjectProperty");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topDataProperty");

    unwantedTypes.add("^^anyURI");
    unwantedTypes.add("^^dateTime");
    rendering.setShortFormProvider(new IriAsShortProvider());
  }

  public <T extends OWLAxiom> OwlAxiomPropertyValue prepareAxiomPropertyValue(
      T axiom,
      String iriFragment,
      String splitFragment,
      Boolean fixRenderedIri,
      boolean bypassClass
  ) {
    String value = rendering.render(axiom);
    for (String unwantedType : unwantedTypes) {
      value = value.replaceAll(unwantedType, "");
    }

    if (bypassClass) {
      value = fixRenderedValue(value, iriFragment, splitFragment, fixRenderedIri);
    }
    OwlAxiomPropertyValue axiomPropertyValue = new OwlAxiomPropertyValue();
    axiomPropertyValue.setValue(value);
    axiomPropertyValue.setType(OwlType.AXIOM);

    RestrictionType restrictionType = RestrictionType.OTHER;
    boolean isRestriction = owlUtils.isRestriction(axiom);
    if (!isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
      OwlType type = OwlType.TAXONOMY;

      if (axiom instanceof OWLSubClassOfAxiom) {
        OWLSubClassOfAxiom clazzAxiom = (OWLSubClassOfAxiom) axiom;
        if (clazzAxiom.isGCI()) {
          type = OwlType.AXIOM;
        }
      }

      axiomPropertyValue.setType(type);
    } else if (isRestriction && axiom instanceof OWLSubClassOfAxiom) {
      OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
      OWLClassExpression owlClassExpression = subClassOfAxiom.getSuperClass();
      ClassExpressionType classExpressionType = owlClassExpression.getClassExpressionType();
      restrictionType = RestrictionType.fromOwlClassExpressionType(classExpressionType);
    }
    axiomPropertyValue.setRestrictionType(restrictionType);

    processingAxioms(axiomPropertyValue, axiom, value);

    return axiomPropertyValue;
  }

  public Set<String> getUnwantedEndOfLeafIri() {
    return this.unwantedEndOfLeafIri;
  }

  //TODO: refactor this method
  private <T extends OWLAxiom> void processingAxioms(
      OwlAxiomPropertyValue axiomPropertyValue,
      T axiom,
      String renderedVal) {
    String argPattern = "/arg%s/";
    String[] splitted = renderedVal.split(" ");
    String openingBrackets = "(";
    String closingBrackets = ")";
    String openingCurlyBrackets = "{";
    String closingCurlyBrackets = "}";
    String comma = ",";

    axiom.signature().forEach(owlEntity -> {
      String eSignature = rendering.render(owlEntity);
      String key;

        for (int countingArg = 0; countingArg < splitted.length; countingArg++) {
        String string = splitted[countingArg].trim();

        // more than 1 because when it's 1, it's a number
        boolean hasOpeningBrackets = string.length() > 1 && string.contains("(");
        int countOpeningBrackets = StringUtils.countLetter(string, '(');

        boolean hasClosingBrackets = string.length() > 1 && string.endsWith(closingBrackets);
        int countClosingBrackets = StringUtils.countLetter(string, ')');

        boolean hasOpeningCurlyBrackets = string.length() > 1 && string.contains("{");
        int countOpeningCurlyBrackets = StringUtils.countLetter(string, '{');

        boolean hasClosingCurlyBrackets = string.length() > 1 && string.contains("}");
        int countClosingCurlyBrackets = StringUtils.countLetter(string, '}');

        boolean hasComma = string.length() > 1 && string.contains(",");
        int countComma = StringUtils.countLetter(string, ',');

        if (hasOpeningBrackets) {
          string = string.substring(countOpeningBrackets);
        }
        if (hasClosingBrackets) {
          string = string.substring(0, string.length() - countClosingBrackets);
        }
        if (hasOpeningCurlyBrackets) {
          string = string.substring(countOpeningCurlyBrackets);
        }
        if (hasClosingCurlyBrackets) {
          string = string.substring(0, string.length() - countClosingCurlyBrackets);
        }
        if (hasComma) {
          string = string.substring(0, string.length() - countComma);
        }
        if (string.equals(eSignature)) {
          String generatedKey = String.format(argPattern, countingArg);
          key = generatedKey;
          String textToReplace = generatedKey;
          if (hasOpeningBrackets) {
            String prefix = String.join("", Collections.nCopies(countOpeningBrackets, openingBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingBrackets) {
            String postfix = String.join("", Collections.nCopies(countClosingBrackets, closingBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasOpeningCurlyBrackets) {
            String prefix = String.join("", Collections.nCopies(countOpeningCurlyBrackets, openingCurlyBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingCurlyBrackets) {
            String postfix = String.join("", Collections.nCopies(countClosingCurlyBrackets, closingCurlyBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasComma) {
            String postfix = String.join("", Collections.nCopies(countComma, comma));
            textToReplace = textToReplace + postfix;
          }

          splitted[countingArg] = textToReplace;

          parser.parseToIri(owlEntity, axiomPropertyValue, key);
        }
        axiomPropertyValue.setLastId(countingArg);
      }
    });

    parser.checkAndParseUriInLiteral(null, splitted, argPattern, axiomPropertyValue);

    String value = String.join(" ", splitted).trim();

    axiomPropertyValue.setValue(value);
    String fullRenderedString =
        parser.parseRenderedString(axiomPropertyValue.getValue(), axiomPropertyValue.getEntityMaping());
    axiomPropertyValue.setFullRenderedString(fullRenderedString);
  }

  private String fixRenderedValue(String axiomString, String iriFragment, String splitFragment,
      boolean fixRenderedIri) {
    String[] axiomParts = axiomString.split(" ");
    LOG.debug("Split fixRenderedValue: {}", Arrays.asList(axiomParts));
    boolean axiomIsInverseOf = INVERSE_OF_SUBJECT.contains(axiomParts[1]);
    boolean axiomSubject = axiomParts[0].contains(iriFragment);

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
