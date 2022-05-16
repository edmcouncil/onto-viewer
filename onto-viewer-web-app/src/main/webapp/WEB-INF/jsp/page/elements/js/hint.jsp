
<script type="text/javascript">

function showHint(event) {
  var searchResults = event.target.value;

  if (searchResults.length < 1) {
    $("#autocomplete").removeClass("show");
    return;
  }
  if (event.key === "ArrowDown") {
    changeFocusToAutocomplete();
    return;
  }
  if (event.key === "Escape") {
    $("#autocomplete").removeClass("show");
    return;
  }
  if (event.key === "Spacebar" || event.key === ' ') {
    $("#search-query").val(searchResults + " ");
  }

  autocomplete();
}

function autocomplete() {
  $text = $("#search-query").val();
  $path = '${pageContext.request.contextPath}/api/hint';
  $.ajax({
    url: $path,
    dataType: 'json',
    accepts: {
      json: 'application/json'
    },
    data: $text,
    contentType: 'text/plain',
    type: 'POST'
  })
    .done(function (results) {
      console.log(results);
      showAutocompletes(results);
    })
    .fail(function (response) {
      console.log("cannot load autocomplete from server, " + response);
      $("#autocomplete").removeClass("show");
    });
}

function showAutocompletes(autocomplates) {
  document.getElementById("autocomplete").innerHTML = "";
  $text = $("#search-query").val();
  $inner = "";
  //<a class="dropdown-item" href="#">Action</a>
  var count = 0;
  $.each(autocomplates, function (index, object) {
  $inner += "<a id=\"ac_" + index + "\" class=\"dropdown-item\" data-toggle=\"tooltip\" data-placement=\"left\" onkeyup=\"autocompleteNavigation(event)\" href=\"search?query=" + object.iri.replace('#', "%23") + "\" title=\"" + object.iri.replace('#', "%23") + "\">" + object.label + "</a>";
    count ++;
  });

  $inner += "<a id=\"ac_" + count + "\" class=\"dropdown-item\" onkeyup=\"autocompleteNavigation(event)\" href=\"search?query=" + $text + "\"> Advanced search for " +  $text + " ...</a>";
  document.getElementById("autocomplete").innerHTML = $inner;

  $("#autocomplete").addClass("show");
}

function changeFocusToAutocomplete() {
  $("#ac_0").focus();
}

function autocompleteNavigation(event) {
  $id = event.target.id;
  $currentNumber = parseInt($id.substring(3), 10);
  $focusID = "";
  if (event.key === "ArrowDown") {
    $next = $currentNumber+1;
    $focusID = "#ac_" + $next;
  }
  if (event.key === "ArrowUp") {
    if ($currentNumber === 0) {
      $focusID = "#search-query";
    } else {
      $next = $currentNumber-1;
      $focusID = "#ac_" + $next;
    }
  }
  if (event.key === "Escape") {
    $("#autocomplete").removeClass("show");
    $focusID = "#search-query";
  }

  $($focusID).focus();
}


</script>