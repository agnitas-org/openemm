(() => {

  $(document).on('tile:show', function (e) {
    const $target = $(e.target);
    const $editor = $target.find('.js-wysiwyg');

    if ($editor.length === 1) {
      const editorId = $editor.attr('id');
      const options = getEditorOptions($editor);

      if (window.Jodit) {
        Jodit.make(`#${editorId}`, {
          theme: AGN.Lib.Helpers.isDarkTheme() ? 'dark' : 'default',
          editHTMLDocumentMode: options.isFullHtml,
          iframe: options.isFullHtml,
          cleanHTML: {
            denyTags: `${options.allowExternalScript ? '' : 'script'}`
          },
          emmMailingId: options.browseMailingId || -1,
          emmToolbarType: options.toolbarType,
          emmShowAiTextGeneration: options.showAiTextGeneration
        });
      } else {
        window.createEditorExt(
          editorId,
          options.width,
          options.height,
          options.browseMailingId,
          options.isFullHtml,
          false,
          options.allowExternalScript
        );
      }
    }
  });

  $(document).on('tile:hide modal:close', function (e) {
    $(e.target).find('.js-wysiwyg').each(function () {
      const $textArea = $(this);

      if (window.Jodit) {
        AGN.Lib.Editor.get($textArea).val($textArea.val());
        Jodit.instances[$textArea.attr('id')]?.destruct();
      } else {
        CKEDITOR.instances[$textArea.attr('id')]?.updateElement();
        AGN.Lib.Editor.get($textArea).val($textArea.val());
        window.removeEditor($textArea.attr('id'));
      }
    });
  });

  $(document).on('form:submit', function (e) {
    const $editor = $(e.target).find('.js-wysiwyg');

    if ($editor.exists()) {
      if (window.Jodit) {
        Object.values(Jodit.instances).forEach(editor => editor.destruct());
      } else if (window.removeAllEditors) {
        window.removeAllEditors();
      }
    }
  });

  const getEditorOptions = $editor => {
    const options = {};

    if (window.Jodit) {
      options.isFullHtml = false;
    } else {
      options.isFullHtml = !!$editor.data('full-tags');
      options.height = $editor.height();
      options.width = '100%';
    }

    options.allowExternalScript = false;
    options.browseMailingId = getMailingId($editor);
    return _.extend(options, AGN.Lib.Helpers.objFromString($editor.data('editor-options')));
  }

  function getMailingId($editor) {
    const formId = $editor.data('form-target');
    const $form = !!formId ? $(formId) : null;

    if (!$form || !$form.exists()) {
      return null;
    }

    const form = AGN.Lib.Form.get($form);

    let mailingId = form.getValue("mailingID");
    if (!mailingId) {
      mailingId = form.getValue("mailingId");
    }
    return mailingId
  }
})();
