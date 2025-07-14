(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Snippets = AGN.Lib.WM.Snippets;

  class SplitNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor(editor) {
      super();
      this.editor = editor;
      this.safeToSave = true;
    }

    get formName() {
      return 'splitNodeForm';
    }
    
    get title() {
      return t('split.mailing');
    }

    fillEditor(node) {
      const data = node.getData();

      this.$form.submit(false);
      this.$form.get(0).reset();

      EditorsHelper.fillFormFromObject(this.formName, data, '');
      this.isValid();
    }

    saveEditor() {
      const formData = EditorsHelper.formToObject(this.formName);
      const splitIcon = EditorsHelper.curEditingNode;
      const oldSplitPortions = SplitNodeEditor.getSplitPortions(splitIcon.data.splitType);
      const newSplitPortions = SplitNodeEditor.getSplitPortions(formData.splitType);

      if (oldSplitPortions.length === newSplitPortions.length) {
        this.#updateParameterValues(splitIcon, newSplitPortions);
      } else {
        this.#updateSplitType(splitIcon, formData.splitType);
      }
      return formData;
    }

    #updateSplitType(splitIcon, splitType) {
      const splitIconPosition = {x: splitIcon.x, y: splitIcon.y};
      const splitIconIncomingNodes = EditorsHelper.getNodesByIncomingConnections(splitIcon);

      this.editor.deleteNode(splitIcon);

      Snippets.createSplitSample(splitType, this.editor.gridBackgroundShown,
          (nodes, connections) => {
            const splitNode = nodes.filter(node => node.isSplitNode)[0];
            const splitIconConnections = splitIconIncomingNodes.map(source => ({source: source, target: splitNode}));
            this.editor.newSnippet(nodes, [...connections, ...splitIconConnections], splitIconPosition)
          });
    }

    #updateParameterValues(splitIcon, newSplitPortions) {
      this.editor.getSplitParameterNodes(splitIcon)
          .forEach((parameterIcon, i) => this.editor._setParameterNodesValue([parameterIcon], newSplitPortions[i]));
    }

    static getSplitPortions(splitType) {
        return splitType.match(/.{1,2}/g).map(val => parseInt(val, 10));
    }
  }

  AGN.Lib.WM.SplitNodeEditor = SplitNodeEditor;
})();
