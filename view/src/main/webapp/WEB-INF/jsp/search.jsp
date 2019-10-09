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

    <c:set var = "clazz" value = "${details}"/>

    <div class="container-fluid">
      <div class="row px-4 py-4">
        <div class="col-md-4 px-2">
          <div class="card">
            <div class="card-body">
              <jsp:directive.include file="page/elements/moduleTree.jsp" />
            </div>
          </div>
        </div>

        <div class="col-md-8 px-2">

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

            <div id="ontograph"></div>
            <script type="text/javascript">
              ${clazz.jsGraphVars}
              // create a network
              var container = document.getElementById('ontograph');
              var data = {
                nodes: nodes,
                edges: edges
              };
              var options = {
                "edges": {
                  "smooth": {
                    "type": "cubicBezier",
                    "forceDirection": "none",
                    "roundness": 0.15
                  }
                },
                "physics": {
                  "forceAtlas2Based": {
                    "gravitationalConstant": -89,
                    "centralGravity": 0.005,
                    "springLength": 200,
                    "springConstant": 0.415
                  },
                  "minVelocity": 0.75,
                  "solver": "forceAtlas2Based"
                }
              }
              var network = new vis.Network(container, data, options);
              var startHeight = 0;
              if(nodes.length!==0){
                startHeight = 500;
              } 
              var height = startHeight + 15 * nodes.length;
              var container = document.getElementById('ontograph');
              container.style.height = height + 'px';
              network.redraw();
            </script>
          </div>

        </div>

      </div>
    </div>

    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
