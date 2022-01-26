package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import java.util.Objects;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntoFiboMaturityLevel {

    private String label;
    private String iri;
    private String icon;

    OntoFiboMaturityLevel(String label, String iri, String icon) {
        this.label = label;
        this.iri = iri;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "OntoFiboMaturityLevel{" + "label=" + label + ", iri=" + iri + ", icon=" + icon + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.label);
        hash = 83 * hash + Objects.hashCode(this.iri);
        hash = 83 * hash + Objects.hashCode(this.icon);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OntoFiboMaturityLevel other = (OntoFiboMaturityLevel) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.iri, other.iri)) {
            return false;
        }
        if (!Objects.equals(this.icon, other.icon)) {
            return false;
        }
        return true;
    }

}
