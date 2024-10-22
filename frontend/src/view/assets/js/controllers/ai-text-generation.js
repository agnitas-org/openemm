AGN.Lib.Controller.new('ai-text-generation', function() {

  const Form = AGN.Lib.Form;
  const Select = AGN.Lib.Select;
  var $modal;
  var lastGeneratedText;

  this.addDomInitializer('ai-text-generation', function () {
    $modal = this.el.closest('.modal');
    initListeners();

    const languageSelect = Select.get(this.el.find("[id^='ai-language']"));
    if (languageSelect.hasOption(window.adminLocale)) {
      languageSelect.selectValue(window.adminLocale);
    }
  });

  function initListeners() {
    $modal.on('modal:enlarged', function (e, $enlargedModal) {
      syncSettings($modal, $enlargedModal);
    });

    $modal.on('modal:enlarged:apply', function (e, $enlargedModal) {
      syncSettings($enlargedModal, $modal);
    });

    $modal.on('saveDynTag', function (e, currentDynTag) {
      const $aiTextGenerationBlock = $('#tab-content-ai-text-generation');

      if ($aiTextGenerationBlock.is(":visible")) {
        if (lastGeneratedText) {
          const promise = AGN.Lib.Confirm.createFromTemplate(
            {dynTagName: currentDynTag.name},
            'mailing-ai-text-generation-apply-question'
          );
          promise.done(function () {
            const event = $.Event('saveDynTagContent');
            $modal.trigger(event, [lastGeneratedText]);
            lastGeneratedText = null;
          });
        } else {
          $modal.modal('toggle')
        }

        e.preventDefault();
      }
    });
  }

  this.addAction({
    enterdown: 'skip-form-submit'
  }, function () {
    this.event.preventDefault();
  });

  this.addAction({click: 'generateText'}, function() {
    const form = Form.get(this.el);
    const $scope = this.el.closest('.tile, .modal');
    const $el = this.el;

    if (validateSettings(form, $scope)) {
      const settings = getSettingsFromUi($scope);

      $.post(AGN.url("/mailing/content/generateText.action"), settings).done(function(resp) {
        lastGeneratedText = resp.data;
        displayGeneratedText(resp.data, $scope);
        $el.find('.text').text(t('ai.regenerateText'));
      }).fail(function() {
        AGN.Lib.Messages(t("Error"), t("defaults.error"), "alert");
      });
    }
  });

  function validateSettings(form, $scope) {
    var isValid = true;
    var $numberOfWords = $scope.find("[id^='ai-numberOfWords']");
    var $contentDescription = $scope.find("[id^='ai-content-description']");

    const numberOfWordsErrors = AGN.Lib.Validator.get('number').errors($numberOfWords, {min: 1, required: true});
    if (numberOfWordsErrors.length > 0) {
      isValid = false;

      numberOfWordsErrors.forEach(function(error, index) {
        form.showFieldError($numberOfWords.attr('name'), error.msg);
      });
    } else {
      form.cleanFieldError($numberOfWords.attr('name'));
    }

    if (!$.trim($contentDescription.val())) {
      isValid = false;
      form.showFieldError($contentDescription.attr('name'), t('fields.required.errors.missing'));
    } else {
      form.cleanFieldError($contentDescription.attr('name'));
    }

    return isValid;
  }

  this.addAction({click: 'assumeGeneratedText'}, function() {
    const $scope = this.el.closest('.tile, .modal');
    const text = $scope.find('.ai-generated-text').val();

    if ($scope.is('.modal')) {
      AGN.Lib.Confirm.get(this.el).positive(text);
    } else {
      const tabId = $scope.data('tab-id');
      openEditorTab(tabId);
      setEditorContent(text, $scope, tabId);
    }
  });

  function openEditorTab(tabId) {
    const $wysiwygTab = $('[data-toggle-tab*="wysiwyg' + tabId + '"]');
    if ($wysiwygTab.exists()) {
      $wysiwygTab.trigger('click');
      return;
    }

    const $htmlTab = $('[data-toggle-tab*="html' + tabId + '"]');
    if ($htmlTab.exists()) {
      $htmlTab.trigger('click');
    }
  }

  function setEditorContent(content, $tile, tabId) {
    const $textArea = $tile.parent().parent().find('.js-wysiwyg');

    if ($('#tab-content-wysiwyg').is(":visible") || $("[id^='tab-grid-wysiwyg" + tabId + "']").is(":visible")) {
      var editor = CKEDITOR.instances[$textArea.attr('id')];
      if (editor.status === 'ready') {
        editor.setData(content)
      } else {
        editor.on("instanceReady", function (event) {
          event.editor.setData(content);
        });
      }
    }
    if ($('#contentEditor').is(":visible") || $("[id^='tab-grid-html" + tabId + "']").is(":visible")) {
      ace.edit($textArea.attr('name') + 'Editor').setValue(content);
    }
  };

  function syncSettings($srcModal, $destModal) {
    const settings = getSettingsFromUi($srcModal);
    displaySettings($destModal, settings);

    const generatedText = $srcModal.find('.ai-generated-text').val();
    if (generatedText) {
      displayGeneratedText(generatedText, $destModal);
    }
  }

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

  function displaySettings($modal, settings) {
    if (settings) {
      Select.get($modal.find('#ai-language')).selectValue(settings.language);
      $modal.find('#ai-numberOfWords').val(settings.numberOfWords);
      Select.get($modal.find('#ai-tonality')).selectValue(settings.tonality);
      $modal.find('#ai-content-description').val(settings.contentDescription);
    }
  }

});