package org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class LabelExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(LabelExtractor.class);
  //app configuration

  public String getLabelOrDefaultFragment(OWLEntity entity, OWLOntology ontology) {

    if (entity == null) {
      return null;
    }

    OWLDataFactory factory = new OWLDataFactoryImpl();

    Set<String> labels = new HashSet<>();

    EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())
        .collect(Collectors.toSet())
        .stream()
        .filter((annotation) -> (annotation.getValue().isLiteral()))
        .forEachOrdered((annotation) -> {
          // TODO: get default lang from configuration, if language present we will check it
          String label = annotation.annotationValue().asLiteral().get().getLiteral();
          labels.add(label);

          String lang = annotation.annotationValue().asLiteral().get().getLang();

          LOGGER.debug("[Label Extractor]: Extract label: '{}' @ '{}' for elemement with IRI: '{}'",
              label, lang.isEmpty() ? "no-lang" : lang, entity.getIRI().toString());
        });
    return labels.isEmpty()
        ? StringUtils.getFragment(entity.getIRI()) : labels.stream().findFirst().get();
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
