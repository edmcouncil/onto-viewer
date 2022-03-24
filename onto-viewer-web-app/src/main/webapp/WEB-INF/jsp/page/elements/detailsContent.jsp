


<div class="container-fluid">
    <div class="row px-3 py-3">
        <div class="col-md-3 px-2" style="height: 100%">
            <div class="mt-5 ml-2">
                <div class="" style="height: 100%">
                    <jsp:directive.include file="moduleTree.jsp" />
                </div>
            </div>
        </div>
        <div class="col-md-9 px-2" style="height: 100%">

            <div class="card">
                <div class="card-body" style="height: 100%">
                    <c:choose>
                        <c:when test="${details_display}">
                            <c:choose>
                                <c:when test="${grouped_details}">
                                    <jsp:directive.include file="viewGrouped.jsp" />
                                </c:when>
                                <c:otherwise>
                                    <jsp:directive.include file="view.jsp" />
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            Operation not supported...
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${not empty clazz.graph}">
                    <div class="row">
                        <h5> <b class="mr-1 ml-4">Data model for "<i>${clazz.label}</i>"</b></h5> 
                        <button type="button" class="btn btn-link" onclick="swapGraphContent()">
                            expand...
                        </button>
                    </div>
                </c:if>
                <div id="graphWindowWraper">
                    <div id="graphWindow">
                        <c:if test="${not empty clazz.graph}">
                            <b class="mr-1 ml-4">Connections: </b> <br />
                            <div class="ml-4">
                                <label>
                                    <input type="checkbox" name="edgesFilter" value="internal" checked="true">
                                    class specific
                                </label>
                                <label>
                                    <input type="checkbox" name="edgesFilter" value="external" checked="true">
                                    inherited
                                </label>
                            </div>
                            <div class="ml-4">
                                <label>
                                    <input type="checkbox" name="edgesFilter" value="optional" checked="true">
                                    optional
                                </label>
                                <label>
                                    <input type="checkbox" name="edgesFilter" value="non_optional" checked="true">
                                    required
                                </label>
                            </div>


                        </c:if>

                        <div id="ontograph"></div>

                    </div>
                </div>



                <div class="modal fade down" id="graphModal" tabindex="-1" role="dialog" aria-labelledby="graphModalLabel" aria-hidden="true">
                    <div class="modal-dialog-full-width modal-dialog momodel modal-fluid" role="document">
                        <div class="modal-content-full-width modal-content">
                            <div class="modal-header-full-width   modal-header">
                                <h5 class="modal-title w-100" id="graphModalLabel"><b class="mr-1 ml-4">Data model for "<i>${clazz.label}</i>"</b></h5>
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close" onclick="swapGraphContent()">
                                    <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                            <div id="graphModalBody" class="modal-body">

                            </div>
                        </div>
                    </div>
                </div>


                <script type="text/javascript">
                    var nodes = new vis.DataSet(${clazz.graph.convertJsonNodes()});
                    var edges = new vis.DataSet(${clazz.graph.convertJsonEdges()});
                    // create a network
                    var container = document.getElementById('ontograph');
                    const edgeFilters = document.getElementsByName('edgesFilter');

                    const edgesFilterValues = {
                        optional: true,
                        non_optional: true,
                        internal: true,
                        external: true

                    };
                    const edgesFilter = (edge) => {

                        return edgesFilterValues[edge.optional] && edgesFilterValues[edge.type];

                    };
                    edgeFilters.forEach(filter => function (e) {
                            filter.checked = "checked";
                        });

                    edgeFilters.forEach(filter => filter.addEventListener('change', (e) => {
                            const {value, checked} = e.target;
                            edgesFilterValues[value] = checked;
                            edgesView.refresh();
                        }));
                    const edgesView = new vis.DataView(edges, {filter: edgesFilter});
                  
                  
                    var nodeView = new vis.DataView(nodes, {
                      filter: function (node) {
                        connEdges = edgesView.get({
                          filter: function (edge) {
                            return(
                                    (edge.to === node.id) || (edge.from === node.id));
                          }});

                        return connEdges.length > 0 || node.id === 1;
                      }
                    });

                   var data = {
                      nodes: nodeView,
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
                                "gravitationalConstant": -95,
                                "centralGravity": 0.005,
                                "springLength": 200,
                                "springConstant": 0.415
                            },
                            "minVelocity": 0.75,
                            "solver": "forceAtlas2Based"
                        }
                    };
                    var network = new vis.Network(container, data, options);
                    var startHeight = 0;
                    if (nodes.length !== 0) {
                        startHeight = 500;
                    }
                    if (nodes.length > 0 && nodes.length < 3) {
                        startHeight = 250;
                    }
                    var height = startHeight + 20 * nodes.length;

                    var container = document.getElementById('ontograph');

                    container.style.height = height + 'px';

                    if (nodes.length > 0 && nodes.length < 3) {
                        container.style.width = 400 + 'px';
                    }
                    network.redraw();

                    network.on("doubleClick", function (params) {
                        params.event = "[original event]";
                        console.log('<h2>oncontext (right click) event:</h2>' + JSON.stringify(params, null, 4));
                        var selectedNodes = params.nodes;
                        var selectedEdges = params.edges;
                        console.log(selectedNodes);
                        console.log(selectedEdges);

                        if (selectedNodes[0] !== undefined) {
                            var sNode = selectedNodes[0];
                            console.log(sNode);
                            nodes.forEach(function (entry) {
                                if (entry.id === sNode) {
                                    window.location.href = "${pageContext.request.contextPath}/entity?iri=" + entry.iri;
                                    //localStorage.setItem("selectElementIri", );
                                }
                            });
                        } else if (selectedEdges[0] !== undefined) {
                            var sEgde = selectedEdges[0];
                            console.log(sEgde);
                            edgesView.forEach(function (entry) {
                                if (entry.id === sEgde) {
                                    window.location.href = "${pageContext.request.contextPath}/entity?iri=" + entry.iri;
                                }
                            });
                        }
                        //show menu on right click on graph
                        /*event.preventDefault();
                         $(".custom-menu").finish().toggle(100).
                         css({
                         top: event.pageY - 20 + "px",
                         left: event.pageX - 20 + "px"
                         });*/
                    });


                    function swapGraphContent() {
                        if (document.getElementById('graphWindowWraper').hasChildNodes()) {
                            document.getElementById('graphModalBody').appendChild(
                                    document.getElementById('graphWindow'))

                            var options2 = {
                                "edges": {
                                    "smooth": {
                                        "type": "cubicBezier",
                                        "forceDirection": "none",
                                        "roundness": 0.15
                                    }

                                },
                                "physics": {
                                    "forceAtlas2Based": {
                                        "gravitationalConstant": -95,
                                        "centralGravity": 0.005,
                                        "springLength": 400,
                                        "springConstant": 0.410
                                    },
                                    "minVelocity": 0.75,
                                    "solver": "forceAtlas2Based"
                                }
                            };

                            network.setOptions(options2)

                            $('#graphModal').modal('show');
                        } else {
                            $('#graphModal').modal('hide');
                        }
                    }

                    $('#graphModal').on('hidden.bs.modal', function (e) {
                        document.getElementById('graphWindowWraper').appendChild(
                                document.getElementById('graphWindow'));
                        network.setOptions(options)
                    })

                </script>
            </div>
        </div>
    </div>
</div>
