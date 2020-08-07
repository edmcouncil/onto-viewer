package org.edmcouncil.spec.fibo.view.model;

public class UpdateRequest {

  private String apiKey;
  private String updateId;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getUpdateId() {
    return updateId;
  }

  public void setUpdateId(String updateId) {
    this.updateId = updateId;
  }

}
