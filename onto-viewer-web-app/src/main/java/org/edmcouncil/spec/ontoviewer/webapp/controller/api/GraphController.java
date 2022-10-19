package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.edmcouncil.spec.ontoviewer.webapp.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphController extends BaseController {

  private final GraphService graphService;

  public GraphController(UpdateBlocker updateBlocker, GraphService graphService) {
    super(updateBlocker);
    this.graphService = graphService;
  }

  @GetMapping
  public ResponseEntity getEntityByIri(
      @RequestParam("iri") String iri, 
      @RequestParam(required = false, defaultValue = "0") int lastId,
      @RequestParam(required = false, defaultValue = "0") int nodeId)
      throws NotFoundElementInOntologyException, OntoViewerException {
    checkIfApplicationIsReady();

    var entityResult = graphService.handleGraph(iri.trim(), nodeId, lastId);
    return ResponseEntity.ok(entityResult);
  }
}
