package org.edmcouncil.spec.fibo.view.controller;

import java.util.Arrays;
import java.util.List;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
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

  private static final Logger LOG = LoggerFactory.getLogger(ModuleController.class);

  @Autowired
  private DetailsManager ontologyManager;

  @GetMapping("/json")
  public ResponseEntity<List<FiboModule>> getAllModulesDataAsJson() {
    LOG.debug("[REQ] GET : module / json");
    List<FiboModule> modules = ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }

  @GetMapping
  public String getModulesMeta(
      @RequestParam(value = "meta", required = false) String query,
      Model model) {
    LOG.debug("[REQ] GET: module /");
    List<FiboModule> modules = ontologyManager.getAllModulesData();
    ModelBuilder mb = new ModelBuilder(model);

//<<<<<<< HEAD
////<<<<<<< HEAD
////    if (query != null) {
////      OwlDetails details = null;
////      try {
////        details = ontologyManager.getDetailsByIri(query);
////      } catch (NotFoundElementInOntologyException ex) {
////        LOG.info("Handle No such field error. Message: '{}'", ex.getMessage());
////        LOG.trace(Arrays.toString(ex.getStackTrace()));
////        ErrorResult er = new ErrorResult();
////        er.setExMessage(ex.getMessage());
////        er.setMessage("Element not found in loaded ontologies.");
////        return "error";
////      }
////      mb.ontoDetails(details).isGrouped(true);
////    }
////=======
//    mb.setResult(null).isGrouped(true);
////>>>>>>> 698e8acd3ee3d6d286e9f0567ed72c6a8d03d7be
//=======
    mb.setResult(null).isGrouped(true);
//>>>>>>> 01e56f0c0ad44a383b9369d3db938ee8271f1b6a

    mb.emptyQuery().modelTree(modules);

    model = mb.getModel();

    return "module";
  }

}
