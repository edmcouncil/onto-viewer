package org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.utils.StringSplitter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
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
        ? StringSplitter.getFragment(entity.getIRI()) : labels.stream().findFirst().get();
  }

  public String getLabelOrDefaultFragment(IRI iri, OWLOntology ontology) {
    OWLEntity entity = ontology.entitiesInSignature(iri).findFirst().get();
    return getLabelOrDefaultFragment(entity, ontology);
  }
}
