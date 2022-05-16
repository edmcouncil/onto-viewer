package org.edmcouncil.spec.ontoviewer.toolkit.io;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.EntityData;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CsvWriter.class);

  public void write(Path outputPath, List<EntityData> data) throws OntoViewerToolkitException {
    LOGGER.debug("Saving {} records to {}...", data.size(), outputPath);

    var records = prepareRecords(data);
    try (var printer = new CSVPrinter(new FileWriter(outputPath.toFile()), CSVFormat.DEFAULT)) {
      printer.printRecord(prepareHeader());
      printer.printRecords(records);
    } catch (IOException ex) {
      throw new OntoViewerToolkitException(
          String.format("Exception occurred while printing data to CSV file. Details: %s", ex.getMessage()), ex);
    }
  }

  private List<String> prepareHeader() {
    return List.of("Term", "Type", "Ontology", "Synonyms", "Definition", "GeneratedDefinition", "Examples",
        "Explanations", "Maturity");
  }

  private List<List<String>> prepareRecords(List<EntityData> data) {
    return data.stream().map(this::prepareRecords).collect(Collectors.toList());
  }

  private List<String> prepareRecords(EntityData entityData) {
    var result = new ArrayList<String>();
    result.add(entityData.getTermLabel());
    result.add(entityData.getTypeLabel());
    result.add(entityData.getOntology());
    result.add(entityData.getSynonyms());
    result.add(entityData.getDefinition());
    result.add(entityData.getGeneratedDefinition());
    result.add(entityData.getExamples());
    result.add(entityData.getExplanations());
    result.add(entityData.getMaturity());
    return result;
  }
}