package org.edmcouncil.spec.ontoviewer.webapp.actuator;

import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.UpdateBlocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

  @Autowired
  private UpdateBlocker updateBlocker;

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

    details.put("initialization done", updateBlocker.isInitializeAppDone());
    details.put("update now", updateBlocker.isUpdateNow());
    details.put("blocked", updateBlocker.isBlocked()); 
    
    return Health.up().withDetails(details).build();
  }
  
}
