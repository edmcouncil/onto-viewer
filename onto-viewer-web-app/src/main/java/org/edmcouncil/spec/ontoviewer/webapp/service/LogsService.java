package org.edmcouncil.spec.ontoviewer.webapp.service;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.validator.GenericValidator;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.webapp.controller.LogsApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogsService {

  private static final Logger LOG = LoggerFactory.getLogger(LogsApiController.class);
  private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";

  private final FileSystemManager fileSystemManager;
  private final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

  public LogsService(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  public List<String> getLogs(String date) throws FileNotFoundException, IOException {
    Path logsDirectoryPath = fileSystemManager.getViewerHomeDir().resolve("logs");
    Path logFilePath = null;

    Date localdate = new Date();
    String dateNow = DATE_FORMAT.format(localdate);

    if (!GenericValidator.isDate(date, DATE_FORMAT_STRING, true)) {
      LOG.info("Date is invalid.");
    }
    List<String> result = new LinkedList<>();
    String line = "";

    if (date == null || date.isEmpty() || dateNow.equals(date)) {
      logFilePath = logsDirectoryPath.resolve("viewer-logs.log");
      ReversedLinesFileReader reader = new ReversedLinesFileReader(logFilePath.toFile(), StandardCharsets.UTF_8);
      while ((line = reader.readLine()) != null) {
        result.add(line);
      }
    } else {
      for (File logFile : getListFiles(logsDirectoryPath.resolve("archived").toFile(), date)) {
        ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, StandardCharsets.UTF_8);
        while ((line = reader.readLine()) != null) {
          result.add(line);
        }
      }
    }
    List<String> reversedResult = Lists.reverse(result);
    return reversedResult;
  }

  public List<File> getListFiles(File dir, String date) {

    List<File> result = new LinkedList<>();
    for (File file : dir.listFiles()) {
      if (!file.isDirectory() && file.getName().contains(date)) {
        result.add(file);
      }
    }
    return result;
  }
}
