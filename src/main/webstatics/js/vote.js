function showResult(direction, data) {
	$.mobile.changePage($("#voted"), { role: "dialog"});
}
function vote(direction) {
	$("#status").text("");
	$.post("vote",
			{vote:$(direction).attr("data-robot-command")},
			function(data) {
				showResult(direction, data);
			}
			)
	.error(function(data, status, err) { $("#status").text(status+": " + err); });
}

