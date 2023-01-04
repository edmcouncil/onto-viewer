package org.edmcouncil.spec.ontoviewer.toolkit.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.edmcouncil.spec.ontoviewer.toolkit.model.ConsistencyCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextWriter.class);

  public void write(Path outputPath, ConsistencyCheckResult consistencyCheckResult) {
    var objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(outputPath.toFile(), consistencyCheckResult);
    } catch (IOException ex) {
      LOGGER.error(
          String.format("Exception thrown while writing consistency result '%s' to the output path '%s'. Details: %s",
              consistencyCheckResult,
              outputPath,
              ex.getMessage()
          ), ex);
    }
  }
}

