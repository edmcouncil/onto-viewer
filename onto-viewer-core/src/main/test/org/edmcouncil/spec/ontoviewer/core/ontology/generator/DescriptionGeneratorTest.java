package org.edmcouncil.spec.ontoviewer.core.ontology.generator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyElementImpl;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;

class DescriptionGeneratorTest {

  private static final String EXAMPLE_IRI = "http://exammple.com/ontology/";

  private DescriptionGenerator descriptionGenerator;

  @BeforeEach
  void setUp() {
    this.descriptionGenerator = new DescriptionGenerator();
  }

  @Test
  void shouldReturnStringWithSingleSuperClassWhenThereIsOnlyOneTaxonomy() {
    var groupedDetails = prepareGroupedDetails(
        "test label",
        List.of(List.of("superTest1", "test")));

    var expectedResult =
        prepareExpectedResult(List.of("Own descriptions:\n- Test label is a kind of superTest1."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithTwoSuperClassesWhenThereAreTwoTaxonomies() {
    var groupedDetails = prepareGroupedDetails(
        "Test label",
        List.of(
            List.of("superTest1", "test"),
            List.of("superTest2", "test")));

    var expectedResult =
        prepareExpectedResult(List.of("Own descriptions:\n- Test label is a kind of superTest1, superTest2."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithThreeSuperClassesWhenThereAreThreeDistinctTaxonomies() {
    var groupedDetails = prepareGroupedDetails(
        "Test label",
        List.of(
            List.of("superTest1", "test"),
            List.of("superTest2", "test"),
            List.of("superTest3", "test")));

    var expectedResult =
        prepareExpectedResult(
            List.of("Own descriptions:\n- Test label is a kind of superTest1, superTest2, superTest3."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithTwoSuperClassesWhenThereAreThreeNonDistinctTaxonomies() {
    var groupedDetails = prepareGroupedDetails(
        "Test label",
        List.of(
            List.of("superTest1", "test"),
            List.of("superTest2", "test"),
            List.of("superTest2", "test")));

    var expectedResult =
        prepareExpectedResult(
            List.of("Own descriptions:\n- Test label is a kind of superTest1, superTest2."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithOneDirectRestrictionWhenThereIsOnlyOne() {
    var groupedDetails = prepareGroupedDetails(
        "Test label",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ min 0 /arg2/",
            new OwlEntity("hasAddress", "has address"),
            new OwlEntity("physicalAddress", "physical address")));

    var expectedResult = prepareExpectedResult(
        List.of("Own descriptions:\n" +
            "- Test label is a kind of superTest1.\n" +
            "- Test label may have address that is physical address."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithTwoDirectRestrictionWhenThereAreTwoAltogether() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ min 0 /arg2/",
            new OwlEntity("hasAddress", "has address"),
            new OwlEntity("physicalAddress", "physical address")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("isRecognizedIn", "is recognized in"),
            new OwlEntity("jurisdiction", "jurisdiction")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity is recognized in some jurisdiction.\n" +
                "- Legal entity may have address that is physical address."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithTwoGroupsOfRestrictionsWhenThereAreRestrictionsInBothGroups() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ exactly 1 /arg2/",
            new OwlEntity("isOrganizedIn", "is organized in"),
            new OwlEntity("jurisdiction", "jurisdiction")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("hasGoal", "has goal"),
            new OwlEntity("goal", "goal")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_INHERITED_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("isRecognizedIn", "is recognized in"),
            new OwlEntity("jurisdiction", "jurisdiction")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity has some goal.\n" +
                "- Legal entity is organized in exactly 1 jurisdiction.",
            "- Legal entity is recognized in some jurisdiction."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithRestrictionHasSomethingWithThisSomethingNotBeingEqualToProperty() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("hasGoal", "has goal"),
            new OwlEntity("goal", "goal")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_INHERITED_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("hasPart", "has part"),
            new OwlEntity("organization", "organization")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity has some goal.",
            "- Legal entity has some part that is organization."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithComplexRestrictionRepresentedWhenThereIsOne() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some (/arg2/ and (/arg3/ some /arg4/))",
            new OwlEntity("hasOriginatingServiceProvider", "has originating service provider"),
            new OwlEntity("financialServiceProvider", "financial service provider"),
            new OwlEntity("isIdentifiedBy", "is identified by"),
            new OwlEntity("NationwideMortgageLicensing", "Nationwide Mortgage Licensing")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity has some originating service provider that is financial service provider and is " +
                "identified by some Nationwide Mortgage Licensing."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithRestrictionContainingMayBePhraseWhenThereIsOne() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ min 0 /arg2/",
            new OwlEntity("isGovernedBy", "is governed by"),
            new OwlEntity("organizationCoveringAgreement", "organization covering agreement")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity may be governed by some organization covering agreement."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  @Test
  void shouldReturnStringWithSortedRestrictions() {
    var groupedDetails = prepareGroupedDetails(
        "legal entity",
        List.of(List.of("superTest1", "test")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("isRecognizedIn", "is recognized in"),
            new OwlEntity("zurisdiction", "zurisdiction")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ min 0 /arg2/",
            new OwlEntity("hasAddress", "has address"),
            new OwlEntity("physicalAddress", "physical address")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("isRecognizedIn", "is recognized in"),
            new OwlEntity("jurisdiction", "jurisdiction")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_LABEL,
        preparePropertyValue(
            "/arg1/ some /arg2/",
            new OwlEntity("hasAddress", "has address"),
            new OwlEntity("virtualAddress", "virtual address")));
    groupedDetails.addProperty(
        DescriptionGenerator.ONTOLOGICAL_CHARACTERISTIC_LABEL,
        DescriptionGenerator.IS_A_RESTRICTIONS_INHERITED_LABEL,
        preparePropertyValue(
            "/arg1/ min 0 /arg2/",
            new OwlEntity("hasAddress", "has address"),
            new OwlEntity("physicalAddress", "physical address")));

    var expectedResult = prepareExpectedResult(
        List.of(
            "Own descriptions:\n" +
                "- Legal entity is a kind of superTest1.\n" +
                "- Legal entity has some address that is virtual address.\n" +
                "- Legal entity is recognized in some jurisdiction.\n" +
                "- Legal entity is recognized in some zurisdiction.\n" +
                "- Legal entity may have address that is physical address.",
            "- Legal entity may have address that is physical address."));

    var actualResult = descriptionGenerator.prepareDescriptionString(groupedDetails);

    assertTrue(actualResult.isPresent());
    assertThat(actualResult.get(), equalTo(expectedResult));
  }

  private OwlGroupedDetails prepareGroupedDetails(String label, List<List<String>> rawTaxonomies) {
    var groupedDetails = new OwlGroupedDetails();
    groupedDetails.setLabel(label);
    groupedDetails.setTaxonomy(prepareTaxonomy(rawTaxonomies));
    return groupedDetails;
  }

  private OwlTaxonomyImpl prepareTaxonomy(List<List<String>> rawListOfTaxonomies) {
    var owlTaxonomy = new OwlTaxonomyImpl();

    rawListOfTaxonomies.stream().map(rawTaxonomy ->
        rawTaxonomy.stream()
            .map(elem -> new OwlTaxonomyElementImpl(EXAMPLE_IRI + elem, elem))
            .collect(Collectors.toList())
    ).forEach(owlTaxonomy::addTaxonomy);

    return owlTaxonomy;
  }

  private List<OwlAnnotationPropertyValue> prepareExpectedResult(List<String> rawPropertyValues) {
    return rawPropertyValues
        .stream().map(rawPropertyValue -> {
          var expectedResult = new OwlAnnotationPropertyValue();
          expectedResult.setValue(rawPropertyValue);
          expectedResult.setType(OwlType.STRING);
          return expectedResult;
        })
        .collect(Collectors.toList());
  }

  private OwlAxiomPropertyValue preparePropertyValue(String propertyValue, OwlEntity... entities) {
    var property = new OwlAxiomPropertyValue();
    property.setType(OwlType.AXIOM);
    property.setValue(propertyValue);
    for (int i = 0; i < entities.length; i++) {
      OwlEntity entity = entities[i];
      property.addEntityValues(
          "/arg" + (i + 1) + "/",
          prepareAxiomPropertyEntity(entity.getIriPart(), entity.getLabel()));
    }
    return property;
  }

  private OwlAxiomPropertyEntity prepareAxiomPropertyEntity(String iriPart, String label) {
    var entity = new OwlAxiomPropertyEntity();
    entity.setIri(EXAMPLE_IRI + iriPart);
    entity.setLabel(label);
    return entity;
  }

  static class OwlEntity {

    private final String iriPart;
    private final String label;

    public OwlEntity(String iriPart, String label) {
      this.iriPart = iriPart;
      this.label = label;
    }

    public String getIriPart() {
      return iriPart;
    }

    public String getLabel() {
      return label;
    }
  }
}