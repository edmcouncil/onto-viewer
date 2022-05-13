package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import java.util.ArrayList;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.edmcouncil.spec.ontoviewer.webapp.util.UrlChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<List<FindResult>> getHints(@RequestBody String query) {
    checkIfApplicationIsReady();

    long startTimestamp = System.currentTimeMillis();

    if (UrlChecker.isUrl(query)) {
      return ResponseEntity.badRequest().body(new ArrayList<>(0));
    }

    List<FindResult> findResults = luceneSearcher.search(query, false);
    long endTimestamp = System.currentTimeMillis();
    LOG.info("For query: '{}' (query time: '{}' ms) hints are:\n {}",
        query, endTimestamp - startTimestamp, findResults);

    return ResponseEntity.ok(findResults);
  }
}
