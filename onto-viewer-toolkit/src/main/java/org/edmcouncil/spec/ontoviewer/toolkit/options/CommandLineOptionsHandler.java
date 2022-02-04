package org.edmcouncil.spec.ontoviewer.toolkit.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;

public class CommandLineOptionsHandler {

  public static final String OPTION_PRESENT = "<present>";

  public CommandLineOptions parseArgs(String[] args) throws OntoViewerToolkitException {
    var optionSettings = prepareOptions();

    try {
      var parser = new DefaultParser();
      var parsedOptions = parser.parse(optionSettings, args);

      return mapParsedOptions(parsedOptions);
    } catch (ParseException ex) {
      throw new OntoViewerToolkitException(
          "Problem with command line arguments occurred. Details: " + ex.getMessage(),
          ex);
    }
  }

  private Options prepareOptions() {
    var options = new Options();

    for (OptionDefinition optionDefinition : OptionDefinition.values()) {
      options.addOption(
          Option.builder().argName(optionDefinition.argName())
              .hasArg(optionDefinition.hasArg())
              .longOpt(optionDefinition.argName())
              .desc(optionDefinition.description())
              .required(optionDefinition.isRequired())
              .build());
    }

    return options;
  }

  private CommandLineOptions mapParsedOptions(CommandLine parsedOptions) {
    var commandLineOptions = new CommandLineOptions();

    for (OptionDefinition optionDefinition : OptionDefinition.values()) {
      if (parsedOptions.hasOption(optionDefinition.argName())) {
        var optionValues = parsedOptions.getOptionValues(optionDefinition.argName());
        if (optionValues != null) {
          commandLineOptions.setOption(optionDefinition, optionValues);
        } else {
          commandLineOptions.setOption(optionDefinition, OPTION_PRESENT);
        }
      } else if (optionDefinition.isNotRequired() && optionDefinition.hasDefaultValue()) {
        commandLineOptions.setOption(optionDefinition, optionDefinition.defaultValue());
      }
    }

    return commandLineOptions;
  }
}
