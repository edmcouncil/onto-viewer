package org.edmcouncil.spec.fibo.view.util;

import java.util.Collection;
import org.edmcouncil.spec.fibo.view.model.Query;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlListDetailsDetails;
import java.util.List;
import java.util.Set;
import org.edmcouncil.spec.fibo.weasel.model.FiboModule;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;

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

  public ModelBuilder ontoDetails(OwlDetails details) {

    model.addAttribute("details", details);
    model.addAttribute("details_display", true);

    return this;
  }

  public ModelBuilder isGrouped(boolean grouped) {
    model.addAttribute("grouped_details", grouped);
    return this;
  }

  public ModelBuilder modelTree(Set<FiboModule> modules) {
    model.addAttribute("modelTree", modules);
    return this;
  }

  public ModelBuilder treeLvl1(String lvl) {
    model.addAttribute("lvl1", lvl);
    return this;
  }

  public ModelBuilder treeLvl2(String lvl) {
    model.addAttribute("lvl2", lvl);
    return this;
  }

}
