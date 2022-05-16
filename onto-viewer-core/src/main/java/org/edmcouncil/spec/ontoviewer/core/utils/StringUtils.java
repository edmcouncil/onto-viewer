package org.edmcouncil.spec.ontoviewer.core.utils;

import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class StringUtils {

  private static final String AXIOM_PATTERN = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.axiom,
      ViewerIdentifierFactory.Element.empty);

  public static String getIdentifier(IRI iri) {
    String iriString = iri.toString();
    if (iriString.contains(AXIOM_PATTERN)) {
      return iriString.substring(iriString.lastIndexOf(".") + 1);
    }
    String iriFragment = iri.getFragment();
    if (iriFragment.isEmpty() || iriFragment.isBlank()) {
      return iriString;
    }
    return iriFragment;
  }

  public static String getIdentifier(String iri) {
    return StringUtils.getIdentifier(IRI.create(iri));
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
