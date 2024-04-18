AGN.Lib.Controller.new('userform-view', function () {
    var FormBuilder = AGN.Lib.FormBuilder.FormBuilder;
    var Editor = AGN.Lib.Editor;
    var Confirm = AGN.Lib.Confirm;

    var actionURLPattern;
    let formId = 0;

    this.addDomInitializer('userform-view', function() {
        var config = this.config;
        actionURLPattern = config.actionURLPattern;
        formId = config.formId;
        $('#userFormForm').dirty();
    });

    this.addAction({click: 'activate-and-test'}, function() {
        const $el = this.el;
        const url = $el.attr('href');

        Confirm.createFromTemplate({}, 'userform-activate-and-test').done(() => {
            $.post(AGN.url(`/webform/${formId}/activate.action`)).done(resp => {
                AGN.Lib.JsonMessages(resp.popups, true);
                if (resp.success) {
                    $('#is-active-switch').prop('checked', true);
                    $el.removeAttr('data-action');
                    window.open(url, '_blank');
                }
            });
        });
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

    this.addAction({change: 'check-velocity-script'}, function() {
        if (this.el.val() !== 'wysiwyg') {
          return;
        }
        checkVelocityScripts(this.el);
    });

    this.addAction({submission: 'saveUserForm'}, function () {
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
        const $form = $('#choose-code-format-modal-form');
        const successMode = $form.find('[name="success_template_mode"]').val();
        const errorMode = $form.find('[name="error_template_mode"]').val();

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

    function checkVelocityScripts($el) {
        const options = AGN.Lib.Helpers.objFromString($el.data("action-options"));
        const text = AGN.Lib.Editor.get($("#" + options.type + "Template")).val();
        if (text.match(/#(?:set|include|macro|parse|if|foreach)/gi)) {
            AGN.Lib.Messages(t("defaults.error"), t("userform.error.velocity_not_allowed"), "alert");
            //switch to previous tab
            $el.val('html').trigger('change');
        }
    }

    function changeLinkState($link, actionId) {
        if ($link.exists()) {
            if (actionId > 0) {
                $link.attr('href', actionURLPattern.replace(':action-ID:', actionId));
                $link.removeClass('hidden');
            } else {
                $link.attr('href', "#");
                $link.addClass('hidden');
            }
        }
    }
});
