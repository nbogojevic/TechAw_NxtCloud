var I18N = {
	strings : [],
	
	handle : function (el, textElement) {
		if (!textElement) {
			textElement = function(item) { return item; };
		}
 		var trans = I18N.strings[el.id];
		if (trans) {
			textElement(el).text(trans);
		}
	}
};

$(window).live('pageinit', function () {
	$('h1.nb-i18n').each(function(index, el) { I18N.handle(el); });
	$('a.nb-i18n').each(function(index, el) {
		I18N.handle(el, 
				function(item) { 
					return $("#" + item.id + " .ui-btn-text"); 
				} ); 
	});
});
