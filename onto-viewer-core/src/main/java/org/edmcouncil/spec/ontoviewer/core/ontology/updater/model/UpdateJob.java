package org.edmcouncil.spec.ontoviewer.core.ontology.updater.model;

public class UpdateJob {

  private long start;
  private String id;
  private UpdateJobStatus status;
  private String msg;

  public UpdateJob() {

  }

  public long getStartTimestamp() {
    return start;
  }

  public void setStartTimestamp(long timestamp) {
    this.start = timestamp;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UpdateJobStatus getStatus() {
    return status;
  }

  public void setStatus(UpdateJobStatus status) {
    this.status = status;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public String toString() {
    return "UpdateJob{" + "timestamp=" + start + ", id=" + id + ", status=" + status + ", msg=" + msg + '}';
  }

  

}
