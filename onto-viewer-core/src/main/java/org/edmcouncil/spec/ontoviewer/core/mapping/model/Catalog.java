package org.edmcouncil.spec.ontoviewer.core.mapping.model;

import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Catalog {

  @Attribute
  private String prefer;

  @ElementList(inline = true)
  private List<Uri> uri;

  public String getPrefer() {
    return prefer;
  }

  public List<Uri> getUri() {
    return uri;
  }
} 