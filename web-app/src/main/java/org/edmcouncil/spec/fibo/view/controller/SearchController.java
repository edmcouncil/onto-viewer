package org.edmcouncil.spec.fibo.view.controller;

import java.util.Arrays;
import org.edmcouncil.spec.fibo.view.model.Query;
import org.edmcouncil.spec.fibo.view.service.OntologySearcherService;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.view.service.TextSearchService;
import org.edmcouncil.spec.fibo.view.util.ModelBuilderFactory;
import org.edmcouncil.spec.fibo.view.util.UrlChecker;
import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

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
  private AppConfiguration config;
  @Autowired
  private UpdateBlocker blocker;

  private static final Integer DEFAULT_MAX_SEARCH_RESULT_COUNT = 25;
  private static final Integer DEFAULT_RESULT_PAGE = 1;

  @PostMapping
  public ModelAndView redirectSearch(@Valid @ModelAttribute("queryValue") Query query) {
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

    LOG.info("[REQ] GET : search ? query = {{}}", query);
    //check blocker
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
    boolean isGrouped = config.getViewerCoreConfig().isGrouped();

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
      ErrorResult er = new ErrorResult();
      er.setExMessage(ex.getMessage());
      er.setMessage("Element Not Found.");
      modelBuilder.emptyQuery();
      modelBuilder.error(er);
      return "error";
    }

    modelBuilder
            .setResult(result)
            .isGrouped(isGrouped)
            .modelTree(modules);

    return "search";
  }
}
