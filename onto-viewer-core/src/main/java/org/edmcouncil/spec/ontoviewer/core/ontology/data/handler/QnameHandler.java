package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.Map;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QnameHandler {

  private final ModuleHandler moduleHandler;
  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;

  public QnameHandler(ModuleHandler moduleHandler,
      ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager) {
    this.moduleHandler = moduleHandler;
    this.applicationConfigurationService = applicationConfigurationService;
    this.ontologyManager = ontologyManager;
  }

  public String getQName(IRI classIri) {
    IRI ontologyIri = moduleHandler.getOntologyIri(classIri);
    if (applicationConfigurationService.getConfigurationData().getApplicationConfig()
        .isDisplayQName()) {
      for (OWLOntology currentOntology : ontologyManager.getOntology().getOWLOntologyManager()
          .ontologies()
          .collect(Collectors.toSet())) {
        var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();
        if (ontologyIriOptional.isPresent()) {
          var currentOntologyIri = ontologyIriOptional.get();
          if (currentOntologyIri.equals(ontologyIri)
              || currentOntologyIri.equals(
              IRI.create(ontologyIri.getIRIString()
                  .substring(0, ontologyIri.getIRIString().length() - 1)))) {
            String qNameOntology = getQnameOntology(currentOntology);
            if (qNameOntology == null) {
              return "";
            }
            String iriFragment = StringUtils.getIdentifier(classIri);
            String result = qNameOntology + iriFragment;
            return result;
          }
        }
      }
    }
    return null;
  }

  public String getQnameOntology(OWLOntology ontology) {
    OWLDocumentFormat format = ontology.getFormat();
    if (applicationConfigurationService.getConfigurationData().getApplicationConfig()
        .isDisplayQName()) {
      if (format.isPrefixOWLDocumentFormat()) {
        Map<String, String> map = format.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
          if (entry.getValue()
              .equals(ontology.getOntologyID().getOntologyIRI().get().getIRIString())) {
            if (!entry.getKey().isEmpty()
                && !entry.getKey().isBlank()
                && !entry.getKey().strip().equals(":")) {
              return entry.getKey();
            } else {
              return null;
            }
          }
        }
      }
    }
    return null;
  }
}
