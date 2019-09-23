<%@page contentType="text/html" pageEncoding="UTF-8"%>

<ul id="myUL">
  <c:forEach items="${modelTree}" var="domainElement">
    <li>
    <weasel:RenderTree element="${domainElement}" searchPath="*"/>
    </li>
  </c:forEach>
</ul>

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

