
$(document).ready(function() {
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
	
});

$('ul.nav').on('click', function(e) {
	e.preventDefault();
	
	$('li.active').removeClass("active");
	$(e.target).parent().addClass("active");
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
});

function loadStatistics(type, period) {
	$.getJSON(window.location.href + "rest/statistics/" + type + "/" + period,
			   function(data) {
			     var tbl = $('table.table');
				 tbl.empty();
				 tbl.append("<tr><th>" + data.groupName + "</th><th>" + data.countName + "</th></tr>");
				 $.each(data.items, function(index, element) {
					 tbl.append("<tr><td>" + element.name + "</td><td>" + element.count + "</td></tr>");
				 });
			   });
}

