package org.edmcouncil.spec.ontoviewer.webapp.configuration;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
  private static final List<String> HEADERS_TO_HIDE = ImmutableList.of("x-api-key", "authorization", "proxy-authorization");
  private static final List<String> PARAMS_TO_HIDE = ImmutableList.of("apikey"); //to compare the names of params we use theLowerCase function

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    try {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      Map<String, String> requestMap = this
          .getTypesafeRequestMap(httpServletRequest);
      Map<String, String> requestMapHeader = this
          .getTypessafeHeaderMap(httpServletRequest);
      BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(
          httpServletRequest);
      BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper(
          httpServletResponse);
      final StringBuilder logMessage = new StringBuilder(
          "REST Request - ").append("[http method:")
          .append(httpServletRequest.getMethod())
          .append("] [path info:")
          .append(httpServletRequest.getServletPath())
          .append("] [request parametrs:").append(prepareStringFromMap(requestMap))
          .append("] [request header:").append(prepareStringFromMap(requestMapHeader))
          .append("] [request body:")
          .append(bufferedRequest.getRequestBody())
          .append("] [remote address:")
          .append(httpServletRequest.getRemoteAddr()).append("]");
      chain.doFilter(bufferedRequest, bufferedResponse);
      logMessage.append(" [response:")
          .append(bufferedResponse.getContent()).append("]");
      log.debug(logMessage.toString());
    } catch (Throwable a) {
      log.error(a.getMessage());
    }
  }

  private String prepareStringFromMap(Map<String, String> input) {
    StringBuilder stringBuilder = new StringBuilder();
    int size = input.size();
    int i = 0;
    for (Map.Entry<String, String> entry : input.entrySet()) {
      stringBuilder.append(entry.getKey())
          .append(":\"")
          .append(entry.getValue());
      if (size == i + 1) {
        stringBuilder.append("\"");
      } else {
        stringBuilder.append("\", ");
      }
      i++;
    }
    return stringBuilder.toString();
  }

  private Map<String, String> getTypesafeRequestMap(HttpServletRequest request) {
    Map<String, String> typesafeRequestMap = new HashMap<String, String>();
    Enumeration<?> requestParamNames = request.getParameterNames();
    while (requestParamNames.hasMoreElements()) {
      String requestParamName = (String) requestParamNames.nextElement();
      String requestParamValue;
      if (PARAMS_TO_HIDE.contains(requestParamName.toLowerCase())) {
        requestParamValue = "********";
      } else {
        requestParamValue = request.getParameter(requestParamName);
      }
      typesafeRequestMap.put(requestParamName, requestParamValue);
    }
    return typesafeRequestMap;
  }

  private Map<String, String> getTypessafeHeaderMap(HttpServletRequest request) {
    Map<String, String> typesafeRequestMap = new HashMap<String, String>();
    Enumeration<?> requestParamNames = request.getHeaderNames();
    while (requestParamNames.hasMoreElements()) {

      String requestParamName = (String) requestParamNames.nextElement();
      String requestParamValue;
      if (HEADERS_TO_HIDE.contains(requestParamName.toLowerCase())) {
        requestParamValue = "********";
      } else {
        requestParamValue = request.getHeader(requestParamName);
      }
      typesafeRequestMap.put(requestParamName, requestParamValue);
    }
    return typesafeRequestMap;
  }

  @Override
  public void destroy() {
  }

  private static final class BufferedRequestWrapper extends
      HttpServletRequestWrapper {

    private ByteArrayInputStream byteArrayInputStream = null;
    private ByteArrayOutputStream byteArrayOutputStream = null;
    private BufferedServletInputStream bufferedServletInputStream = null;
    private byte[] buffer = null;

    public BufferedRequestWrapper(HttpServletRequest httpServletRequest)
        throws IOException {
      super(httpServletRequest);
      InputStream inputStream = httpServletRequest.getInputStream();
      this.byteArrayOutputStream = new ByteArrayOutputStream();
      byte buf[] = new byte[1024];
      int read;
      while ((read = inputStream.read(buf)) > 0) {
        this.byteArrayOutputStream.write(buf, 0, read);
      }
      this.buffer = this.byteArrayOutputStream.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream() {
      this.byteArrayInputStream = new ByteArrayInputStream(this.buffer);
      this.bufferedServletInputStream = new BufferedServletInputStream(this.byteArrayInputStream);
      return this.bufferedServletInputStream;
    }

    String getRequestBody() throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          this.getInputStream()));
      String line = null;
      StringBuilder inputBuffer = new StringBuilder();
      do {
        line = reader.readLine();
        if (null != line) {
          inputBuffer.append(line.trim());
        }
      } while (line != null);
      reader.close();
      return inputBuffer.toString().trim();
    }
  }

  private static final class BufferedServletInputStream extends
      ServletInputStream {

    private ByteArrayInputStream byteArrayInputStream;

    public BufferedServletInputStream(ByteArrayInputStream byteArrayInputStream) {
      this.byteArrayInputStream = byteArrayInputStream;
    }

    @Override
    public int available() {
      return this.byteArrayInputStream.available();
    }

    @Override
    public int read() {
      return this.byteArrayInputStream.read();
    }

    @Override
    public int read(byte[] buffer, int off, int len) {
      return this.byteArrayInputStream.read(buffer, off, len);
    }

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
  }

  public class TeeServletOutputStream extends ServletOutputStream {

    private final TeeOutputStream targetStream;

    public TeeServletOutputStream(OutputStream one, OutputStream two) {
      targetStream = new TeeOutputStream(one, two);
    }

    @Override
    public void write(int arg0) throws IOException {
      this.targetStream.write(arg0);
    }

    @Override
    public void flush() throws IOException {
      super.flush();
      this.targetStream.flush();
    }

    @Override
    public void close() throws IOException {
      super.close();
      this.targetStream.close();
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }
  }

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
}
