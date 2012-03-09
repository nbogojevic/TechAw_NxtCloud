function showResult(direction, data) {
	if (data["isLeader"] == "false") {
		$.mobile.changePage($("#notleading"), { role: "dialog"});
	}
	else {
		$.mobile.changePage($("#goingWithTheCrowd"), { role: "dialog"});		
	}
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

