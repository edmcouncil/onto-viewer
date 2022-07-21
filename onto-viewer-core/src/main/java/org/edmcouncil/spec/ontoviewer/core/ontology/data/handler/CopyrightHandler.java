package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.ApplicationConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
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
  private final ApplicationConfig applicationConfig;
  private final ModuleHandler moduleHandler;

  public CopyrightHandler(OntologyManager ontologyManager, ApplicationConfigurationService applicationConfigurationService, ModuleHandler moduleHandler) {
    this.ontologyManager = ontologyManager;
    this.applicationConfig = applicationConfigurationService.getConfigurationData().getApplicationConfig();
    this.moduleHandler = moduleHandler;
  }

  public OwlDetailsProperties<PropertyValue> getCopyrightFromOntology(IRI ontologyIri) {

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    OWLOntologyManager manager = ontologyManager.getOntology().getOWLOntologyManager();
    Set<String> visitedOntologies = new HashSet<>();

    for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
      var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();
      if (ontologyIriOptional.isPresent()) {
        var currentOntologyIri = ontologyIriOptional.get();
        var createIri = IRI.create(ontologyIri.getIRIString().substring(0, ontologyIri.getIRIString().length() - 1));

        if ((currentOntologyIri.equals(ontologyIri)
            || currentOntologyIri.equals(createIri))
            && !visitedOntologies.contains(currentOntologyIri.toString())) {

          visitedOntologies.add(currentOntologyIri.toString());

          for (OWLAnnotation annotation : currentOntology.annotations().collect(Collectors.toList())) {
            for (String copyrightIri : applicationConfig.getCopyright()) {
              if (copyrightIri.equals(annotation.getProperty().getIRI().toString())) {

                String copyright = annotation.getValue().toString();
                copyright = copyright.replace("\"^^xsd:string", "").replace("\"", "");
                PropertyValue annotationPropertyValue = new OwlAnnotationPropertyValue();
                annotationPropertyValue.setType(OwlType.STRING);
                annotationPropertyValue.setValue(copyright);
                result.addProperty(copyrightIri, annotationPropertyValue);
              }
            }
          }
        }
      }
    }
    return result;
  }

  public OwlDetailsProperties<PropertyValue> getCopyright(IRI entityIri) {
    if (applicationConfig.isDisplayCopyright()) {
      IRI ontologyIri = moduleHandler.getOntologyIri(entityIri);
      OwlDetailsProperties<PropertyValue> copyright = getCopyrightFromOntology(ontologyIri);
      return copyright;
    }
    return null;
  }

  public boolean isCopyrightExist(OwlDetailsProperties<PropertyValue> owlDetailsProperties) {
    for (Map.Entry<String, List<PropertyValue>> entry : owlDetailsProperties.getProperties().entrySet()) {
      if (applicationConfig.getCopyright().contains(entry.getKey())) {
        return true;
      }
    }
    return false;
  }
}
