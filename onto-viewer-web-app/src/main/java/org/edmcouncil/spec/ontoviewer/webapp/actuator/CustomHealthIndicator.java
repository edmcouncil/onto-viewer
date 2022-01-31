package org.edmcouncil.spec.ontoviewer.webapp.actuator;

import java.util.HashMap;
import java.util.Map;
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

  public CustomHealthIndicator(UpdateBlocker updateBlocker) {
    this.updateBlocker = updateBlocker;
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
    Map<String, Boolean> details = new HashMap<>();
    try {
      details.put(HealthDetailsField.INITIALIZATION_DONE.name(), updateBlocker.isInitializeAppDone());
      details.put(HealthDetailsField.UPDATE_ONTOLOGY_IN_PROGRESS.name(), updateBlocker.isUpdateNow());
      details.put(HealthDetailsField.BLOCKED.name(), updateBlocker.isBlocked());
    } catch (Exception e) {
      return Health.down(e).build();
    }
    return Health.up().withDetails(details).build();
  }

  public UpdateBlocker getUpdateBlocker() {
    return updateBlocker;
  }
}
