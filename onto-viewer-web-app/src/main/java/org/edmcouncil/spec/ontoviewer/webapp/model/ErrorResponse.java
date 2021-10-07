package org.edmcouncil.spec.ontoviewer.webapp.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ErrorResponse extends BaseResponse {

  private final String exMessage;

  public ErrorResponse(String message, String exMessage) {
    super(message);
    this.exMessage = exMessage;
  }

  public String getExMessage() {
    return exMessage;
  }
}
