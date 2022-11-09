package org.edmcouncil.spec.ontoviewer.toolkit.config;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlMemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.RestrictionGraphDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Import({
    AnnotationsDataHandler.class,
    CustomDataFactory.class,
    DetailsManager.class,
    IndividualDataHelper.class,
    LabelProvider.class,
    OntologyManager.class,
    OwlDataExtractor.class,
    ClassHandler.class,
    RestrictionGraphDataHandler.class,
})
@ComponentScan(
    value = "org.edmcouncil.spec.ontoviewer.core"
)
public class ApplicationConfiguration {

  @Bean
  public ApplicationConfigurationService getApplicationConfigurationService() {
    return new YamlMemoryBasedConfigurationService();
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    var propsConfig = new PropertySourcesPlaceholderConfigurer();
    propsConfig.setLocation(new ClassPathResource("git.properties"));
    propsConfig.setIgnoreResourceNotFound(true);
    propsConfig.setIgnoreUnresolvablePlaceholders(true);
    return propsConfig;
  }
}