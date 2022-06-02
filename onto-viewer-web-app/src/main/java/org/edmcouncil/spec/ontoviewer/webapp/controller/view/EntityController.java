package org.edmcouncil.spec.ontoviewer.webapp.controller.view;

import java.util.Arrays;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.service.EntityService;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.Query;
import org.edmcouncil.spec.ontoviewer.webapp.util.ModelBuilder;
import org.edmcouncil.spec.ontoviewer.webapp.util.ModelBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EntityController {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityController.class);

  private final UpdateBlocker updateBlocker;
  private final EntityService entityService;
  private final ModelBuilderFactory modelBuilderFactory;
  private final ModuleHandler moduleHandler;
  private final ApplicationConfigurationService applicationConfigurationService;

  public EntityController(UpdateBlocker updateBlocker,
      EntityService entityService,
      ModelBuilderFactory modelBuilderFactory,
      ModuleHandler moduleHandler,
      ApplicationConfigurationService applicationConfigurationService) {
    this.updateBlocker = updateBlocker;
    this.entityService = entityService;
    this.modelBuilderFactory = modelBuilderFactory;
    this.moduleHandler = moduleHandler;
    this.applicationConfigurationService = applicationConfigurationService;
  }

  @GetMapping("entity")
  public String search(@RequestParam("iri") String iri, Model model) {
    if (!updateBlocker.isInitializeAppDone()) {
      LOGGER.debug("Application initialization has not completed");
      ModelBuilder mb = new ModelBuilder(model);
      mb.emptyQuery();
      return "error_503";
    }

    Query q = new Query();
    q.setValue(iri);
    ModelBuilder modelBuilder = modelBuilderFactory.getInstance(model);
    List<OntologyModule> modules = moduleHandler.getModules();
    boolean isGrouped = applicationConfigurationService.hasConfiguredGroups();
    long startTimestamp = System.currentTimeMillis();
    SearcherResult result = null;

    try {
      result = entityService.getEntityDetailsByIri(iri);

      long endTimestamp = System.currentTimeMillis();
      LOGGER.info("URL detected: '{}' (query time: '{}' ms) result is:\n {}",
          iri, endTimestamp - startTimestamp, result);

      modelBuilder.emptyQuery();
    } catch (ViewerException ex) {
      LOGGER.info("Handle ViewerException. Message: '{}'", ex.getMessage());
      LOGGER.trace(Arrays.toString(ex.getStackTrace()));
      modelBuilder.emptyQuery();
      modelBuilder.error(new ErrorResponse("Element Not Found.", ex.getMessage()));
      return "error";
    }

    modelBuilder
        .setResult(result)
        .isGrouped(isGrouped)
        .modelTree(modules);

    return "search";
  }
}
