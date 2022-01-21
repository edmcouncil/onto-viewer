package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.model.onto.OntologyResources;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.IndividualDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This data handler working with FIBO ontology.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class FiboDataHandler {

    //TODO: move this to set to configuration
    private static final String MODULE_IRI = "http://www.omg.org/techprocess/ab/SpecificationMetadata/Module";
    private static final String RELEASE_IRI = "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Release";

    private static final Logger LOG = LoggerFactory.getLogger(FiboDataHandler.class);
    private static final String instanceKey = ViewerIdentifierFactory.createId(
            ViewerIdentifierFactory.Type.function, OwlType.INSTANCES.name().toLowerCase());

    private final AnnotationsDataHandler annotationsDataHandler;
    private final IndividualDataHandler individualDataHandler;
    private final CustomDataFactory customDataFactory;
    private final LabelProvider labelExtractor;
    private final FiboOntologyHandler fiboOntologyHandler;

    private String resourceInternal;
    private String resourceExternal;

    private List<FiboModule> modules;

    private Map<String, OntologyResources> resources = null;

    public FiboDataHandler(AnnotationsDataHandler annotationsDataHandler,
            IndividualDataHandler individualDataHandler,
            CustomDataFactory customDataFactory,
            LabelProvider labelExtractor,
            FiboOntologyHandler fiboOntologyHandler) {
        this.annotationsDataHandler = annotationsDataHandler;
        this.individualDataHandler = individualDataHandler;
        this.customDataFactory = customDataFactory;
        this.labelExtractor = labelExtractor;
        this.fiboOntologyHandler = fiboOntologyHandler;
    }

    public OwlDetailsProperties<PropertyValue> handleFiboOntologyMetadata(IRI iri,
            OWLOntology ontology, OwlListDetails details) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OwlDetailsProperties<PropertyValue> annotations = null;

        for (OWLOntology onto : manager.ontologies().collect(Collectors.toSet())) {

            if (!onto.getOntologyID().getOntologyIRI().isPresent()) {
                continue;
            }

            if (onto.getOntologyID().getOntologyIRI().get().equals(iri)
                    || onto.getOntologyID().getOntologyIRI().get()
                            .equals(IRI.create(iri.getIRIString().substring(0, iri.getIRIString().length() - 1)))) {

                IRI ontoIri = onto.getOntologyID().getOntologyIRI().get();
                annotations = annotationsDataHandler.handleOntologyAnnotations(onto.annotations(), details);

                OntologyResources ontologyResources = getOntologyResources(ontoIri.toString(), ontology);
                if (ontologyResources != null) {
                    for (Map.Entry<String, List<PropertyValue>> entry : ontologyResources.getResources()
                            .entrySet()) {
                        for (PropertyValue propertyValue : entry.getValue()) {
                            annotations.addProperty(entry.getKey(), propertyValue);
                        }
                    }
                }
                details.setMaturityLevel(fiboOntologyHandler.getMaturityLevelForElement(iri.toString()));
                break;
            }
        }

        return annotations;
    }

    public List<FiboModule> getAllModulesData(OWLOntology ontology) {
        if (modules == null) {
            List<FiboModule> result = new LinkedList<>();
            IRI moduleIri = IRI.create(MODULE_IRI);
            Optional<OWLClass> clazzOpt = ontology
                    .classesInSignature(Imports.INCLUDED)
                    .filter(c -> c.getIRI().equals(moduleIri))
                    .findFirst();
            if (clazzOpt.isEmpty()) {
                return new LinkedList<>();
            }

            OwlDetailsProperties<PropertyValue> indi
                    = individualDataHandler.handleClassIndividuals(ontology, clazzOpt.get());

            if (indi.getProperties().isEmpty()) {
                return result;
            }

            Set<String> modulesIriSet = new HashSet<>();

            indi.getProperties().get(instanceKey).stream()
                    .map((propertyValue) -> (OwlListElementIndividualProperty) propertyValue)
                    .map((individProperty) -> (String) individProperty.getValue().getIri())
                    .forEachOrdered((elIri) -> {
                        modulesIriSet.add(elIri);
                    });

            List<String> rootModulesIris = getRootModulesIris(modulesIriSet, ontology);

            rootModulesIris.stream()
                    .map((rootModulesIri) -> {
                        FiboModule fm = new FiboModule();
                        fm.setIri(rootModulesIri);
                        fm.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(rootModulesIri)));
                        fm.setMaturityLevel(chooseTheRightVersion(
                                fiboOntologyHandler.getMaturityLevelForOntology(IRI.create(rootModulesIri))));
                        fm.setSubModule(getSubModules(rootModulesIri, ontology));
                        return fm;
                    }).forEachOrdered(result::add);

            modules = result.stream()
                    .sorted((obj1, obj2) -> obj1.getLabel().compareTo(obj2.getLabel()))
                    .map(r -> {
                        r.sort();
                        return r;
                    }).collect(Collectors.toList());

            modules.forEach(this::checkAndCompleteMaturityLevel);

            return modules;
        }

        return modules;
    }

    public List<String> getRootModulesIris(Set<String> modulesIriSet, OWLOntology ontology) {
        Map<String, Integer> referenceCount = new LinkedHashMap<>();
        modulesIriSet.forEach((mIri) -> {
            Set<String> hasPartModules = getHasPartElements(IRI.create(mIri), ontology);
            referenceCount.putIfAbsent(mIri, 0);
            hasPartModules.forEach((partModule) -> {
                Integer c = referenceCount.getOrDefault(partModule, 0);
                c++;
                referenceCount.put(partModule, c);
            });
        });
        List<String> rootModulesIris = referenceCount.entrySet().stream()
                .filter(r -> r.getValue() == 0).map(r -> r.getKey())
                .collect(Collectors.toList());
        return rootModulesIris;
    }

    public OntologyResources getOntologyResources(String iri, OWLOntology ontology) {
        if (resources == null) {
            loadAllOntologyResources(ontology);
        }

        return resources.get(iri);
    }

    public Set<String> getHasPartElements(IRI iri, OWLOntology ontology) {
        OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
        Optional<OWLNamedIndividual> individual = ontology
                .individualsInSignature(Imports.INCLUDED)
                .filter(c -> c.getIRI().equals(iri))
                .findFirst();
        if (individual.isEmpty()) {
            return new HashSet<>(0);
        }
        Iterator<OWLAnnotation> iteratorAnnotation = EntitySearcher
                .getAnnotations(
                        individual.get(),
                        ontology.importsClosure(),
                        dataFactory.getOWLAnnotationProperty(IRI.create("http://purl.org/dc/terms/hasPart")))
                .iterator();

        Set<String> result = new LinkedHashSet<>();
        while (iteratorAnnotation.hasNext()) {
            OWLAnnotation annotation = iteratorAnnotation.next();
            String s = annotation.annotationValue().toString();
            result.add(s);
        }

        return result;
    }

    public void populateOntologyResources(OWLOntology ontology) {
        // TODO: Make loadAllOntologyResources and setOntologyResources private and use this method
        //       instead
        this.resources = loadAllOntologyResources(ontology);
    }

    private Map<String, OntologyResources> loadAllOntologyResources(OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        Map<String, OntologyResources> allResources = new HashMap<>();

        completeResourceKeys();

        manager.ontologies().collect(Collectors.toSet()).forEach((owlOntology) -> {
            OntologyResources ontoResources = extractOntologyResources(owlOntology);

            if (ontoResources != null) {
                String ontIri = owlOntology.getOntologyID().getOntologyIRI().get().toString();
                if (!ontIri.equals("https://spec.edmcouncil.org/fibo/ontology")) {
                    allResources.put(ontIri, ontoResources);
                }
            }
        });
        return allResources;
    }

    private OntologyResources extractOntologyResources(OWLOntology selectedOntology) {
        OntologyResources ontoResources = new OntologyResources();
        Optional<IRI> opt = selectedOntology.getOntologyID().getOntologyIRI();
        IRI ontologyIri;
        if (opt.isPresent()) {
            ontologyIri = opt.get();
        } else {
            opt = selectedOntology.getOntologyID().getDefaultDocumentIRI();
            if (opt.isPresent()) {
                ontologyIri = opt.get();
                LOG.debug("IRI for this ontology doesn't exist, use Default Document IRI {}", ontologyIri);
            } else {
                LOG.debug("Ontology doesn't have any iri to present... Ontology ID: {}",
                        selectedOntology.getOntologyID().toString());
                return null;
            }
        }

        selectedOntology.annotationPropertiesInSignature()
                .map(c -> {
                    String istring = c.getIRI().toString();
                    OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
                    return pv;
                })
                .forEachOrdered(c -> ontoResources
                .addElement(selectResourceIriString(c, ontologyIri,
                        ViewerIdentifierFactory.Element.annotationProperty), c));

        selectedOntology.classesInSignature()
                .map(c -> {
                    String istring = c.getIRI().toString();
                    OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
                    return pv;
                })
                .forEachOrdered(c -> ontoResources
                .addElement(
                        selectResourceIriString(c, ontologyIri, ViewerIdentifierFactory.Element.clazz), c)
                );

        selectedOntology.dataPropertiesInSignature()
                .map(c -> {
                    String istring = c.getIRI().toString();
                    OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
                    return pv;
                })
                .forEachOrdered(c -> ontoResources
                .addElement(selectResourceIriString(c, ontologyIri,
                        ViewerIdentifierFactory.Element.dataProperty), c));

        selectedOntology.objectPropertiesInSignature()
                .map(c -> {
                    String istring = c.getIRI().toString();
                    OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
                    return pv;
                })
                .forEachOrdered(c -> ontoResources
                .addElement(selectResourceIriString(c, ontologyIri,
                        ViewerIdentifierFactory.Element.objectProperty), c));

        selectedOntology.individualsInSignature()
                .map(individual -> customDataFactory.createAnnotationIri(individual.getIRI().toString()))
                .forEachOrdered(individual
                        -> ontoResources.addElement(
                        selectResourceIriString(
                                individual,
                                ontologyIri,
                                ViewerIdentifierFactory.Element.instance),
                        individual
                ));

        selectedOntology.datatypesInSignature()
                .map(c -> {
                    String istring = c.getIRI().toString();
                    OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
                    return pv;
                })
                .forEachOrdered(c -> ontoResources
                .addElement(selectResourceIriString(c, ontologyIri,
                        ViewerIdentifierFactory.Element.objectProperty), c));

        ontoResources.sortInAlphabeticalOrder();

        return ontoResources;
    }

    private void completeResourceKeys() {

        resourceInternal = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal,
                ViewerIdentifierFactory.Element.empty);
        LOG.debug("Internal resource iri: {}", resourceInternal);

        resourceExternal = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external,
                ViewerIdentifierFactory.Element.empty);
        LOG.debug("External resource iri: {}", resourceExternal);

    }

    private List<FiboModule> getSubModules(String moduleIri, OWLOntology ontology) {
        List<FiboModule> result = new LinkedList<>();

        Set<String> hasPartModules = getHasPartElements(IRI.create(moduleIri), ontology);

        hasPartModules.stream().map((partModule) -> {
            FiboModule fm = new FiboModule();
            fm.setIri(partModule);
            fm.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(partModule)));
            fm.setMaturityLevel(
                    chooseTheRightVersion(
                            fiboOntologyHandler.getMaturityLevelForOntology(IRI.create(partModule))));
            fm.setSubModule(getSubModules(partModule, ontology));
            return fm;
        }).forEachOrdered(result::add);

        return result;
    }

    /**
     * @param c Annotation iri
     * @param ontologyIri IRI ontology used to compare with annotations IRI
     * @param element Create IRI for this element
     * @return IRI represented as String
     */
    private String selectResourceIriString(OwlAnnotationIri c, IRI ontologyIri,
            ViewerIdentifierFactory.Element element) {
        String annotationIri = c.getValue().getIri();

        return annotationIri.contains(ontologyIri)
                ? ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, element)
                : ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, element);
    }

    /**
     * @param elementIri IRI element to which path we want to find
     * @param ontology
     * @return Returns the path to the module in which it is located, empty list
     * if element not present in modules
     */
    public List<String> getElementLocationInModules(String elementIri, OWLOntology ontology) {
        List<String> result = new LinkedList<>();
        if (resources == null) {
            loadAllOntologyResources(ontology);
        }
        if (modules == null) {
            modules = getAllModulesData(ontology);
        }

        if (modules.isEmpty()) {
            return result;
        }

        String ontologyIri = findElementInOntology(elementIri);

        ontologyIri = ontologyIri == null ? elementIri : ontologyIri;

        LOG.debug("[FIBO Data Handler] Element found in ontology {}", ontologyIri);
        if (ontologyIri != null) {
            for (FiboModule module : modules) {
                if (trackingThePath(module, ontologyIri, result, elementIri)) {
                    LOG.debug("[FIBO Data Handler] Location Path {}", Arrays.toString(result.toArray()));
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * @return ontology iri where the element is present
     */
    private String findElementInOntology(String elementIri) {
        // https://spec.edmcouncil.org/fibo/ontology
        String ontologyIri = null;
        for (Map.Entry<String, OntologyResources> entry : resources.entrySet()) {
            for (Map.Entry<String, List<PropertyValue>> entryResource : entry.getValue().getResources()
                    .entrySet()) {
                if (entryResource.getKey().contains(resourceInternal)) {
                    for (PropertyValue propertyValue : entryResource.getValue()) {
                        OwlAnnotationIri annotation = (OwlAnnotationIri) propertyValue;
                        if (annotation.getValue().getIri().equals(elementIri)) {
                            if (elementIri.contains(entry.getKey())) {
                                ontologyIri = entry.getKey();
                                break;
                            }
                        }
                    }
                }

            }
        }
        return ontologyIri;
    }

    private Boolean trackingThePath(FiboModule node, String ontologyIri, List<String> track,
            String elementIri) {

        if (node == null) {
            return false;
        }

        if (node.getIri().equals(elementIri)) {
            track.add(node.getIri());
            return true;
        }

        if (node.getIri().equals(ontologyIri)) {
            track.add(node.getIri());
            return true;
        }

        for (FiboModule child : node.getSubModule()) {
            if (trackingThePath(child, ontologyIri, track, elementIri)) {
                track.add(0, node.getIri());
                return true;
            }
        }
        return false;
    }

    private void checkAndCompleteMaturityLevel(FiboModule module) {
        if (module.getMaturityLevel() != null && !module.getMaturityLevel().getLabel().equals("")) {
            return;
        }
        Set<FiboMaturityLevel> levels = new HashSet<>();
        for (FiboModule subModule : module.getSubModule()) {
            checkAndCompleteMaturityLevel(subModule);
            levels.add(subModule.getMaturityLevel());
        }
        module.setMaturityLevel(chooseTheRightVersion(levels));

    }

    private FiboMaturityLevel chooseTheRightVersion(OntoFiboMaturityLevel maturityLevelFromOntology) {
        if (maturityLevelFromOntology.getIri().equals(RELEASE_IRI)) {
            return FiboMaturityLevelFactory.prod;
        } else if (maturityLevelFromOntology.getIri().equals("")) {
            return FiboMaturityLevelFactory.emptyAppFiboMaturityLabel();
        } else {
            return FiboMaturityLevelFactory.dev;
        }
    }

    private FiboMaturityLevel chooseTheRightVersion(Set<FiboMaturityLevel> levels) {

        int prodCount = 0;
        int devCount = 0;
        for (FiboMaturityLevel level : levels) {
            if (level.equals(FiboMaturityLevelFactory.prodDev)) {
                return level;
            } else if (level.equals(FiboMaturityLevelFactory.prod)) {
                prodCount++;
            } else if (level.equals(FiboMaturityLevelFactory.dev)) {
                devCount++;
            }
        }
        LOG.trace("Version select, prodCount:{}, devCount:{}, size:{}", prodCount, devCount,
                levels.size());
        if (prodCount == levels.size()) {
            return FiboMaturityLevelFactory.prod;
        } else if (devCount == levels.size()) {
            return FiboMaturityLevelFactory.dev;
        } else {
            return FiboMaturityLevelFactory.prodDev;
        }

    }

    public void clearAndSetNewModules(OWLOntology ontology) {
        if (modules != null) {
            modules.clear();
            modules = null;
        }
        getAllModulesData(ontology);
    }

    public List<FiboModule> getModules() {
        return modules;
    }

    public FiboOntologyHandler getFiboOntologyHandler() {
        return fiboOntologyHandler;
    }
}
