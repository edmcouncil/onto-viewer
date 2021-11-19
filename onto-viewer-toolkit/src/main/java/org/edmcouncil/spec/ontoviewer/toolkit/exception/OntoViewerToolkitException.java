package org.edmcouncil.spec.ontoviewer.toolkit.exception;

public class OntoViewerToolkitException extends Exception {

  public OntoViewerToolkitException(String message) {
    super(message);
  }

  public OntoViewerToolkitException(String message, Exception ex) {
    super(message, ex);
  }
}