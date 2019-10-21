package org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigMissingLanguageElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
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

  @Autowired
  private AppConfiguration config;

  private static final Logger LOGGER = LoggerFactory.getLogger(LabelExtractor.class);
  private Boolean forceLabelLang;
  private String labelLang;
  private Boolean useLabels;
  private ConfigMissingLanguageElement.MissingLanguageAction missingLanguageAction;

  @PostConstruct
  public void init() {
    WeaselConfiguration weaselConfig = (WeaselConfiguration) config.getWeaselConfig();
    this.forceLabelLang = weaselConfig.isForceLabelLang();
    this.labelLang = weaselConfig.getLabelLang();
    this.useLabels = weaselConfig.useLabels();
    this.missingLanguageAction = weaselConfig.getMissingLanguageAction();
  }

  public String getLabelOrDefaultFragment(OWLEntity entity, OWLOntology ontology) {

    if (entity == null) {
      return null;
    }

    OWLDataFactory factory = new OWLDataFactoryImpl();
    Map<String, String> labels = new HashMap<>();
    //Set<String> labels = new HashSet<>();
    if (useLabels) {
      EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())
          .collect(Collectors.toSet())
          .stream()
          .filter((annotation) -> (annotation.getValue().isLiteral()))
          .forEachOrdered((annotation) -> {
            // TODO: get default lang from configuration, if language present we will check it
            String label = annotation.annotationValue().asLiteral().get().getLiteral();

            String lang = annotation.annotationValue().asLiteral().get().getLang();

            if (forceLabelLang) {
              if (lang.equals(labelLang)) {

                labels.put(label, lang);
                LOGGER.debug("[Label Extractor]: Extract label: '{}' @ '{}' for element with IRI: '{}'",
                    label, lang.isEmpty() ? "no-lang" : lang, entity.getIRI().toString());

              } else {
                LOGGER.debug("[Label Extractor]: REJECTED label: '{}' @ '{}' for element with IRI: '{}', "
                    + "Reason: Language is not present.",
                    label, lang.isEmpty() ? "no-lang" : lang, entity.getIRI().toString());
              }

            } else {
              labels.put(label, lang);
              LOGGER.debug("[Label Extractor]: Extract label: '{}' @ '{}' for element with IRI: '{}'",
                  label, lang.isEmpty() ? "no-lang" : lang, entity.getIRI().toString());
            }

          });
    }

    if (labels.isEmpty()) {
      return StringUtils.getFragment(entity.getIRI());
    } else if (labels.size() > 1) {
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
          
          return StringUtils.getFragment(entity.getIRI());
        }

      }
      return lab;
    } else {
      return labels.entrySet()
          .stream()
          .findFirst()
          .get().getKey();
    }

  }

  public String getLabelOrDefaultFragment(IRI iri, OWLOntology ontology) {
    OWLEntity entity = ontology.entitiesInSignature(iri).findFirst().orElse(
        ontology.getOWLOntologyManager().getOWLDataFactory().getOWLEntity(EntityType.CLASS, iri));
    if (iri.toString().endsWith("/")) {
      //it's ontology, we have to get the label from another way
      return getOntologyLabelOrDefaultFragment(iri, ontology);
    }
    return getLabelOrDefaultFragment(entity, ontology);
  }

  private String getOntologyLabelOrDefaultFragment(IRI iri, OWLOntology ontology) {
    Set<String> labels = new HashSet<>();
    // TODO: get default lang from configuration, if language present we will check it
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OWLDataFactory df = OWLManager.getOWLDataFactory();
    for (OWLOntology onto : manager.ontologies().collect(Collectors.toSet())) {
      if (onto.getOntologyID().getOntologyIRI().get().equals(iri)) {
        onto.annotations(df.getRDFSLabel()).collect(Collectors.toSet()).forEach((annotation) -> {
          String label = annotation.annotationValue().asLiteral().get().getLiteral();
          labels.add(label);

          String lang = annotation.annotationValue().asLiteral().get().getLang();

          LOGGER.debug("[Label Extractor]: Extract Ontology Label: '{}' @ '{}' for elemement with IRI: '{}'",
              label, lang.isEmpty() ? "no-lang" : lang, iri.toString());
        });
        break;
      }
    }
    return labels.isEmpty()
        ? StringUtils.getFragment(iri) : labels.stream().findFirst().get();
  }
}
