(() => {
  const Messaging = AGN.Lib.Messaging;

  class AiEditor {

    constructor(dynTagEditor) {
      this.dynTagEditor = dynTagEditor;
    }

    get generatedText() {
      return $('[data-ai-result]').val()?.trim();
    }

    isAiTextGeneratedAndNotApplied() {
      const $textGenerationBlock = $('#tab-content-ai-text-generation');
      const notApplied = $textGenerationBlock.is(":visible") && this.generatedText;

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
  }

  AGN.Lib.MailingContent.AiEditor = AiEditor;
})();
