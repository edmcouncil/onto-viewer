package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.Optional;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Service;

/**
 *
  * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
@Service
public class CopyrightHandler {


  private final OntologyManager ontologyManager;
  private final ApplicationConfigurationService applicationConfigurationService;
  private final ModuleHandler moduleHandler;

  public CopyrightHandler(OntologyManager ontologyManager, ApplicationConfigurationService applicationConfigurationService, ModuleHandler moduleHandler) {
    this.ontologyManager = ontologyManager;
    this.applicationConfigurationService = applicationConfigurationService;
    this.moduleHandler = moduleHandler;
  }

  public String getCopyrightFromOntology(IRI ontologyIri) {
    OWLOntologyManager manager = ontologyManager.getOntology().getOWLOntologyManager();

    for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
      var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();

      if (ontologyIriOptional.isPresent()) {
        var currentOntologyIri = ontologyIriOptional.get();

        if (currentOntologyIri.equals(ontologyIri)
            || currentOntologyIri.equals(
                IRI.create(ontologyIri.getIRIString().substring(0, ontologyIri.getIRIString().length() - 1)))) {
          Optional<OWLAnnotation> copyrightAnnotationOptional = currentOntology.annotations((annotation)
              -> annotation.getProperty().getIRI().equals(IRI.create("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright")))
              .findFirst();

          if (copyrightAnnotationOptional.isPresent()) {
            String copyright = copyrightAnnotationOptional.get().getValue().toString();
            copyright = copyright.replace("\"^^xsd:string", "").replace("\"", "");
            return copyright;
          } else {
            return null;
          }
        }
      }
    }
    return null;
  }

  public String getCopyright(IRI entityIri) {
    if (applicationConfigurationService.getConfigurationData().getOntologiesConfig().isDisplayCopyright()) {
      IRI ontologyIri = moduleHandler.getOntologyIri(entityIri);
      String copyright = getCopyrightFromOntology(ontologyIri);
      return copyright;
    }
    return null;
  }

  public String getCopyright(IRI entityIri, boolean isOntology) {
    if (isOntology && applicationConfigurationService.getConfigurationData().getOntologiesConfig().isDisplayCopyright()) {
      return getCopyrightFromOntology(entityIri);
    } else {
      return getCopyright(entityIri);
    }
  }
}