package org.edmcouncil.spec.ontoviewer.webapp.actuator;

import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

  private final UpdateBlocker updateBlocker;
  private final OntologyManager ontologyManager;

  public CustomHealthIndicator(UpdateBlocker updateBlocker, OntologyManager ontologyManager) {
    this.updateBlocker = updateBlocker;
    this.ontologyManager = ontologyManager;
  }

  @Override
  public Health getHealth(boolean includeDetails) {
    return check();
  }

  @Override
  public Health health() {
    return check();
  }

  private Health check() {
    Map<String, Object> details = new HashMap<>();
    try {
      details.put(HealthDetailsField.INITIALIZATION_DONE.name(), updateBlocker.isInitializeAppDone());
      details.put(HealthDetailsField.UPDATE_ONTOLOGY_IN_PROGRESS.name(), updateBlocker.isUpdateNow());
      details.put(HealthDetailsField.BLOCKED.name(), updateBlocker.isBlocked());
      details.put(HealthDetailsField.MISSING_IMPORTS.name(), ontologyManager.getMissingImports());
    } catch (Exception e) {
      return Health.down(e).build();
    }
    return Health.up().withDetails(details).build();
  }

  public UpdateBlocker getUpdateBlocker() {
    return updateBlocker;
  }
}
