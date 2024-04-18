AGN.Lib.Controller.new('content-source-view', function () {
  const Select = AGN.Lib.Select;
  const InputTable = AGN.Lib.InputTable;
  const Helpers = AGN.Lib.Helpers;
  let form;
  let htmlSourcesAllowed;
  let availableMailings = [];
  let resourceType; // HTML / XML
  let $urlSettings;
  let contentBlockSelect;
  let targetGroupSelect;
  let $urlsTable;

  this.addDomInitializer('content-source-view', function () {
    form = AGN.Lib.Form.get($('#content-source-form'));
    resourceType = $('#resource-type').val();
    $urlsTable = $('#urls-table')
    $urlSettings = $('#url-settings');
    contentBlockSelect = Select.get($('#content-block-name'))
    targetGroupSelect = Select.get($('#target-group-select'));
    htmlSourcesAllowed = this.config.htmlSourcesAllowed;
    availableMailings = this.config.availableMailings;

    updateUiBasedOnType();
    initUrlSettings();
  });

  function updateUiBasedOnType() {
    const $urlInputs = $urlsTable.find('[data-name="url"]');
    if (resourceType === 'HTML' && htmlSourcesAllowed) {
      $urlInputs.each((i, urlInput) => toggleEditBtn($(urlInput)));
      $('[data-add-row]').closest('td').show();
      $('[data-action="configure-url"]').closest('td').show();
    } else if (resourceType === 'XML') {
      $('[data-add-row]').closest('td').hide();
      $('[data-action="configure-url"]').closest('td').hide();
      $urlInputs.first().attr('data-field', 'required');
      form.initFields();
    }
  }

  this.addAction({change: 'change-resource-type'}, function () {
    resourceType = this.el.val();
    updateUiBasedOnType();
  });

  function initUrlSettings() {
    $('#mailing-name').on('change', updateContentBlocksByMailing);
    showUrlSettings($urlsTable.find('tr:first'));
  }

  function updateContentBlocksByMailing() {
    const options = availableMailings
      .find(mailing => mailing.name === $(this).val())
      .blocks
      .map(name => ({id: name, text: name}));
    contentBlockSelect.setOptions(options);
  }

  function toggleEditBtn($input) {
    $input.closest('td').next().toggleClass('hidden', !$($input).val());
  }

  this.addAction({change: 'url-input', keyup: 'url-input', paste: 'url-input'}, function () {
    toggleEditBtn(this.el);
  });

  this.addAction({click: 'configure-url'}, function () {
    if (!form.validate()) {
      return;
    }
    form.cleanFieldFeedback();
    storeCurrentUrlSettings();
    showUrlSettings(this.el.closest('tr'));
  });

  function updateTargetSelect(urlData) {
    targetGroupSelect.selectValueOrSelectFirst(urlData.targetGroup);
  }

  function isUrlSettingsShouldBeShown($urlRow) {
    return resourceType !== 'XML' && $urlRow.find('[data-name="url"]')?.val()?.trim()?.length > 0;
  }

  function showUrlSettings($urlRow) {
    const urlData = InputTable.get($urlsTable).getRowData($urlRow);
    $urlSettings.toggle(isUrlSettingsShouldBeShown($urlRow));
    $urlSettings.data('urlRow', $urlRow);
    $('#url-ph').text(urlData.url);

    const option = urlData.contentBlockName
      ? {id: urlData.contentBlockName, text: urlData.contentBlockName}
      : {id: '', text: t('contentSource.mailingContentSelect')}
    contentBlockSelect.setOptions([option]);
    updateTargetSelect(urlData);
  }

  function storeCurrentUrlSettings() {
    const $urlRow = $urlSettings.data('urlRow');
    $urlRow.find('[data-name="contentBlockName"]').val($('#content-block-name').val());
    $urlRow.find('[data-name="targetGroup"]').val($('#target-group-select').val());
  }

  function isValidForm(urlsData) {
    if (resourceType === 'HTML') {
      for (let i = 0; i < urlsData.length; i++) {
        if (urlsData[i].contentBlockName.trim()?.length <= 0) {
          showUrlSettings($urlsTable.find('tr').eq(i));
          return form.validate();
        }
      }
    }
    return true;
  }

  this.addAction({submission: 'content-source-save'}, function () {
    storeCurrentUrlSettings();
    const urlsData = InputTable.get($urlsTable).collect();
    if (!isValidForm(urlsData)) {
      return;
    }
    _.each(urlsData, function (urlResource, index) {
      form.setValue('urls[' + index + '].url', urlResource.url);
      form.setValue('urls[' + index + '].contentBlockName', urlResource.contentBlockName);
      form.setValue('urls[' + index + '].targetGroup', urlResource.targetGroup || 0);
    });
    form.submit();
  });

  AGN.Lib.Validator.new('content-source-url', {
    valid: function ($e, options) {
      return !this.errors($e, options).length;
    },
    errors: function ($e) {
      if (!$e.val() || Helpers.isUrl($e.val())) {
        return [];
      }
      return [{field: $e, msg: t('url.invalid')}];
    }
  })
});
