
$(document).ready(function() {
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
	
});

function loadStatistics(type, period) {
	$.getJSON(window.location.href + "statistics/" + type + "/" + period + "?callback=?",
			   function(data) {
			     alert("here it is-> " + data);         
			   });
}
