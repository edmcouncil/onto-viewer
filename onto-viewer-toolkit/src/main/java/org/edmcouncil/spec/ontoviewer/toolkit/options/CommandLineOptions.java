package org.edmcouncil.spec.ontoviewer.toolkit.options;

import java.util.HashMap;
import java.util.Map;

public class CommandLineOptions {

  private final Map<OptionDefinition, String> options = new HashMap<>();

  public void addOption(OptionDefinition optionDefinition, String optionValue) {
    options.put(optionDefinition, optionValue);
  }

  public String getOption(OptionDefinition optionDefinition) {
    return options.get(optionDefinition);
  }
}
