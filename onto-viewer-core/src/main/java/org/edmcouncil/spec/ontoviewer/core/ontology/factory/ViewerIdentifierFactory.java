package org.edmcouncil.spec.ontoviewer.core.ontology.factory;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ViewerIdentifierFactory {

  private static final String IRI_FORMAT = "@viewer.%s.%s";

  public enum Element {
    clazz, dataProperty, objectProperty, instance, empty, annotationProperty, dataType
  }

  public enum Type {
    internal, external, axiom, function, stats
  }

  /**
   * IRI creation using the pattern. This IRI is used in the "Fibo-viewer" functions to recognize
   * elements that are not represented by IRI. The pattern looks like @viewer.type#element.
   *
   * @param type Specific type
   * @param element Specific element
   * @return IRI created using parameters
   */
  public static String createId(Type type, Element element) {
    if (element == Element.empty) {
      return String.format(IRI_FORMAT, type.name(), "");
    }

    return String.format(IRI_FORMAT, type.name(), element.name());
  }

   /**
   * IRI creation using the pattern. This IRI is used in the "Fibo-viewer" functions to recognize
   * elements that are not represented by IRI. The pattern looks like @viewer.type#element.
   *
   * @param type Specific type
   * @param element Specific element
   * @return IRI created using parameters
   */
  public static String createId(Type type, String element) {
    if (element == null || element.isEmpty()) {
      return String.format(IRI_FORMAT, type.name(), "");
    }

    return String.format(IRI_FORMAT, type.name(), element);
  }
}
