package org.edmcouncil.spec.fibo.weasel.ontology.data;

import org.edmcouncil.spec.fibo.weasel.model.OwlSimpleProperty;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class CustomDataFactory {

  public static OwlAnnotationIri createAnnotationIri(String iri) {
    String fragment = StringUtils.getFragment(iri);
    OwlAnnotationIri owlAnnotationIri = new OwlAnnotationIri();
    OwlSimpleProperty osp = new OwlSimpleProperty();
    osp.setLabel(fragment);
    osp.setIri(iri);
    owlAnnotationIri.setValue(osp);
    owlAnnotationIri.setType(WeaselOwlType.IRI);
    return owlAnnotationIri;
  }
}
