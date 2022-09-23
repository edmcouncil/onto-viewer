package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import java.util.Arrays;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindMode;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResults;
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
  public FindResults findByTerm(@RequestParam String term,
      @RequestParam(required = false, defaultValue = "basic") String mode,
      @RequestParam(required = false, defaultValue = "") String findProperties,
      @RequestParam(required = false, defaultValue = "true") boolean useHighlighting,
      @RequestParam(required = false, defaultValue = "1") String page) {
    checkIfApplicationIsReady();

    var modeEnum = FindMode.getMode(mode);

    var pageNumber = 1;
    try {
      pageNumber = Integer.parseInt(page);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(String.format("Page '%s' can't be parsed as number.", page));
    }

    if (pageNumber < 1) {
      throw new IllegalArgumentException(String.format("Page '%s' is lower than '1'.", page));
    }

    if (modeEnum == null || FindMode.BASIC == modeEnum) {
      return luceneSearcher.search(term, useHighlighting, pageNumber);
    } else if (FindMode.ADVANCE == modeEnum) {
      if (!findProperties.isBlank()) {
        var findPropertiesList = Arrays.asList(findProperties.split("\\."));
        return luceneSearcher.searchAdvance(term, findPropertiesList, useHighlighting, pageNumber);
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
