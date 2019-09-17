<%-- 
    Document   : nodeTree
    Author     : Michał Daniel (michal.daniel@makolab.com)
--%>

<%@tag description="Display the whole nodeTree" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="node" type="org.edmcouncil.spec.fibo.weasel.model.FiboModule" required="true" %>

<%@taglib prefix="template" tagdir="/WEB-INF/tags" %>

<%-- any content can be specified here e.g.: --%>
<li>${node.label}
<c:if test="${not empty node.subModule}">
  <b>not empty</b>
    <ul>
    <c:forEach var="child" items="${node.subModule}">
        <template:nodeTree node="${child}"/>
    </c:forEach>
    </ul>
</c:if>
</li>