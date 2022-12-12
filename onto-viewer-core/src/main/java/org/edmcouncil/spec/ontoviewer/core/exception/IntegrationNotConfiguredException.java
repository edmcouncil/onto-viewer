package org.edmcouncil.spec.ontoviewer.core.exception;

public class IntegrationNotConfiguredException extends RuntimeException {

  public IntegrationNotConfiguredException(String message) {
    super(message);
  }

  public IntegrationNotConfiguredException(String message, Throwable cause) {
    super(message, cause);
  }
}
