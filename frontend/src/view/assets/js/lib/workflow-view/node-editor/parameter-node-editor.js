(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Select = AGN.Lib.Select;

  class ParameterNodeEditor extends AGN.Lib.WM.NodeEditor  {
    constructor(campaignEditor) {
      super();
      this.campaignEditor = campaignEditor;
    }

    get saveOnOpen () {
      return true;
    }

    get formName() {
      return 'parameterForm';
    }

    get title() {
      return t('workflow.parameter');
    }

    fillEditor(node) {
      Select.get(this.$find('select[name=value]')).setOptions(this.campaignEditor.getParametersOptions(node));

      const data = node.getData();

      this.$form.submit(false);
      this.$form.get(0).reset();

      EditorsHelper.fillFormFromObject(this.formName, data, '');
    }

    saveEditor() {
      return EditorsHelper.formToObject(this.formName);
    }

    alwaysReadonly(node) {
      return this.campaignEditor.isSplitParameterNode(node);
    }
  }

  AGN.Lib.WM.ParameterNodeEditor = ParameterNodeEditor;
})();
