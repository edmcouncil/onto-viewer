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

            <div class="ml-2">
              <label>
                <input type="checkbox" name="edgesFilter" value="internal" checked="true">
                internal
              </label>
              <label>
                <input type="checkbox" name="edgesFilter" value="external" checked="true">
                external
              </label>
            </div>
            <div class="ml-2">
              <label>
                <input type="checkbox" name="edgesFilter" value="optional" checked="true">
                optional
              </label>
              <label>
                <input type="checkbox" name="edgesFilter" value="non_optional" checked="true">
                required
              </label>
            </div>

            <div id="ontograph"></div>

            <script type="text/javascript">
              ${clazz.graph.toJsVars()}
              // create a network
              var container = document.getElementById('ontograph');
              const edgeFilters = document.getElementsByName('edgesFilter')

              const edgesFilterValues = {
                optional: true,
                non_optional: true,
                internal: true,
                external: true
              };

              const edgesFilter = (edge) => {
                return edgesFilterValues[edge.optional] && edgesFilterValues[edge.type];
              };


              edgeFilters.forEach(filter => filter.addEventListener('change', (e) => {
                  const {value, checked} = e.target;
                  edgesFilterValues[value] = checked;
                  edgesView.refresh();

                }));

              const edgesView = new vis.DataView(edges, {filter: edgesFilter});

              var data = {
                nodes: nodes,
                edges: edgesView
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
              if (nodes.length !== 0) {
                startHeight = 500;
              }
              var height = startHeight + 20 * nodes.length;
              var container = document.getElementById('ontograph');
              container.style.height = height + 'px';
              network.redraw();

              network.on("oncontext", function (params) {
                params.event = "[original event]";
                console.log('<h2>oncontext (right click) event:</h2>' + JSON.stringify(params, null, 4));
                console.log('pointer', params.pointer);
                // Avoid the real one
                event.preventDefault();

                // Show contextmenu
                $(".custom-menu").finish().toggle(100).
                        // In the right position (the mouse)
                        css({
                          top: event.pageY -30 + "px",
                          left: event.pageX -30 + "px"
                          //top: event.pageY - 130 + "px",
                          //left: params.pointer.DOM.x + "px"
                        });
              });
            </script>
          </div>

        </div>

      </div>
    </div>
    <div class='custom-menu'>
      <ul>
        
        <li data-action = "first">Show info</li>
      </ul>
    </div>
    <script type="text/javascript">
      $(".custom-menu").bind("mousedown", function (e) {
        console.log("mouse down");
        // If the clicked element is not the menu
        if (!$(e.target).parents(".custom-menu").length > 0) {
          // Hide it
          $(".custom-menu").hide(100);
        }
      });
      $(".custom-menu").bind("mouseleave", function (e) {
        console.log("mouse leave");
        $(".custom-menu").hide(100);
        // If the clicked element is not the menu
        /*if (!$(e.target).parents(".custom-menu").length > 0) {
         // Hide it
         
         }*/
      });
    </script>
    <jsp:directive.include file="page/elements/footer.jsp" />
  </body>

</html>
