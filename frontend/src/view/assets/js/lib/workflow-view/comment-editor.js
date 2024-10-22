(function () {
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;

  class CommentEditor {

    constructor() {
      this.formName = 'iconCommentForm';
      this.safeToSave = true;
    }

    fillEditor(node) {
      $('#iconComment').val(node.getComment());
    }

    saveEditor() {
      EditorsHelper.saveIconComment($('#iconComment').val());
      this.cancelEditor();
    }

    cancelEditor() {
      $('#icon-comment-editor').dialog('close');
      return false;
    }
  }

  AGN.Lib.WM.CommentEditor = CommentEditor;
})();
