AGN.Lib.Controller.new('ai-text-generation', function () {

  const Form = AGN.Lib.Form;
  const Messaging = AGN.Lib.Messaging;
  let lastGeneratedText;

  this.addDomInitializer('ai-text-generation', function () {
    const languageSelect = AGN.Lib.Select.get(this.el.find('[data-ai-language]'));
    if (languageSelect.hasOption(window.adminLocale)) {
      languageSelect.selectValue(window.adminLocale);
    }
  });

  this.addAction({enterdown: 'skip-form-submit'}, function () {
    this.event.preventDefault();
  });

  this.addAction({click: 'generateText'}, function () {
    const $scope = getParentScope$(this.el);

    if (validateSettings($scope)) {
      const settings = getSettingsFromUi($scope);

      $.post(AGN.url('/mailing/content/generateText.action'), settings).done(resp => {
        lastGeneratedText = resp.data;
        displayGeneratedText(resp.data, $scope);
        this.el.find('.text').text(t('ai.regenerateText'));
      });
    }
  });

  function validateSettings($scope) {
    let isValid = true;
    const $numberOfWords = $scope.find('[data-ai-numberOfWords]');
    const $contentDescription = $scope.find('[data-ai-contentDescription]');

    const numberOfWordsErrors = AGN.Lib.Validator.get('number').errors($numberOfWords, {min: 1, required: true});
    if (numberOfWordsErrors.length > 0) {
      isValid = false;
      numberOfWordsErrors.forEach(error => Form.showFieldError$($numberOfWords, error.msg));
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

  function applyGeneratedText($el) {
    const $scope = getParentScope$($el);
    const text = getAiResult$($scope).val();
    const tabId = $el.closest('[data-tab-id]').data('tab-id');
    openEditorTab(tabId);
    setEditorContent(text, $scope, tabId);
  }

  this.addAction({click: 'applyGeneratedText'}, function () {
    applyGeneratedText(this.el);
  });

  Messaging.subscribe('mailing-content:applyGeneratedText', applyGeneratedText);

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

  function setEditorContent(content, $scope, tabId) {
    const $textArea = $scope.find('.js-wysiwyg');

    if ($('#tab-content-wysiwyg').is(":visible") || $(`[id^='tab-grid-wysiwyg${tabId}']`).is(":visible")) {
      if (window.Jodit) {
        const jodit = Jodit.instances[$textArea.attr('id')];
        if (jodit.o.editHTMLDocumentMode) {
          const doc = new DOMParser().parseFromString(jodit.value, "text/html");
          doc.body.innerHTML = content;

          content = `${jodit.o.iframeDoctype}\n${doc.documentElement.outerHTML}`;
        }

        jodit.value = content;
      } else {
        const editor = CKEDITOR.instances[$textArea.attr('id')];
        if (editor.status === 'ready') {
          editor.setData(content)
        } else {
          editor.on("instanceReady", event => event.editor.setData(content));
        }
      }
    }
    if ($('#contentEditor').is(":visible") || $(`[id^='tab-grid-html${tabId}']`).is(":visible")) {
      ace.edit(`${$textArea.attr('name')}Editor`).setValue(content);
    }
  }

  function getSettingsFromUi($scope) {
    return {
      language: $scope.find('[data-ai-language]').val(),
      numberOfWords: $scope.find('[data-ai-numberOfWords]').val(),
      tonality: $scope.find('[data-ai-tonality]').val(),
      contentDescription: $scope.find('[data-ai-contentDescription]').val()
    }
  }

  function displayGeneratedText(text, $scope) {
    getAiResult$($scope).val(text).trigger('change');
    $scope.find('#ai-apply-text-btn').removeClass('hidden');
  }

  function getAiResult$($scope) {
    return $scope.find('[data-ai-result]');
  }

  function getParentScope$($el) {
    return $el.closest('.tile');
  }
});