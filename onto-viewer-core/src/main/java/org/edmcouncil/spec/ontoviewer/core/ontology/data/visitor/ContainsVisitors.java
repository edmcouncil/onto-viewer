package org.edmcouncil.spec.ontoviewer.core.ontology.data.visitor;

import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class ContainsVisitors {

  private static final Logger LOG = LoggerFactory.getLogger(OwlDataHandler.class);

  public final OWLObjectVisitorEx<Boolean> isRestrictionVisitor
      = new OWLObjectVisitorEx<Boolean>() {
    @Override
    public Boolean visit(OWLSubClassOfAxiom subClassAxiom) {
      OWLClassExpression superClass = subClassAxiom.getSuperClass();
      ClassExpressionType classExpressionType = superClass.getClassExpressionType();
      return !classExpressionType.equals(ClassExpressionType.OWL_CLASS);
    }
  };

  public final OWLObjectVisitorEx<OWLSubClassOfAxiom> getAxiomElement(IRI rootIri) {

    return new OWLObjectVisitorEx() {
      @Override
      public OWLSubClassOfAxiom visit(OWLSubClassOfAxiom subClassAxiom) {
        {
          return subClassAxiom;
        }
      }
    };
  }

  public final OWLObjectVisitorEx<Boolean> visitorObjectProperty(IRI iri) {

    return new OWLObjectVisitorEx<Boolean>() {

      @Override
      public Boolean visit(OWLObjectPropertyRangeAxiom ax) {
        OWLClassExpression oce = ax.getRange();
        Set<OWLEntity> es = oce.signature().collect(Collectors.toSet());

        for (OWLEntity owlEntity : es) {
          LOG.debug("ContainsVisitors -> owl entity iri {}", owlEntity.getIRI());
          if (owlEntity.getIRI().equals(iri)) {
            LOG.debug("ContainsVisitors -> visitPropertyRangeAxiom {}", owlEntity.getIRI());
            return true;
          }
        }
        return false;
      }

      @Override
      public Boolean visit(OWLObjectPropertyDomainAxiom ax) {
        OWLClassExpression oce = ax.getDomain();
        Set<OWLEntity> es = oce.signature().collect(Collectors.toSet());

        for (OWLEntity owlEntity : es) {
          LOG.debug("ContainsVisitors -> owl entity iri {}", owlEntity.getIRI());
          if (owlEntity.getIRI().equals(iri)) {
            LOG.debug("ContainsVisitors -> visitPropertyDomainAxiom {}", owlEntity.getIRI());
            return true;
          }
        }
        return false;
      }

      @Override
      public Boolean doDefault(Object object) {
        LOG.debug("Unsupported axiomObjectProperty: " + object);
        LOG.debug("Unsupported axiom type ObjectProperty: " + object.getClass().getName());
        return false;
      }

    };

  }

  ;

  public final OWLAxiomVisitorEx<Boolean> visitor(IRI iri) {

    return new OWLAxiomVisitorEx<Boolean>() {

      @Override
      public Boolean visit(OWLSubClassOfAxiom ax) {

        for (OWLEntity owlEntity : ax.signature().collect(Collectors.toList())) {
          if (owlEntity.getIRI().equals(iri)) {
            LOG.debug("ContainsVisitors -> visitSubClassOf {}", owlEntity.getIRI());
            return true;

          }

        }
        return false;

      }

      @Override
      public Boolean visit(OWLObjectPropertyAssertionAxiom ax) {
        for (OWLEntity owlEntity : ax.getProperty().signature().collect(Collectors.toList())) {
          if (owlEntity.getIRI().equals(iri)) {
            LOG.debug("ContainsVisitors -> visitPropertyDomainAxiom {}", owlEntity.getIRI());
            return true;
          }
        }
        return false;
      }

      @Override
      public Boolean doDefault(Object object) {
        LOG.debug("Unsupported axiomObjectProperty: " + object);
        LOG.debug("Unsupported axiom type ObjectProperty: " + object.getClass().getName());
        return false;
      }

    };
  }
}
