package org.edmcouncil.spec.fibo.view.controller;

import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
public class IndexController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

  @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
  public String index(Model model) {
    LOGGER.debug("GET: index");

    model = new ModelBuilder(model)
        .emptyQuery()
        .getModel();
    
    return "index";
  }

}
