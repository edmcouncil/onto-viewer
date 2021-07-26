package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class HintControllerIT extends BaseControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturn200WithOneResult() throws Exception {
    String query = "business entity";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$[0].label", is("business entity")));
  }

  @Test
  void shouldReturn200WithMoreThanOneResult() throws Exception {
    String query = "license";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.length()", is(13)))
        .andExpect(jsonPath("$[0].iri", is("http://purl.org/dc/terms/license")));
  }

  @Test
  void shouldReturn200WithMoreThanOneResultWithMaxParameterUsedWhenItIsPresent() throws Exception {
    String query = "license";
    int max = 5;

    this.mockMvc.perform(post("/api/hint/max/" + max).content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath("$.length()", is(5)))
        .andExpect(jsonPath("$[0].iri", is("http://purl.org/dc/terms/license")));
  }

  @Test
  void shouldReturn400WhenQueryIsNotProvided() throws Exception {
        this.mockMvc.perform(post("/api/hint"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenQueryIsAnUrl() throws Exception {
    String query = "http://example.com/";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
  }
}