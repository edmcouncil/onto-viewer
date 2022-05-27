package org.edmcouncil.spec.ontoviewer.core.ontology.scope;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class is resposible for automatic generation of scope IRI. ScopeIRI helps application can recognize which links
 * are from the ontology and which should be generated outside. Many ontologies have more than one scope and the
 * ontology scope should be generated automatically from the IRI loaded ontologies.
 *
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class ScopeIriOntology {

  private static final Logger LOG = LoggerFactory.getLogger(ScopeIriOntology.class);

  private Set<String> scopes = new HashSet<>();

  /**
   * This method return scopes IRI of ontology.
   *
   * @param ontology This is a loaded ontology.
   * @return scopesOntologies These are the IRI scopes of the ontology.
   */
  public Set<String> getScopeIri(OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();

    Set<OWLOntology> ontologies = manager.ontologies().collect(Collectors.toSet());
    Set<String> scopesOntologies = new HashSet<>();
    for (OWLOntology onto : ontologies) {
      LOG.debug("Scope IRI ontology: {}", onto.getOntologyID());
      Optional<IRI> ontologyVersionIri = onto.getOntologyID().getVersionIRI();

      if (ontologyVersionIri.isPresent()) {
        LOG.debug("Ontology Version IRI: {}", ontologyVersionIri.isPresent());
        LOG.debug("Ontology Version IRI namespace: {}", ontologyVersionIri.get().getNamespace());
        if (!ontologyVersionIri.get().getIRIString().isEmpty()) {
          scopesOntologies.add(ontologyVersionIri.get().getIRIString());
        }
      }

      Optional<IRI> ontologyIri = onto.getOntologyID().getOntologyIRI();
      if (ontologyIri.isPresent()) {
        LOG.debug("Defined ontology IRI: {}", ontologyIri.isPresent());
        LOG.debug("Ontology IRI namespace: {}", ontologyIri.get().getNamespace());
        if (!ontologyIri.get().getIRIString().isEmpty()) {
          scopesOntologies.add(ontologyIri.get().getIRIString());
        }
      }
      if (!ontologyIri.isPresent() && !ontologyVersionIri.isPresent()) {
        LOG.debug("One ontology does not have an iri defined (ontology iri is not defined): {} {}",
            !ontologyIri.isPresent(), !ontologyVersionIri.isPresent());
        Optional<IRI> defaultDocumentIri = onto.getOntologyID().getDefaultDocumentIRI();
        if (defaultDocumentIri.isPresent()) {
          LOG.debug("Default document IRI is definied: {}", defaultDocumentIri.isPresent());
          if (!defaultDocumentIri.get().getIRIString().isEmpty()) {
            scopesOntologies.add(defaultDocumentIri.get().getIRIString());
          }
        } else {
          LOG.debug("Default document IRI is not definied: {}", !defaultDocumentIri.isPresent());
        }
      }
    }
//    Set<String> scopeConfig = appConfiguration.getViewerCoreConfig().getScope();
//    scopesOntologies.addAll(scopeConfig) ;
    for (String sc : scopesOntologies) {
      LOG.debug("Difined scope: {}", sc);
    }
    return scopesOntologies;
  }

  public boolean scopeIri(String uri) {
    for (String scope : scopes) {
      //    LOG.debug("Contains: {} -> {}", uri, scope);
      if (uri.contains(scope)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;

  }

  public void setScopes(Set<String> scopes) {
    this.scopes = scopes;
  }
}
