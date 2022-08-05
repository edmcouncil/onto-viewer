package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.OntologySource.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractOntologyLoader {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected static final Set<String> SUPPORTED_EXTENSIONS = Set.of("rdf", "owl", "ttl");

  protected void mappingDirectory(Path ontologiesDirPath, Set<OntologySource> ontologySources,
      String originalLocation) {
    if (ontologiesDirPath == null || Files.notExists(ontologiesDirPath)) {
      logger.warn("Directory with path '{}' doesn't exist.", ontologiesDirPath);
      return;
    }

    try (var pathsStream = Files.walk(ontologiesDirPath, FileVisitOption.FOLLOW_LINKS)) {
      var paths = pathsStream.collect(Collectors.toSet());

      for (Path path : paths) {
        if (path.equals(ontologiesDirPath)) {
          continue;
        }

        if (Files.isRegularFile(path)) {
          String fileExtension = FilenameUtils.getExtension(path.toString());
          if (SUPPORTED_EXTENSIONS.contains(fileExtension)) {
            ontologySources.add(new OntologySource(path.toString(), originalLocation, SourceType.FILE));
          } else {
            logger.debug("File with extension '{}' is not supported. Supported extensions: {}", fileExtension,
                SUPPORTED_EXTENSIONS);
          }
        } else if (Files.isDirectory(path)) {
          mappingDirectory(path, ontologySources, originalLocation);
        }
      }
    } catch (IOException ex) {
      logger.error(String.format("Exception thrown while iterating through directory '%s'.", ontologiesDirPath));
    }
  }
}