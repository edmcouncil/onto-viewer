package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.webapp.service.TextSearchService;
import org.edmcouncil.spec.ontoviewer.webapp.util.UrlChecker;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@RestController
@RequestMapping(value = {"/api/hint"})
public class HintController {

  private static final Logger LOG = LoggerFactory.getLogger(HintController.class);
  private static final Integer DEFAULT_MAX_HINT_RESULT_COUNT = 20;

  private final TextSearchService textSearch;
  private final UpdateBlocker blocker;

  public HintController(TextSearchService textSearch, UpdateBlocker blocker) {
    this.textSearch = textSearch;
    this.blocker = blocker;
  }

  @PostMapping(value = {"", "/max/{max}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<HintItem>> getHints(
          @RequestBody String query,
          @PathVariable Optional<Integer> max) {
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
    long startTimestamp = System.currentTimeMillis();
    Integer maxHintCount = max.orElse(DEFAULT_MAX_HINT_RESULT_COUNT);

    if (UrlChecker.isUrl(query)) {
      return ResponseEntity.badRequest().body(new ArrayList<>(0));
    }

    List<HintItem> result = textSearch.getHints(query, maxHintCount);
    long endTimestamp = System.currentTimeMillis();
    LOG.info("For query: '{}' (query time: '{}' ms) hints are:\n {}", query, endTimestamp - startTimestamp, result);

    return ResponseEntity.ok(result);
  }
}
