package org.edmcouncil.spec.fibo.view.controller;

import java.util.ArrayList;
import java.util.List;
import org.edmcouncil.spec.fibo.view.service.TextSearchService;
import org.edmcouncil.spec.fibo.view.util.UrlChecker;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.hint.HintItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/hint"})
public class HintController {

  private static final Logger LOG = LoggerFactory.getLogger(HintController.class);
  private static final String DEFAULT_MAX_HINT_RESULT_COUNT = "20";

  @Autowired
  private TextSearchService textSearch;

  @GetMapping("/{query}")
  public ResponseEntity getHints(
      @PathVariable("query") String query,
      @RequestParam(required = false,
          defaultValue = DEFAULT_MAX_HINT_RESULT_COUNT,
          value = "max") Integer maxHintCount) {
    LOG.debug("[REQ] GET hint/ {{}} ?max = {{}}", query, maxHintCount);

    if (UrlChecker.isUrl(query)) {
      //TODO: throw ?
      return ResponseEntity.badRequest().body(new ArrayList<HintItem>(0));
    }

    List<HintItem> result = textSearch.getHints(query, maxHintCount);

    //return list of hint result
    return ResponseEntity.ok(result);
  }

}
