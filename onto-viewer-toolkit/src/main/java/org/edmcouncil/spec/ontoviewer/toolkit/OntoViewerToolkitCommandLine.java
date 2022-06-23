package org.edmcouncil.spec.ontoviewer.toolkit;

import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.ONTOLOGY_IRI;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.ONTOLOGY_MAPPING;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.OUTPUT;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.VERSION;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntologyCatalogParser;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.Uri;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.CommandLineOntologyLoader;
import org.edmcouncil.spec.ontoviewer.toolkit.config.ApplicationConfigProperties;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyConsistencyChecker;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyImportsMerger;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyTableDataExtractor;
import org.edmcouncil.spec.ontoviewer.toolkit.io.CsvWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.io.TextWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptions;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptionsHandler;
import org.edmcouncil.spec.ontoviewer.toolkit.options.Goal;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
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

  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final DataHandler dataHandler;
  private final OntologyTableDataExtractor ontologyTableDataExtractor;
  private final OntologyConsistencyChecker ontologyConsistencyChecker;
  private final OntologyImportsMerger ontologyImportsMerger;
  private final StandardEnvironment environment;
  private final ApplicationConfigProperties applicationConfigProperties;
  private final FileSystemManager fileSystemManager;

  public OntoViewerToolkitCommandLine(
      ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager,
      DataHandler dataHandler,
      OntologyTableDataExtractor ontologyTableDataExtractor,
      OntologyConsistencyChecker ontologyConsistencyChecker,
      OntologyImportsMerger ontologyImportsMerger,
      StandardEnvironment environment,
      ApplicationConfigProperties applicationConfigProperties,
      FileSystemManager fileSystemManager) {
    this.applicationConfigurationService = applicationConfigurationService;
    // We don't need the default paths configuration in OV Toolkit
    applicationConfigurationService.getConfigurationData().getOntologiesConfig().getPaths().clear();

    this.ontologyManager = ontologyManager;
    this.dataHandler = dataHandler;
    this.ontologyTableDataExtractor = ontologyTableDataExtractor;
    this.ontologyConsistencyChecker = ontologyConsistencyChecker;
    this.ontologyImportsMerger = ontologyImportsMerger;
    this.environment = environment;
    this.applicationConfigProperties = applicationConfigProperties;
    this.fileSystemManager = fileSystemManager;
  }

  @Override
  public void run(String... args) throws Exception {
    var stopwatch = Stopwatch.createStarted();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Current working directory: {}", System.getProperty("user.dir"));
      logSystemAndSpringProperties();
      LOGGER.debug("Raw command line arguments: {}", Arrays.toString(args));
    }
    var commandLineOptionsHandler = new CommandLineOptionsHandler();
    var commandLineOptions = commandLineOptionsHandler.parseArgs(args);

    var versionOption = commandLineOptions.getOption(VERSION);
    if (versionOption.isPresent()) {
      System.out.println(getApplicationInfo());
      System.exit(0);
    }

    populateConfiguration(commandLineOptions);

    var goal = resolveGoal();
    loadOntology(goal);

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
      case MERGE_IMPORTS:
        var newOntologyIri = commandLineOptions.getOption(ONTOLOGY_IRI);
        if (newOntologyIri.isEmpty()) {
          throw new OntoViewerToolkitRuntimeException("'ontology-iri' for 'merge-imports' goal should be provided");
        }
        var mergedOntology = ontologyImportsMerger.mergeImportOntologies(newOntologyIri.get());
        var owlOntologyManager = OWLManager.createOWLOntologyManager();
        var outputOption = commandLineOptions.getOption(OUTPUT);
        if (outputOption.isPresent()) {
          owlOntologyManager.saveOntology(
              mergedOntology,
              new RDFXMLDocumentFormat(),
              IRI.create(new File(outputOption.get())));
        } else {
          throw new OntoViewerToolkitRuntimeException("Error: 'output' argument is not present.");
        }

        break;
      default:
        var message = String.format("Goal '%s' not recognized. Should not happen.", goal.getName());
        LOGGER.error(message);
        System.exit(1);
    }

    stopwatch.stop();
    LOGGER.debug("Application finished task in {} seconds.", stopwatch.elapsed(TimeUnit.SECONDS));
  }

  private void populateConfiguration(CommandLineOptions commandLineOptions) {
    var configurationData = applicationConfigurationService.getConfigurationData();

    var ontologyMappingOption = commandLineOptions.getOption(ONTOLOGY_MAPPING);
    if (ontologyMappingOption.isPresent()) {
      var ontologyMappingPath = ontologyMappingOption.get();
      if (!ontologyMappingPath.isBlank()) {
        try {
          var catalog = new OntologyCatalogParser().readOntologyMapping(ontologyMappingPath);
          var ontologyMappingParentPath = Path.of(ontologyMappingPath).getParent();

          var mappings = new HashMap<String, String>();
          for (Uri mapping : catalog.getUri()) {
            var ontologyPath = Path.of(mapping.getUri());
            if (!ontologyPath.isAbsolute()) {
              ontologyPath = ontologyMappingParentPath.resolve(ontologyPath).normalize();
            }
            if (Files.exists(ontologyPath)) {
              mappings.put(mapping.getName(), ontologyPath.toString());
            } else {
              LOGGER.warn("File ('{}') that should map to an ontology ('{}') doesn't exist.",
                  ontologyPath, mapping.getName());
            }
          }

          configurationData.getOntologiesConfig().getOntologyMappings().putAll(mappings);
        } catch (OntoViewerException ex) {
          var message = String.format("Error while handling ontology mapping from path '%s'. "
              + "Details: %s", ontologyMappingPath, ex.getMessage());
          LOGGER.warn(message, ex);
        }
      }
    }

    var ontologyPathOptions = commandLineOptions.getOptions(OptionDefinition.DATA);
    if (ontologyPathOptions != null) {
      configurationData.getOntologiesConfig().getPaths().addAll(ontologyPathOptions);
    }
    LOGGER.debug("Using ontology paths: {}", configurationData.getOntologiesConfig().getPaths());

    var filterPattern = commandLineOptions.getOption(OptionDefinition.FILTER_PATTERN).orElse("");
    configurationData.getToolkitConfig().setFilterPattern(filterPattern);

    var goal = commandLineOptions.getOption(OptionDefinition.GOAL).or(() -> {
      LOGGER.error("Unable to detect correct goal.");
      System.exit(1);
      return Optional.empty();
    });
    configurationData.getToolkitConfig().setGoal(goal.get());
  }

  private Goal resolveGoal() {
    var goal = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .getGoal();
    return Goal.byName(goal);
  }

  private void loadOntology(Goal goal) throws OntoViewerToolkitException {
    try {
      var ontologyLoader = new CommandLineOntologyLoader(
          applicationConfigurationService.getConfigurationData(),
          fileSystemManager);
      var loadedOntology = ontologyLoader.load();
      LOGGER.debug("Loaded ontology contains {} axioms.",
          loadedOntology.getAxiomCount(Imports.INCLUDED));

      ontologyManager.updateOntology(loadedOntology);
      if (shouldPopulateOntologyResources(goal)) {
        dataHandler.populateOntologyResources(loadedOntology);
      }
    } catch (Exception ex) {
      var message = String.format(
          "Exception occurred while loading ontology. Details: %s",
          ex.getMessage());
      throw new OntoViewerToolkitException(message, ex);
    }
  }

  private boolean shouldPopulateOntologyResources(Goal goal) {
    return goal != Goal.MERGE_IMPORTS;
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

  private String getApplicationInfo() {
    var applicationName = applicationConfigProperties.getApplicationName();
    var applicationVersion = applicationConfigProperties.getApplicationVersion();
    var commitId = applicationConfigProperties.getCommitId();
    return String.format("%s %s (%s)", applicationName, applicationVersion, commitId);
  }
}