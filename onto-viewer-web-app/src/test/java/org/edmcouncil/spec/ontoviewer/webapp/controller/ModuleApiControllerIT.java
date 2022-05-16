package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ModuleApiControllerIT extends BaseControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturn200WithListOfModules() throws Exception {
    mockMvc.perform(get("/api/module"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("$[0].iri", is("https://spec.edmcouncil.org/fibo/ontology/BE/MetadataBE/BEDomain")))
        .andExpect(jsonPath("$[0].label", is("Business Entities")))
        .andExpect(jsonPath("$[0].maturityLevel.label", is("prod")))
        .andExpect(jsonPath("$[0].subModule.length()", is(9)));
  }
}