package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class MaturityLevelFactory {

  private static final Logger LOG = LoggerFactory.getLogger(MaturityLevelFactory.class);
  private final List<MaturityLevel> maturityLevels = new LinkedList<>();
  public static final MaturityLevel MIXED = new MaturityLevel("Mixed", "https://spec.edmcouncil.org/ontoviewer/Mixed");
  public static final MaturityLevel NOT_SET = new MaturityLevel("Not Set", "https://spec.edmcouncil.org/ontoviewer/NotSet");

  public MaturityLevelFactory(ApplicationConfigurationService applicationConfigurationService) {
    for (Pair maturityLevel
        : applicationConfigurationService.getConfigurationData().getOntologiesConfig().getMaturityLevelDefinition()) {
      LOG.info("Pair: {}", maturityLevel);
      this.maturityLevels.add(new MaturityLevel(maturityLevel.getLabel(), maturityLevel.getIri()));
    }
  }

  public MaturityLevel notSet() {
    return NOT_SET;
  }

  public MaturityLevel mixed() {
    return MIXED;
  }

  public Optional<MaturityLevel> getByIri(String maturityLevelIri) {
    for (MaturityLevel value : maturityLevels) {
      if (value.getIri().equals(maturityLevelIri)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  public List<MaturityLevel> getMaturityLevels() {
    return maturityLevels;
  }
}
