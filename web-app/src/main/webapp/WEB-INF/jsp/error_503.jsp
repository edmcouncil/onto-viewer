<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix = "weasel" uri = "tags/propertyRender.tld"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML>
<html>

  <head>
    <jsp:directive.include file="page/elements/head.jsp" />
  </head>
  <body>
    <jsp:directive.include file="page/elements/header.jsp" />

    <button onclick="topFunction()" id="goToTopBtn" title="Go to top">Top</button>


    <div class="row">
      <div class="my-3 px-3 col-3">
      </div>            
      <div class="my-5 px-3">
        <div class="row">
          <b><h4>503 Service Unavailable</h4></b>
        </div>
        <div class="row">
          <h5>The application is currently being initialized. Please wait a few minutes</h5>
        </div>
        <div class="row">
          <a href="${pageContext.request.contextPath}/index">Go to home page</a>
        </div>
      </div>            
    </div>
    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
