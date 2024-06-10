package org.edmcouncil.spec.ontoviewer.toolkit.handlers;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import openllet.owlapi.OpenlletReasonerFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.springframework.stereotype.Service;

@Service
public class OntologyConsistencyChecker {

  private final DetailsManager detailsManager;

  public OntologyConsistencyChecker(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public boolean checkOntologyConsistency() {
    var ontology = detailsManager.getOntology();
    var reasoner1 = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
    var reasoner2 = new Reasoner(new Configuration(), ontology);

    // Create an executor service to run tasks concurrently
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // Create CompletableFutures for the isConsistent calls
    CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(reasoner1::isConsistent, executor);
    CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(reasoner2::isConsistent, executor);

    // Use the anyOf method to return the result of the first completed future
    CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(future1, future2);

    try {
        // Get the result of the first completed future
        Boolean result = (Boolean) firstCompleted.get();
        return result;
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        return false; // or handle the exception as needed
    } finally {
        // Shutdown the executor service
        executor.shutdown();
    }
  }
}