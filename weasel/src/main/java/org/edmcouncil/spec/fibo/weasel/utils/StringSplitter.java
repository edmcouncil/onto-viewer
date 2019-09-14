package org.edmcouncil.spec.fibo.weasel.utils;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class StringSplitter {

  public static String getFragment(IRI iri) {
    String iriString = iri.toString();
    return getFragment(iriString);
  }

  public static String getFragment(String iri) {
    String[] splitIri = iri.split("/");
    String lastElement = splitIri[splitIri.length - 1];
    if (iri.endsWith("/")) {
      return lastElement;
    } else if (lastElement.contains("#")) {
      return lastElement.substring(lastElement.indexOf("#")+1);
    } else {
      return lastElement;
    }
  }
}
