package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
class DirectoryOntologyLoader implements OntologyLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryOntologyLoader.class);

    private FileSystemManager fsm;

    DirectoryOntologyLoader(FileSystemManager fsm) {
        this.fsm = fsm;
    }

    @Override
    public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {
        Path dirPath = fsm.getPathToOntologyFile(path);

        LOG.debug("Path to directory is '{}'", dirPath);

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology onto = manager.createOntology();
        onto = openOntologiesFromDirectory(dirPath.toFile(), manager, onto);
        manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(onto)));
        Stream<OWLOntology> imports = manager.imports(onto);
        LOG.debug("create ontology");
        onto = manager.createOntology(IRI.create(""), imports, false);
        return onto;
    }

    /**
     * This method is used to open all Ontologies from directory
     *
     * @param ontologiesDir OntologiesDir is a loaded ontology file.
     * @param manager Manager loading and acessing ontologies.
     * @return set of ontology.
     */
    private OWLOntology openOntologiesFromDirectory(File ontologiesDir, OWLOntologyManager manager, OWLOntology onto) throws OWLOntologyCreationException {

        for (File file : ontologiesDir.listFiles()) {
            LOG.debug("Load ontology file : {}", file.getName());

            if (file.isFile()) {
                if (!getFileExtension(file).equalsIgnoreCase("xml")) {
                    manager.loadOntologyFromOntologyDocument(file);
                    OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
                            .getOWLImportsDeclaration(IRI.create(file));
                    manager.applyChange(new AddImport(onto, importDeclaration));
                    manager.makeLoadImportRequest(importDeclaration);
                }
            } else if (file.isDirectory()) {
                openOntologiesFromDirectory(file, manager, onto);

            }

        }
        return onto;
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

}
