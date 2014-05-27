
$(document).ready(function() {
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
	
});

function loadStatistics(type, period) {
	$.getJSON(window.location.href + "rest/statistics/" + type + "/" + period,
			   function(data) {
			     alert("here it is-> " + data);         
			   });
}
