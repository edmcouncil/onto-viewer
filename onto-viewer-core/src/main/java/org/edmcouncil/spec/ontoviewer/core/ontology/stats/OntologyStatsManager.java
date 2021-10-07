package org.edmcouncil.spec.ontoviewer.core.ontology.stats;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.WeaselOwlType;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.IndividualDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michal Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyStatsManager {

    private static final String MODULE_IRI = "http://www.omg.org/techprocess/ab/SpecificationMetadata/Module";
    private static final String instanceKey = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function, WeaselOwlType.INSTANCES.name().toLowerCase());
    private static final Logger LOG = LoggerFactory.getLogger(OntologyStatsManager.class);

    @Autowired
    private FiboDataHandler fiboDataHandler;
    @Autowired
    private IndividualDataHandler individualDataHandler;
    @Autowired
    private LabelProvider labelProvider;

    private OntologyStatsMapped ontologyStatsMapped;

    public OntologyStatsMapped getOntologyStats() {
        return ontologyStatsMapped;
    }

    public void clear() {
        ontologyStatsMapped = new OntologyStatsMapped();
    }

    public void generateStats(OWLOntology ontology) {
        Optional<OWLClass> clazzOpt = getModuleClazz(ontology);

        Map<String, Number> stats = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>();

        if (clazzOpt.isPresent()) {

            OwlDetailsProperties<PropertyValue> indi = individualDataHandler.handleClassIndividuals(ontology, clazzOpt.get());
            if (indi.getProperties().isEmpty()) {
                //--noDomain
                String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDomain");
                stats.put(id, 0);
                labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
                //--numberOfModule
                id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
                stats.put(id, 0);
                labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
            }

            Set<String> modulesIriSet = new HashSet<>();

            indi.getProperties().get(instanceKey).stream()
                    .map((propertyValue) -> (OwlListElementIndividualProperty) propertyValue)
                    .map((individProperty) -> (String) individProperty.getValue().getIri())
                    .forEachOrdered((elIri) -> {
                        modulesIriSet.add(elIri);
                    });
            List<String> rootModulesIris = fiboDataHandler.getRootModulesIris(modulesIriSet, ontology);
            //--numberOfDomain
            String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDomain");
            stats.put(id, rootModulesIris.size());
            labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
            //--numberOfModule
            id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
            stats.put(id, modulesIriSet.size() - rootModulesIris.size());
            labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        } else {
            //--numberOfDomain
                String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDomain");
                stats.put(id, 0);
                labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
                //--numberOfModule
                id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
                stats.put(id, 0);
                labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        }

        //--numberOfClass
        String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfClass");
        stats.put(id, ontology.classesInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfObjectProperty
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfObjectProperty");
        stats.put(id, ontology.objectPropertiesInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfDataPropertyy
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDataProperty");
        stats.put(id, ontology.dataPropertiesInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfOAnnotationProperty
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfAnnotationProperty");
        stats.put(id, ontology.annotationPropertiesInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfIndividuals
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfIndividuals");
        stats.put(id, ontology.individualsInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfAxiom
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfAxiom");
        stats.put(id, ontology.axioms().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfDatatype
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDatatype");
        stats.put(id, ontology.datatypesInSignature().count());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));

        Set<OWLOntology> ontologies = new HashSet<>();
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.ontologies().collect(Collectors.toSet()).forEach((owlOntology) -> {
            ontologies.add(owlOntology);
        });
        //--numberOfDatatype
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfOntologies");
        stats.put(id, ontologies.size());
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));

        LOG.debug(stats.toString());
        OntologyStatsMapped osm = new OntologyStatsMapped();
        osm.setLabels(labels);
        osm.setStats(stats);
        
        ontologyStatsMapped = osm;

    }

    private Optional<OWLClass> getModuleClazz(OWLOntology ontology) {
        List<FiboModule> result = new LinkedList<>();
        IRI moduleIri = IRI.create(MODULE_IRI);
        Optional<OWLClass> clazzOpt = ontology
                .classesInSignature()
                .filter(c -> c.getIRI().equals(moduleIri))
                .findFirst();
        return clazzOpt;
    }

}
