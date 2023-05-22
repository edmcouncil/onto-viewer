//package org.edmcouncil.spec.ontoviewer.webapp.controller;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//import org.edmcouncil.spec.ontoviewer.core.exception.ApplicationNotInitializedException;
//import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
//import org.edmcouncil.spec.ontoviewer.webapp.controller.api.HintController;
//import org.edmcouncil.spec.ontoviewer.webapp.model.FindResults;
//import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.ResponseEntity;
//
//@SpringBootTest
//class HintControllerTest {
//
//  @Autowired
//  private HintController hintController;
//
//  @MockBean
//  private UpdateBlocker updateBlocker;
//  @MockBean
//  private LuceneSearcher luceneSearcher;
//
//  @Test
//  void shouldReturn200WhenAppIsInitialized() {
//    String query = "test";
//
//    when(updateBlocker.isInitializeAppDone()).thenReturn(true);
//    when(luceneSearcher.search(anyString(), anyBoolean(), anyInt())).thenReturn(FindResults.empty());
//
//    ResponseEntity<FindResults> expectedResult = ResponseEntity.ok().body(FindResults.empty());
//
//    ResponseEntity<FindResults> actualResult = hintController.getHints(query, "1");
//
//    assertThat(actualResult, equalTo(expectedResult));
//  }
//
//  @Test
//  void shouldReturn503ServiceUnavailableWhenTheAppIsNotInitializedYet() {
//    String query = "test";
//
//    when(updateBlocker.isInitializeAppDone()).thenReturn(false);
//
//    assertThrows(ApplicationNotInitializedException.class, () -> {
//      hintController.getHints(query, "1");
//    });
//  }
//}
