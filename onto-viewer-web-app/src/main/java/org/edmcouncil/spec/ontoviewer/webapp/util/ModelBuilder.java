package org.edmcouncil.spec.ontoviewer.webapp.util;

import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsMapped;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.Query;
import org.springframework.ui.Model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ModelBuilder {

  private final Model model;

  public ModelBuilder(Model builderModel) {
    this.model = builderModel;
  }

  public Model getModel() {
    return model;
  }

  public ModelBuilder setQuery(String query) {
    if (query == null) {
      model.addAttribute("query", new Query());
    } else {
      Query q = new Query();
      q.setValue(query);
      model.addAttribute("query", q);
    }
    return this;
  }

  public ModelBuilder emptyQuery() {
    model.addAttribute("query", new Query());
    return this;
  }

  public ModelBuilder setResult(SearcherResult result) {
    model.addAttribute("result", result);
    model.addAttribute("details_display", true);

    return this;
  }

  public ModelBuilder isGrouped(boolean grouped) {
    model.addAttribute("grouped_details", grouped);
    return this;
  }

  public ModelBuilder modelTree(List<OntologyModule> modules) {
    model.addAttribute("modelTree", modules);
    return this;
  }
  
  public ModelBuilder stats(OntologyStatsMapped s) {
    model.addAttribute("stats", s);
    return this;
  }

  public ModelBuilder setVersion(String version) {
    model.addAttribute("version", version);
    return this;
  }

  public ModelBuilder error(ErrorResponse er) {
    model.addAttribute("error", er);
    return this;
  }

}
