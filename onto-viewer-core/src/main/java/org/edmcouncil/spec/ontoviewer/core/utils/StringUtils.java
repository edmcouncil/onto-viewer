package org.edmcouncil.spec.ontoviewer.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class StringUtils {

  private static final Pattern IRI_FRAGMENT_AFTER_HASH_PATTERN
      = Pattern.compile("[#][A-Za-z0-9]+");

  private static final Pattern IRI_FRAGMENT_BEFORE_HASH_PATTERN
      = Pattern.compile("[/][A-Za-z0-9]+[#]");

  private static final Pattern IRI_FRAGMENT_PATTERN
      = Pattern.compile("[A-Za-z0-9]+");

  private static final String AXIOM_PATTERN = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.axiom,
      ViewerIdentifierFactory.Element.empty);

  public static String getFragment(IRI iri) {
    String iriString = iri.toString();
    if (iriString.contains(AXIOM_PATTERN)) {
      return iriString.substring(iriString.lastIndexOf(".") + 1);
    }
    String result = null;
    if (iriString.endsWith("#")) {
      Matcher matcher = IRI_FRAGMENT_BEFORE_HASH_PATTERN.matcher(iriString);
      while (matcher.find()) {
        String match = matcher.group();
        result = IRI_FRAGMENT_PATTERN.matcher(match).group();
      }
    } else {
      Matcher matcher = IRI_FRAGMENT_AFTER_HASH_PATTERN.matcher(iriString);
      while (matcher.find()) {
        String match = matcher.group();
        result = IRI_FRAGMENT_PATTERN.matcher(match).group();
      }
    }
    return result;
  }

  public static String getFragment(String iri) {
    return getFragment(IRI.create(iri));
  }

  public static int countLetter(String string, char letter) {
    int count = 0;

    //Counts each character except space    
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == letter) {
        count++;
      }
    }
    return count;
  }

  /**
   *
   * @param string Given string to cutting
   * @param length Length of output string
   * @param soft If it set to true, string will be cut in next whitespace
   * @return
   */
  public static String cutString(String string, int length, boolean soft) {
    if (string == null || string.trim().isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder(string);
    int actualLength = length - 3;
    if (sb.length() > actualLength) {
      // -3 because we add 3 dots at the end. Returned string length has to be length including the dots.
      if (!soft) {
        return sb.insert(actualLength, "...").substring(0, actualLength + 3);
      } else {
        int endIndex = sb.indexOf(" ", actualLength);
        if (endIndex == -1) {
          return string;
        }
        endIndex = endIndex >= sb.length() ? sb.length() : endIndex;
        return sb.insert(endIndex, "...").substring(0, endIndex + 3);
      }
    }
    return string;
  }
}
