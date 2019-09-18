package org.edmcouncil.spec.fibo.view.util.custom.jsp.tag;

import java.io.IOException;
import java.util.List;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import org.edmcouncil.spec.fibo.weasel.model.FiboModule;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TreeRenderTag extends SimpleTagSupport {

  private static final String DEFAULT_WRAPPER = "span";
  private static final String URL_PATTERN = "<a href=\"%s\">%s</a>";
  private static final String URL_SEARCH_QUERY_PATTERN = "<a href=\"%s/search?query=%s\">%s</a>";
  private static final String WRAPPER_PATTERN = "<%1$s> %2$s </%1$s>";
  private static final String SPAN_WRAPPER_CARET_PATTERN = "<span class=\"caret\">%s</span>";
  private static final String SPAN_WRAPPER_CLEAN_PATTERN = "<span>%s</span>";

  private String elementWrapper;
  private String searchPath;
  private FiboModule element;

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

  public FiboModule getElement() {
    return element;
  }

  public void setElement(FiboModule element) {
    this.element = element;
  }

  private void renderElement(String toRender) throws IOException {
    JspWriter out = getJspContext().getOut();
    out.println(toRender);
  }

  private void renderTreeElement(FiboModule property) throws IOException {
    String link = null;
    String val = null;
    link = property.getIri();
    val = property.getLabel();
    String result = wrapToLink(link, "(Show meta)");
    renderElement("<li>");
    List<FiboModule> fmList = property.getSubModule();
    String text = fmList != null && fmList.size() > 0 ? wrapSpanCaret(val) : wrapSpanClean(val);
    renderElement(text);
    renderElement(result);
    if (fmList != null && fmList.size() > 0) {
      renderElement("<ul class=\"nested\">");
      for (FiboModule fiboModule : fmList) {
        renderTreeElement(fiboModule);
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
    result = String.format(URL_SEARCH_QUERY_PATTERN, searchPath, link, val);
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

  private String wrapSpanClean(String result) {
    result = String.format(SPAN_WRAPPER_CLEAN_PATTERN, result);
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
