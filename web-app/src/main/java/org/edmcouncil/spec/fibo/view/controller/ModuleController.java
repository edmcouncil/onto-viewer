package org.edmcouncil.spec.fibo.view.controller;

import java.util.List;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/", "/index", "module"})
public class ModuleController {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleController.class);

  @Autowired
  private DetailsManager ontologyManager;
  @Autowired
  private UpdateBlocker blocker;

  @GetMapping
  public String getModulesMeta(
          @RequestParam(value = "meta", required = false) String query,
          Model model) {
    LOG.debug("[REQ] GET: module /");

    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      ModelBuilder mb = new ModelBuilder(model);
      mb.emptyQuery();
      model = mb.getModel();
      return "error_503";
    }

    List<FiboModule> modules = ontologyManager.getAllModulesData();
    ModelBuilder mb = new ModelBuilder(model);

    mb.setResult(null).isGrouped(true);

    mb.emptyQuery().modelTree(modules);

    model = mb.getModel();

    return "module";
  }

}
