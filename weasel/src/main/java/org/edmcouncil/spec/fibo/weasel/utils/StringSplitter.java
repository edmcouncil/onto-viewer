package org.edmcouncil.spec.fibo.weasel.utils;

import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class StringSplitter {

  public static String getFragment(IRI iri) {
    String iriString = iri.toString();
    String[] splitIri = iriString.split("/");
    String lastElement = splitIri[splitIri.length - 1];
    if (iriString.endsWith("/")) {
      return lastElement;
    } else if (lastElement.contains("#")) {
      return lastElement.substring(lastElement.indexOf("#"));
    } else {
      return lastElement;
    }
  }
}
