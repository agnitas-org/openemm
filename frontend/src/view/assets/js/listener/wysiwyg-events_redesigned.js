(() => {

  var Form = AGN.Lib.Form,
      Editor = AGN.Lib.Editor;

  $(document).on('tile:show', function(e) {
    var $target = $(e.target);
    var $editor = $target.find('.js-wysiwyg');
    var editorId = $editor.attr('id');

    if ($editor.length === 1) {
      var options = getEditorOptions($editor);

      window.createEditorExt(
        editorId,
        options.width,
        options.height,
        options.browseMailingId,
        options.isFullHtml,
        options.isFullHeight,
        options.allowExternalScript
      );

      if (options.isFullHeight) {
        CKEDITOR.instances[editorId].on("instanceReady", getOnInstanceReadyListener($editor));
      }
    }
  });

  $(document).on('tile:hide modal:close', function(e) {
    $(e.target).find('.js-wysiwyg').each(function() {
      if (CKEDITOR.instances[$(this).attr('id')]) {
        CKEDITOR.instances[$(this).attr('id')].updateElement();
      }

      if (isFullHeightEditor($(this))) {
        $(window).off('resize.' + getResizeAlias($(this)));
      }

      Editor.get($(this)).val($(this).val());
      window.removeEditor($(this).attr('id'));
    });
  });

  $(document).on('form:submit', function(e) {
    var $editor = $(e.target).find('.js-wysiwyg');

    if ( $editor.length > 0 && window.removeAllEditors ) {
      window.removeAllEditors();
    }
  });

  function getMailingId($editor) {
    var mailingId = $editor.data('browse-mailing-id');
    if(!!mailingId) {
      return mailingId;
    }

    var formId = $editor.data('form-target');

    var $form = !!formId ? $(formId) : null;
    if(!$form || $form.length === 0) {
      return null;
    }

    var form = Form.get($form);

    mailingId = form.getValue("mailingID");
    if(!mailingId) {
        mailingId = form.getValue("mailingId");
    }
    return mailingId
  }

  function getOnInstanceReadyListener($textAreaContainer) {
    return function (event) {
      var width = $textAreaContainer.parent().width();
      var height = $textAreaContainer.parent().height();
      event.editor.resize(width, height);
      event.editor.resresize_enabledze = false;
      $(window).on('resize.' + getResizeAlias($textAreaContainer), getResizeListener($textAreaContainer));
    };
  }

  var getResizeListener = function($textAreaContainer) {
    return function () {
      var $parent = $textAreaContainer.parent();
      CKEDITOR.instances[$textAreaContainer.attr('id')].resize($parent.width(), $parent.height());
    }
  };

  var getResizeAlias = function($textAreaContainer) {
    return 'ck-editor-' + $textAreaContainer.attr('id');
  };

  var isFullHeightEditor = function($textAreaContainer) {
    return !!$textAreaContainer.data('full-height-editor');
  };

  var getEditorOptions = function($editor) {
    var options = {};
    options.isFullHtml = !!$editor.data('full-tags');
    options.isFullHeight = isFullHeightEditor($editor);
    options.browseMailingId = getMailingId($editor);
    options.height = options.isFullHeight ? 0 : $editor.height();
    options.width = options.isFullHeight ? $editor.parent().width() : '100%';
    options.allowExternalScript = false;

    return _.extend(options, AGN.Lib.Helpers.objFromString($editor.data('editor-options')));
  }
})();
