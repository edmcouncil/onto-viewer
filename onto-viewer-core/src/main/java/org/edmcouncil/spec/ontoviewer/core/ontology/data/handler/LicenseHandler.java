package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
@Service
public class LicenseHandler {

  private final ApplicationConfig applicationConfig;
  private final ModuleHandler moduleHandler;
  private final OntologyUtils ontologyUtils;

  public LicenseHandler(ApplicationConfigurationService applicationConfigurationService,
      ModuleHandler moduleHandler, OntologyUtils ontologyUtils) {
    this.applicationConfig = applicationConfigurationService.getConfigurationData()
        .getApplicationConfig();
    this.moduleHandler = moduleHandler;
    this.ontologyUtils = ontologyUtils;
  }

  public OwlDetailsProperties<PropertyValue> getLicenseFromOntology(IRI ontologyIri) {

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    var optionalOntology = ontologyUtils.getOntologyByIRI(ontologyIri);
    if (optionalOntology.isPresent()) {
      var ontology = optionalOntology.get();
      for (OWLAnnotation annotation : ontology.annotations()
          .collect(Collectors.toList())) {
        for (String licenseIri : applicationConfig.getLicense()) {
          if (licenseIri.equals(annotation.getProperty().getIRI().toString())) {
            String license = annotation.getValue().toString();
            license = license.replace("\"^^xsd:anyURI", "").replace("\"", "");

            PropertyValue annotationPropertyValue = new OwlAnnotationPropertyValue();
            annotationPropertyValue.setType(OwlType.ANY_URI);
            annotationPropertyValue.setValue(license);
            result.addProperty(licenseIri, annotationPropertyValue);
          }
        }
        return result;
      }
    }
    return result;
  }

  public OwlDetailsProperties<PropertyValue> getLicense(IRI entityIri) {
    if (applicationConfig.isDisplayLicense()) {
      IRI ontologyIri = moduleHandler.getOntologyIri(entityIri);
      OwlDetailsProperties<PropertyValue> license = getLicenseFromOntology(ontologyIri);
      return license;
    }
    return null;
  }

  public boolean isLicenseExist(OwlDetailsProperties<PropertyValue> owlDetailsProperties) {
    for (Map.Entry<String, List<PropertyValue>> entry : owlDetailsProperties.getProperties()
        .entrySet()) {
      if (applicationConfig.getLicense().contains(entry.getKey())) {
        return true;
      }
    }
    return false;
  }
}
