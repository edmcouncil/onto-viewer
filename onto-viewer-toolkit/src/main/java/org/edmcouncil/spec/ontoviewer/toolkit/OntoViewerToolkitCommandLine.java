package org.edmcouncil.spec.ontoviewer.toolkit;

import com.google.common.base.Stopwatch;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.CommandLineOntologyLoader;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyConsistencyChecker;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyTableDataExtractor;
import org.edmcouncil.spec.ontoviewer.toolkit.io.CsvWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.io.TextWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptions;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptionsHandler;
import org.edmcouncil.spec.ontoviewer.toolkit.options.Goal;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

@Component
public class OntoViewerToolkitCommandLine implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntoViewerToolkitCommandLine.class);

  private final ConfigurationService configurationService;
  private final OntologyManager ontologyManager;
  private final FiboDataHandler fiboDataHandler;
  private final OntologyTableDataExtractor ontologyTableDataExtractor;
  private final OntologyConsistencyChecker ontologyConsistencyChecker;
  private final StandardEnvironment environment;

  public OntoViewerToolkitCommandLine(
      ConfigurationService configurationService,
      OntologyManager ontologyManager,
      FiboDataHandler fiboDataHandler,
      OntologyTableDataExtractor ontologyTableDataExtractor,
      OntologyConsistencyChecker ontologyConsistencyChecker,
      StandardEnvironment environment) {
    this.configurationService = configurationService;
    this.ontologyManager = ontologyManager;
    this.fiboDataHandler = fiboDataHandler;
    this.ontologyTableDataExtractor = ontologyTableDataExtractor;
    this.ontologyConsistencyChecker = ontologyConsistencyChecker;
    this.environment = environment;
  }

  @Override
  public void run(String... args) throws Exception {
    var stopwatch = Stopwatch.createStarted();

    if (LOGGER.isDebugEnabled()) {
      logSystemAndSpringProperties();
      LOGGER.debug("Raw command line arguments: {}", Arrays.toString(args));
    }
    var commandLineOptionsHandler = new CommandLineOptionsHandler();
    var commandLineOptions = commandLineOptionsHandler.parseArgs(args);
    populateConfiguration(commandLineOptions);

    var goal = resolveGoal();

    loadOntology();

    LOGGER.info("Running goal '{}'...", goal.getName());

    switch (goal) {
      case CONSISTENCY_CHECK: {
        var consistencyResult = ontologyConsistencyChecker.checkOntologyConsistency();

        var optionOutputPath = commandLineOptions.getOption(OptionDefinition.OUTPUT)
            .orElseThrow(() ->
                new OntoViewerToolkitRuntimeException("There is no option for output path set."));
        var outputPath = Path.of(optionOutputPath);
        new TextWriter().write(outputPath, consistencyResult);

        break;
      }
      case EXTRACT_DATA: {
        var ontologyTableData = ontologyTableDataExtractor.extractEntityData();

        var optionOutputPath = commandLineOptions.getOption(OptionDefinition.OUTPUT)
            .orElseThrow(() ->
                new OntoViewerToolkitRuntimeException("There is no option for output path set."));
        var outputPath = Path.of(optionOutputPath);
        new CsvWriter().write(outputPath, ontologyTableData);

        break;
      }
      default:
        var message = String.format("Goal '%s' not recognized. Should not happen.", goal.getName());
        LOGGER.error(message);
        System.exit(1);
    }

    stopwatch.stop();
    LOGGER.debug("Application finished task in {} seconds.", stopwatch.elapsed(TimeUnit.SECONDS));
  }

  private void populateConfiguration(CommandLineOptions commandLineOptions) {
    var configuration = configurationService.getCoreConfiguration();

    for (String ontologyPath : commandLineOptions.getOptions(OptionDefinition.DATA)) {
      configuration.addConfigElement(
          ConfigKeys.ONTOLOGY_PATH,
          new StringItem(ontologyPath));
    }
    LOGGER.debug("Using ontology paths: {}", configuration.getOntologyLocation().values());

    var filterPattern = commandLineOptions.getOption(OptionDefinition.FILTER_PATTERN).orElse("");
    configuration.addConfigElement(
        OptionDefinition.FILTER_PATTERN.argName(),
        new StringItem(filterPattern));

    var goal = commandLineOptions.getOption(OptionDefinition.GOAL).or(() -> {
      LOGGER.error("Unable to detect correct goal.");
      System.exit(1);
      return Optional.empty();
    });
    configuration.addConfigElement(
        OptionDefinition.GOAL.argName(),
        new StringItem(goal.get()));
  }

  private Goal resolveGoal() {
    var goal = configurationService.getCoreConfiguration()
        .getSingleStringValue(OptionDefinition.GOAL.argName())
        .orElseThrow(IllegalArgumentException::new);
    return Goal.byName(goal);
  }

  private void loadOntology() throws OntoViewerToolkitException {
    try {
      var ontologyLoader = new CommandLineOntologyLoader(
          configurationService.getCoreConfiguration());
      var loadedOntology = ontologyLoader.load();
      LOGGER.debug("Loaded ontology contains {} axioms.",
          loadedOntology.getAxiomCount(Imports.INCLUDED));

      ontologyManager.updateOntology(loadedOntology);
      fiboDataHandler.populateOntologyResources(loadedOntology);
    } catch (Exception ex) {
      var message = String.format(
          "Exception occurred while loading ontology. Details: %s",
          ex.getMessage());
      throw new OntoViewerToolkitException(message, ex);
    }
  }

  private void logSystemAndSpringProperties() {
    environment.getPropertySources().forEach(propertySource -> {
      if (propertySource instanceof MapPropertySource) {
        var mapPropertySource = (MapPropertySource) propertySource;
        var stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapPropertySource.getSource().entrySet()) {
          stringBuilder.append("'").append(entry.getKey()).append("' = '").append(entry.getValue())
              .append("', ");
        }
        LOGGER.debug("Property source '{}' content: {}", propertySource.getName(), stringBuilder);
      }
    });
  }
}