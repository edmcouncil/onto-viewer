package org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener;

import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.MissingImportEvent;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
@Component
public class MissingImportListenerImpl implements MissingImportListener {

  private final Set<MissingImport> missingImports = new HashSet<>();

  @Override
  public void importMissing(MissingImportEvent missingImportEvent) {
    MissingImport missingImport = new MissingImport();
    missingImport.setIri(missingImportEvent.getImportedOntologyURI().getIRIString());
    missingImport.setCause(missingImportEvent.getCreationException().getMessage());
    missingImports.add(missingImport);
  }

  public Set<MissingImport> getNotImportUri() {
    return missingImports;
  }
  
  public void addAll(Set<MissingImport> missingImports){
    this.missingImports.addAll(missingImports);
  }
}
