(function () {

    var Utils = AGN.Lib.FormBuilder.Utils;
    var EmmControls = AGN.Lib.FormBuilder.EmmControls;
    var Templates = AGN.Lib.FormBuilder.Templates;

    var template = "<html>\n" +
        "    <head>\n" +
        "        <script type=\"text/javascript\" src=\"" + AGN.url("/js/lib/jquery/jquery-3.5.1.js") + "\"></script>\n" +
        "        <script type=\"text/javascript\" src=\"" + AGN.url("/assets/js/vendor/pickadate-3.5.6.js") + "\"></script>\n" +
        "        <script type=\"text/javascript\" src=\"" + AGN.url("/assets/js/vendor/pickadate-3.5.6.date.js") + "\"></script>\n" +
        "        <script type=\"text/javascript\" src=\"" + AGN.url("/js/lib/formbuilder/generated-form.js") + "\"></script>\n" +
        "        <link rel=\"stylesheet\" href=\"" + AGN.url("/assets/form.css") + "\">" +
        "        {{ if (cssUrl) { }}\n" +
        "        <link rel=\"stylesheet\" href=\"{{- cssUrl }}\">\n" +
        "        {{ } }}\n" +
        "    </head>\n" +
        "    <body>\n" +
        "        <div class=\"container\" style=\"margin-top: 50px;\">\n" +
        "            <form action=\"form.action\">\n" +
        "                {{= generatedHTML}}\n" +
        "            </form>\n" +
        "        </div>\n" +
        "    </body>\n" +
        "</html>"

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
            disableFields: ['file', 'autocomplete'],
            defaultFields: FormBuilder.defaultFields(builder),
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
                {type: 'checkbox-group'},
                {type: 'number'},
                {type: 'textarea'}, //added this type to lang files
                {type: 'date'} //added this type to lang files
            ],
            templates: {
                text: Templates.default,
                hidden: Templates.hidden,
                textarea: Templates.default,
                date: Templates.default,
                select: Templates.default,
                'radio-group': Templates.default,
                'checkbox-group': Templates.default,
                number: Templates.default
            },
            controls: builder.controls,
            i18n: {
                locale: window.adminLocale,
                location: AGN.url('/js/lib/formbuilder/translation/')
            },
            controlOrder: [
                'header',
                'paragraph',
                'mediapool',
                '',
                'text',
                'date',
                'textarea',
                'number',
                '',
                'select',
                'radio-group',
                'checkbox-group',
                '',
                'hidden',
                'formName',
                'button'
            ],
            inputSets: FormBuilder.getDefaultPresetsConfig(builder)
        };
    }

    FormBuilder.defaultFields = function(builder) {
        return [{
            type: "hidden",
            name: "agnCI",
            value: builder.options.companyId.toString()
        },
        {
            type: "hidden",
            name: "agnUID",
            value: "$!agnUID"
        }];
    }

    FormBuilder.getDefaultPresetsConfig = function (builder) {
        return [
            {
                label: createTemplateName(t('userform.formBuilder.template.subscribe')),
                name: 'subscribe',
                fields: [
                    {
                        type: 'hidden',
                        name: 'agnCI',
                        value: builder.options.companyId.toString(),
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnMAILINGLIST',
                        value: '',
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnSUBSCRIBE',
                        value: '1',
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnUID',
                        value: '$!agnUID',
                        label: ' '
                    },
                    {
                        type: 'text',
                        name: 'firstname',
                        value: '$!customerData.firstname',
                        emmField: 'firstname',
                        label: 'Firstname:'
                    },
                    {
                        type: 'text',
                        name: 'lastname',
                        value: '$!customerData.lastname',
                        emmField: 'lastname',
                        label: 'Lastname:'
                    },
                    {
                        type: 'text',
                        name: 'email',
                        value: '$!customerData.email',
                        emmField: 'email',
                        label: 'Email:'
                    },
                    {
                        type: 'button',
                        subtype: 'submit',
                        style: 'primary',
                        label: 'Subscribe'
                    },
                    {
                        type: 'formName',
                        label: ' '
                    }
                ]
            },
            {
                label: createTemplateName(t('userform.formBuilder.template.unsubscribe')),
                name: 'unsubscribe',
                fields: [
                    {
                        type: 'hidden',
                        name: 'agnCI',
                        value: builder.options.companyId.toString(),
                        label: ' '
                    },
                    {
                        type: 'formName',
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnUID',
                        value: '$!agnUID',
                        label: ' '
                    },
                    {
                        type: 'button',
                        subtype: 'submit',
                        style: 'primary',
                        label: 'Unsubscribe'
                    }
                ]
            },
            {
                label: createTemplateName(t('userform.formBuilder.template.profile_change')),
                name: 'profile_change',
                fields: [
                    {
                        type: 'hidden',
                        name: 'agnCI',
                        value: builder.options.companyId.toString(),
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnUID',
                        value: '$!agnUID',
                        label: ' '
                    },
                    {
                        type: 'formName',
                        label: ' '
                    },
                    {
                        type: 'text',
                        name: 'firstname',
                        value: '$!customerData.firstname',
                        emmField: 'firstname',
                        label: 'Firstname:'
                    },
                    {
                        type: 'text',
                        name: 'lastname',
                        value: '$!customerData.lastname',
                        emmField: 'lastname',
                        label: 'Lastname:'
                    },
                    {
                        type: 'text',
                        name: 'email',
                        value: '$!customerData.email',
                        emmField: 'email',
                        label: 'Email:'
                    },
                    {
                        type: 'button',
                        subtype: 'submit',
                        style: 'primary',
                        label: 'Subscribe'
                    }
                ]
            },
            {
                label: createTemplateName(t('userform.formBuilder.template.other')),
                name: 'other',
                fields: [
                    {
                        type: 'hidden',
                        name: 'agnCI',
                        value: builder.options.companyId.toString(),
                        label: ' '
                    },
                    {
                        type: 'hidden',
                        name: 'agnUID',
                        value: '$!agnUID',
                        label: ' '
                    }
                ]
            }
        ];
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
        this.controls = [];
        Utils.pushControl(this.controls, EmmControls.mediapool(this));
        Utils.pushControl(this.controls, EmmControls.nextform(this));
    }

    FormBuilder.prototype.generateHtml = function() {
        var formData = this.builder.actions.getData();
        var cssUrl = this.options.cssUrl;

        var $renderContainer = $('<form/>');
        $renderContainer.formRender({
            formData: formData,
            controls: this.controls,
            templates: {
                date: Templates.dateForRender
            }
        });
        var generatedHtml;
        if(template) {
            generatedHtml = _.template(template)({generatedHTML: $renderContainer.html(), cssUrl: cssUrl});
        } else {
            generatedHtml = $renderContainer.html();
        }
        $renderContainer.remove();

        generatedHtml = addNewLinesAfterTags(generatedHtml);

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

        var confirmationModal = this.options.confirmationModal;

        if(!confirmationModal || !editor.val()) {
            processHtmlGeneration(self, editor);
            return;
        }

        Confirm.createFromTemplate({}, 'warning-html-generation-modal').done(function () {
            processHtmlGeneration(self, editor);
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

    FormBuilder.prototype.getData = function() {
        return this.builder.actions.getData();
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

    FormBuilder.prototype.getInitJson = function () {
        return JSON.stringify(this.options.data);
    }

    FormBuilder.prototype.isChanged = function () {
        var self = this;
        if (self.options.data) {
            return self.getInitJson() !== self.getJson();
        } else {
            return _.isEqual(self.getData(), FormBuilder.defaultFields(self));
        }
    }

    function processHtmlGeneration(formBuilder, editor) {
        editor.val(formBuilder.generateHtml());
        AGN.Lib.Messages(t("defaults.success"), t('userform.formBuilder.success.htmlGenerated'), 'success');
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
        var typesForTextEmmAttrs = ['text', 'hidden', 'textarea', 'select', 'radio-group', 'checkbox-group'];
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

    function createTemplateName(templateName) {
        return t('userform.formBuilder.template.template') + ': ' + templateName;
    }

    function addNewLinesAfterTags(html) {
        if(!html || html.length <= 0) {
            return html;
        }
        return html.replace(/<\/.*?>/g, "$&\n");
    }

    AGN.Lib.FormBuilder.FormBuilder = FormBuilder;
})();
