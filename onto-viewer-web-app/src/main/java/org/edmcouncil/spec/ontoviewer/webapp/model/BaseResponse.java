package org.edmcouncil.spec.ontoviewer.webapp.model;

public class BaseResponse {

  private final String message;

  public BaseResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}