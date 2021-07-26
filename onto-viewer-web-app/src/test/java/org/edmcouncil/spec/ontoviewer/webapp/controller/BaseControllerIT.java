package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.awaitility.Awaitility.await;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.UpdateBlocker;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerIT {

  @Autowired
  private UpdateBlocker updateBlocker;

  @BeforeEach
  void setUp() {
    waitUntilServiceReady();
  }

  private void waitUntilServiceReady() {
    await()
        .atMost(30, TimeUnit.SECONDS)
        .until(() -> updateBlocker.isInitializeAppDone());
  }
}