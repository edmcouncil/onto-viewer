package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.exception.ApplicationNotInitializedException;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.api.HintController;
import org.edmcouncil.spec.ontoviewer.webapp.model.FindResult;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class HintControllerTest {

  @Autowired
  private HintController hintController;

  @MockBean
  private UpdateBlocker updateBlocker;
  @MockBean
  private LuceneSearcher luceneSearcher;

  @Test
  void shouldReturn200WhenAppIsInitialized() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(true);

    ResponseEntity<List<FindResult>> expectedResult = ResponseEntity.ok().body(Collections.emptyList());

    ResponseEntity<List<FindResult>> actualResult = hintController.getHints(query);

    assertThat(actualResult, equalTo(expectedResult));
  }

  @Test
  void shouldReturn503ServiceUnavailableWhenTheAppIsNotInitializedYet() {
    String query = "test";

    when(updateBlocker.isInitializeAppDone()).thenReturn(false);

    assertThrows(ApplicationNotInitializedException.class, () -> {
      hintController.getHints(query);
    });
  }
}
