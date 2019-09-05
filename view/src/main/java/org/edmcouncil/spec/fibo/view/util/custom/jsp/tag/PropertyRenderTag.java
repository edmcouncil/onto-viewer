package org.edmcouncil.spec.fibo.view.util.custom.jsp.tag;

import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAxiomPropertyValue;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.edmcouncil.spec.fibo.config.configuration.model.PairImpl;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlListElementIndividualProperty;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class PropertyRenderTag extends SimpleTagSupport {

  private static final String DEFAULT_WRAPPER = "span";
  private static final String URL_PATTERN = "<a href=\"%s\">%s</a>";
  private static final String URL_SEARCH_QUERY_PATTERN = "<a href=\"%s/search?query=%s\">%s</a>";
  private static final String WRAPPER_PATTERN = "<%1$s> %2$s </%1$s>";

  private String elementWrapper;
  private String searchPath;
  private PropertyValue property;

  public void setElementWrapper(String elementWrapper) {
    this.elementWrapper = elementWrapper;
  }

  public void setProperty(PropertyValue property) {
    this.property = property;
  }

  public void setSearchPath(String searchPath) {
    this.searchPath = searchPath;
  }

  @Override
  public void doTag()
      throws JspException, IOException {

    
    switch (property.getType()) {
      case STRING:
        renderStringProperty(property);
        break;
      case IRI:
        renderIriProperty(property);
        break;
      case ANY_URI:
        renderAnyUri(property);
        break;
      case AXIOM:
        renderAxiom(property);
        break;
      case OTHER:
        renderStringProperty(property);
        break;
      case DIRECT_SUBCLASSES:
        renderDirectedSubclasses(property);
        break;
      case INSTANCES: 
        renderInstances(property);
        break;
    }

  }

  private void renderProperty(String toRender) throws IOException {
    JspWriter out = getJspContext().getOut();
    out.println(toRender);
  }

  private void renderStringProperty(PropertyValue property) throws IOException {
    String val = (String) property.getValue();
    String result = wrapString(val);
    renderProperty(result);
  }

  private void renderIriProperty(PropertyValue property) throws IOException {
    String result = wrapIri((String) property.getValue());
    renderProperty(result);
  }

  private void renderAnyUri(PropertyValue property) throws IOException {
    OwlAnnotationPropertyValue val = (OwlAnnotationPropertyValue) property;
    String result = parseAnyUri(val);
    renderProperty(result);
  }

  private void renderAxiom(PropertyValue property) throws IOException {
    OwlAxiomPropertyValue axiomPropertyVal = (OwlAxiomPropertyValue) property;
    String result = axiomPropertyVal.getValue();
    for (Map.Entry<String, String> entry : axiomPropertyVal.getEntityMaping().entrySet()) {
      String replecment = parseIriWithoutWraping(entry.getValue(), entry.getKey());
      String regex = String.format("\\b%s\\b", entry.getKey());
      result = Pattern.compile(regex).matcher(result).replaceAll(replecment);
    }
    if (elementWrapper != null && !elementWrapper.isEmpty()) {
      result = wrapElement(result);
    }
    renderProperty(result);
  }

  private String wrapString(String val) {
    if (elementWrapper != null && !elementWrapper.isEmpty()) {
      val = wrapElement(val);
    }
    return val;
  }

  private String wrapIri(String link) {
    String result = "";
    if (searchPath.isEmpty()) {
      result = parseLink(link, link);
    } else {
      result = parseSearchPath(link, link);
    }
    if (elementWrapper != null && !elementWrapper.isEmpty()) {
      result = wrapElement(result);
    }
    return result;
  }

  private String wrapIri(String link, String val) {
    String result = "";
    if (searchPath.isEmpty()) {
      result = parseLink(link, val);
    } else {
      result = parseSearchPath(link, val);
    }
    if (elementWrapper != null && !elementWrapper.isEmpty()) {
      result = wrapElement(result);
    }
    return result;
  }

  private String parseSearchPath(String val) {
    return parseSearchPath(val, val);
  }

  private String parseSearchPath(String link, String val) {
    String result;
    result = String.format(URL_SEARCH_QUERY_PATTERN, searchPath, link, val);
    return result;
  }

  private String parseLink(String val) {
    return parseLink(val, val);
  }

  private String parseLink(String link, String val) {
    String result;
    result = String.format(URL_PATTERN, link, val);
    return result;
  }

  private String parseAnyUri(OwlAnnotationPropertyValue val) {
    String result = val.getValue();
    result = String.format(URL_PATTERN, val.getValue(), val.getValue());
    if (elementWrapper != null && !elementWrapper.isEmpty()) {
      result = wrapElement(result);
    }
    return result;
  }

  private String wrapElement(String result) {
    result = String.format(WRAPPER_PATTERN, elementWrapper, result);
    return result;
  }

  private String parseIriWithoutWraping(String link, String val) {
    String result = "";
    if (searchPath.isEmpty()) {
      result = parseLink(link, val);
    } else {
      result = parseSearchPath(link, val);
    }

    return result;
  }

  private void renderDirectedSubclasses(PropertyValue property) throws IOException {
    OwlDirectedSubClassesProperty subclassProperty = (OwlDirectedSubClassesProperty) property;
    PairImpl value = subclassProperty.getValue();
    String link = wrapIri((String)value.getValueB(), (String)value.getValueA());
    renderProperty(link);
  }
  private void renderInstances(PropertyValue property) throws IOException {
    OwlListElementIndividualProperty instanceProperty = (OwlListElementIndividualProperty) property;
    PairImpl value = instanceProperty.getValue();
    String link = wrapIri((String)value.getValueB(), (String)value.getValueA());
    renderProperty(link);
  }

}
