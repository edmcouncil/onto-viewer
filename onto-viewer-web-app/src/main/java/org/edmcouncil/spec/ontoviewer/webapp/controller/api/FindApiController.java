package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import java.util.Arrays;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindMode;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/find")
public class FindApiController extends BaseController {

  private final LuceneSearcher luceneSearcher;

  public FindApiController(LuceneSearcher luceneSearcher, UpdateBlocker updateBlocker) {
    super(updateBlocker);
    this.luceneSearcher = luceneSearcher;
  }

  @GetMapping
  public List<FindResult> findByTerm(@RequestParam String term,
      @RequestParam(required = false, defaultValue = "basic") String mode,
      @RequestParam(required = false, defaultValue = "") String findProperties,
      @RequestParam(required = false, defaultValue = "true") boolean useHighlighting) {
    checkIfApplicationIsReady();

    var modeEnum = FindMode.getMode(mode);

    if (modeEnum == null || FindMode.BASIC == modeEnum) {
      return luceneSearcher.search(term, useHighlighting);
    } else if (FindMode.ADVANCE == modeEnum) {
      if (!findProperties.isBlank()) {
        var findPropertiesList = Arrays.asList(findProperties.split("\\."));
        return luceneSearcher.searchAdvance(term, findPropertiesList, useHighlighting);
      } else {
        throw new IllegalArgumentException("findProperties parameter mustn't be empty.");
      }
    } else {
      throw new IllegalArgumentException(
          "Mode parameter should be one of values: " + Arrays.toString(FindMode.values()));
    }
  }

  @GetMapping("/properties")
  public List<FindProperty> getFindProperties() {
    checkIfApplicationIsReady();

    return luceneSearcher.getFindProperties();
  }
}
