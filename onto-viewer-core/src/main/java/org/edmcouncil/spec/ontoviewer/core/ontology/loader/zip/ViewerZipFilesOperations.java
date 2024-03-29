package org.edmcouncil.spec.ontoviewer.core.ontology.loader.zip;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michal Daniel(michal.daniel@makolab.com)
 */
public class ViewerZipFilesOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerZipFilesOperations.class);

  @SuppressWarnings("null")
  public Set<MissingImport> prepareZipToLoad(ConfigurationData config,
      FileSystemService fileSystemService) {

    Set<MissingImport> missingImports = new LinkedHashSet<>();
    List<String> zipUrls = config.getOntologiesConfig().getZipUrls();
    List<String> downloadDirectories = config.getOntologiesConfig().getDownloadDirectory();
    LOGGER.trace("Ontology ZIP URLS : {}", zipUrls);
    LOGGER.trace("Ontology Download Dir : {}", downloadDirectories);
    String downloadDirectory = downloadDirectories.stream().findFirst().orElseGet(() -> { LOGGER.warn("use default 'download' directory"); return "download"; });

    //checking if zip files can be downloaded at all
    if (!zipUrls.isEmpty()) {
      try {
        var downloadDirectoryPath = fileSystemService.getPathToFile(downloadDirectory);
        if (!downloadDirectoryPath.toFile().exists()) {
          fileSystemService.createDir(downloadDirectoryPath);
        }
      } catch (IOException ex) {
        LOGGER.info("The directory has not been created. Exception: {}", ex.toString());
      }

      Path downloadDir = null;
      try {
        downloadDir = fileSystemService.getPathToFile(downloadDirectory);
      } catch (IOException e) {
        LOGGER.error(e.toString());
        MissingImport missingImport = new MissingImport();
        missingImport.setIri(downloadDirectory);
        missingImport.setCause(e.toString());
        missingImports.add(missingImport);
      }

      if (downloadDir != null && downloadDir.toFile().exists() && downloadDir.toFile()
          .isDirectory()) {
        try {
          clearDirectory(downloadDir.toFile());
        } catch (IOException ex) {
          LOGGER.error(ex.toString());
        }

        for (String fileUrl : zipUrls) {
          File downloadedFile = null;
          try {
            downloadedFile = downloadFileFromUrlToDir(fileUrl, downloadDir);
          } catch (IOException ex) {
            LOGGER.error(ex.toString());
            MissingImport missingImport = new MissingImport();
            missingImport.setIri(fileUrl);
            missingImport.setCause(ex.toString());
            missingImports.add(missingImport);
          }
          if (downloadedFile != null) {
            try {
              unzipFolder(downloadedFile.toPath(), downloadDir);
            } catch (ZipException ex) {
              LOGGER.error(ex.toString());
              MissingImport missingImport = new MissingImport();
              missingImport.setIri(downloadedFile.toPath().toString());
              missingImport.setCause(ex.toString());
              missingImports.add(missingImport);
            }
            if (!downloadedFile.delete()) {
              LOGGER.error("Can not delete file: {}", downloadedFile.toPath().toString());
            }
          }
        }

      } else {
        MissingImport missingImport = new MissingImport();
        missingImport.setIri(downloadDir.toString());
        missingImport.setCause("Download directory not exists or it's not 'directory'");
        missingImports.add(missingImport);
      }
    }
    return missingImports;
  }

  private File downloadFileFromUrlToDir(String fileUrl, Path downloadDir)
      throws MalformedURLException, IOException {
    File outputFile = downloadDir
        .resolve(fileUrl.substring(fileUrl.lastIndexOf("/") + 1))
        .toFile();
    FileUtils.copyURLToFile(new URL(fileUrl), outputFile);
    return outputFile;
  }

  private void unzipFolder(Path source, Path target) throws ZipException {
    new ZipFile(source.toFile())
        .extractAll(target.toString());
  }

  private void clearDirectory(File downloadDir) throws IOException {
    File[] allContents = downloadDir.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        if (file.isDirectory()) {
          FileUtils.deleteDirectory(file);
        }
        file.delete();
      }
    }
  }
}
