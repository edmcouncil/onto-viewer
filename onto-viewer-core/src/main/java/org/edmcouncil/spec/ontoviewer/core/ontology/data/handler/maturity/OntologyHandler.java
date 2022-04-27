package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import static org.edmcouncil.spec.ontoviewer.core.FiboVocabulary.HAS_MATURITY_LEVEL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Service
public class OntologyHandler {
  private static final Logger LOG = LoggerFactory.getLogger(OntologyHandler.class);
    

    private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
    private static final Pattern ONTOLOGY_IRI_PATTERN
            = Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
    private static final String PROD = "prod";
    private static final String PROD_DEV_MIXED = "prod_and_dev_mixed";
    private static final String DEV  = "dev";
    
    private static final String PROD_ICON  = "prod";
    private static final String PROD_DEV_MIXED_ICON   = "prod_and_dev_mixed";
    private static final String DEV_ICON  = "dev";
    
    private final OntologyManager ontologyManager;
    private final LabelProvider labelProvider;
    private final AnnotationsDataHandler annotationsDataHandler;

    // Cache for ontologies' maturity level
    private Map<IRI, OntoMaturityLevel> maturityLevels = new ConcurrentHashMap<>();
    private List<FiboModule> modules = Collections.emptyList();

    
    public OntologyHandler(OntologyManager ontologyManager,
            LabelProvider labelProvider, AnnotationsDataHandler annotationsDataHandler) {
        this.ontologyManager = ontologyManager;
        this.labelProvider = labelProvider;
       this.annotationsDataHandler = annotationsDataHandler;
    }

    /**
     * Find the ontology containing the resources with given iri and extract their level of
     * maturity. When don't find resource in ontologies or ontology doesn't have maturity level
     * method return empty fibo maturity level.
     *
     * @param entityIri IRI of element
     * @return extracted fibo maturity level
     */
    public OntoMaturityLevel getMaturityLevelForElement(String entityIri) {
        String ontologyIri = getOntologyIri(entityIri);
        if (ontologyIri != null) {
            OntoMaturityLevel ontoMaturityLevel = getMaturityLevelForOntology(IRI.create(ontologyIri));
            if (ontoMaturityLevel.getIri().isEmpty() && ontoMaturityLevel.getLabel().isEmpty()) {
                ontoMaturityLevel = generateMaturityLevelForModules(entityIri);
            }
            return ontoMaturityLevel;
        }
        return MaturityLevelFactory.empty();
    }

    public void setModulesTree(List<FiboModule> modules) {
        this.modules = modules;
    }

    public OntoMaturityLevel getMaturityLevelForOntology(IRI ontologyIri) {
        if (!maturityLevels.containsKey(ontologyIri)) {

            var ontologies = ontologyManager.getOntologyWithImports().collect(Collectors.toSet());

            for (OWLOntology ontology : ontologies) {
                var maturityLevelOptional = getMaturityLevelForParticularOntology(ontology, ontologyIri);
                if (maturityLevelOptional.isPresent()) {
                    maturityLevels.put(ontologyIri, maturityLevelOptional.get());
                    break;
                }
            }
        }

        return maturityLevels.getOrDefault(ontologyIri, MaturityLevelFactory.empty());
    }

    private Optional<OntoMaturityLevel> getMaturityLevelForParticularOntology(
            OWLOntology ontology,
            IRI ontologyIri) {
        var currentOntologyIri = ontology.getOntologyID().getOntologyIRI();
        if (currentOntologyIri.isPresent() && currentOntologyIri.get().equals(ontologyIri)) {
            for (OWLAnnotation annotation : ontology.annotationsAsList()) {
                var annotationValue = annotation.annotationValue();

                if (annotation.getProperty().getIRI().equals(HAS_MATURITY_LEVEL.getIri())
                        && annotationValue.isIRI()
                        && annotationValue.asIRI().isPresent()) {
                    String annotationIri = annotationValue.asIRI().get().toString();
                    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(annotationIri));
                 return Optional.of(MaturityLevelFactory.create(label, annotationIri, annotationsDataHandler.getIconForMaturityLevel(label)));
                }
            }
        }

        return Optional.empty();
    }

    private String getOntologyIri(String elementIri) {
        var matcher = ONTOLOGY_IRI_PATTERN.matcher(elementIri);

        if (matcher.matches()) {
            return matcher.group(ONTOLOGY_IRI_GROUP_NAME);
        }

        return elementIri;
    }

    public OntoMaturityLevel generateMaturityLevelForModules(String iri) {
        OntoMaturityLevel result = null;
        for (FiboModule fiboModule : modules) {
            result = generateMaturityLevelForModules(fiboModule, iri);
            if (result != null) {
                break;
            }
        }
        return result != null ? result : MaturityLevelFactory.empty();
    }

    private OntoMaturityLevel generateMaturityLevelForModules(FiboModule fiboModule, String iri) {
        OntoMaturityLevel result = null;
        if (fiboModule.getIri().equals(iri)) {
            return generateModuleMaturity(fiboModule);
        }
        for (FiboModule fiboSubModule : fiboModule.getSubModule()) {
            result = generateMaturityLevelForModules(fiboSubModule, iri);
            if (result != null) {
                break;
            }
        }
        return result;
    }

 private OntoMaturityLevel generateModuleMaturity(FiboModule fiboModule) {
        switch (fiboModule.getMaturityLevel().getLabel()) {
            case PROD:
                return MaturityLevelFactory.create("", "",PROD_ICON);
            case PROD_DEV_MIXED:
                return MaturityLevelFactory.create("", "",PROD_DEV_MIXED_ICON );
            case DEV:
                return MaturityLevelFactory.create("", "",DEV_ICON);
        }
        return MaturityLevelFactory.empty();
    }
}
