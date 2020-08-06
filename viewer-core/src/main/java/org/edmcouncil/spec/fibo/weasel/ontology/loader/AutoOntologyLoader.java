package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class AutoOntologyLoader {
  
  private FileSystemManager fsm;
  private ViewerCoreConfiguration viewerCoreConfiguration;
  


  public AutoOntologyLoader(FileSystemManager fsm, ViewerCoreConfiguration viewerCoreConfiguration) {
    this.fsm = fsm;
    this.viewerCoreConfiguration = viewerCoreConfiguration;
  }
  
  public OWLOntology load(){
    
  }
  
  

  private OWLOntology loadOntologiesFromIRIs(Set<IRI> iris, OWLOntology onto, OWLOntologyManager manager) throws OWLOntologyCreationException {

    for (IRI iri : iris) {
      OWLOntology o = manager.loadOntology(iri);
      OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
              .getOWLImportsDeclaration(iri);
      manager.applyChange(new AddImport(onto, importDeclaration));
      manager.makeLoadImportRequest(importDeclaration);

    }

    return onto;
  }
}
