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
        <h2 class="my-3 ml-2">Search results:</h2>
        <c:forEach items="${result.result}" var="resEl">
          <div class="ml-5">
            <div class="row">
              <span ><h5> <a class="font-weight-bold" href="search?query=${resEl.iri}">${resEl.label}</a> </h5></span>
            </div>
            <div class="row">
              <span> <a class="text-secondary" href="search?query=${resEl.iri}">${resEl.iri}</a> </span>
            </div>
            <div class="row">
              <span> ${resEl.description} </span>
            </div>
            <div class="border-bottom col-12 mt-1 mb-2 ml-0 mr-0"></div>
          </div>
        </c:forEach>

      </div>

    </div>

  </div>
</div>