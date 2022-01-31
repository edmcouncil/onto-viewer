package org.edmcouncil.spec.ontoviewer.core.exception;

public class ApplicationNotInitializedException extends RuntimeException {

  public ApplicationNotInitializedException(String message) {
    super(message);
  }

  public ApplicationNotInitializedException(String message, Throwable cause) {
    super(message, cause);
  }
}