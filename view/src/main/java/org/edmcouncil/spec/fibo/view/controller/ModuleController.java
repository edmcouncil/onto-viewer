package org.edmcouncil.spec.fibo.view.controller;

import org.edmcouncil.spec.fibo.weasel.model.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.WeaselOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Set;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping("module")
public class ModuleController {
    
  @Autowired
  private WeaselOntologyManager ontologyManager;
  
  @GetMapping("/json")
  public ResponseEntity getAllModulesData(){
    Set<FiboModule> modules =  ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }
  
  
}
