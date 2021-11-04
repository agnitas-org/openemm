AGN.Lib.Controller.new('userform-view', function () {
    var FormBuilder = AGN.Lib.FormBuilder.FormBuilder;
    var Editor = AGN.Lib.Editor;
    var Confirm = AGN.Lib.Confirm;

    var actionURLPattern;

    this.addDomInitializer('userform-view', function() {
        var config = this.config;
        actionURLPattern = config.actionURLPattern;
        $('#userFormForm').dirty();
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
        var successFBChanged = isFormbuilderChanged('successFormBuilder');
        var errorFBChanged = isFormbuilderChanged('errorFormBuilder');

        var isSuccessTabsDiffContent = successFBChanged && isTemplateCodeChanged('successTemplate');
        var isErrorTabsDiffContent = errorFBChanged && isTemplateCodeChanged('errorTemplate');

        if (isSuccessTabsDiffContent || isErrorTabsDiffContent) {
            Confirm.createFromTemplate(
                {
                        needSuccessCodeChoose: isSuccessTabsDiffContent,
                        needErrorCodeChoose: isErrorTabsDiffContent
                },
                'warning-save-different-tabs'
            );
        } else {
            var successMode = successFBChanged ? 'FORM_BUILDER' : 'HTML';
            var errorMode = errorFBChanged ? 'FORM_BUILDER' : 'HTML';
            save(successMode, errorMode);
        }
    });

    this.addAction({click: 'save-specific-code-mode'}, function () {
        var $form = $('#choose-code-format-modal-form');
        var successMode = $form.find('[name="success_template_mode"]:checked').val();
        var errorMode = $form.find('[name="error_template_mode"]:checked').val();

        save(successMode, errorMode);
    });
    
    this.addAction({change: 'formTestRecipient'}, function() {
        var $a = $('#formTestLink');
        var $s = $('#formTestRecipient');

        $a.attr('href', $s.val());
    });

    function isTemplateCodeChanged(editorId) {
        var ckeditor = CKEDITOR.instances[editorId];
        if(ckeditor) {
            ckeditor.updateElement();
        }

        return $('#userFormForm').dirty('isFieldDirty', $('#' + editorId));
    }

    function isFormbuilderChanged(id) {
        if(FormBuilder.isCreated('#' + id)) {
            return FormBuilder.get('#' + id).isChanged();
        }

        return false;
    }

    function save(successCodeMode, errorCodeMode) {
        var form = AGN.Lib.Form.get($("form#userFormForm"));

        if(FormBuilder.isCreated('#successFormBuilder')) {
            var successFB = FormBuilder.get('#successFormBuilder');
            if(successCodeMode === "FORM_BUILDER") {
                Editor.get($('#successTemplate')).val(successFB.generateHtml());
            }
            var successFormBuilderJson = successFB.getJson();
            form.setValueOnce("successSettings.formBuilderJson", successFormBuilderJson);
            var successFormDisabledFields = $('#successFormBuilder').find('input:enabled,select:enabled');
            successFormDisabledFields.prop('disabled', true);
        }

        if(FormBuilder.isCreated('#errorFormBuilder')) {
            var errorFB = FormBuilder.get('#errorFormBuilder');
            if(errorCodeMode === "FORM_BUILDER") {
                Editor.get($('#errorTemplate')).val(errorFB.generateHtml());
            }
            var errorFormBuilderJson = errorFB.getJson();
            form.setValueOnce("errorSettings.formBuilderJson", errorFormBuilderJson);
            var errorFormDisabledFields = $('#errorFormBuilder').find('input:enabled,select:enabled');
            errorFormDisabledFields.prop('disabled', true);
        }

        form.submit();

        if(successFormDisabledFields) {
            successFormDisabledFields.prop('disabled', false);
        }
        if(errorFormDisabledFields) {
            errorFormDisabledFields.prop('disabled', false);
        }
    }

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
