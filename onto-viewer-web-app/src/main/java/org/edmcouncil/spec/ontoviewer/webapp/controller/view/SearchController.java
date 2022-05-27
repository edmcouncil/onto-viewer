package org.edmcouncil.spec.ontoviewer.webapp.controller.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ListResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult.Type;
import org.edmcouncil.spec.ontoviewer.core.service.EntityService;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.model.Query;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.edmcouncil.spec.ontoviewer.webapp.service.OntologySearcherService;
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
  private OntologySearcherService ontologySearcher;
  @Autowired
  private ApplicationConfigurationService applicationConfigurationService;
  @Autowired
  private UpdateBlocker blocker;
  @Autowired
  private EntityService entityService;
  @Autowired
  private LuceneSearcher luceneSearcher;

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
    List<OntologyModule> modules = dataManager.getAllModulesData();
    boolean isGrouped = applicationConfigurationService.hasConfiguredGroups();
    long startTimestamp = System.currentTimeMillis();
    SearcherResult result = null;
    try {
      if (UrlChecker.isUrl(query)) {
        LOG.info("URL detected: '{}'", query);
        result = ontologySearcher.search(query, 0);

        long endTimestamp = System.currentTimeMillis();
        LOG.info("URL detected: '{}' (query time: '{}' ms) result is:\n {}", query, endTimestamp - startTimestamp,
            result);

        modelBuilder.emptyQuery();
      } else {
        modelBuilder.setQuery(query);
        List<FindResult> findResults = luceneSearcher.search(query, true);
        List<SearchItem> searchResults = findResults.stream()
            .map(findResult -> {
              var searchItem = new SearchItem();
              searchItem.setIri(findResult.getIri());
              searchItem.setLabel(findResult.getLabel());
              var firstHighlight = findResult.getHighlights().stream().findFirst();
              searchItem.setDescription(firstHighlight.isPresent() ? firstHighlight.get().getHighlightedText() : "");
              searchItem.setRelevancy(findResult.getScore());
              return searchItem;
            })
            .collect(Collectors.toList());
        result = new ListResult(Type.list, searchResults);

        long endTimestamp = System.currentTimeMillis();
        LOG.info("String detected: '{}' (query time: '{}' ms), max: '{}', page '{}', result '{}'", query,
            endTimestamp - startTimestamp, max, page, result);
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
