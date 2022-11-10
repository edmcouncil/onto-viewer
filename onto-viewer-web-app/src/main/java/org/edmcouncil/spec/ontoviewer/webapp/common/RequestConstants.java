package org.edmcouncil.spec.ontoviewer.webapp.common;

public final class RequestConstants {

  private RequestConstants() {
  }

  public static final String API_KEY_NOT_VALID_MESSAGE = "ApiKey is not valid for this instance.";
  public static final String X_API_KEY = "X-API-Key";
  public static final String SUCCESS_RESPONSE = "Request succeeded.";
  public static final String APPLICATION_ID= "ApplicationId";
  public static final String APPLICATION_ID_THE_SAME_MESSAGE= "The application identifier from the request is the same as the local application. The request is ignored to prevent self-restart.";
}
