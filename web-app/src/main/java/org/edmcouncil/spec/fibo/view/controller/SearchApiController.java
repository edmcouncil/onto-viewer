package org.edmcouncil.spec.fibo.view.controller;

import java.util.Arrays;
import org.edmcouncil.spec.fibo.view.service.OntologySearcherService;
import java.util.Optional;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.view.service.TextSearchService;
import org.edmcouncil.spec.fibo.view.util.UrlChecker;
import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping("/api/search")
public class SearchApiController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchApiController.class);

  @Autowired
  private TextSearchService textSearchService;
  @Autowired
  private OntologySearcherService ontologySearcher;

  @Autowired
  private UpdateBlocker blocker;

  private static final Integer DEFAULT_MAX_SEARCH_RESULT_COUNT = 20;
  private static final Integer DEFAULT_RESULT_PAGE = 1;

  @PostMapping(value = {"", "/max/{max}", "/page/{page}", "/max/{max}/page/{page}"})
  public <SearcherResult> ResponseEntity searchJson(
          @RequestBody String query,
          Model model,
          @PathVariable Optional<Integer> max,
          @PathVariable Optional<Integer> page
  ) {

    LOG.info("[REQ] POST : api / search / max / {{}} / page /{{}} | RequestBody = {{}}", query);
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>("503 Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    SearcherResult result = null;
    try {

      if (UrlChecker.isUrl(query)) {
        LOG.info("URL detected, search specyfic element");
        result = (SearcherResult) ontologySearcher.search(query, 0);
      } else {
        Integer maxResults = max.isPresent() ? max.get() : DEFAULT_MAX_SEARCH_RESULT_COUNT;
        Integer currentPage = page.isPresent() ? page.get() : DEFAULT_RESULT_PAGE;
        LOG.info("String detected, search elements with given label");
        result = (SearcherResult) textSearchService.search(query, maxResults, currentPage);
      }

    } catch (ViewerException ex) {
      return getError(ex);
    }

    return ResponseEntity.ok(result);
  }

  private ResponseEntity getError(ViewerException ex) {
    LOG.info("Handle NotFoundElementInOntologyException. Message: '{}'", ex.getMessage());
    LOG.trace(Arrays.toString(ex.getStackTrace()));
    ErrorResult er = new ErrorResult();
    er.setExMessage(ex.getMessage());
    er.setMessage("Element Not Found.");
    return ResponseEntity.badRequest().body(er);
  }
}
