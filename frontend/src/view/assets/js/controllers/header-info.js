AGN.Lib.Controller.new('header-info', function() {
    var mailingParamsIndex = 0;

    this.addDomInitializer('mailing-params', function() {
        var config = this.config;
        mailingParamsIndex = config.maxIndex;
    });

    this.addAction({click : 'add-mailing-parameter'}, function() {
        var trLast = $('#mailingParamsTable tr:last');
        var parameterKey = this.el.data('parameter-key');

        var newRow = _.template(AGN.Opt.Templates['header-info-mailing-param-row-new'], { key: ++parameterKey })
        trLast.after(newRow);

        var newBtn = trLast.find('#newMailingParameterBtn');
        newBtn.after('<a href="#" class="btn btn-regular btn-alert" data-action="remove-mailing-parameter" id="removeMailingParameterBtn"> <i class="icon icon-trash-o"></i> </a>');
        newBtn.remove();

    });

    this.addAction({'click' : 'remove-mailing-parameter'}, function() {
        var $currentTr = this.el.closest('tr');
        $currentTr.remove();
    });
});