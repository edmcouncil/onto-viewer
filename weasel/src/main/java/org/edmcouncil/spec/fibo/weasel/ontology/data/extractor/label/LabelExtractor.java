package org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigMissingLanguageElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class LabelExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LabelExtractor.class);
  
  @Autowired
  private OntologyManager ontology;
  private Boolean forceLabelLang;
  private String labelLang;
  private Boolean useLabels;
  private ConfigMissingLanguageElement.MissingLanguageAction missingLanguageAction;

  @Inject
  public LabelExtractor(AppConfiguration config) {
    WeaselConfiguration weaselConfig = (WeaselConfiguration) config.getWeaselConfig();
    this.forceLabelLang = weaselConfig.isForceLabelLang();
    this.labelLang = weaselConfig.getLabelLang();
    this.useLabels = weaselConfig.useLabels();
    this.missingLanguageAction = weaselConfig.getMissingLanguageAction();
  }

  public String getLabelOrDefaultFragment(OWLEntity entity) {
    if (entity == null) {
      return null;
    }

    OWLDataFactory factory = new OWLDataFactoryImpl();
    Map<String, String> labels = new HashMap<>();
    if (useLabels) {
      EntitySearcher.getAnnotations(entity, ontology.getOntology(), factory.getRDFSLabel())
          .collect(Collectors.toSet())
          .stream()
          .filter((annotation) -> (annotation.getValue().isLiteral()))
          .forEachOrdered((annotation) -> {
            // TODO: get default lang from configuration, if language present we will check it
            String label = annotation.annotationValue().asLiteral().get().getLiteral();

            String lang = annotation.annotationValue().asLiteral().get().getLang();

            labelProcessing(lang, labels, label, entity.getIRI());

          });
    }

    if (labels.isEmpty()) {
      return StringUtils.getFragment(entity.getIRI());
    } else if (labels.size() > 1) {
      return getTheRightLabel(labels, entity.getIRI());
    } else {
      return labels.entrySet()
          .stream()
          .findFirst()
          .get().getKey();
    }

  }

  private String getTheRightLabel(Map<String, String> labels, IRI entityIri) {
    String lab = labels.entrySet()
        .stream()
        .filter(p -> p.getValue().equals(labelLang))
        .map(m -> {
          return m.getKey();
        })
        .findFirst()
        .get();
    if (lab.isEmpty()) {
      LOGGER.debug("[Label Extractor]: Entity has more than one label but noone have a language");

      if (missingLanguageAction == ConfigMissingLanguageElement.MissingLanguageAction.FIRST) {
        String missingLab = labels.entrySet()
            .stream()
            .findFirst()
            .get().getKey();
        LOGGER.debug("[Label Extractor]: Return an first element of label list: {}", missingLab);

      } else if (missingLanguageAction == ConfigMissingLanguageElement.MissingLanguageAction.FRAGMENT) {

        return StringUtils.getFragment(entityIri);
      }

    }
    return lab;
  }

  private void labelProcessing(String lang, Map<String, String> labels, String label, IRI entityIri) {
    if (forceLabelLang) {
      if (lang.equals(labelLang)) {

        labels.put(label, lang);
        LOGGER.debug("[Label Extractor]: Extract label: '{}' @ '{}' for element with IRI: '{}'",
            label, lang.isEmpty() ? "no-lang" : lang, entityIri.toString());

      } else {
        LOGGER.debug("[Label Extractor]: REJECTED label: '{}' @ '{}' for element with IRI: '{}', "
            + "Reason: Language is not present.",
            label, lang.isEmpty() ? "no-lang" : lang, entityIri.toString());
      }

    } else {
      labels.put(label, lang);
      LOGGER.debug("[Label Extractor]: Extract label: '{}' @ '{}' for element with IRI: '{}'",
          label, lang.isEmpty() ? "no-lang" : lang, entityIri.toString());
    }
  }

  public String getLabelOrDefaultFragment(IRI iri) {
    OWLEntity entity = ontology.getOntology().entitiesInSignature(iri).findFirst().orElse(
        ontology.getOntology().getOWLOntologyManager().getOWLDataFactory().getOWLEntity(EntityType.CLASS, iri));
    if (iri.toString().endsWith("/")) {
      //it's ontology, we have to get the label from another way
      return getOntologyLabelOrDefaultFragment(iri);
    }
    return getLabelOrDefaultFragment(entity);
  }

  private String getOntologyLabelOrDefaultFragment(IRI iri) {
    Map<String, String> labels = new HashMap<>();
    OWLOntologyManager manager = ontology.getOntology().getOWLOntologyManager();
    OWLDataFactory df = OWLManager.getOWLDataFactory();
    for (OWLOntology onto : manager.ontologies().collect(Collectors.toSet())) {
      if (onto.getOntologyID().getOntologyIRI().get().equals(iri)) {
        onto.annotations(df.getRDFSLabel()).collect(Collectors.toSet()).forEach((annotation) -> {
          String label = annotation.annotationValue().asLiteral().get().getLiteral();

          String lang = annotation.annotationValue().asLiteral().get().getLang();
          labelProcessing(lang, labels, label, iri);
        });
        break;
      }
    }
    if (labels.isEmpty()) {
      return StringUtils.getFragment(iri);
    } else if (labels.size() > 1) {
      return getTheRightLabel(labels, iri);
    } else {
      return labels.entrySet()
          .stream()
          .findFirst()
          .get().getKey();
    }
  }
}
