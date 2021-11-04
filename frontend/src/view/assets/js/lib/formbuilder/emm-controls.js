(function () {

    AGN.Lib.FormBuilder = AGN.Lib.FormBuilder || {};

    AGN.Lib.FormBuilder.EmmControls = {
        mediapool: function(builder) {
            return {
                newControlName: 'mediapool',
                build: function () {
                    return {
                        field: this.markup('img', null, {}),
                        layout: 'agnMediapool'
                    };
                },
                onRender: function (e) {
                    if (this.preview) {
                        var $label = $(e.target).parents('.form-field').find('.field-label')
                        $label.html(t('userform.formBuilder.mediapoolImage') + ': ' + builder.options.mediapoolImages[this.config.image]);
                    }
                    var $el = $(this.element);
                    $el.attr('src', this.config.image)
                        .attr('alt', this.config.alt);

                    if (this.config.width) {
                        $el.attr('width', this.config.width);
                    }
                    if (this.config.height) {
                        $el.attr('height', this.config.height);
                    }
                },
                definition: function () {
                    return {
                        i18n: {
                            default: t('userform.formBuilder.mediapoolImage')
                        }
                    };
                }
            }
        },
        nextform: function() {
            return {
                newControlName: 'formName',
                build: function () {
                    return {
                        field: this.markup('input', null, {
                            name: 'agnFN',
                            type: 'hidden'
                        }),
                        layout: 'hidden'
                    };
                },
                onRender: function (e) {
                    if (this.preview) {
                        var $label = $(e.target).parents('.form-field').find('.field-label')
                        $label.html(t('userform.formBuilder.nextForm') + ': ' + this.config.formName);
                    }
                    $(this.element).attr('value', this.config.formName);
                },
                definition: function () {
                    return {
                        i18n: {
                            default: t('userform.formBuilder.nextForm')
                        }
                    };
                }
            }
        }
    };
})();
