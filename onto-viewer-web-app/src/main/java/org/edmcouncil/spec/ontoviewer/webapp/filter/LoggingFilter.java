package org.edmcouncil.spec.ontoviewer.webapp.filter;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
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
}
