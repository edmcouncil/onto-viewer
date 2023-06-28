package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseControllerIT;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class HintControllerIT extends BaseControllerIT {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private LuceneSearcher luceneSearcher;

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
    luceneSearcher.populateIndex();
  }

  @Test
  void shouldReturn200WithOneResult() throws Exception {
    String query = "business entity";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(25)))
        .andExpect(jsonPath("$[0].label", is("business entity")));
  }

  @Test
  void shouldReturn200WithMoreThanOneResult() throws Exception {
    String query = "license";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(11)))
        .andExpect(
            jsonPath(
                "$[0].iri",
                is("https://spec.edmcouncil.org/fibo/ontology/FND/Law/LegalCapacity/License")));
  }

  @Test
  void shouldReturn200WithMoreThanOneResultWithMaxParameterUsedWhenItIsPresent() throws Exception {
    String query = "license";

    this.mockMvc.perform(post("/api/hint").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(11)))
        .andExpect(jsonPath(
            "$[0].iri",
            is("https://spec.edmcouncil.org/fibo/ontology/FND/Law/LegalCapacity/License")));
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
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }
}