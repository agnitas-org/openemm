(() => {

  const BlocksManager = AGN.Lib.MailingContent.BlocksManager;

  class EmcTextModulesManager extends BlocksManager { // EMC Template -> Content tab

    constructor(config) {
      super(config)
    }

    get saveUrl() {
      return `/layoutbuilder/template/${this.config.templateId}/textModules.action`;
    }
  }

  AGN.Lib.MailingContent.EmcTextModulesManager = EmcTextModulesManager;
})();
