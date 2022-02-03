package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import static org.edmcouncil.spec.ontoviewer.core.FiboVocabulary.HAS_MATURITY_LEVEL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Service;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Service
public class FiboOntologyHandler {

    private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
    private static final Pattern ONTOLOGY_IRI_PATTERN
            = Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
    private static final String PROD = "prod";
    private static final String PROD_DEV = "dev";
    private static final String DEV  = "prodDev";
    private static final String PROD_ICON  = "release";
    private static final String PROD_DEV_ICON  = "mixed";
    private static final String DEV_ICON  = "develop";
    
    private final OntologyManager ontologyManager;
    private final LabelProvider labelProvider;
    private final AnnotationsDataHandler annotationsDataHandler;

    // Cache for ontologies' maturity level
    private Map<IRI, OntoFiboMaturityLevel> maturityLevels = new ConcurrentHashMap<>();
    private List<FiboModule> modules = Collections.emptyList();

    public FiboOntologyHandler(OntologyManager ontologyManager,
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
    public OntoFiboMaturityLevel getMaturityLevelForElement(String entityIri) {
        String ontologyIri = getOntologyIri(entityIri);
        if (ontologyIri != null) {
            OntoFiboMaturityLevel ontoFiboMaturityLevel = getMaturityLevelForOntology(IRI.create(ontologyIri));
            if (ontoFiboMaturityLevel.getIri().isEmpty() && ontoFiboMaturityLevel.getLabel().isEmpty()) {
                ontoFiboMaturityLevel = generateMaturityLevelForModules(entityIri);
            }
            return ontoFiboMaturityLevel;
        }
        return FiboMaturityLevelFactory.empty();
    }

    public void setModulesTree(List<FiboModule> modules) {
        this.modules = modules;

    }

    public OntoFiboMaturityLevel getMaturityLevelForOntology(IRI ontologyIri) {
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

        return maturityLevels.getOrDefault(ontologyIri, FiboMaturityLevelFactory.empty());
    }

    private Optional<OntoFiboMaturityLevel> getMaturityLevelForParticularOntology(
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
                    String ontologyLabel = labelProvider.getLabelOrDefaultFragment(IRI.create(annotationIri));

                 return Optional.of(FiboMaturityLevelFactory.create(ontologyLabel, annotationIri, annotationsDataHandler.getIconForMaturityLevel(ontologyLabel)));
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

    public OntoFiboMaturityLevel generateMaturityLevelForModules(String iri) {
        OntoFiboMaturityLevel result = null;
        for (FiboModule fiboModule : modules) {
            result = generateMaturityLevelForModules(fiboModule, iri);
            if (result != null) {
                break;
            }
        }
        return result != null ? result : FiboMaturityLevelFactory.empty();
    }

    private OntoFiboMaturityLevel generateMaturityLevelForModules(FiboModule fiboModule, String iri) {
        OntoFiboMaturityLevel result = null;
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

 private OntoFiboMaturityLevel generateModuleMaturity(FiboModule fiboModule) {
        switch (fiboModule.getMaturityLevel().getLabel()) {
            case PROD:
                return FiboMaturityLevelFactory.create("", "",PROD_ICON);
            case PROD_DEV:
                return FiboMaturityLevelFactory.create("", "",PROD_DEV_ICON);
            case DEV:
                return FiboMaturityLevelFactory.create("", "",DEV_ICON);
        }
        return FiboMaturityLevelFactory.empty();
    }
}
