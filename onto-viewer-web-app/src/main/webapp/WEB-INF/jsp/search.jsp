<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix = "weasel" uri = "tags/propertyRender.tld"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<c:choose>
    <c:when test="${empty pageContext.request.contextPath}">
       <c:set var = "context" value = "*"/>
    </c:when>
    <c:otherwise>
        <c:set var = "context" value = "${pageContext.request.contextPath}"/>
    </c:otherwise>
</c:choose>
<!DOCTYPE HTML>
<html>
  <head>
    <jsp:directive.include file="page/elements/head.jsp" />
  </head>
  <body>
    <jsp:directive.include file="page/elements/header.jsp" />

    <button onclick="topFunction()" id="goToTopBtn" title="Go to top">Top</button>

    <c:set var = "detailsString" value = "details"/>
    <c:choose>
      <c:when test="${result.type.name() eq detailsString}">
        <c:set var = "clazz" value = "${result.result}"/>
        <jsp:directive.include file="page/elements/detailsContent.jsp" />
      </c:when>
      <c:otherwise>
        <jsp:directive.include file="page/elements/listContent.jsp" />
      </c:otherwise>
    </c:choose>


    <!--
        <ul class='custom-menu'>
          <li data-action = "goto">Show info</li>
        </ul>
        <script type="text/javascript">
    
          $(".custom-menu").bind("mouseleave", function (e) {
            console.log("mouse leave");
            $(".custom-menu").hide(100);
          });
          $(".custom-menu li").click(function () {
    
            switch ($(this).attr("data-action")) {
    
              case "goto":
                $iri = localStorage.getItem("selectElementIri");
                window.location.href = "/search?query=" + $iri;
                break;
            }
            $(".custom-menu").hide(100);
          });
        </script>
    -->
    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
