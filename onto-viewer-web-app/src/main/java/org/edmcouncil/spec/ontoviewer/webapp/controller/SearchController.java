package org.edmcouncil.spec.ontoviewer.webapp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.Query;
import org.edmcouncil.spec.ontoviewer.webapp.service.OntologySearcherService;
import org.edmcouncil.spec.ontoviewer.webapp.service.TextSearchService;
import org.edmcouncil.spec.ontoviewer.webapp.util.ModelBuilder;
import org.edmcouncil.spec.ontoviewer.webapp.util.ModelBuilderFactory;
import org.edmcouncil.spec.ontoviewer.webapp.util.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping("/search")
public class SearchController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private DetailsManager dataManager;
  @Autowired
  private ModelBuilderFactory modelFactory;
  @Autowired
  private TextSearchService textSearchService;
  @Autowired
  private OntologySearcherService ontologySearcher;
  @Autowired
  private ConfigurationService config;
  @Autowired
  private UpdateBlocker blocker;

  private static final Integer DEFAULT_MAX_SEARCH_RESULT_COUNT = 25;
  private static final Integer DEFAULT_RESULT_PAGE = 1;

  @PostMapping
  public ModelAndView redirectSearch(@ModelAttribute("queryValue") Query query) {
    Map<String, Object> model = new HashMap<>();
    model.put("query", query.getValue());
    ModelAndView mv = new ModelAndView("redirect:/search", model);
    return mv;
  }

  @GetMapping
  public String search(@RequestParam("query") String query,
          Model model,
          @RequestParam(value = "max", required = false, defaultValue = "25") Integer max,
          @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      ModelBuilder mb = new ModelBuilder(model);
      mb.emptyQuery();
      model = mb.getModel();
      return "error_503";
    }

    Query q = new Query();
    q.setValue(query);
    ModelBuilder modelBuilder = modelFactory.getInstance(model);
    List<FiboModule> modules = dataManager.getAllModulesData();
    boolean isGrouped = config.getCoreConfiguration().isGrouped();

    SearcherResult result = null;
    try {
      if (UrlChecker.isUrl(query)) {
        LOG.info("URL detected, search specyfic element");
        result = ontologySearcher.search(query, 0);
        modelBuilder.emptyQuery();
      } else {
        LOG.info("String detected, search elements with given label");
        modelBuilder.setQuery(query);
        result = textSearchService.search(query, max, page);
      }
    } catch (ViewerException ex) {
      LOG.info("Handle ViewerException. Message: '{}'", ex.getMessage());
      LOG.trace(Arrays.toString(ex.getStackTrace()));
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
