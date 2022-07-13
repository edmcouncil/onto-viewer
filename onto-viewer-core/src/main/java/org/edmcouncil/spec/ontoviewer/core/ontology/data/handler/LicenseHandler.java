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
public class LicenseHandler {

  private final OntologyManager ontologyManager;
  private final ApplicationConfigurationService applicationConfigurationService;
  private final ModuleHandler moduleHandler;

  public LicenseHandler(OntologyManager ontologyManager, ApplicationConfigurationService applicationConfigurationService, ModuleHandler moduleHandler) {
    this.ontologyManager = ontologyManager;
    this.applicationConfigurationService = applicationConfigurationService;
    this.moduleHandler = moduleHandler;
  }

  public String getLicenseFromOntology(IRI ontologyIri) {
    OWLOntologyManager manager = ontologyManager.getOntology().getOWLOntologyManager();

    for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
      var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();

      if (ontologyIriOptional.isPresent()) {
        var currentOntologyIri = ontologyIriOptional.get();

        if (currentOntologyIri.equals(ontologyIri)
            || currentOntologyIri.equals(
                IRI.create(ontologyIri.getIRIString().substring(0, ontologyIri.getIRIString().length() - 1)))) {
          Optional<OWLAnnotation> licenseAnnotationOptional = currentOntology.annotations((annotation)
              -> annotation.getProperty().getIRI().equals(IRI.create("http://purl.org/dc/terms/license")))
              .findFirst();

          if (licenseAnnotationOptional.isPresent()) {
            String license = licenseAnnotationOptional.get().getValue().toString();
            license = license.replace("\"^^xsd:anyURI", "").replace("\"", "");
            return license;
          } else {
            return null;
          }
        }
      }
    }
    return null;
  }

  public String getLicense(IRI entityIri) {
    if (applicationConfigurationService.getConfigurationData().getOntologiesConfig().isDisplayLicense()) {
      IRI ontologyIri = moduleHandler.getOntologyIri(entityIri);
      String license = getLicenseFromOntology(ontologyIri);
      return license;
    }
    return null;
  }

  public String getLicense(IRI entityIri, boolean isOntology) {
    if (isOntology && applicationConfigurationService.getConfigurationData().getOntologiesConfig().isDisplayLicense()) {
      return getLicenseFromOntology(entityIri);
    } else {
      return getLicense(entityIri);
    }
  }
}
