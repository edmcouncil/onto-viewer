package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class MaturityLevelFactory {

  public static final MaturityLevel MIXED = new MaturityLevel("Mixed", "https://spec.edmcouncil.org/ontoviewer/Mixed");
  public static final MaturityLevel NOT_SET = new MaturityLevel("Not Set",
      "https://spec.edmcouncil.org/ontoviewer/NotSet");

  private final ApplicationConfigurationService applicationConfigurationService;
  private List<MaturityLevel> maturityLevels = null;

  public MaturityLevelFactory(ApplicationConfigurationService applicationConfigurationService) {
    this.applicationConfigurationService = applicationConfigurationService;
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

  public Optional<MaturityLevel> getByLabel(String maturityLevelLabel) {
    for (MaturityLevel value : maturityLevels) {
      if (value.getLabel().equals(maturityLevelLabel)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }

  public List<MaturityLevel> getMaturityLevels() {
    if (maturityLevels == null) {
      this.maturityLevels = new ArrayList<>();
      var ontologiesConfig = applicationConfigurationService.getConfigurationData().getOntologiesConfig();
      for (Pair maturityLevel : ontologiesConfig.getMaturityLevelDefinition()) {
        this.maturityLevels.add(new MaturityLevel(maturityLevel.getLabel(), maturityLevel.getIri()));
      }
    }
    return maturityLevels;
  }

  public Optional<MaturityLevel> getMaturityLevel(String candidateIriString) {
    return getMaturityLevels()
        .stream()
        .filter(maturityLevelCandidate -> maturityLevelCandidate.getIri().equals(candidateIriString))
        .findFirst();
  }
}
