package org.edmcouncil.spec.fibo.view.controller;

import java.util.Arrays;
import org.edmcouncil.spec.fibo.view.model.Query;
import org.edmcouncil.spec.fibo.view.service.UrlSearchService;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.validation.Valid;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.view.util.ModelBuilderFactory;
import org.edmcouncil.spec.fibo.view.util.UrlChecker;
import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping("/search")
public class SearchController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private UrlSearchService searchService;
  @Autowired
  private DataManager dataManager;
  @Autowired
  private ModelBuilderFactory modelFactory;

  @PostMapping
  public ModelAndView redirectSearch(@Valid @ModelAttribute("queryValue") Query query) {
    Map<String, Object> model = new HashMap<>();
    model.put("query", query.getValue());
    ModelAndView mv = new ModelAndView("redirect:/search", model);
    return mv;
  }

  @GetMapping
  public String search(@RequestParam("query") String query, Model model) {

    LOG.info("[REQ] GET : search ? query = {{}}", query);
    Query q = new Query();
    q.setValue(query);
    ModelBuilder modelBuilder = modelFactory.getInstance(model);
    List<FiboModule> modules = dataManager.getAllModulesData();
    
    if(UrlChecker.isUrl(query)){
      LOG.info("URL detected, search specyfic element");
    } else {
      LOG.info("String detected, search elements with given label");
    }
    try {
      searchService.search(query, modelBuilder);
    } catch (NotFoundElementInOntologyException ex) {
      LOG.info("Handle NotFoundElementInOntologyException. Message: '{}'", ex.getMessage());
      LOG.trace(Arrays.toString(ex.getStackTrace()));
      ErrorResult er = new ErrorResult();
      er.setExMessage(ex.getMessage());
      er.setMessage("Element Not Found.");
      modelBuilder.emptyQuery();
      modelBuilder.error(er);
      return "error";
    }
    modelBuilder.modelTree(modules);

    return "search";
  }

  @PostMapping("/json")
  public <T extends OwlDetails> ResponseEntity searchJson(@RequestBody String query, Model model) {

    LOG.info("[REQ] POST : search / json   RequestBody = {{}}", query);
    ModelBuilder modelBuilder = new ModelBuilder(model);

    OwlDetails search = null;
    try {
      search = searchService.search(query, modelBuilder);
    } catch (NotFoundElementInOntologyException ex) {
      LOG.info("Handle NotFoundElementInOntologyException. Message: '{}'", ex.getMessage());
      LOG.trace(Arrays.toString(ex.getStackTrace()));
      ErrorResult er = new ErrorResult();
      er.setExMessage(ex.getMessage());
      er.setMessage("Element Not Found.");
      return ResponseEntity.badRequest().body(er);
    }

    return ResponseEntity.ok((T) search);
  }

}
