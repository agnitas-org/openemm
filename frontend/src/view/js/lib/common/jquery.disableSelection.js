jQuery.fn.extend({
    disableSelection : function() {
        this.each(function() {
            // Safari/Chrome/Konqueror/Opera/Mozilla/ie
            this.onselectstart = function() { return false; };
            this.unselectable = "on";
            jQuery(this).css("-moz-user-select", "none");
            jQuery(this).css("-webkit-user-select", "none");
            jQuery(this).css("-khtml-user-select", "none");
        });
        return this;
    }
});

