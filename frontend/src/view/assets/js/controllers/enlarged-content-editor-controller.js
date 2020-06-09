AGN.Lib.Controller.new("enlarged-content-editor-controller", function () {
  var Modal = AGN.Lib.Modal;
  var Confirm = AGN.Lib.Confirm;

  var content;

  this.addDomInitializer('enlarged-content-editor-initializer', function ($modal) {
    var config = Modal.get($modal);
    content = config.content;
    synchronizeEditorTab(config.editorType);
  });

  this.addAction({
    click: 'updateContent'
  }, function () {
    var $button = $(this.el);
    var deferred = Confirm.get($button);
    deferred.positive(updateContentCallback());
    $('#enlarged_content_modal').modal('toggle');
  });

  var updateContentCallback = function() {
    return {content: getContent(), editorType: getActiveEditor()};
  };

  var getContent = function () {
    var $wysiwygEditorBlock = $('#tab-enlargedContent-wysiwyg');
    var $htmlEditorBlock = $('#enlargedContentEditor');

    if ($wysiwygEditorBlock.is(":visible")) {
      var wysiwygEditor = CKEDITOR.instances['enlargedContent'];
      return wysiwygEditor.getData();
    }

    if ($htmlEditorBlock.is(":visible")) {
      var htmlEditor = ace.edit("enlargedContentEditor");
      return htmlEditor.getValue();
    }
  };

  var getActiveEditor = function() {
    var $wysiwygEditorBlock = $('#tab-enlargedContent-wysiwyg');

    if ($wysiwygEditorBlock.is(":visible")) {
      return 'wysiwyg';
    }

    var $htmlEditorBlock = $('#enlargedContentEditor');
    if ($htmlEditorBlock.is(":visible")) {
      return 'html';
    }

    return '';
  };

  var synchronizeEditorTab = function(editorType) {
    if (!editorType || editorType === '') {
      return;
    }

    var tabLink = $('[data-toggle-tab$="' + editorType + '"]');
    if (tabLink) {
      tabLink.trigger("click");
    }
  };
});