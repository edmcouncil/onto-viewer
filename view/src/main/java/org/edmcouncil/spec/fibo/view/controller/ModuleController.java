package org.edmcouncil.spec.fibo.view.controller;

import java.util.Collection;
import org.edmcouncil.spec.fibo.weasel.model.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.WeaselOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Set;
import org.edmcouncil.spec.fibo.view.service.SearchService;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleController.class);
  
  @Autowired
  private WeaselOntologyManager ontologyManager;

  @GetMapping("/json")
  public ResponseEntity<Set<FiboModule>> getAllModulesDataAsJson() {
    LOGGER.debug("[GET]: module/json");
    Set<FiboModule> modules = ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }
  
  @GetMapping
  public String getModulesMeta(
      @RequestParam(value = "meta", required = false) String query,
      Model model,
      @RequestParam(value = "lvl1", required = false) String lvl1,
      @RequestParam(value = "lvl2", required = false) String lvl2) {
    LOGGER.debug("[GET]: module/");
    Set<FiboModule> modules = ontologyManager.getAllModulesData();
    ModelBuilder mb = new ModelBuilder(model);

    if (query != null) {
      OwlDetails details = ontologyManager.getDetailsByIri(query);
      mb.ontoDetails(details).isGrouped(true);
    }

    mb.emptyQuery().modelTree(modules);
    if(lvl1 != null){
      mb.treeLvl1(lvl1);
    }
    if(lvl2 != null){
      mb.treeLvl2(lvl2);
    }
    model = mb.getModel();

    return "module";
  }

}
