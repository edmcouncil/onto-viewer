package org.edmcouncil.spec.fibo.view.util;

import org.edmcouncil.spec.fibo.view.model.Query;
import java.util.List;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;

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

  public ModelBuilder modelTree(List<FiboModule> modules) {
    model.addAttribute("modelTree", modules);
    return this;
  }

  public ModelBuilder setVersion(String version) {
    model.addAttribute("version", version);
    return this;
  }

  public ModelBuilder error(ErrorResult er) {
    model.addAttribute("error", er);
    return this;
  }

}
