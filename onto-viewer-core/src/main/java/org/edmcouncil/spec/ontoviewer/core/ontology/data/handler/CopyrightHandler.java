package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.ApplicationConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.utils.OntologyUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
@Service
public class CopyrightHandler {

  private final ApplicationConfig applicationConfig;
  private final ModuleHandler moduleHandler;
  private final OntologyUtils ontologyUtils;

  public CopyrightHandler(ApplicationConfigurationService applicationConfigurationService,
      ModuleHandler moduleHandler,
      OntologyUtils ontologyUtils) {
    this.applicationConfig = applicationConfigurationService.getConfigurationData()
        .getApplicationConfig();
    this.moduleHandler = moduleHandler;
    this.ontologyUtils = ontologyUtils;
  }

  public OwlDetailsProperties<PropertyValue> getCopyrightFromOntology(IRI ontologyIri) {

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var optionalOntology = ontologyUtils.getOntologyByIRI(ontologyIri);
    if (optionalOntology.isPresent()) {
      var ontology = optionalOntology.get();

      for (OWLAnnotation annotation : ontology.annotations()
          .collect(Collectors.toList())) {
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
      return result;
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
    for (Map.Entry<String, List<PropertyValue>> entry : owlDetailsProperties.getProperties()
        .entrySet()) {
      if (applicationConfig.getCopyright().contains(entry.getKey())) {
        return true;
      }
    }
    return false;
  }
}
