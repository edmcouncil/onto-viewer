package org.edmcouncil.spec.ontoviewer.core.mapping.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Uri {

  @Attribute(required = false)
  private String id;

  @Attribute
  private String name;

  @Attribute
  private String uri;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }
}