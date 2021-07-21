<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>     


  <div class="row">
    <h3><b class="">Stats for loaded ontologies:</b></h3>
  </div>
  <div class="border-bottom col-12 mt-1 mb-2 ml-0 mr-0"></div>

  <c:forEach var="prop" items="${stats.stats}">
    <div class="row">
      <h4><b class="col-12 px-0">${stats.labels[prop.key]}: </b>${prop.value}</h4>
    </div>
    
  </c:forEach>

  <c:if test="${empty stats.stats}">
    There is nothing to display...
  </c:if>
