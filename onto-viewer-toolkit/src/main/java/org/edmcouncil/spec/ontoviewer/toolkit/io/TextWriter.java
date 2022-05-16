package org.edmcouncil.spec.ontoviewer.toolkit.io;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextWriter.class);

  public void write(Path outputPath, boolean consistencyResult) {
    var result = Boolean.toString(consistencyResult);
    try {
      Files.write(
          outputPath,
          result.getBytes(StandardCharsets.UTF_8),
          WRITE, CREATE, TRUNCATE_EXISTING);
    } catch (IOException ex) {
      LOGGER.error(
          String.format("Exception thrown while writing consistency result '%s' to the output path '%s'. Details: %s",
              consistencyResult,
              outputPath,
              ex.getMessage()
          ), ex);
    }
  }
}

