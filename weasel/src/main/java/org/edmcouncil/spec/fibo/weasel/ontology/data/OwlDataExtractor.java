package org.edmcouncil.spec.fibo.weasel.ontology.data;

import java.util.stream.Collectors;
import javax.swing.text.html.parser.Entity;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import static org.semanticweb.owlapi.search.Filters.annotations;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlDataExtractor {

  public WeaselOwlType extractAnnotationType(OWLAnnotationAssertionAxiom next) {
    if (next.getValue().isIRI()) {
      return WeaselOwlType.IRI;
    } else if (next.getValue().isLiteral()) {
      String datatype = next.getValue().asLiteral().get().getDatatype().toString();
      //TODO: move this strings to list and use contains
      if (datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
          || datatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
        return WeaselOwlType.STRING;
      } else if (datatype.equals("xsd:anyURI")) {
        return WeaselOwlType.ANY_URI;
      }
    }
    return WeaselOwlType.OTHER;
  }

  /**
   * This method is used to extract annotation type for ontology.
   *
   * @param next
   * @return
   */
  public WeaselOwlType extractAnnotationType(OWLAnnotation next) {
    if (next.getValue().isIRI()) {
      return WeaselOwlType.IRI;
    } else if (next.getValue().isLiteral()) {
      String datatype = next.getValue().asLiteral().get().getDatatype().toString();
      //TODO: move this strings to list and use contains
      if (datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
          || datatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
        return WeaselOwlType.STRING;
      } else if (datatype.equals("xsd:anyURI")) {
        return WeaselOwlType.ANY_URI;
      }
    }
    return WeaselOwlType.OTHER;
  }

  public String extractAnyUriToString(String anyUri) {
    String uriString = anyUri.replaceFirst("\"", "");
    uriString = uriString.substring(0, uriString.length() - 13);
    return uriString;
  }

  public String getLabelFromOntologyByIri(IRI iri, OWLOntology ontology) {

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLDataFactory factory = new OWLDataFactoryImpl();
//   OWLEntity entity = createEntity(ontology,iri );
//    for (OWLOntologyIRIMapper iriMapper : manager.getIRIMappers()) {
//      factory.getRDFSLabel(iri);
//     for (OWLOntologyManager annotation : EntitySearcher.getAnnotations(manager.loadOntology(iri), ontology, factory.getRDFSLabel(iri))) {   
//    }

    OWLClass owlEntity = factory.getOWLEntity(EntityType.CLASS, iri);
    for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlEntity, ontology, factory.getRDFSLabel()).collect(Collectors.toSet())) {
      OWLAnnotationValue owlav = owlEntity.getIRI();
      if (annotation.getValue().isLiteral()) {
        EntitySearcher.getAnnotations(owlEntity, ontology);
        //((OWLiteral)annotation.getValue());
        //owlav.asLiteral().toString();

       String o = owlav.annotationValue().asLiteral().toString(); 
        //LOGGER.debug("iri : {}, label: {}", annotation.getValue(), annotation. );
        //factory.getRDFSLabel().typeIndex();

//        annotation.get
        // EntitySearcher.getAnnotations(iri, ontology);
      }

//      if (iri instanceof OWLLiteral) {
////        owlav.isLiteral();
////        owlEntity.getEntityType().getIRI();
//        EntitySearcher.getAnnotations(owlEntity, ontology);
//        ((OWLLiteral) iri).getLiteral();
//      }
    }
    return null;
  }
}
