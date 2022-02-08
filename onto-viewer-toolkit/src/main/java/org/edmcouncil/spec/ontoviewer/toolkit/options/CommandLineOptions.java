package org.edmcouncil.spec.ontoviewer.toolkit.options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandLineOptions {

  private final Map<OptionDefinition, List<String>> options = new HashMap<>();

  public void setOption(OptionDefinition optionDefinition, String optionValue) {
    options.put(optionDefinition, List.of(optionValue));
  }

  public void setOption(OptionDefinition optionDefinition, String[] optionValues) {
    options.put(optionDefinition, List.of(optionValues));
  }

  public Optional<String> getOption(OptionDefinition optionDefinition) {
    var values = options.get(optionDefinition);
    if (values != null && !values.isEmpty()) {
      return Optional.of(values.get(0));
    }
    return Optional.empty();
  }

  public List<String> getOptions(OptionDefinition openDefinition) {
    return options.get(openDefinition);
  }
}
