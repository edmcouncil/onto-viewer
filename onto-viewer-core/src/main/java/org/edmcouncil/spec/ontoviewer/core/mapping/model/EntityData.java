package org.edmcouncil.spec.ontoviewer.core.mapping.model;

import java.util.Objects;
import java.util.StringJoiner;

public class EntityData {

  private String iri;
  private String termLabel;
  private String typeLabel;
  private String ontology;
  private String synonyms;
  private String definition;
  private String generatedDefinition;
  private String examples;
  private String explanations;
  private String maturity;
  private Boolean deprecated;

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

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

  public Boolean getDeprecated() {
    return deprecated;
  }
  
  public void setDeprecated(Boolean deprecated) {
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EntityData)) {
      return false;
    }
    EntityData that = (EntityData) o;
    return Objects.equals(iri, that.iri) && Objects.equals(termLabel, that.termLabel)
        && Objects.equals(typeLabel, that.typeLabel) && Objects.equals(ontology, that.ontology)
        && Objects.equals(synonyms, that.synonyms) && Objects.equals(definition, that.definition)
        && Objects.equals(generatedDefinition, that.generatedDefinition) && Objects.equals(examples,
        that.examples) && Objects.equals(explanations, that.explanations) && Objects.equals(maturity,
        that.maturity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iri, termLabel, typeLabel, ontology, synonyms, definition, generatedDefinition, examples,
        explanations, maturity);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", EntityData.class.getSimpleName() + "[", "]")
        .add("iri='" + iri + "'")
        .add("termLabel='" + termLabel + "'")
        .add("typeLabel='" + typeLabel + "'")
        .add("ontology='" + ontology + "'")
        .add("synonyms='" + synonyms + "'")
        .add("definition='" + definition + "'")
        .add("generatedDefinition='" + generatedDefinition + "'")
        .add("examples='" + examples + "'")
        .add("explanations='" + explanations + "'")
        .add("maturity='" + maturity + "'")
        .toString();
  }

 
}
