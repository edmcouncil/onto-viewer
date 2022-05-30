package org.edmcouncil.spec.ontoviewer.webapp.util.custom.jsp.tag;

import java.io.IOException;
import java.util.List;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TreeRenderTag extends SimpleTagSupport {

  private static final String DEFAULT_WRAPPER = "span";
  private static final String URL_PATTERN = "<a href=\"%s\">%s</a>";
  private static final String URL_SEARCH_QUERY_PATTERN = "<a href=\"%s/entity?iri=%s\">%s</a>";
  private static final String WRAPPER_PATTERN = "<%1$s> %2$s </%1$s>";
  private static final String SPAN_WRAPPER_CARET_PATTERN = "<span class=\"caret\">%s</span>";
  private static final String SPAN_WRAPPER_CARET_DOWN_PATTERN = "<span class=\"caret caret-down font-weight-bold\">%s</span>";
  private static final String SPAN_WRAPPER_CLEAN_PATTERN = "<span class=\"ml-4\">%s</span>";
  private static final String SPAN_WRAPPER_CLEAN_MATCH_PATTERN = "<span class=\"font-weight-bold\">%s</span>";
  private static final String UL_NESTED = "<ul class=\"nested\">";
  private static final String UL_NESTED_ACTIVE = "<ul class=\"nested active\">";
  private static final String MATURITY_INDICATOR_PATTERN = "<i class=\"%sIndicator\"></i>";

  private String elementWrapper;
  private String searchPath;
  private OntologyModule element;
  private List<String> elementLocation;
  private String contextPath;


  @Override
  public void doTag()
      throws JspException, IOException {

    renderTreeElement(element);

  }

  public String getElementWrapper() {
    return elementWrapper;
  }

  public void setElementWrapper(String elementWrapper) {
    this.elementWrapper = elementWrapper;
  }

  public String getSearchPath() {
    return searchPath;
  }

  public void setSearchPath(String searchPath) {
    this.searchPath = searchPath;
  }

  public OntologyModule getElement() {
    return element;
  }

  public void setElement(OntologyModule element) {
    this.element = element;
  }

  public List<String> getElementLocation() {
    return elementLocation;
  }

  public void setElementLocation(List<String> elementLocation) {
    this.elementLocation = elementLocation;
  }

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }
  
  private void renderElement(String toRender) throws IOException {
    JspWriter out = getJspContext().getOut();
    out.println(toRender);
  }

  private void renderTreeElement(OntologyModule property) throws IOException {
    String link = null;
    String val = null;
    String emptyDisplayedVal = "";
    link = property.getIri();
    val = property.getLabel();
    String result = wrapToLink(link, val);//"<i class='fas fa-file-export'></i>");
    renderElement("<li>");
    List<OntologyModule> fmList = property.getSubModule();
    String text = null;
    if (fmList != null && fmList.size() > 0) {
      if (elementLocation != null && elementLocation.contains(link)) {
        text = wrapSpanCaretDown(emptyDisplayedVal);
        result = String.format(SPAN_WRAPPER_CLEAN_MATCH_PATTERN, result);
      } else {
        text = wrapSpanCaret(emptyDisplayedVal);
      }
    } else {
      if (elementLocation != null && elementLocation.contains(link)) {
        text = wrapSpanCleanMatch(emptyDisplayedVal);
        result = String.format(SPAN_WRAPPER_CLEAN_MATCH_PATTERN, result);
      } else {
        text = wrapSpanClean(emptyDisplayedVal);
      }
    }
    renderElement(text);
    renderElement(String.format(MATURITY_INDICATOR_PATTERN, property.getMaturityLevel().getLabel()));
    renderElement(result);

    if (fmList != null && fmList.size() > 0) {

      if (elementLocation != null && elementLocation.contains(link)) {
        renderElement(UL_NESTED_ACTIVE);
      } else {
        renderElement(UL_NESTED);
      }
      for (OntologyModule ontologyModule : fmList) {
        renderTreeElement(ontologyModule);
      }
      renderElement("</ul>");
    }
    renderElement("</li>");
  }

  private String wrapToLink(String link, String val) {
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

  private String parseSearchPath(String link, String val) {
    String result;
    String tmpSearchPath = searchPath.equals("*") ? "" : searchPath;
    result = String.format(URL_SEARCH_QUERY_PATTERN, tmpSearchPath, link, val);
    return result;
  }

  private String parseLink(String link, String val) {
    String result;
    result = String.format(URL_PATTERN, link, val);
    return result;
  }

  private String wrapElement(String result) {
    result = String.format(WRAPPER_PATTERN, elementWrapper, result);
    return result;
  }

  private String wrapSpanCaret(String result) {
    result = String.format(SPAN_WRAPPER_CARET_PATTERN, result);
    return result;
  }

  private String wrapSpanCaretDown(String result) {
    result = String.format(SPAN_WRAPPER_CARET_DOWN_PATTERN, result);
    return result;
  }

  private String wrapSpanClean(String result) {
    result = String.format(SPAN_WRAPPER_CLEAN_PATTERN, result);
    return result;
  }

  private String wrapSpanCleanMatch(String result) {
    result = String.format(SPAN_WRAPPER_CLEAN_PATTERN, result);
    //result = String.format(SPAN_WRAPPER_CLEAN_MATCH_PATTERN, result);
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

}
