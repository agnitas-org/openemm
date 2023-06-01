AGN.Lib.Controller.new("enlarged-content-editor-controller", function () {
  var Modal = AGN.Lib.Modal;
  var Confirm = AGN.Lib.Confirm;

  var content;

  this.addDomInitializer('enlarged-content-editor-initializer', function ($modal) {
    var config = Modal.get($modal);
    content = config.content;
    synchronizeEditorTab(config.editorType);
    displayCharCounter();
  });
  
  function displayCharCounter() {
    if (getActiveEditor() === 'html') {
      showCharCounter();
    } else {
      hideCharCounter();
    }
  }

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
  
  const updateCharsCounter = function($el) {
      const count = $el.val().length;
      $('[data-enlarged-char-counter-for="' + $el.attr('id') + '"]')
        .find('span:first')
        .text(t('fields.content.charactersEntered', count));
  }
  
  this.addAction({
    'editor:create': 'count-enlarged-textarea-chars',
    'editor:change': 'count-enlarged-textarea-chars'
  }, function() {
    updateCharsCounter(this.el);
  });
  
  this.addAction({click: 'hide-enlarged-char-counter'}, function() {
    hideCharCounter();
  });

  this.addAction({click: 'show-enlarged-char-counter'}, function() {
    showCharCounter();
  });
  
  function showCharCounter() {
    $('[data-enlarged-char-counter-for]').first().show();
  }
  
  function hideCharCounter() {
    $('[data-enlarged-char-counter-for]').first().hide();
  }
});