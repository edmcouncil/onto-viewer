package org.edmcouncil.spec.ontoviewer.toolkit.handlers;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import openllet.owlapi.OpenlletReasonerFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.toolkit.OntoViewerToolkitCommandLine;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OntologyConsistencyChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(OntoViewerToolkitCommandLine.class);

    private final DetailsManager detailsManager;

  public OntologyConsistencyChecker(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public boolean checkOntologyConsistency() {
    var ontology = detailsManager.getOntology();
    boolean reasoner1IsReady = false;
    boolean reasoner2IsReady = false;
    OWLReasoner reasoner1 = null;
    OWLReasoner reasoner2 = null;
    try {
        reasoner1 = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        reasoner1IsReady = true;
    }
    catch (Exception ex) {
    }
    try {
        reasoner2 = new Reasoner(new Configuration(), ontology);
        reasoner2IsReady = true;
      }
    catch (Exception exception) {
        StackTraceElement[] exceptionElements = exception.getStackTrace();
        LOGGER.error("Exception occurred while checking ontology consistency check: {}", exceptionElements[0]);
      }

    if (reasoner1IsReady & reasoner2IsReady) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(reasoner1::isConsistent, executor);
        CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(reasoner2::isConsistent, executor);
        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(future1, future2);

        try {
            Boolean result = (Boolean) firstCompleted.get();
            future1.complete(true);
            future2.complete(true);
            executor.shutdownNow();
            return result;
        } catch (InterruptedException | ExecutionException exception) {
            StackTraceElement[] exceptionElements = exception.getStackTrace();
            LOGGER.error("Exception occurred while checking ontology consistency check: {}", exceptionElements[0]);
            return false;
        } finally {
            executor.shutdown();
        }
    }

    if (reasoner1IsReady) {
        return reasoner1.isConsistent();
    }

    if (reasoner2IsReady) {
        return reasoner2.isConsistent();
    }

    return false;
  }
}