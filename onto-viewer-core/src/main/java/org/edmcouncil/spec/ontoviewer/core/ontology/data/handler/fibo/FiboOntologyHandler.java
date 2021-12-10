package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import static org.edmcouncil.spec.ontoviewer.core.FiboVocabulary.HAS_MATURITY_LEVEL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
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

    private final OntologyManager ontologyManager;
    private final LabelProvider labelProvider;
    // private final ModuleManager moduleManager;

    // Cache for ontologies' maturity level
    private Map<IRI, OntoFiboMaturityLevel> maturityLevels = new ConcurrentHashMap<>();

    public FiboOntologyHandler(OntologyManager ontologyManager,
            LabelProvider labelProvider) {
        this.ontologyManager = ontologyManager;
        this.labelProvider = labelProvider;
        // this.moduleManager = moduleManager;
    }

    /**
     * Find the ontology containing the resources with given iri and extract
     * their level of maturity. When don't find resource in ontologies or
     * ontology doesn't have maturity level method return empty fibo maturity
     * level.
     *
     * @param entityIri IRI of element
     * @return extracted fibo maturity level
     */
    public OntoFiboMaturityLevel getMaturityLevelForElement(String entityIri) {
        String ontologyIri = getOntologyIri(entityIri);
        if (ontologyIri != null) {
            OntoFiboMaturityLevel ontoFiboMaturityLevel = getMaturityLevelForOntology(IRI.create(ontologyIri));
            if (ontoFiboMaturityLevel.getIri().isEmpty() && ontoFiboMaturityLevel.getLabel().isEmpty()) {
                //   ontoFiboMaturityLevel =  moduleManager.getModulesData(entityIri);
                ontoFiboMaturityLevel = getMaturityLevelForOntology(IRI.create(entityIri));
            }
            return ontoFiboMaturityLevel;
        }
        return FiboMaturityLevelFactory.empty();
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

                    return Optional.of(FiboMaturityLevelFactory.create(ontologyLabel, annotationIri));
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

    public void generateMaturityLevelForModules(List<FiboModule> modules) {
        Map<IRI, OntoFiboMaturityLevel> result = new ConcurrentHashMap<>();
        for (FiboModule fiboModule : modules) {
            result.putAll(generateMaturityLevelForModules(fiboModule));
        }
        maturityLevels = result;
    }

    private Map<IRI, OntoFiboMaturityLevel> generateMaturityLevelForModules(FiboModule fiboModule) {
        Map<IRI, OntoFiboMaturityLevel> result = new ConcurrentHashMap<>();
        result.put(IRI.create(fiboModule.getIri()), generateModuleMaturity(fiboModule));
        for (FiboModule fiboSubModule : fiboModule.getSubModule()) {
            result.putAll(generateMaturityLevelForModules(fiboSubModule));
        }
        return result;
    }

    private OntoFiboMaturityLevel generateModuleMaturity(FiboModule fiboModule) {
        switch (fiboModule.getMaturityLevel().getLabel()) {
            case "prod":
                return FiboMaturityLevelFactory.create("prod", "");
            case "prodDev":
                return FiboMaturityLevelFactory.create("prodDev", "");
            case "dev":
                return FiboMaturityLevelFactory.create("dev", "");
        }
        return FiboMaturityLevelFactory.empty();
    }

}
