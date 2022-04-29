package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import java.util.Objects;
import java.util.StringJoiner;

public class AppMaturityLevel implements MaturityLevel{

    String label;

    public AppMaturityLevel(String str) {
        this.label = str;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppMaturityLevel.class.getSimpleName() + "[", "]")
            .add("label='" + label + "'")
            .toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.label);
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
        final AppMaturityLevel other = (AppMaturityLevel) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return true;
    }
    
    

}
