package org.edmcouncil.spec.fibo.view.model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ErrorResult {

  private String message;
  private String exMessage;

  public ErrorResult() {
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getExMessage() {
    return exMessage;
  }

  public void setExMessage(String exMessage) {
    this.exMessage = exMessage;
  }

}
