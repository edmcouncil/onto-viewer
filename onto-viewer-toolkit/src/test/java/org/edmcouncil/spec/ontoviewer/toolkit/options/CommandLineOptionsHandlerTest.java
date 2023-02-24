package org.edmcouncil.spec.ontoviewer.toolkit.options;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CommandLineOptionsHandlerTest {

  private CommandLineOptionsHandler commandLineOptionsHandler;

  @BeforeEach
  void setUp() {
    this.commandLineOptionsHandler = new CommandLineOptionsHandler();
  }

  @ParameterizedTest
  @MethodSource("prepareTestParameters")
  void test(TestParameter testParameter) throws OntoViewerToolkitException {
    var expectedResult = testParameter.getExpectedResult();

    var actualResult = commandLineOptionsHandler.parseArgs(testParameter.getArgs());

    assertEquals(expectedResult.getOption(OptionDefinition.DATA), actualResult.getOption(OptionDefinition.DATA));
    assertEquals(expectedResult.getOption(OptionDefinition.OUTPUT), actualResult.getOption(OptionDefinition.OUTPUT));
    assertEquals(expectedResult.getOption(OptionDefinition.FILTER_PATTERN),
        actualResult.getOption(OptionDefinition.FILTER_PATTERN));
    assertEquals(expectedResult.getOption(OptionDefinition.GOAL), actualResult.getOption(OptionDefinition.GOAL));
    assertEquals(expectedResult.getOption(OptionDefinition.MATURITY_LEVEL),
        actualResult.getOption(OptionDefinition.MATURITY_LEVEL));
    assertEquals(expectedResult.getOption(OptionDefinition.ONTOLOGY_IRI),
        actualResult.getOption(OptionDefinition.ONTOLOGY_IRI));
    assertEquals(expectedResult.getOption(OptionDefinition.ONTOLOGY_MAPPING),
        actualResult.getOption(OptionDefinition.ONTOLOGY_MAPPING));
    assertEquals(expectedResult.getOption(OptionDefinition.ONTOLOGY_VERSION_IRI),
        actualResult.getOption(OptionDefinition.ONTOLOGY_VERSION_IRI));
    assertEquals(expectedResult.getOption(OptionDefinition.VERSION), actualResult.getOption(OptionDefinition.VERSION));
    assertEquals(expectedResult.getOption(OptionDefinition.EXTRACT_DATA_COLUMN),
        actualResult.getOption(OptionDefinition.EXTRACT_DATA_COLUMN));
  }

  private static List<TestParameter> prepareTestParameters() {
    var testParameters = new ArrayList<TestParameter>();

    // data -- single
    var commandLineOptions1 = getCommandLineOptions(
        Map.of(OptionDefinition.DATA, "foo/bar/baz.rdf"));
    testParameters.add(
        new TestParameter(
            new String[] {"--data", "foo/bar/baz.rdf"},
            commandLineOptions1));

    // data -- multiple
    var commandLineOptions2 = getCommandLineOptions(
        Map.of(OptionDefinition.DATA, new String[] {"foo/bar/baz.rdf", "foo/bar/baz2.rdf"}));
    testParameters.add(
        new TestParameter(
            new String[] {"--data", "foo/bar/baz.rdf", "--data", "foo/bar/baz2.rdf"},
            commandLineOptions2));

    // output
    var commandLineOptions3 = getCommandLineOptions(
        Map.of(OptionDefinition.OUTPUT, "/home/user/test.rdf"));
    testParameters.add(
        new TestParameter(
            new String[] {"--output", "/home/user/test.rdf"},
            commandLineOptions3));

    // filter pattern
    var commandLineOptions4 = getCommandLineOptions(
        Map.of(OptionDefinition.FILTER_PATTERN, "my-filter-pattern"));
    testParameters.add(
        new TestParameter(
            new String[] {"--filter-pattern", "my-filter-pattern"},
            commandLineOptions4));

    // filter pattern
    var commandLineOptions5 = getCommandLineOptions(
        Map.of(OptionDefinition.GOAL, "extract-data"));
    testParameters.add(
        new TestParameter(
            new String[] {"--goal", "extract-data"},
            commandLineOptions5));

    // maturity-level
    var commandLineOptions6 = getCommandLineOptions(
        Map.of(OptionDefinition.MATURITY_LEVEL, "http://example.com=label"));
    testParameters.add(
        new TestParameter(
            new String[] {"--maturity-level", "http://example.com=label"},
            commandLineOptions6));

    // ontology-iri
    var commandLineOptions7 = getCommandLineOptions(
        Map.of(OptionDefinition.ONTOLOGY_IRI, "http://example.com/ontology"));
    testParameters.add(
        new TestParameter(
            new String[] {"--ontology-iri", "http://example.com/ontology"},
            commandLineOptions7));

    // ontology-mapping
    var commandLineOptions8 = getCommandLineOptions(
        Map.of(OptionDefinition.ONTOLOGY_MAPPING, "foo/bar/catalog-v001.xml"));
    testParameters.add(
        new TestParameter(
            new String[] {"--ontology-mapping", "foo/bar/catalog-v001.xml"},
            commandLineOptions8));

    // version
    var commandLineOptions9 = getCommandLineOptions(
        Map.of(OptionDefinition.VERSION, "<present>"));
    testParameters.add(
        new TestParameter(
            new String[] {"--version"},
            commandLineOptions9));

    // extract-data-column
    var commandLineOptions10 = getCommandLineOptions(
        Map.of(OptionDefinition.EXTRACT_DATA_COLUMN, "synonyms=http://example.com/synonyms"));
    testParameters.add(
        new TestParameter(
            new String[] {"--extract-data-column", "synonyms=http://example.com/synonyms"},
            commandLineOptions10));

    return testParameters;
  }

  private static CommandLineOptions getCommandLineOptions(Map<OptionDefinition, Object> dataMap) {
    var commandLineOptions = new CommandLineOptions();
    commandLineOptions.setOption(OptionDefinition.FILTER_PATTERN, "");
    commandLineOptions.setOption(OptionDefinition.GOAL, "extract-data");

    for (Entry<OptionDefinition, Object> entry : dataMap.entrySet()) {
      if (entry.getValue() instanceof String) {
        var entryValueString = (String) entry.getValue();
        commandLineOptions.setOption(entry.getKey(), entryValueString);
      } else if (entry.getValue() instanceof String[]) {
        var entryValueStringArray = (String[]) entry.getValue();
        commandLineOptions.setOption(entry.getKey(), entryValueStringArray);
      }
    }
    return commandLineOptions;
  }

  private static class TestParameter {

    private final String[] args;
    private final CommandLineOptions expectedResult;

    public TestParameter(String[] args, CommandLineOptions expectedResult) {
      this.args = args;
      this.expectedResult = expectedResult;
    }

    public String[] getArgs() {
      return args;
    }

    public CommandLineOptions getExpectedResult() {
      return expectedResult;
    }
  }
}