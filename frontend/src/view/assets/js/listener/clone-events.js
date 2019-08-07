(function() {
    var Action = AGN.Lib.Action;

    Action.new({'click': '[data-clone]'}, function() {
        var template = this.el.data('clone'),
            target   = this.el.data('clone-target'),
            count    = parseInt(this.el.data('clone-count')),
            html, $html;

        count += 1;
        this.el.data('clone-count', count);

        html = _.template(AGN.Opt.Templates[template], {count: count});
        $html = $(target).append(html);
        AGN.runAll($html);
    });

    $(document).on('click', 'a[data-clone]', function(e){
        e.preventDefault();
    });

})();
