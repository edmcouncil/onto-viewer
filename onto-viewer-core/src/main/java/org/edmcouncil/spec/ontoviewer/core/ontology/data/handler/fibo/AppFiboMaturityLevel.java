package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import java.util.Objects;

public class AppFiboMaturityLevel implements FiboMaturityLevel{

    String label;

    AppFiboMaturityLevel(String str) {
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
        final AppFiboMaturityLevel other = (AppFiboMaturityLevel) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        return true;
    }
    
    

}
