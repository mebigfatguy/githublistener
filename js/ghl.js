

$(document).ready(function() {
	$("#weightslink[rel]").overlay();
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	
	loadStatistics(parts[0], parts[1]);
	loadWeights();

});

$('ul.nav').on('click', function(e) {
	e.preventDefault();

	$('li.active').removeClass("active");
	$(e.target).parent().addClass("active");
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
});

function loadWeights() {
	var href = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1);
	$.getJSON(href + "rest/statistics/weights", function(data) {
		var tbl = $('#weights');
		$.each(data, function(key, value) {
			tbl.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
		});
	});
}

function loadStatistics(type, period) {
	var href = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1);
	$.getJSON(href + "rest/statistics/" + type + "/" + period,
			function(data) {
				var tbl = $('#scores');
				tbl.empty();
				tbl.append("<tr><th>" + data.groupName + "</th><th>"
						+ data.countName + "</th></tr>");
				$.each(data.items, function(index, element) {
					tbl.append("<tr><td><a href='https://github.com/" + element.name + "' target='blank'>" + element.name + "</a></td><td>"
							+ element.count + "</td></tr>");
				});
			});
}
