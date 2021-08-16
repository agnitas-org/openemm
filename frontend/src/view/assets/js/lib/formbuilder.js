(function () {

    var TOOLTIPS_TEXT = {
        agnCI: t('userform.formBuilder.tooltips.agnCI'),
        agnUID: t('userform.formBuilder.tooltips.agnUID')
    }

    var DEFAULT_EMM_HIDDEN_FIELDS = ['agnCI', 'agnUID'];

    var Confirm = AGN.Lib.Confirm;

    var FormBuilder;

    var DEFAULT_ATTR = ['required', 'label', 'description', 'name', 'className', 'placeholder', 'value'];

    FormBuilder = function (textArea, options) {
        this.options = $.extend({}, options);
        var self = this;
        self.$el = $(textArea);
        self.$target = $(this.$el.data('target'));
        self.initCustomControls();
        self.builder = self.$el.formBuilder(FormBuilder.getBuilderDefaultOptions(self));
        self.defered = $.Deferred();
        if(self.options.data) {
            self.setJson(self.options.data);
        }
        self.builder.promise.then(function () {
            self.builder = self.$el.data('formBuilder');
            self.defered.resolve();
        });
    }

    FormBuilder.getBuilderDefaultOptions = function(builder) {
        return {
            roles: {},
            disabledAttrs: ['access', 'description'],
            disableFields: ['file'],
            defaultFields: [{
                    type: "hidden",
                    name: "agnCI",
                    value: "<cid>"
                },
                {
                    type: "hidden",
                    name: "agnUID",
                    value: "$!agnUID"
                }],
            disabledActionButtons: ['data', 'save'],
            actionButtons: [{
                    id: 'downloadHtml',
                    className: 'btn btn-info',
                    label: t('workflow.reaction.download'),
                    type: 'button',
                    events: {
                        click: function () {
                            builder.downloadHtml.bind(builder)();
                        }
                    }
                },
                {
                    id: 'generateHtml',
                    className: 'btn btn-success',
                    label: t('userform.formBuilder.generateHtml'),
                    type: 'button',
                    events: {
                        click: function () {
                            builder.generateHtmlToTarget.bind(builder)();
                        }
                    }
                }],
            editOnAdd: true,
            typeUserDisabledAttrs: {
                'formName': DEFAULT_ATTR,
                'mediapool': DEFAULT_ATTR,
                'text': ['subtype'],
                'textarea': ['subtype']
            },
            typeUserAttrs: getTypeUserAttrs(builder),
            layoutTemplates: {
                agnMediapool: function() { return document.createElement('span'); }
            },
            fields: [ //fields where use custom templates
                {type: 'text'},
                {type: 'hidden'},
                {type: 'select'},
                {type: 'radio-group'},
                {type: 'autocomplete'},
                {type: 'checkbox-group'},
                {type: 'number'},
                {type: 'textarea'}, //added this type to lang files
                {type: 'date'} //added this type to lang files
             ],
            templates: {
                text: getDefaultEmmTemplate,
                hidden: getHiddenTemplate,
                textarea: getDefaultEmmTemplate,
                date: getDefaultEmmTemplate,
                select: getDefaultEmmTemplate,
                'radio-group': getDefaultEmmTemplate,
                autocomplete: getDefaultEmmTemplate,
                'checkbox-group': getDefaultEmmTemplate,
                number: getDefaultEmmTemplate
            },
            controls: builder.controls,
            i18n: {
                locale: window.adminLocale,
                location: AGN.url('/js/lib/formbuilder/translation/')
            }
        };
    }

    FormBuilder.get = function(textArea, options) {
        var $el = $(textArea);
        var formBuilder = $el.data('_form-builder');

        if (formBuilder) {
            return formBuilder;
        }

        formBuilder = new FormBuilder(textArea, options);
        $el.data('_form-builder', formBuilder);
        return formBuilder;
    }

    FormBuilder.isCreated = function(textArea) {
        return !!$(textArea).data('_form-builder');
    }

    FormBuilder.prototype.initCustomControls = function() {
        var builderSelf = this;
        builderSelf.controls = [];
        pushControl(builderSelf.controls, {
            newControlName: 'mediapool',
            build: function () {
                return {
                    field: this.markup('img', null, {}),
                    layout: 'agnMediapool'
                };
            },
            onRender:  function(e) {
                if(this.preview) {
                    var $label = $(e.target).parents('.form-field').find('.field-label')
                    $label.html(t('userform.formBuilder.mediapoolImage') + ': ' + builderSelf.options.mediapoolImages[this.config.image]);
                }
                var $el = $(this.element);
                $el.attr('src', this.config.image)
                    .attr('alt', this.config.alt);

                if(this.config.width) {
                    $el.attr('width', this.config.width);
                }
                if(this.config.height) {
                    $el.attr('height', this.config.height);
                }
            },
            definition: function() {
                return {
                    i18n: {
                        default: t('userform.formBuilder.mediapoolImage')
                    }
                };
            }
        });

        pushControl(builderSelf.controls, {
            newControlName: 'formName',
            build: function () {
                return {
                    field: this.markup('input', null, {
                        id: 'agnFN',
                        type: 'hidden'
                    }),
                    layout: 'hidden'
                };
            },
            onRender: function(e) {
                if(this.preview) {
                    var $label = $(e.target).parents('.form-field').find('.field-label')
                    $label.html(t('userform.formBuilder.nextForm') + ': ' + this.config.formName);
                }
                $(this.element).attr('value', this.config.formName);
            },
            definition: function() {
                return {
                    i18n: {
                        default: t('userform.formBuilder.nextForm')
                    }
                };
            }
        });
    }

    FormBuilder.prototype.generateHtml = function() {
        var formData = this.builder.actions.getData();
        var cssUrl = this.options.cssUrl;

        var $renderContainer = $('<form/>');
        $renderContainer.formRender({formData: formData, controls: this.controls});
        var template = AGN.Opt.Templates[this.options.template];
        var generatedHtml;
        if(template) {
            generatedHtml = _.template(template)({generatedHTML: $renderContainer.html(), cssUrl: cssUrl});
        } else {
            generatedHtml = $renderContainer.html();
        }
        $renderContainer.remove();
        return generatedHtml;
    }

    FormBuilder.prototype.generateHtmlToTarget = function() {
        var self = this;
        var $target = this.$target;
        if(!$target) {
            return;
        }
        var editor = AGN.Lib.Editor.get($target);
        if(!editor) {
            return;
        }

        var $undoButton = $(this.options.undoButton);
        var confirmationModal = this.options.confirmationModal;

        if(!confirmationModal || !editor.val()) {
            processHtmlGeneration(self, editor, $undoButton);
            return;
        }

        Confirm.createFromTemplate({}, 'warning-html-generation-modal').done(function () {
            processHtmlGeneration(self, editor, $undoButton);
        });
    }

    FormBuilder.prototype.downloadHtml = function() {
        var generatedHtml = this.generateHtml();
        var encodedValue = window.btoa(generatedHtml);
        var fileName = 'Generated form.txt';
        if(this.options.formName) {
            fileName = this.options.formName + '.txt';
        }

        var tmpElement = document.createElement('a');
        tmpElement.setAttribute('href', 'data:application/octet-stream;charset=utf-8;base64,' + encodedValue);
        tmpElement.setAttribute('download', fileName);

        tmpElement.style.display = 'none';
        document.body.appendChild(tmpElement);

        tmpElement.click();

        document.body.removeChild(tmpElement);
    }

    FormBuilder.prototype.getJson = function() {
        return this.builder.actions.getData('json', false);
    }

    FormBuilder.prototype.setJson = function(data) {
        var self = this;
        self.defered.done(function () {
            return self.builder.actions.setData(data);
        })
    }

    FormBuilder.prototype.promise = function () {
        return this.defered.promise();
    }

    function processHtmlGeneration(formBuilder, editor, $undoButton) {
        if($undoButton) {
            $undoButton.show();
            $undoButton.data('undoData', editor.val());
            $undoButton.on('click', function () {
                editor.val($undoButton.data('undoData'));
                $undoButton.hide();
            });
        }
        editor.val(formBuilder.generateHtml());
        AGN.Lib.Messages(t("defaults.success"), t('userform.formBuilder.success.htmlGenerated'), 'success');
    }

    function pushControl(controls, options) {
        if(!Array.isArray(controls)) {
            return;
        }
        controls.push(function (superControlClass) {
            var NewControl = (function (controlClass) {
                var superConstructor = controlClass.prototype.constructor;
                function NewControl() {
                    return superConstructor.apply(this, arguments);
                }

                NewControl.prototype = Object.create(controlClass && controlClass.prototype, {
                    constructor: { value: NewControl, writable: true, configurable: true }
                });

                NewControl.__proto__ = controlClass;

                if(options.build) {
                    NewControl.prototype.build = options.build;
                }

                if(options.onRender) {
                    NewControl.prototype.onRender = options.onRender;
                }

                Object.defineProperty(NewControl, 'definition', { get: options.definition });

                return NewControl;
            })(superControlClass);

            // register this control for the following types & text subtypes
            superControlClass.register(options.newControlName, NewControl);
            return NewControl;
        });
    }

    function getDefaultEmmTemplate() {
        return {
            onRender: function(e) {
                var $formField = $(e.target).parents('.form-field');
                var newEmmField = this.config.emmField;
                var oldEmmField = $formField.data('currentEmmField');
                if (newEmmField !== oldEmmField) { // selected another field
                    $formField.data('currentEmmField', newEmmField);
                    var $elements = $formField.find('.form-elements');
                    var $valueIpt = $elements.find('input[name="value"]'),
                        $nameIpt = $elements.find('input[name="name"]');

                    var fieldSelected = newEmmField !== 'none';
                    var value = fieldSelected ? '$!customerData.' + newEmmField : '',
                        name = fieldSelected ? newEmmField : '';
                    $valueIpt.val(value);
                    $nameIpt.val(name);
                    if (fieldSelected) {
                        $nameIpt.prop('disabled', true);
                    } else {
                        $nameIpt.prop('disabled', false);
                    }
                }
            }
        };
    }

    function getHiddenTemplate() {
        return {
            onRender: function (e) {
                var name = this.config.id.replace('-preview', '');
                var $elements = $(e.target).parents('.form-field').find('.form-elements');
                var $emmFieldWrap = $elements.find('.emmField-wrap');
                if (DEFAULT_EMM_HIDDEN_FIELDS.includes(name)) {
                    $emmFieldWrap.hide();
                } else {
                    $emmFieldWrap.show();
                }

                if (!DEFAULT_EMM_HIDDEN_FIELDS.includes(name)) {
                    var fieldSelected = this.config.emmField !== 'none';
                    var value = fieldSelected ? '$!customerData.' + this.config.emmField : '',
                        newName = fieldSelected ? this.config.emmField : null;

                    var $valueIpt = $elements.find('input[name="value"]'),
                        $nameIpt = $elements.find('input[name="name"]');

                    $valueIpt.val(value);
                    if(newName) {
                        $nameIpt.val(newName);
                    }
                    if (fieldSelected) {
                        $nameIpt.prop('disabled', true);
                    } else {
                        $nameIpt.prop('disabled', false);
                    }
                }

                var tooltipValue = TOOLTIPS_TEXT[name];
                var $tooltip = $(e.target).parents('.form-field').find('.tooltip-element');
                if(!tooltipValue) {
                    $tooltip.hide();
                    return;
                }
                $tooltip.attr('tooltip', tooltipValue);
                $tooltip.show();
            }
        };
    }

    function getTypeUserAttrs(builder) {
        var userAttrsByType = {
            formName: {
                formName: {
                    label: t('userform.formBuilder.formName'),
                    options: builder.options.namesJson.reduce(function(map, item) {
                        map[item] = item;
                        return map;
                    }, {})
                }
            },
            mediapool: {
                image: {
                    label: t('userform.formBuilder.mediapoolImage'),
                    options: builder.options.mediapoolImages
                },
                alt: {
                    label: t('userform.formBuilder.imageAlt'),
                    value: ''
                },
                width: {
                    label: t('userform.formBuilder.imageWidth'),
                    value: 0
                },
                height: {
                    label: t('userform.formBuilder.imageHeight'),
                    value: 0
                }
            },
            date: {
                emmField: {
                    label: t('userform.formBuilder.emmField'),
                    options: builder.options.dateProfileFields
                }
            },
            number: {
                emmField: {
                    label: t('userform.formBuilder.emmField'),
                    options: builder.options.numberProfileFields
                }
            }
        };
        var typesForTextEmmAttrs = ['text', 'hidden', 'textarea', 'select', 'radio-group', 'autocomplete', 'checkbox-group'];
        typesForTextEmmAttrs.forEach(function (type) {
            userAttrsByType[type] = {
                emmField: {
                    label: t('userform.formBuilder.emmField'),
                    options: builder.options.textProfileFields
                }
            };
        });
        return userAttrsByType;
    }

    AGN.Lib.FormBuilder = FormBuilder;
})();
