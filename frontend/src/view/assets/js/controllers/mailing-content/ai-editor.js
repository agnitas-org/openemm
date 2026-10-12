(() => {
  const Messaging = AGN.Lib.Messaging;
  const JsonMessages = AGN.Lib.JsonMessages;
  const Select = AGN.Lib.Select;

  class AiEditor {

    constructor(dynTagEditor) {
      this.dynTagEditor = dynTagEditor;
      AGN.Lib.Controller.init(this.$container);
      AGN.runAll(this.$container);
    }

    loadAiSettings() {
      if (!this.saveEnabled) {
        return;
      }
      Messaging.resubscribe("mailing-content:save-ai-settings", settings => this.saveAiSettings(settings));
      this.resetSettings();
      this.toggleSaveAiSettingsBtn();
      if (this.contentId > 0) {
        $
          .get(AGN.url(`/mailing/content/${this.contentId}/ai-settings.action`))
          .done(settings => this.fillSettings(settings));
      }
    }

    get saveEnabled() {
      return this.$container.exists() && this.$saveAiSettingsBtn.exists();
    }

    get $saveAiSettingsBtn() {
      return $('#save-ai-settings');
    }

    get $container() {
      return $('#tab-content-ai-text-generation');
    }

    get generatedText() {
      return $('[data-ai-result]').val()?.trim();
    }

    isAiTextGeneratedAndNotApplied() {
      const notApplied = this.$container.is(":visible") && this.generatedText;

      if (notApplied) {
        const dynTagName = this.dynTagEditor.currentDynTag?.name;
        this.showApplyGeneratedTextModal(dynTagName);
      }
      return notApplied;
    }

    showApplyGeneratedTextModal(dynTagName) {
      AGN.Lib.Confirm
        .from('mailing-ai-text-generation-apply-question', {dynTagName})
        .then(() => this.applyAItextAndSave());
    }

    applyAItextAndSave() {
      if (this.dynTagEditor.currentContentBlock) {
        this.dynTagEditor.currentDynTag.changeContent(
          this.dynTagEditor.currentContentBlock.uniqueId,
          this.generatedText
        );
      }
      Messaging.send('mailing-content:applyGeneratedText', $('#ai-apply-text-btn'));
      this.dynTagEditor.saveDynTags();
    }

    saveAiSettings(settings) {
      if (this.dynTagEditor.currentContentBlock) {
        $
          .post(AGN.url(`/mailing/content/${this.contentId}/ai-settings.action`), settings)
          .done(resp => {
            JsonMessages(resp.popups)
            if (resp.success) {
              this.dynTagEditor.updateMailingPreview();
            }
          });
      }
    }

    get contentId() {
      return this.dynTagEditor.currentContentBlock.id;
    }

    fillSettings(settings) {
      if (!settings) {
        return;
      }
      $('[data-ai-numberofwords]').val(settings.numberOfWords);
      $('[data-ai-contentdescription]').val(settings.contentDescription);
      Select.get($('[data-ai-language]')).selectValue(settings.language);
      Select.get($('[data-ai-tonality]')).selectValue(settings.tonality);
      Select.get($('[data-ai-profilefield]')).selectValue(settings.profileField);
    }

    toggleSaveAiSettingsBtn() {
      this.$saveAiSettingsBtn.prop('disabled', this.contentId <= 0);
    }

    resetSettings() {
      this.fillSettings({
        numberOfWords: 0,
        contentDescription: '',
        language: window.adminLocale,
        tonality: 'NONE',
        profileField: '',
      })
    }
  }

  AGN.Lib.MailingContent.AiEditor = AiEditor;
})();
