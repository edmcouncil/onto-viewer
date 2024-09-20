package org.edmcouncil.spec.ontoviewer.toolkit.handlers;


import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.PelletReasoner;
import openllet.owlapi.explanation.PelletExplanation;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.toolkit.OntoViewerToolkitCommandLine;
import org.edmcouncil.spec.ontoviewer.toolkit.model.ConsistencyCheckResult;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import  org.semanticweb.owlapi.reasoner.InferenceType;

@Service
public class OntologyConsistencyChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(OntoViewerToolkitCommandLine.class);

    private final DetailsManager detailsManager;

  public OntologyConsistencyChecker(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public ConsistencyCheckResult checkOntologyConsistency() {
    var ontology = detailsManager.getOntology();
    ReasonerFactory factory = new ReasonerFactory();
    boolean reasoner1IsReady = false;
    boolean reasoner2IsReady = false;
    OpenlletReasoner reasoner1 = null;
    OWLReasoner reasoner2 = null;
    Boolean isConsistent = true;
    Boolean v = true;
    ConsistencyCheckResult consistencyCheckResult = null;

    try {
        reasoner1 = new PelletReasoner(ontology, BufferingMode.BUFFERING);
        reasoner1IsReady = true;
    }
    catch (Exception exception) {
        StackTraceElement[] exceptionElements = exception.getStackTrace();
        LOGGER.error("Exception occurred while checking ontology consistency check with Openllet: {}", exceptionElements[0]);
    }
    try {
            Configuration configuration=new Configuration();
            configuration.throwInconsistentOntologyException=false;
            reasoner2=factory.createReasoner(ontology, configuration);
            reasoner2IsReady = true;
      }
    catch (Exception exception) {
        StackTraceElement[] exceptionElements = exception.getStackTrace();
        LOGGER.error("Exception occurred while checking ontology consistency check with Hermit: {}", exceptionElements[0]);
      }

    if (reasoner1IsReady & reasoner2IsReady) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(reasoner1::isConsistent, executor);
        CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(reasoner2::isConsistent, executor);
        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(future1, future2);

        try {
                isConsistent = (Boolean) firstCompleted.get();
                if (future1.isDone()) {
                    consistencyCheckResult = getPelletConsistencyCheckResult(reasoner1, isConsistent);
                }
                if (future2.isDone()) {
                    consistencyCheckResult = getHermitConsistencyCheckResult(ontology, factory, reasoner2, isConsistent);
                }
                future1.complete(true);
                future2.complete(true);
                executor.shutdownNow();
                return consistencyCheckResult;
        } catch (InterruptedException | ExecutionException exception) {
            StackTraceElement[] exceptionElements = exception.getStackTrace();
            LOGGER.error("Exception occurred while checking ontology consistency: {}", exceptionElements[0]);
            return new ConsistencyCheckResult(false, "");
        } finally {
            executor.shutdown();
      }
    }

      if (reasoner1IsReady) {
          isConsistent = reasoner1.isConsistent();
          consistencyCheckResult = getPelletConsistencyCheckResult(reasoner1, isConsistent);
          return consistencyCheckResult;
      }

      if (reasoner2IsReady) {
          isConsistent = reasoner2.isConsistent();
          consistencyCheckResult = getHermitConsistencyCheckResult(ontology, factory, reasoner2, isConsistent);
          return consistencyCheckResult;
      }

      LOGGER.error("Exception occurred while checking ontology consistency - both reasoners were not ready");
      return new ConsistencyCheckResult(false, "");
  }

  private ConsistencyCheckResult getPelletConsistencyCheckResult(OpenlletReasoner reasoner, Boolean isConsistent) {
      String inconsistencyExplanation = reasoner.getKB().getExplanation();
      ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult(isConsistent, inconsistencyExplanation);
      return consistencyCheckResult;
  }

    private ConsistencyCheckResult getHermitConsistencyCheckResult(OWLOntology ontology, ReasonerFactory factory, OWLReasoner reasoner, Boolean isConsistent) {
        BlackBoxExplanation explanation = new BlackBoxExplanation(ontology, factory, reasoner);
        IRI owlThing = IRI.create("http://www.w3.org/2002/07/owl#Thing");
        var explanations = explanation.getExplanation(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(owlThing));
        String inconsistencyExplanation = explanations.toString();
        ConsistencyCheckResult consistencyCheckResult = new ConsistencyCheckResult(isConsistent, inconsistencyExplanation);
        return consistencyCheckResult;
    }
}