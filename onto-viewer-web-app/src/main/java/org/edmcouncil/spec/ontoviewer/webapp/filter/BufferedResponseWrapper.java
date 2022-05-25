package org.edmcouncil.spec.ontoviewer.webapp.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class BufferedResponseWrapper implements HttpServletResponse {

  HttpServletResponse httpServletResponse;
  TeeServletOutputStream teeServletOutputStream;
  ByteArrayOutputStream byteArrayOutputStream;

  public BufferedResponseWrapper(HttpServletResponse response) {
    httpServletResponse = response;
  }

  public String getContent() {
    if (byteArrayOutputStream == null) {
      return "";
    }
    return byteArrayOutputStream.toString();
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return httpServletResponse.getWriter();
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (teeServletOutputStream == null) {
      byteArrayOutputStream = new ByteArrayOutputStream();
      teeServletOutputStream = new TeeServletOutputStream(httpServletResponse.getOutputStream(),
          byteArrayOutputStream);
    }
    return teeServletOutputStream;
  }

  @Override
  public String getCharacterEncoding() {
    return httpServletResponse.getCharacterEncoding();
  }

  @Override
  public String getContentType() {
    return httpServletResponse.getContentType();
  }

  @Override
  public void setCharacterEncoding(String charset) {
    httpServletResponse.setCharacterEncoding(charset);
  }

  @Override
  public void setContentLength(int len) {
    httpServletResponse.setContentLength(len);
  }

  @Override
  public void setContentLengthLong(long l) {
    httpServletResponse.setContentLengthLong(l);
  }

  @Override
  public void setContentType(String type) {
    httpServletResponse.setContentType(type);
  }

  @Override
  public void setBufferSize(int size) {
    httpServletResponse.setBufferSize(size);
  }

  @Override
  public int getBufferSize() {
    return httpServletResponse.getBufferSize();
  }

  @Override
  public void flushBuffer() throws IOException {
    teeServletOutputStream.flush();
  }

  @Override
  public void resetBuffer() {
    httpServletResponse.resetBuffer();
  }

  @Override
  public boolean isCommitted() {
    return httpServletResponse.isCommitted();
  }

  @Override
  public void reset() {
    httpServletResponse.reset();
  }

  @Override
  public void setLocale(Locale loc) {
    httpServletResponse.setLocale(loc);
  }

  @Override
  public Locale getLocale() {
    return httpServletResponse.getLocale();
  }

  @Override
  public void addCookie(Cookie cookie) {
    httpServletResponse.addCookie(cookie);
  }

  @Override
  public boolean containsHeader(String name) {
    return httpServletResponse.containsHeader(name);
  }

  @Override
  public String encodeURL(String url) {
    return httpServletResponse.encodeURL(url);
  }

  @Override
  public String encodeRedirectURL(String url) {
    return httpServletResponse.encodeRedirectURL(url);
  }

  @Override
  public String encodeUrl(String url) {
    return httpServletResponse.encodeURL(url);
  }

  @Override
  public String encodeRedirectUrl(String url) {
    return httpServletResponse.encodeRedirectURL(url);
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    httpServletResponse.sendError(sc, msg);
  }

  @Override
  public void sendError(int sc) throws IOException {
    httpServletResponse.sendError(sc);
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    httpServletResponse.sendRedirect(location);
  }

  @Override
  public void setDateHeader(String name, long date) {
    httpServletResponse.setDateHeader(name, date);
  }

  @Override
  public void addDateHeader(String name, long date) {
    httpServletResponse.addDateHeader(name, date);
  }

  @Override
  public void setHeader(String name, String value) {
    httpServletResponse.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    httpServletResponse.addHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    httpServletResponse.setIntHeader(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    httpServletResponse.addIntHeader(name, value);
  }

  @Override
  public void setStatus(int sc) {
    httpServletResponse.setStatus(sc);
  }

  @Override
  public void setStatus(int sc, String sm) {
    httpServletResponse.setStatus(sc);
  }

  @Override
  public String getHeader(String arg0) {
    return httpServletResponse.getHeader(arg0);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return httpServletResponse.getHeaderNames();
  }

  @Override
  public Collection<String> getHeaders(String arg0) {
    return httpServletResponse.getHeaders(arg0);
  }

  @Override
  public int getStatus() {
    return httpServletResponse.getStatus();
  }
}
