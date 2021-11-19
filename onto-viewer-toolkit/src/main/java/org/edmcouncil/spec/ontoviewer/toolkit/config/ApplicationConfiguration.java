package org.edmcouncil.spec.ontoviewer.toolkit.config;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.MemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.RestrictionGraphDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.IndividualDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.Updater;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    AnnotationsDataHandler.class,
    CustomDataFactory.class,
    DetailsManager.class,
    FiboDataHandler.class,
    IndividualDataHandler.class,
    LabelProvider.class,
    OntologyManager.class,
    OwlDataExtractor.class,
    OwlDataHandler.class,
    RestrictionGraphDataHandler.class,
})
@ComponentScan(
    value = "org.edmcouncil.spec.ontoviewer.core",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Updater.class)
    }
)
public class ApplicationConfiguration {

  @Bean
  public ConfigurationService getConfigurationService() {
    return new MemoryBasedConfigurationService();
  }
}