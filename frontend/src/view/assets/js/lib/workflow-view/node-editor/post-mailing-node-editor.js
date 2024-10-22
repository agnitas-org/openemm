(function () {

  class PostMailingNodeEditor extends AGN.Lib.WM.MailingNodeEditor {
    constructor(mailingEditorBase) {
      super(mailingEditorBase);
      this.safeToSave = false;
    }

    get formName() {
      return 'postMailingForm';
    }

    get title() {
      return t('workflow.mailing.mediatype_post');
    }

    get $editor() {
      return $('#mailing_mediatype_post-editor');
    }
  }

  AGN.Lib.WM.PostMailingNodeEditor = PostMailingNodeEditor;
})();
