<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix = "weasel" uri = "tags/propertyRender.tld"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 
<%@ taglib prefix="templateTree" tagdir="/WEB-INF/tags" %> 

<!DOCTYPE HTML>
<html>

  <head>
    <jsp:directive.include file="page/elements/head.jsp" />
  </head>
  <body>
    <jsp:directive.include file="page/elements/header.jsp" />

    <button onclick="topFunction()" id="goToTopBtn" title="Go to top">Top</button>

    <div class="container-fluid">
      <div class="row px-4 py-4">

        <div class="col-md-6 px-2">

          <div class="card">
            <div class="card-body">
              <ul id="myUL">
                <c:forEach items="${modelTree}" var="domainElement">
                  <li>
                    <span class="caret">${domainElement.label}</span>
                    <a href="/module?meta=${domainElement.iri}&lvl1=${domainElement.label}" class="btn btn-link" >(Show meta)</a>
                    <c:if test="${not empty domainElement.subModule}">
                      <c:choose>
                        <c:when test="${not empty lvl1 && lvl1 eq domainElement.label}">
                          <ul class="nested active">
                          </c:when>
                          <c:otherwise>
                            <ul class="nested">
                            </c:otherwise>
                          </c:choose> 
                          <c:forEach items="${domainElement.subModule}" var="moduleElement">
                            <li>
                              <span class="caret">${moduleElement.label}</span>
                              <a href="/module?meta=${moduleElement.iri}&lvl1=${domainElement.label}&lvl2=${moduleElement.label}" class="btn btn-link" >(Show meta)</a>
                              <c:if test="${not empty moduleElement.subModule}">
                                <c:choose>
                                  <c:when test="${not empty lvl2 && lvl2 eq moduleElement.label}">
                                    <ul class="nested active">
                                    </c:when>
                                    <c:otherwise>
                                      <ul class="nested">
                                      </c:otherwise>
                                    </c:choose> 
                                    <c:forEach items="${moduleElement.subModule}" var="ontoElement">
                                      <li>${ontoElement.label} 
                                        <a href="/module?lvl1=${domainElement.label}&lvl2=${moduleElement.label}&meta=${ontoElement.iri}" class="btn btn-link" >(Show meta)</a>
                                      </li>
                                    </c:forEach>
                                  </ul>
                                </c:if>
                            </li>  
                          </c:forEach>
                        </ul>
                      </c:if>

                  </li>

                </c:forEach>
              </ul>

              <ul id="myUL">
                <c:forEach items="${modelTree}" var="domainElement">
                  <li>
                    <weasel:RenderTree element="${domainElement}" searchPath="*"/>
                  </li>
                </c:forEach>
              </ul>

            </div>
          </div>
        </div>
        <div class="col-md-6 px-2">
          <div class="card">
            <div class="card-body">
              <c:if test="${grouped_details}">
                <jsp:directive.include file="page/elements/viewGrouped.jsp" />
              </c:if>
            </div>
          </div>
        </div>
      </div>
    </div>

    <script>
      //move this script to separate file
      var toggler = document.getElementsByClassName("caret");
      var i;

      for (i = 0; i < toggler.length; i++) {
        toggler[i].addEventListener("click", function () {
          this.parentElement.querySelector(".nested").classList.toggle("active");
          this.classList.toggle("caret-down");
        });
      }
    </script>

    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
