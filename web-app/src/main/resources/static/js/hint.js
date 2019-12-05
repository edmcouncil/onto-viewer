
function showHint(event) {
  var x = event.target.value;

  if (x.length < 3) {
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


  autocomplete();
}

function autocomplete() {
  $text = $("#search-query").val();
  $path = '/hint/'+$text;
  $.ajax({
    url: $path,
    dataType: 'json',
    contentType: 'application/json',
    type: 'GET'
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

  $inner = "";
  //<a class="dropdown-item" href="#">Action</a>
  $.each(autocomplates, function (index, object) {
    $inner += "<a id=\"ac_" + index + "\" class=\"dropdown-item\" onkeyup=\"autocompleteNavigation(event)\" href=\"search?query=" + object.iri + "\">" + object.label + "</a>";
  });

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
