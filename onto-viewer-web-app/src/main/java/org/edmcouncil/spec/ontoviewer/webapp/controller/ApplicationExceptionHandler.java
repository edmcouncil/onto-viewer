package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.core.exception.ApplicationNotInitializedException;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.exception.RequestHandlingException;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {

  @ExceptionHandler(value = ApplicationNotInitializedException.class)
  ResponseEntity<ErrorResponse> handleApplicationNotInitialized() {
    var errorResponse = new ErrorResponse("Application is not initialized yet.", null);
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(value = RequestHandlingException.class)
  ResponseEntity<ErrorResponse> handleRequestHandlingException(RuntimeException exception) {
    var errorResponse = new ErrorResponse(
        "Error occurred while handling request.",
        exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(value = IllegalArgumentException.class)
  ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
    var errorResponse = new ErrorResponse(
        "Incorrect argument was sent.",
        exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = NotFoundElementInOntologyException.class)
  ResponseEntity<ErrorResponse> handleNotFoundElementInOntology(NotFoundElementInOntologyException exception) {
    var errorResponse = new ErrorResponse(
        "Element not found.",
        exception.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }
}
