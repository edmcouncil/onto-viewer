package org.edmcouncil.spec.ontoviewer.core.exception;

public class RequestHandlingException extends RuntimeException {

  public RequestHandlingException(String message) {
    super(message);
  }

  public RequestHandlingException(String message, Throwable cause) {
    super(message, cause);
  }
}
