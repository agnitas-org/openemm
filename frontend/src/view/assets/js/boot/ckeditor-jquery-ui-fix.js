(function(){
    var orig_allowInteraction = $.ui.dialog.prototype._allowInteraction;
    $.ui.dialog.prototype._allowInteraction = function(event) {
        var $target = $(event.target);
        if ($target.closest('.cke_dialog').length || $target.closest('.cke_panel_frame').length) {
            return true;
        }
        return orig_allowInteraction.apply(this, arguments);
    };
})();
