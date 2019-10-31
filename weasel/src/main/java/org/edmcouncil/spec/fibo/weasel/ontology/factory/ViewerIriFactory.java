package org.edmcouncil.spec.fibo.weasel.ontology.factory;

import org.semanticweb.owlapi.model.IRI;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ViewerIriFactory {

  private static final String IRI_FORMAT = "http://viewer.%s#%s";

  public enum Element {
    clazz, dataProperty, objectProperty, instance, empty
  }

  public enum Type {
    internal, external
  }

  public static String createIri(Type type, Element element) {
    if(element == Element.empty){
      return String.format(IRI_FORMAT, type.name(), "");
    }

    return String.format(IRI_FORMAT, type.name(), element.name());
  }
}
