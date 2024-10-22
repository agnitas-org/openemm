(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;

  class NodeEditor {
    constructor() {
      if (this.constructor === NodeEditor) {
        throw new Error("Cannot instantiate abstract class");
      }
    }

    get saveOnOpen () {
      return false;
    }
    
    get $form() {
      return $(`form[name="${this.formName}"]`);
    }
    
    get form() {
      return AGN.Lib.Form.get(this.$form);
    }
    
    get formName() {
      throw new Error("formName() must be implemented")
    }

    $find(selector) {
      return $(`form[name="${this.formName}"] ${selector}`);
    }
    
    toggle(selector, show) {
      this.$find(selector).toggle(show);
    }

    hide(selector) {
      this.toggle(selector, false);
    }

    show(selector) {
      this.toggle(selector, true);
    }

    // Date picker uses a date in UTC but JS uses a local timezone so we need to workaround that
    dateAsUTC(date) {
      return date ? new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())) : null;
    }

    getForwardParams(elemSelector) {
      return {
        nodeId: EditorsHelper.curEditingNode.getId(),
        elementId: encodeURIComponent(elemSelector),
      }
    }

    getForwardParamsStr() {
      return Object.entries(this.getForwardParams())
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join(';')
    }
    
    isValid() {
      return this.form.validate();
    }

    save() {
      if (this.isValid()) {
        EditorsHelper.saveCurrentEditorWithUndo();
      }
    }
  }
  
  AGN.Lib.WM.NodeEditor = NodeEditor;
})();
