(() => {

  class MustacheTemplateHeaderComponent {

    init(params) {
      if (params.templateName && AGN.Lib.Template.exists(params.templateName)) {
        const templateParams = _.merge({
          text: params.displayName
        }, params.templateParams ?? {});

        this.eGui = AGN.Lib.Template.dom(params.templateName, templateParams).get(0);
      } else {
        this.eGui = document.createElement('span');
        this.eGui.innerHTML = params.displayName;
      }
    }

    getGui() {
      return this.eGui;
    }

  }

  AGN.Opt.Components['MustacheTemplateHeader'] = MustacheTemplateHeaderComponent;

})();