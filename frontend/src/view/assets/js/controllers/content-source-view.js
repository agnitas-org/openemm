AGN.Lib.Controller.new('content-source-view', function () {
    var Select = AGN.Lib.Select;
    var Template = AGN.Lib.Template;
    var form;
    var $urlsContainer;
    var urlResources = [];
    var linkForSettings;
    var htmlSourcesAllowed;
    var availableMailings = [];
    var resourceType; // HTML / XML

    function UrlResource(index, address) {
        this.address = address;
        this.contentBlockName = '';
        this.targetGroup = 0;
        this.index = index;
    }

    UrlResource.create = function (index, address) {
        return new UrlResource(index, address);
    };

    this.addDomInitializer('content-source-view', function () {
        $urlsContainer = $('#urlsContainer');
        form = AGN.Lib.Form.get($('#content-source-form'));
        resourceType = $('#resource-type').val();
        urlResources = [];

        updateUiBasedOnType();

        if (this.config.urlResources) {
            showExistingUrlResources(this.config.urlResources);
        }

        linkForSettings = this.config.urls.linkForSettings;
        htmlSourcesAllowed = this.config.htmlSourcesAllowed;
    });

    function updateUiBasedOnType() {
        var $container = $('#dynamic-container');
        $urlsContainer.empty();
        $container.empty();

        if (resourceType === 'HTML' && htmlSourcesAllowed) {
            var $addBtn = $(Template.text('add-url-btn', {}));
            $container.append($addBtn);
        } else if (resourceType === 'XML') {
            var $xmlUrlInput = $(Template.text('xml-source-url'));
            $container.append($xmlUrlInput);
        }
    }

    this.addAction({change: 'change-resource-type'}, function () {
        resourceType = this.el.val();
        updateUiBasedOnType();
        urlResources = [];
    });

    function showExistingUrlResources(urlResources) {
        if (resourceType === 'XML') {
            if (urlResources.length > 0) {
                // filling of url input for xml resource
                $('#xml-url-address').val(urlResources[0].url);
            }
        } else if (resourceType === 'HTML') {
            _.each(urlResources, function (inputUrlResource) {
                var urlResource = pushNewUrlResource(inputUrlResource);

                var $newDataBlock = $(Template.text("html-source-url", {
                    index: urlResource.index,
                    address: urlResource.address
                }));

                var $configureBtn = $newDataBlock.find("button[data-action='configure-url']");
                var $addressInput = $newDataBlock.find("input[data-source-url]");

                showInfoLabel($newDataBlock, '.content-block-info', inputUrlResource.contentBlockName, '.content-name-value', inputUrlResource.contentBlockName);
                showInfoLabel($newDataBlock, '.target-group-info', inputUrlResource.targetGroupName, '.target-group-value', inputUrlResource.targetGroupName);

                $urlsContainer.append($newDataBlock);
                addAddressInputListener($addressInput, $configureBtn);
            });
        }
    }

    function pushNewUrlResource(inputUrlResource) {
        var urlResource = UrlResource.create(urlResources.length);
        urlResource.address = inputUrlResource.url;
        urlResource.contentBlockName = inputUrlResource.contentBlockName;
        urlResource.targetGroup = inputUrlResource.targetGroup;

        urlResources.push(urlResource);

        return urlResource;
    }

    this.addDomInitializer('content-sources-mailings', function () {
        availableMailings = this.config;

        var $mailingSelect = $('#mailing-name');

        fillMailingSelect($mailingSelect);
        addChangeHandlerOnMailingSelect($mailingSelect);
    });

    function fillMailingSelect($mailingSelect) {
        availableMailings.forEach(function (mailing) {
            var $option = $('<option>', {value: mailing.name, text: mailing.name});
            $mailingSelect.append($option);
        });
    }

    function addChangeHandlerOnMailingSelect($mailingSelect) {
        var $contentBlockSelect = $('#content-block-name');

        $mailingSelect.on('change', function () {
            $contentBlockSelect.empty();
            fillBlockSelect($(this).val(), $contentBlockSelect);
            Select.get($contentBlockSelect).selectFirstValue();
        });
    }

    function fillBlockSelect(selectedMailingName, $contentBlockSelect) {
        var foundMailing = availableMailings.find(function (mailing) {
            return mailing.name === selectedMailingName;
        });

        foundMailing.blocks.forEach(function (contentBlock) {
            var $option = $('<option>', {value: contentBlock, text: contentBlock});
            $contentBlockSelect.append($option);
        });
    }

    this.addAction({click: 'add-url-resource'}, function () {
        var index = urlResources.length;

        var $newUrlBlock = $(Template.text("html-source-url", {index: index, address: ''}));
        var $configureBtn = $newUrlBlock.find("button[data-action='configure-url']");
        var $addressInput = $newUrlBlock.find("input[data-source-url]");

        $urlsContainer.append($newUrlBlock);
        $configureBtn.hide();
        addAddressInputListener($addressInput, $configureBtn);

        urlResources.push(UrlResource.create(index));
    });

    function addAddressInputListener($addressInput, $configureBtn) {
        if($addressInput && $configureBtn) {
            $addressInput.on('change keyup paste', function () {
                if ($(this).val()) {
                    $configureBtn.show();
                } else {
                    $configureBtn.hide();
                }
            });
        }
    }

    this.addAction({click: 'configure-url'}, function () {
        var $formGroup = this.el.closest('.form-group');
        var $urlInput = $formGroup.find('.form-control');
        var inputIndex = $formGroup.data('url-index');

        var params = {
            url: $urlInput.val(),
            index: inputIndex
        };

        var foundUrlResource = urlResources.find(function (sourceUrl) {
            return sourceUrl.index === inputIndex;
        });

        loadConfigurationModal(params, foundUrlResource);
    });

    this.addAction({click: 'remove-url'}, function () {
        var $formGroup = this.el.closest('.form-group');
        var foundIndex = $formGroup.data('url-index');

        $formGroup.remove();

        urlResources = urlResources.filter(function (urlResource) {
            return urlResource.index !== foundIndex;
        });
    });

    function loadConfigurationModal(params, urlResource) {
        var jqxhr = $.get(linkForSettings);
        jqxhr.done(function (resp) {
            var modalHtml = _.template(resp)(params);

            form.updateHtml(modalHtml);

            initModalSettings(urlResource);
            initModalSaveHandler();
        });
    }

    function initModalSettings(urlResource) {
        var $configurationModal = $('div[data-modal-index="' + urlResource.index + '"]');

        if (urlResource.contentBlockName) {
            var $blockNameSelect = $configurationModal.find('#content-block-name');
            var $firstOption = $blockNameSelect.find("option:first-child");
            $firstOption.val(urlResource.contentBlockName);
            $firstOption.text(urlResource.contentBlockName);
            $blockNameSelect.change();
        }

        if (urlResource.targetGroup > 0) {
            var $targetGroupSelect = $configurationModal.find('#target-group-select');
            $targetGroupSelect.find('option[value=' + urlResource.targetGroup + ']').attr("selected", "selected");
            $targetGroupSelect.change();
        }
    }

    function initModalSaveHandler() {
        $('[data-confirm-positive]').on('click', function () {
            var $modal = $(this).closest('.modal');
            var modalIndex = $modal.data('modal-index');

            var configurableUrlResource = urlResources.find(function (urlResource) {
                return urlResource.index === modalIndex;
            });

            var $targetGroupSelect = $modal.find('#target-group-select');

            configurableUrlResource.targetGroup = $targetGroupSelect.val();
            configurableUrlResource.contentBlockName = $modal.find('#content-block-name').val();

            showUrlConfiguredData(configurableUrlResource, $targetGroupSelect);
            form.handleErrors();
        });
    }

    function showUrlConfiguredData(urlResource, $targetGroupSelect) {
        var $groupContainer = $("div[data-url-index='" + urlResource.index + "']");

        var targetGroupText = $targetGroupSelect.find('option:selected').text();

        showInfoLabel($groupContainer, '.content-block-info', urlResource.contentBlockName, '.content-name-value', urlResource.contentBlockName);
        showInfoLabel($groupContainer, '.target-group-info', urlResource.targetGroup, '.target-group-value', targetGroupText);
    }

    function showInfoLabel($parent, containerSelector, canShow, valueSelector, value) {
        var $container = $parent.find(containerSelector);

        if (canShow) {
            $container.find(valueSelector).text(value);
            $container.show();
        } else {
            $container.hide();
        }
    }

    this.addAction({submission: 'content-source-save'}, function () {
        form.initFields();

        prepareDataForSave();

        if (form.valid()) {
            _.each(urlResources, function (urlResource, index) {
                var targetGroupValue = urlResource.targetGroup ? urlResource.targetGroup : 0;

                form.setValue('urls[' + index + '].url', urlResource.address);
                form.setValue('urls[' + index + '].contentBlockName', urlResource.contentBlockName);
                form.setValue('urls[' + index + '].targetGroup', targetGroupValue);
            });
        }

        form.submit();
    });

    function prepareDataForSave() {
        if (resourceType === 'XML') {
            var xmlUrlResource = UrlResource.create(0, $('#xml-url-address').val());
            urlResources = [];
            urlResources.push(xmlUrlResource);
        } else if (resourceType === 'HTML') {
            fillResourcesAddresses();
        }
    }

    function fillResourcesAddresses() {
        _.each($urlsContainer.find('[data-source-url]'), function (addressInput) {
            var $addressInput = $(addressInput);
            var urlIndex = $addressInput.closest('.form-group').data('url-index');

            var foundSourceUrl = urlResources.find(function (sourceUrl) {
                return sourceUrl.index === urlIndex;
            });

            foundSourceUrl.address = $addressInput.val();
        });
    }

    AGN.Lib.Validator.new('mailing-content-block', {
        valid: function ($e, options) {
            return !this.errors($e, options).length;
        },

        errors: function ($e, options) {
            var errors = [];

            var index = $e.closest('.form-group').data('url-index');

            var foundUrlResource = urlResources.find(function (sourceUrl) {
                return sourceUrl.index === index;
            });

            if (!foundUrlResource.contentBlockName) {
                errors.push({
                    field: $e,
                    msg: t('contentSource.error.block_not_set')
                })
            }

            return errors;
        }
    });
});
