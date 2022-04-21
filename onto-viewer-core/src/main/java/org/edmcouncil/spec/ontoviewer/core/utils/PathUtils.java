package org.edmcouncil.spec.ontoviewer.core.utils;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class PathUtils {

  public static Path getPathWithoutFilePrefix(String pathString) {
    Path resultPath;
    if (Pattern.compile("^file:\\/.:").matcher(pathString).find()) {
      resultPath = Path.of(pathString.replaceAll("^file:\\/", ""));
    } else {
      resultPath = Path.of(pathString.replaceAll("^file:", ""));
    }
    return resultPath;
  }
}