package org.edmcouncil.spec.fibo.weasel.ontology;

import java.io.File;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlListDetails;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigGroupsElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.weasel.model.FiboModule;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.ontology.data.OwlDataHandler;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class DataManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);
  private static final String DEFAULT_GROUP_NAME = "other";

  @Autowired
  private OntologyManager ontologyManager;
  @Autowired
  private OwlDataHandler dataHandler;
  @Autowired
  private AppConfiguration config;

  public OWLOntology getOntology() {
    return ontologyManager.getOntology();
  }

  public <T extends OwlDetails> T getDetailsByIri(String iriString) {
    IRI iri = IRI.create(iriString);
    OwlListDetails result = null;
    //FIBO: if '/' is at the end of the URL, we extract the ontolog metadata
    if (iriString.endsWith("/")) {
      LOGGER.debug("Handle ontology metadata. IRI: {}", iriString);
      OwlListDetails wd = dataHandler.handleOntologyMetadata(iri, ontologyManager.getOntology());

      result = wd;
    } else {
      if (ontologyManager.getOntology().containsClassInSignature(iri)) {
        LOGGER.debug("Handle class data.");
        OwlListDetails wd = dataHandler.handleParticularClass(iri, ontologyManager.getOntology());
        result = wd;
      } else if (ontologyManager.getOntology().containsDataPropertyInSignature(iri)) {
        LOGGER.info("Handle data property.");
        OwlListDetails wd = dataHandler.handleParticularDataProperty(iri, ontologyManager.getOntology());
        result = wd;
      } else if (ontologyManager.getOntology().containsObjectPropertyInSignature(iri)) {
        LOGGER.info("Handle object property.");
        OwlListDetails wd = dataHandler.handleParticularObjectProperty(iri, ontologyManager.getOntology());
        result = wd;
      } else if (ontologyManager.getOntology().containsIndividualInSignature(iri)) {
        LOGGER.info("Handle individual data.");
        OwlListDetails wd = dataHandler.handleParticularIndividual(iri, ontologyManager.getOntology());
        result = wd;
      }
    }

    WeaselConfiguration weaselConfig = (WeaselConfiguration) config.getWeaselConfig();
    if (weaselConfig.hasRenamedGroups()) {
      OwlDetailsProperties<PropertyValue> prop = new OwlDetailsProperties<>();
      for (Map.Entry<String, List<PropertyValue>> entry : result.getProperties().entrySet()) {
        String key = entry.getKey();
        String newName = weaselConfig.getNewName(key);
        newName = newName == null ? key : newName;
        for (PropertyValue propertyValue : entry.getValue()) {
          prop.addProperty(newName, propertyValue);
        }
      }
      result.setProperties(prop);
    }
    result.setIri(iriString);

    //Path to element in modules
    List<String> elementLocation = dataHandler.getElementLocationInModules(iriString, ontologyManager.getOntology());
    result.setLocationInModules(elementLocation);

    if (!config.getWeaselConfig().isEmpty()) {
      WeaselConfiguration cfg = (WeaselConfiguration) config.getWeaselConfig();
      if (cfg.isGrouped()) {
        OwlGroupedDetails newResult = groupDetails(result, cfg);
        return (T) newResult;
      } else {
        sortResults(result);
      }
    }
    return (T) result;
  }

  private OwlGroupedDetails groupDetails(OwlListDetails owlDetails, WeaselConfiguration cfg) {
    OwlGroupedDetails newResult = null;
    OwlGroupedDetails groupedDetails = new OwlGroupedDetails();
    Set<ConfigElement> groups = cfg.getConfiguration().get(WeaselConfigKeys.GROUPS);

    for (Map.Entry<String, List<PropertyValue>> entry : owlDetails.getProperties().entrySet()) {
      String propertyKey = entry.getKey();
      String propertyName = null;
      if (cfg.hasRenamedGroups()) {
        propertyName = cfg.getOldName(propertyKey);
        propertyName = propertyName == null ? propertyKey : propertyName;
      }
      String groupName = null;
      groupName = getGroupName(groups, propertyName);
      groupName = groupName == null ? DEFAULT_GROUP_NAME : groupName;
      for (PropertyValue property : entry.getValue()) {
        groupedDetails.addProperty(groupName, propertyKey, property);
      }
    }
    groupedDetails.setTaxonomy(owlDetails.getTaxonomy());
    groupedDetails.setLabel(owlDetails.getLabel());
    groupedDetails.setIri(owlDetails.getIri());
    groupedDetails.setLocationInModules(owlDetails.getLocationInModules());
    groupedDetails.sortProperties(groups, cfg);

    newResult = groupedDetails;
    return newResult;
  }

  private String getGroupName(Set<ConfigElement> groups, String propertyKey) {
    String result = null;
    if (propertyKey == null || propertyKey.isEmpty()) {
      return result;
    }
    for (ConfigElement g : groups) {
      ConfigGroupsElement group = (ConfigGroupsElement) g;
      if (group.getElements() != null && group.getElements().size() > 0) {
        if (group.contains(propertyKey)) {
          return group.getName();
        }
      }
    }
    return result;
  }

  private void sortResults(OwlListDetails result) {
    Set set = (Set) config.getWeaselConfig()
        .getConfigVal(WeaselConfigKeys.PRIORITY_LIST);
    if (set == null) {
      return;
    }
    List prioritySortList = new LinkedList();
    result.sortProperties(prioritySortList);
  }

  public List<FiboModule> getAllModulesData() {

    return dataHandler.getAllModulesData(ontologyManager.getOntology());
  }
}
