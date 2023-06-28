package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseControllerIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class SearchApiControllerIT extends BaseControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturn200WhenThereAreNotAnyMatchingEntities() throws Exception {
    var query = "somerandomrubbish";

    this.mockMvc.perform(post("/api/search").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void shouldReturn200ListWithResultWhenThereAreMatchingEntities() throws Exception {
    var query = "license";

    this.mockMvc.perform(post("/api/search").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(7)))
        .andExpect(jsonPath("$.type", is("list")))
        .andExpect(jsonPath("$.result.length()", is(11)))
        .andExpect(jsonPath(
            "$.result[0].iri",
            is("https://spec.edmcouncil.org/fibo/ontology/FND/Law/LegalCapacity/License")));
  }

  @Test
  void shouldReturn200ListWithPagedResultsWhenThereAreManyMatchingEntities() throws Exception {
    var query = "entity";

    this.mockMvc.perform(post("/api/search").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(7)))
        .andExpect(jsonPath("$.type", is("list")))
        .andExpect(jsonPath("$.result.length()", is(6)));
  }

  @Test
  void shouldReturn400ResultWhenEntityWithUrlInQueryIsNotPresentInOntology() throws Exception {
    var query = "http://example.com/test";

    this.mockMvc.perform(post("/api/search").content(query))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Element Not Found")))
        .andExpect(jsonPath("$.exMessage", is("Not found element with IRI: " + query)));
  }

  @Test
  void shouldReturn200SingleResultWhenQueryContainsExistingClassUrl() throws Exception {
    var query = "https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/LegalPersons/BusinessLicense";

    this.mockMvc.perform(post("/api/search").content(query))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("$.type", is(SearcherResult.Type.details.name())))
        .andExpect(jsonPath("$.result.length()", is(8)))
        .andExpect(jsonPath("$.result.label", is("business license")))
        .andExpect(jsonPath("$.result.iri", is(query)))
        .andExpect(jsonPath("$.result.qName", is("QName: fibo-be-le-lp:BusinessLicense")))
        .andExpect(jsonPath("$.result.taxonomy.value[0].length()", is(5)))
        .andExpect(jsonPath("$.result.maturityLevel.label", is("release")))
        .andExpect(jsonPath("$.result.properties.length()", is(4)));
  }

  @Test
  void shouldReturn400WhenQueryBodyParameterIsNotProvided() throws Exception {
    this.mockMvc.perform(post("/api/search"))
        .andExpect(status().isBadRequest());
  }
}
