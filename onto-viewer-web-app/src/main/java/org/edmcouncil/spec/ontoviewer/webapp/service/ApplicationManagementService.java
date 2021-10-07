package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationManagementService.class);
  private static final int APPLICATION_STOPPED_CODE = 100;

  private final ApplicationContext applicationContext;

  public ApplicationManagementService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void stopApplication() {
    LOGGER.info("Attempt to stop the application.");

    Runnable runnable = () -> {
      LOGGER.info("Stopping application right now...");
      var exitCode = SpringApplication.exit(applicationContext, () -> APPLICATION_STOPPED_CODE);
      System.exit(exitCode);
    };

    var executor = Executors.newScheduledThreadPool(1);
    executor.schedule(runnable, 5, TimeUnit.SECONDS);
  }
}
