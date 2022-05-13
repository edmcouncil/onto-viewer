package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ListResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult.Type;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.edmcouncil.spec.ontoviewer.webapp.service.OntologySearcherService;
import org.edmcouncil.spec.ontoviewer.webapp.util.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping("/api/search")
public class SearchApiController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchApiController.class);

  private final LuceneSearcher luceneSearcher;
  private final OntologySearcherService ontologySearcher;
  private final UpdateBlocker blocker;

  private static final Integer DEFAULT_MAX_SEARCH_RESULT_COUNT = 20;

  public SearchApiController(LuceneSearcher luceneSearcher, OntologySearcherService ontologySearcherService,
      UpdateBlocker blocker) {
    this.luceneSearcher = luceneSearcher;
    this.ontologySearcher = ontologySearcherService;
    this.blocker = blocker;
  }

  @PostMapping(value = "")
  public ResponseEntity<SearcherResult> searchJson(@RequestBody String query) {
    if (!blocker.isInitializeAppDone()) {
      LOGGER.debug("Application initialization has not completed");
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    LOGGER.warn("This API is deprecated and will be removed in the future.");

    long startTimestamp = System.currentTimeMillis();

    SearcherResult result;
    try {
      if (UrlChecker.isUrl(query)) {
        result = ontologySearcher.search(query, 0);
        long endTimestamp = System.currentTimeMillis();
        LOGGER.info("For query: '{}' (query time: '{}' ms) {}", query, endTimestamp - startTimestamp, result);
      } else {
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
        LOGGER.info("For query: '{}' (query time: '{}' ms) result is:\n {}",
            query, endTimestamp - startTimestamp, result);
      }
    } catch (ViewerException ex) {
      return getError(ex);
    }

    return ResponseEntity.ok(result);
  }

  private ResponseEntity getError(ViewerException ex) {
    LOGGER.info("Handle NotFoundElementInOntologyException. Message: '{}'", ex.getMessage());

    return ResponseEntity.badRequest().body(
        new ErrorResponse("Element Not Found.", ex.getMessage()));
  }
}
