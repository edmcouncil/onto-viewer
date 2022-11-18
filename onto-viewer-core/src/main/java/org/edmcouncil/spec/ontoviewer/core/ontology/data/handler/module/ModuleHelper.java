package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModuleHelper {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleHelper.class);
  private final ModuleHandler moduleHandler;
  private Map<IRI, IRI> entityIriToOntologyIriMap;

  public ModuleHelper(ModuleHandler moduleHandler) {
    this.moduleHandler = moduleHandler;
  }

  public MaturityLevel getMaturityLevel(IRI iri) {
    return moduleHandler.getMaturityLevelForElement(iri);
  }

  public List<OntologyModule> getAllModules() {
    return moduleHandler.getModules();
  }

  public List<String> getElementLocationInModules(String iriString) { // TODO use 'IRI'
    LOG.debug("[Data Handler] Handle location for element {}", iriString);
    return getElementLocationInModules(IRI.create(iriString));
  }

  public List<String> getElementLocationInModules(IRI elementIri) {
    List<String> result = new LinkedList<>();

    var allModules = moduleHandler.getModules();

    if (allModules.isEmpty()) {
      return result;
    }

    IRI ontologyIri = findElementInOntology(elementIri);
    ontologyIri = ontologyIri == null ? elementIri : ontologyIri;

    LOG.debug("Element found in ontology {}", ontologyIri);
    if (ontologyIri != null) {
      for (OntologyModule module : allModules) {
        if (trackingThePath(module, ontologyIri, result, elementIri)) {
          LOG.debug("[Data Handler] Location Path {}", Arrays.toString(result.toArray()));
          return result;
        }
      }
    }
    return result;
  }

  public boolean trackingThePath(OntologyModule node, IRI ontologyIri, List<String> track,
      IRI elementIri) {
    if (node == null) {
      return false;
    }

    if (IRI.create(node.getIri()).equals(elementIri)) {
      track.add(node.getIri());
      return true;
    }

    if (node.getIri().equals(ontologyIri.toString())) {
      track.add(node.getIri());
      return true;
    }

    for (OntologyModule child : node.getSubModule()) {
      if (trackingThePath(child, ontologyIri, track, elementIri)) {
        track.add(0, node.getIri());
        return true;
      }
    }
    return false;
  }

  public IRI findElementInOntology(IRI elementIri) {
    return entityIriToOntologyIriMap.get(elementIri);
  }


  public void setEntityIriToOntologyIriMap(
      Map<IRI, IRI> entityIriToOntologyIriMap) {
    this.entityIriToOntologyIriMap = entityIriToOntologyIriMap;
  }
}
