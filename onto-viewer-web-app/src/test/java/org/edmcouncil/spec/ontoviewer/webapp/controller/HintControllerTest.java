package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.edmcouncil.spec.ontoviewer.webapp.service.TextSearchService;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.UpdateBlocker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class HintControllerTest {

  @Autowired
  private HintController hintController;

  @MockBean
  private UpdateBlocker updateBlocker;
  @MockBean
  private TextSearchService textSearch;

  @Test
  void shouldReturn200WhenAppIsInitialized() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(true);
    when(textSearch.getHints(eq(query), any())).thenReturn(prepareHints());

    ResponseEntity<List<HintItem>> expectedResult = ResponseEntity.ok().body(Collections.emptyList());

    ResponseEntity<List<HintItem>> actualResult = hintController.getHints(query, Optional.empty());

    assertThat(actualResult, equalTo(expectedResult));
  }

  @Test
  void shouldReturn503ServiceUnavailableWhenTheAppIsNotInitializedYet() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(false);

    ResponseEntity<List<HintItem>> expectedResult = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

    ResponseEntity<List<HintItem>> actualResult = hintController.getHints(query, Optional.empty());

    assertThat(actualResult, equalTo(expectedResult));
  }

  private List<HintItem> prepareHints() {
    return Collections.emptyList();
  }
}
