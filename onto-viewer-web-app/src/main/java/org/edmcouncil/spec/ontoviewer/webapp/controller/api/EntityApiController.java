package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.service.EntityService;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EntityApiController extends BaseController {

  private final EntityService entityService;

  public EntityApiController(UpdateBlocker updateBlocker, EntityService entityService) {
    super(updateBlocker);
    this.entityService = entityService;
  }

  @GetMapping("entity")
  public ResponseEntity<SearcherResult<OwlDetails>> getEntityByIri(@RequestParam("iri") String iri)
      throws NotFoundElementInOntologyException {
    checkIfApplicationIsReady();

    SearcherResult<OwlDetails> entityResult = entityService.getEntityDetailsByIri(iri.trim());
    return ResponseEntity.ok(entityResult);
  }
}