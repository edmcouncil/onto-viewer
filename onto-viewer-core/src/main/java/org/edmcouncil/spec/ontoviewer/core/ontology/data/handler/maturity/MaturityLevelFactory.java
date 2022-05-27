package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import static org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition.NOT_SET;

import java.util.EnumMap;
import java.util.Optional;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class MaturityLevelFactory {

  private MaturityLevelFactory() {
  }

  private static final EnumMap<MaturityLevelDefinition, MaturityLevel> MATURITY_LEVELS
      = new EnumMap<>(MaturityLevelDefinition.class);

  static {
    for (MaturityLevelDefinition definition : MaturityLevelDefinition.values()) {
      MATURITY_LEVELS.put(definition, createMaturityLevel(definition));
    }
  }

  public static MaturityLevel get(MaturityLevelDefinition maturityLevelDefinition) {
    return MATURITY_LEVELS.get(maturityLevelDefinition);
  }

  public static MaturityLevel notSet() {
    return MATURITY_LEVELS.get(NOT_SET);
  }

  private static MaturityLevel createMaturityLevel(MaturityLevelDefinition maturityLevelDefinition) {
    return new MaturityLevel(maturityLevelDefinition.name(), maturityLevelDefinition.getIri());
  }

  public static Optional<MaturityLevel> getByIri(String maturityLevelIri) {
    var maturityLevelDefinition = MaturityLevelDefinition.getByIri(maturityLevelIri);
    if (maturityLevelDefinition.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(MATURITY_LEVELS.get(maturityLevelDefinition.get()));
  }
}