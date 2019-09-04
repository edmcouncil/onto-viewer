<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix = "weasel" uri = "tags/propertyRender.tld"%>

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

        <div class="col-md-12 px-2">

          <div class="card">
            <div class="card-body">
              <c:choose>
                <c:when test="${details_display}">
                  <c:choose>
                    <c:when test="${grouped_details}">
                      <jsp:directive.include file="page/elements/viewGrouped.jsp" />
                    </c:when>
                    <c:otherwise>
                      <jsp:directive.include file="page/elements/view.jsp" />
                    </c:otherwise>
                  </c:choose>
                </c:when>
                <c:otherwise>
                  Operation not supported...
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </div>
      </div>
    </div>

    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
