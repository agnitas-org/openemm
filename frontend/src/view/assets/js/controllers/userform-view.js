AGN.Lib.Controller.new('userform-view', function () {
    var actionURLPattern;

    this.addDomInitializer('userform-view', function() {
        var config = this.config;
        actionURLPattern = config.actionURLPattern;

    });

    this.addAction({change: 'change-intro-action'}, function() {
        var link = $('#startActionLink');
        var actionId = this.el.val();
        changeLinkState(link, actionId);

    });

    this.addAction({change: 'change-final-action'}, function() {
        var $link = $('#finalActionLink');
        var actionId = this.el.val();
        changeLinkState($link, actionId);
    });

    this.addAction({click: 'check-velocity-script'}, function() {
        var $el = $(this.el);
        var options = AGN.Lib.Helpers.objFromString($el.data("action-options"));
        checkVelocityScripts(options.type);
    });

    function checkVelocityScripts(type) {
        var text = AGN.Lib.Editor.get($("#" + type + "Template")).val();
        if (text.match(/#(?:set|include|macro|parse|if|foreach)/gi)) {
            AGN.Lib.Messages(t("defaults.error"), t("userform.error.velocity_not_allowed"), "alert");
            //switch to previous tab
            var link = $('[data-toggle-tab="#tab-' + type + '-template-html"]');
            if (link) {
                link.trigger('click');
            }
        }
    }

    function changeLinkState($link, actionId) {
        if ($link.exists()) {
            if (actionId > 0) {
                $link.attr('href', actionURLPattern.replace('{action-ID}', actionId));
                $link.removeClass('hidden');
            } else {
                $link.attr('href', "#");
                $link.addClass('hidden');
            }
        }
    }
});
