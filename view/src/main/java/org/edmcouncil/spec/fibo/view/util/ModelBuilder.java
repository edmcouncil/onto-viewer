package org.edmcouncil.spec.fibo.view.util;

import java.util.Collection;
import org.edmcouncil.spec.fibo.view.model.Query;
import org.edmcouncil.spec.fibo.weasel.model.OwlDetails;
import java.util.List;

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

  public ModelBuilder ontoDetails(Collection details) {

    model.addAttribute("details_list", details);
    model.addAttribute("details_display", true);

    return this;
  }

  public ModelBuilder isGrouped(boolean grouped) {
    model.addAttribute("grouped_details", grouped);
    return this;
  }

}
