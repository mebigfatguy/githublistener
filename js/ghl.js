

$(document).ready(function() {
	initialLoad();
});

$('ul.nav').on('click', function(e) {
	e.preventDefault();

	$('li.active').removeClass("active");
	$(e.target).parent().addClass("active");
	var selTab = $('li.active').attr('id');
	var parts = selTab.split("_");
	loadStatistics(parts[0], parts[1]);
});

function initialLoad() {
	var href = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1);
	$.getJSON(href + "rest/statistics/text", function(data) {
		$( 'title' ).append(data.title);
		$( 'h1' ).append(data.title);
		$( 'h4' ).append(data.description);
		$( '#projects_month' ).append("<a href='#'>" + data.projectsByMonth + "</a>");
		$( '#projects_week' ).append("<a href='#'>" + data.projectsByWeek + "</a>");
		$( '#projects_day' ).append("<a href='#'>" + data.projectsByDay + "</a>");
		$( '#users_month' ).append("<a href='#'>" + data.usersByMonth + "</a>");
		$( '#users_week' ).append("<a href='#'>" + data.usersByWeek + "</a>");
		$( '#users_day' ).append("<a href='#'>" + data.usersByDay + "</a>");
		$( '#weights' ).append("<tr><th>" + data.event + "</th><th>" + data.weight + "</th></tr>");
		$("#weightslink[rel]").overlay();
		
		var selTab = $('li.active').attr('id');
		var parts = selTab.split("_");
		
		loadStatistics(parts[0], parts[1]);
		loadWeights();
	});
}

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
