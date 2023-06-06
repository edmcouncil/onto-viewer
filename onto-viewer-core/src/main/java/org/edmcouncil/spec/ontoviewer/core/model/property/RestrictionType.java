package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.semanticweb.owlapi.model.ClassExpressionType;

public enum RestrictionType {
  EXISTENTIAL_QUANTIFICATION, // ObjectSomeValuesFrom
  UNIVERSAL_QUANTIFICATION, // ObjectAllValuesFrom
  INDIVIDUAL_VALUE_RESTRICTION, // ObjectHasValue
  SELF_RESTRICTION, // ObjectHasSelf
  OBJECT_MINIMUM_CARDINALITY, // ObjectMinN
  OBJECT_MAXIMUM_CARDINALITY, // ObjectMaxN
  OTHER;

  public static RestrictionType fromOwlClassExpressionType(ClassExpressionType classExpressionType) {
    RestrictionType restrictionType = OTHER;
    switch (classExpressionType) {
      case OBJECT_SOME_VALUES_FROM:
        restrictionType = EXISTENTIAL_QUANTIFICATION;
        break;
      case OBJECT_ALL_VALUES_FROM:
        restrictionType = UNIVERSAL_QUANTIFICATION;
        break;
      case OBJECT_HAS_VALUE:
        restrictionType = SELF_RESTRICTION;
        break;
      case OBJECT_HAS_SELF:
        restrictionType = INDIVIDUAL_VALUE_RESTRICTION;
        break;
      case OBJECT_MIN_CARDINALITY:
        restrictionType = OBJECT_MINIMUM_CARDINALITY;
        break;
      case OBJECT_MAX_CARDINALITY:
        restrictionType = OBJECT_MAXIMUM_CARDINALITY;
    }
    return restrictionType;
  }
}
