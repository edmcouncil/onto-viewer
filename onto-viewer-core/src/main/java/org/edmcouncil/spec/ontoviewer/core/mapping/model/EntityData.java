package org.edmcouncil.spec.ontoviewer.core.mapping.model;

public class EntityData {

  private String termLabel;
  private String typeLabel;
  private String ontology;
  private String synonyms;
  private String definition;
  private String generatedDefinition;
  private String examples;
  private String explanations;
  private String maturity;

  public String getTermLabel() {
    return termLabel;
  }

  public void setTermLabel(String termLabel) {
    this.termLabel = termLabel;
  }

  public String getTypeLabel() {
    return typeLabel;
  }

  public void setTypeLabel(String typeLabel) {
    this.typeLabel = typeLabel;
  }

  public String getOntology() {
    return ontology;
  }

  public void setOntology(String ontology) {
    this.ontology = ontology;
  }

  public String getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(String synonyms) {
    this.synonyms = synonyms;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getGeneratedDefinition() {
    return generatedDefinition;
  }

  public void setGeneratedDefinition(String generatedDefinition) {
    this.generatedDefinition = generatedDefinition;
  }

  public String getExamples() {
    return examples;
  }

  public void setExamples(String examples) {
    this.examples = examples;
  }

  public String getExplanations() {
    return explanations;
  }

  public void setExplanations(String explanations) {
    this.explanations = explanations;
  }

  public String getMaturity() {
    return maturity;
  }

  public void setMaturity(String maturity) {
    this.maturity = maturity;
  }
}
