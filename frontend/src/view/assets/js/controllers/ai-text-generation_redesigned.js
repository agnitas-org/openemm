AGN.Lib.Controller.new('ai-text-generation', function() {

  const Form = AGN.Lib.Form;
  const Select = AGN.Lib.Select;
  let lastGeneratedText;

  this.addDomInitializer('ai-text-generation', function () {
    const languageSelect = Select.get($('#ai-language'));
    if (languageSelect.hasOption(window.adminLocale)) {
      languageSelect.selectValue(window.adminLocale);
    }
  });

  this.addAction({
    enterdown: 'skip-form-submit'
  }, function () {
    this.event.preventDefault();
  });

  this.addAction({click: 'generateText'}, function() {
    const $tile = this.el.closest('.tile');

    if (validateSettings($tile)) {
      const settings = getSettingsFromUi($tile);

      $.post(AGN.url("/mailing/content/generateText.action"), settings).done(resp => {
        lastGeneratedText = resp.data;
        displayGeneratedText(resp.data, $tile);
      }).fail(() => AGN.Lib.Messages.defaultError());
    }
  });

  function validateSettings($tile) {
    let isValid = true;
    const $numberOfWords = $tile.find("[id^='ai-numberOfWords']");
    const $contentDescription = $tile.find("[id^='ai-content-description']");

    const numberOfWordsErrors = AGN.Lib.Validator.get('number').errors($numberOfWords, {min: 1, required: true});
    if (numberOfWordsErrors.length > 0) {
      isValid = false;

      numberOfWordsErrors.forEach(function(error, index) {
        Form.showFieldError$($numberOfWords, error.msg);
      });
    } else {
      Form.cleanFieldFeedback$($numberOfWords);
    }

    if (!$.trim($contentDescription.val())) {
      isValid = false;
      Form.showFieldError$($contentDescription, t('fields.required.errors.missing'));
    } else {
      Form.cleanFieldFeedback$($contentDescription);
    }

    return isValid;
  }

  this.addAction({click: 'assumeGeneratedText'}, function() {
    const $tile = this.el.closest('.tile');
    const tabId = this.el.data('tab-id');
    const text = $tile.find('.ai-generated-text').val();

    openEditorTab(tabId);
    setEditorContent(text, $tile, tabId);
  });

  function openEditorTab(tabId) {
    const $wysiwygTab = $(`[data-toggle-tab*="wysiwyg${tabId}"]`);
    if ($wysiwygTab.exists()) {
      $wysiwygTab.trigger('click');
      return;
    }

    const $htmlTab = $(`[data-toggle-tab*="html${tabId}"]`);
    if ($htmlTab.exists()) {
      $htmlTab.trigger('click');
    }
  }

  function setEditorContent(content, $tile, tabId) {
    const $textArea = $tile.find('.js-wysiwyg');

    if ($('#tab-content-wysiwyg').is(":visible") || $(`[id^='tab-grid-wysiwyg${tabId}']`).is(":visible")) {
      var editor = CKEDITOR.instances[$textArea.attr('id')];
      if (editor.status === 'ready') {
        editor.setData(content)
      } else {
        editor.on("instanceReady", function (event) {
          event.editor.setData(content);
        });
      }
    }
    if ($('#contentEditor').is(":visible") || $(`[id^='tab-grid-html${tabId}']`).is(":visible")) {
      ace.edit(`${$textArea.attr('name')}Editor`).setValue(content);
    }
  };

  function getSettingsFromUi($scope) {
    return {
      language: $scope.find('[id^="ai-language"]').val(),
      numberOfWords: $scope.find('[id^="ai-numberOfWords"]').val(),
      tonality: $scope.find('[id^="ai-tonality"]').val(),
      contentDescription: $scope.find('[id^="ai-content-description"]').val()
    }
  }

  function displayGeneratedText(text, $scope) {
    $scope.find('.ai-generated-text').val(text);
    $scope.find('#ai-assume-generated-text').removeClass('hidden');
  }
});