(function() {
    var Action = AGN.Lib.Action,
        View  = AGN.Lib.View;

    Action.new({'change': '[data-view]'}, function() {
        View.trigger(this.el);
        this.el.trigger('click.bs.dropdown.data-api');
    });

})();
