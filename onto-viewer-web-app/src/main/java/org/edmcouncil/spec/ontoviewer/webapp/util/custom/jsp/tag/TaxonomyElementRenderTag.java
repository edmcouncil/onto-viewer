package org.edmcouncil.spec.ontoviewer.webapp.util.custom.jsp.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyElementImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TaxonomyElementRenderTag extends SimpleTagSupport {

  private static final String DEFAULT_WRAPPER = "span";
  private static final String URL_PATTERN = "<a href=\"%s\">%s</a>";
  private static final String URL_SEARCH_QUERY_PATTERN = "<a href=\"%s/entity?iri=%s\">%s</a>";
  private static final String WRAPPER_PATTERN = "<%1$s> %2$s </%1$s>";

  private String elementWrapper;
  private String searchPath;
  private OwlTaxonomyElementImpl element;

  public void setElementWrapper(String elementWrapper) {
    this.elementWrapper = elementWrapper;
  }

  public void setElement(OwlTaxonomyElementImpl element) {
    this.element = element;
  }

  public void setSearchPath(String searchPath) {
    this.searchPath = searchPath;
  }

  @Override
  public void doTag()
      throws JspException, IOException {

    renderTaxonomyElement(element);

  }

  private void renderElement(String toRender) throws IOException {
    JspWriter out = getJspContext().getOut();
    out.println(toRender);
  }

  private void renderTaxonomyElement(OwlTaxonomyElementImpl property) throws IOException {
    String link = property.getIri();
    String val = property.getLabel();

    String result = wrapToLink(link, val);

    renderElement(result);
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
    return result.replaceAll("#", "%23");
  }

  private String parseLink(String link, String val) {
    String result;
    result = String.format(URL_PATTERN, link, val);
    return result.replaceAll("#", "%23");
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

}
