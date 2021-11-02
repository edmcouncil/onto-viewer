package org.edmcouncil.spec.ontoviewer.core.service;

import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlGroupedDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class ChangerIriToLabelService {

  private static final Logger LOG = LoggerFactory.getLogger(ChangerIriToLabelService.class);

  private final LabelProvider labelProvider;

  public ChangerIriToLabelService(LabelProvider labelProvider) {
    this.labelProvider = labelProvider;
  }

  //TODO: changeIriKeysOnLabels, Information about GrupedDetails - dev code

  public OwlGroupedDetails changeIriKeysInGroupedDetails(OwlGroupedDetails det) {
    OwlGroupedDetailsProperties<PropertyValue> newProp = new OwlGroupedDetailsProperties<>();

    LOG.trace("Change IRI Keys in grouped details for object with iri: {}", det.getIri());
    det.getProperties().entrySet().forEach((entryLVL1) -> {
      LOG.trace("\t LVL 1 key: {}", entryLVL1.getKey());
      entryLVL1.getValue().entrySet().forEach((entryLVL2) -> {
        LOG.trace("\t\t LVL 2 key: {}", entryLVL2.getKey());
        String newKey = labelProvider.getLabelOrDefaultFragment(IRI.create(entryLVL2.getKey()));
        entryLVL2.getValue().forEach((propertyValue) -> {
          LOG.trace("\t\t\t LVL 3 added val: {}", propertyValue.toString());
          newProp.addProperty(entryLVL1.getKey(), newKey, propertyValue);
        });
      });
    });
    det.setProperties(newProp);

    return det;
  }
}
