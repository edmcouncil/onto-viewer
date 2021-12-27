package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.OntoFiboMaturityLevel;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class SearchItem {

    private String iri;
    private String label;
    private String description;
    private double relevancy;
    private OntoFiboMaturityLevel maturityLevel;

    public String getIri() {
        return iri;
    }

    public double getRelevancy() {
        return relevancy;
    }

    public void setRelevancy(double relevancy) {
        this.relevancy = relevancy;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaturityLevel(OntoFiboMaturityLevel maturityLevel) {
        this.maturityLevel = maturityLevel;
    }

    public OntoFiboMaturityLevel getMaturityLevel() {
        return maturityLevel;
    }
}
