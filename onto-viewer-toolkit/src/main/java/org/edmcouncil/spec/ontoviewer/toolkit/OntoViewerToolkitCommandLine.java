package org.edmcouncil.spec.ontoviewer.toolkit;

import java.nio.file.Path;
import java.util.Arrays;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.CommandLineOntologyLoader;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyTableDataExtractor;
import org.edmcouncil.spec.ontoviewer.toolkit.io.CsvWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptions;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptionsHandler;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OntoViewerToolkitCommandLine implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntoViewerToolkitCommandLine.class);

  private final ConfigurationService configurationService;
  private final OntologyManager ontologyManager;
  private final FiboDataHandler fiboDataHandler;
  private final OntologyTableDataExtractor ontologyTableDataExtractor;

  public OntoViewerToolkitCommandLine(
      ConfigurationService configurationService,
      OntologyManager ontologyManager,
      FiboDataHandler fiboDataHandler,
      OntologyTableDataExtractor ontologyTableDataExtractor) {
    this.configurationService = configurationService;
    this.ontologyManager = ontologyManager;
    this.fiboDataHandler = fiboDataHandler;
    this.ontologyTableDataExtractor = ontologyTableDataExtractor;
  }

  @Override
  public void run(String... args) throws Exception {
    LOGGER.debug("Raw command line arguments: {}", Arrays.toString(args));
    var commandLineOptionsHandler = new CommandLineOptionsHandler();
    var commandLineOptions = commandLineOptionsHandler.parseArgs(args);
    populateConfiguration(commandLineOptions);

    loadOntology();

    var ontologyTableData = ontologyTableDataExtractor.extractEntityData();

    var outputPath = Path.of(commandLineOptions.getOption(OptionDefinition.OUTPUT));
    new CsvWriter().write(outputPath, ontologyTableData);
  }

  private void populateConfiguration(CommandLineOptions commandLineOptions) {
    var configuration = configurationService.getCoreConfiguration();
    configuration.addConfigElement(
        ConfigKeys.ONTOLOGY_PATH,
        new StringItem(commandLineOptions.getOption(OptionDefinition.INPUT)));
    configuration.addConfigElement(
        OptionDefinition.FILTER_PATTERN.argName(),
        new StringItem(commandLineOptions.getOption(OptionDefinition.FILTER_PATTERN)));

  }

  private void loadOntology() throws OntoViewerToolkitException {
    try {
      var ontologyLoader = new CommandLineOntologyLoader(
          configurationService.getCoreConfiguration());
      var loadedOntology = ontologyLoader.load();
      LOGGER.debug("Loaded ontology contains {} axioms.", loadedOntology.getAxiomCount());
      ontologyManager.updateOntology(loadedOntology);
      fiboDataHandler.populateOntologyResources(loadedOntology);
    } catch (Exception ex) {
      var message = String.format(
          "Exception occurred while loading ontology. Details: %s",
          ex.getMessage());
      throw new OntoViewerToolkitException(message, ex);
    }
  }
}