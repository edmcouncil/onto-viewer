package org.edmcouncil.spec.ontoviewer.core.ontology.factory;

import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.DefaultAppLabels;

/**
 * Factory create default application labels for embeded functions and ontology resourcess.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class DefaultLabelsFactory {

  /**
   * @return complete instance of DefaultAppLabels
   */
  public static DefaultAppLabels createDefaultAppLabels() {
    DefaultAppLabels result = new DefaultAppLabels();
    String iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, ViewerIdentifierFactory.Element.clazz);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_CLASS);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, ViewerIdentifierFactory.Element.clazz);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_CLASS);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, ViewerIdentifierFactory.Element.dataProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_DATA_PROPERTIES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, ViewerIdentifierFactory.Element.dataProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_DATA_PROPERTIES);
    
    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, ViewerIdentifierFactory.Element.dataType);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_DATA_TYPE);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, ViewerIdentifierFactory.Element.dataType);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_DATA_TYPE);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, ViewerIdentifierFactory.Element.objectProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_OBJECT_PROPERTIES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, ViewerIdentifierFactory.Element.objectProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_OBJECT_PROPERTIES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, ViewerIdentifierFactory.Element.instance);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_INSTANCES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, ViewerIdentifierFactory.Element.instance);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_INSTANCES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.DIRECT_SUBCLASSES.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_DIRECT_SUBCLASSES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.INSTANCES.name().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_INSTANCES);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.ANONYMOUS_ANCESTOR.name().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_ANONYMOUS_ANCESTOR);

    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.USAGE_CLASSES.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_USAGE_CLASSES);
    
    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.DIRECT_SUB_OBJECT_PROPERTY.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_DIRECT_SUB_OBJECT_PROPERTY);
    
    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.DIRECT_SUB_ANNOTATION_PROPERTY.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_DIRECT_SUB_ANNOTATION_PROPERTY);
    
    iri = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, OwlType.DIRECT_SUB_DATA_PROPERTY.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_DIRECT_SUB_DATA_PROPERTY);

    return result;

  }

}
