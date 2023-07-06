package org.edmcouncil.spec.ontoviewer.configloader.configuration.model.exception;

public class UnableToLoadConfigurationException extends Exception {

   public UnableToLoadConfigurationException(String message) {
    super(message);
  }

  public UnableToLoadConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}