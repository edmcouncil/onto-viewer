package org.edmcouncil.spec.fibo.weasel.ontology.factory;

import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.vocabulary.DefaultAppLabels;
import org.semanticweb.owlapi.model.IRI;

/**
 * Factory create default application labels for embeded functions and ontology resourcess.
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class DefaultLabelsFactory {

  /**
   * @return complete instance of DefaultAppLabels
   */
  public static DefaultAppLabels createDefaultAppLabels(){
    DefaultAppLabels result = new DefaultAppLabels();
    IRI iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.external, ViewerIriFactory.Element.clazz);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_CLASS);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.internal, ViewerIriFactory.Element.clazz);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_CLASS);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.external, ViewerIriFactory.Element.dataProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_DATA_PROPERTIES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.internal, ViewerIriFactory.Element.dataProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_DATA_PROPERTIES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.external, ViewerIriFactory.Element.objectProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_OBJECT_PROPERTIES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.internal, ViewerIriFactory.Element.objectProperty);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_OBJECT_PROPERTIES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.external, ViewerIriFactory.Element.instance);
    result.addLabel(iri, DefaultAppLabels.DEF_EXT_INSTANCES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.internal, ViewerIriFactory.Element.instance);
    result.addLabel(iri, DefaultAppLabels.DEF_INT_INSTANCES);
   
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.function, WeaselOwlType.DIRECT_SUBCLASSES.toString().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_DIRECT_SUBCLASSES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.function, WeaselOwlType.INSTANCES.name().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_INSTANCES);
    
    iri = ViewerIriFactory.createIri(ViewerIriFactory.Type.function, WeaselOwlType.ANONYMOUS_ANCESTOR.name().toLowerCase());
    result.addLabel(iri, DefaultAppLabels.DEF_ANONYMOUS_ANCESTOR);
    
    return result;
    
  }
  
}
