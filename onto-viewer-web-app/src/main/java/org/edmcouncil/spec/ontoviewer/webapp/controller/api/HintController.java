package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResults;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.edmcouncil.spec.ontoviewer.webapp.util.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@RestController
@RequestMapping(value = {"/api/hint"})
public class HintController extends BaseController {

  private static final Logger LOG = LoggerFactory.getLogger(HintController.class);

  private final LuceneSearcher luceneSearcher;

  public HintController(LuceneSearcher luceneSearcher, UpdateBlocker updateBlocker) {
    super(updateBlocker);
    this.luceneSearcher = luceneSearcher;
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FindResults> getHints(@RequestBody String query,
      @RequestParam(required = false, defaultValue = "1") String page) {
    checkIfApplicationIsReady();

    long startTimestamp = System.currentTimeMillis();

    int pageNumber = 1;
    try {
      pageNumber = Integer.parseInt(page);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(String.format("Page '%s' can't be parsed as number.", page));
    }

    if (pageNumber < 1) {
      throw new IllegalArgumentException(String.format("Page '%s' is lower than '1'.", page));
    }

    if (UrlChecker.isUrl(query)) {
      return ResponseEntity.badRequest().body(FindResults.empty());
    }

    FindResults findResults = luceneSearcher.search(query, false, pageNumber);
    long endTimestamp = System.currentTimeMillis();
    LOG.info("For query: '{}' (query time: '{}' ms) hints are:\n {}",
        query, endTimestamp - startTimestamp, findResults);

    return ResponseEntity.ok(findResults);
  }
}
