package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.edmcouncil.spec.ontoviewer.webapp.service.TextSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class HintControllerTest {

  @Autowired
  private HintController hintController;

  @MockBean
  private UpdateBlocker updateBlocker;
  @MockBean
  private TextSearchService textSearch;
  @MockBean
  private LuceneSearcher luceneSearcher;

  @Test
  void shouldReturn200WhenAppIsInitialized() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(true);
    when(textSearch.getHints(eq(query), any())).thenReturn(prepareHints());

    ResponseEntity<List<FindResult>> expectedResult = ResponseEntity.ok().body(Collections.emptyList());

    ResponseEntity<List<FindResult>> actualResult = hintController.getHints(query);

    assertThat(actualResult, equalTo(expectedResult));
  }

  @Test
  void shouldReturn503ServiceUnavailableWhenTheAppIsNotInitializedYet() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(false);

    ResponseEntity<List<FindResult>> expectedResult = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

    ResponseEntity<List<FindResult>> actualResult = hintController.getHints(query);

    assertThat(actualResult, equalTo(expectedResult));
  }

  private List<HintItem> prepareHints() {
    return Collections.emptyList();
  }
}
