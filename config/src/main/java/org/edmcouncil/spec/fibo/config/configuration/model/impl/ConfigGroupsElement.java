package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementAbstract;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementType;
import org.edmcouncil.spec.fibo.config.configuration.model.GroupType;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ConfigGroupsElement extends ConfigElementAbstract {

  private String name;
  private Set<ConfigStringElement> elements;
  private GroupType groupType;
  
  public ConfigGroupsElement() {
    elements = new LinkedHashSet<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<ConfigStringElement> getElements() {
    return elements;
  }

  public void setElements(Set<ConfigStringElement> elements) {
    this.elements = elements;
  }


  public ConfigGroupsElement(ConfigElementType type) {
    super(type);
  }

  public void addElement(ConfigStringElement el) {
    if (elements == null) {
      elements = new LinkedHashSet<>();
    }
    elements.add(el);
  }

  public boolean contains(String val) {
    if (elements == null) {
      return false;
    }
    //return elements.contains(new ConfigStringElement(val));
    /*return elements.stream()
        .map(ConfigStringElement::toString)
        .filter(val::equals)
        .findFirst()
        .isPresent();*/

    for (ConfigStringElement element : elements) {
      if (element.toString().equals(val)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "ConfigGroupsElement{" + "name=" + name + ", elements=" + elements + ", groupType=" + groupType + '}';
  }

  public GroupType getGroupType() {
    return groupType;
  }

  public void setGroupType(GroupType groupType) {
    this.groupType = groupType;
    addElement(new ConfigStringElement(groupType.name()));
  }

}
