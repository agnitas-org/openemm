AGN.Lib.Controller.new('userform-view', function () {
    var FormBuilder = AGN.Lib.FormBuilder;

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

    this.addAction({click: 'saveUserForm'}, function () {
        var form = AGN.Lib.Form.get($("form#userFormForm"));

        if(FormBuilder.isCreated('#successFormBuilder')) {
            var successFormBuilderJson = FormBuilder.get('#successFormBuilder').getJson();
            form.setValueOnce("successSettings.formBuilderJson", successFormBuilderJson);
            var successFormDisabledFields = $('#successFormBuilder').find('input:enabled,select:enabled');
            successFormDisabledFields.prop('disabled', true);
        }

        if(FormBuilder.isCreated('#errorFormBuilder')) {
            var errorFormBuilderJson = FormBuilder.get('#errorFormBuilder').getJson();
            form.setValueOnce("errorSettings.formBuilderJson", errorFormBuilderJson);
            var errorFormDisabledFields = $('#errorFormBuilder').find('input:enabled,select:enabled');
            errorFormDisabledFields.prop('disabled', true);
        }

        form.submit();
        successFormDisabledFields.prop('disabled', false);
        errorFormDisabledFields.prop('disabled', false);
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
