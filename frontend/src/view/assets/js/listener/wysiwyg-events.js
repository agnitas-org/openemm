(() => {
  const Messaging = AGN.Lib.Messaging;
  const inputKeys = ['Enter', 'Backspace', 'Delete', 'Tab'];

  window.addEventListener('message', (e) => {
    if (e.data?.type === 'scroll-transfer') {
      const editorId = e.data.wysiwygId;
      const iframe = window.Jodit
        ? Jodit.instances[editorId].iframe
        : CKEDITOR.instances[editorId].container.findOne('iframe').$;

      const container = AGN.Lib.Scrollbar.get($(iframe))?.$el?.get(0);

      if (container) {
        container.scrollTop = container.scrollTop + e.data.deltaY;
      }
    }
  });

  $(document).on('tile:show', function (e) {
    const $target = $(e.target);
    const $editor = $target.find('.js-wysiwyg');

    if ($editor.length === 1) {
      const editorId = $editor.attr('id');
      const options = getEditorOptions($editor);

      if (window.Jodit) {
        const jodit = Jodit.make(`#${editorId}`, {
          theme: AGN.Lib.Helpers.isDarkTheme() ? 'dark' : 'default',
          editHTMLDocumentMode: options.isFullHtml,
          language: window.adminLocale?.split('-')[0] || 'en',
          cleanHTML: {
            denyTags: `${options.allowExternalScript ? '' : 'script'}`
          },
          emmMailingId: options.browseMailingId || -1,
          emmToolbarType: options.toolbarType
        });

        if (options.isFullHtml) {
          $(jodit.editor).trigger('click'); // trigger HTML cleaner
          fixJoditEvents(jodit);
          setTimeout(() => {
            jodit.e.on('keyup', (e) => {
              if (e.key.length === 1 || inputKeys.includes(e.key)) {
                jodit.userInputHappened = true;
              }
            });
            Messaging.send("html-version-wysiwyg-shown")
          }, 100);
        }

        addTransferScrollBehavior(jodit.iframe, jodit.id);
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
        if (options.isFullHtml) {
          Messaging.send("html-version-wysiwyg-shown");
        }

        window.setTimeout(() => {
          const iframe = CKEDITOR.instances[editorId].container.findOne('iframe').$;
          addTransferScrollBehavior(iframe, editorId);
        }, 500);
      }
      Messaging.send("wysiwyg-shown")
    }
  });

  $(document).on('tile:hide modal:close', function (e) {
    $(e.target).find('.js-wysiwyg').each(function () {
      const $textArea = $(this);

      if (window.Jodit) {
        const jodit = Jodit.instances[$textArea.attr('id')];
        if (jodit?.options.editHTMLDocumentMode) {
          jodit.e.fire('outsideClick');
          jodit.synchronizeValues();
        }
        jodit?.destruct();
        AGN.Lib.Editor.get($textArea).val($textArea.val());
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

  /**
   * Jodit adds UI elements that help to edit content, for example image resizer.
   * After user scroll editor in editHTMLDocumentMode: true, these elements get stuck
   * and cannot be scrolled with other content elements. This code fixes this behavior.
   */
  function fixJoditEvents(jodit) {
    const doc = jodit.iframe.contentWindow.document;
    if (jodit.iframe.contentWindow.document.documentElement) {
      jodit.e
        .on(doc.documentElement, 'mousedown touchend', () => {
          if (!jodit.s.isFocused()) {
            jodit.s.focus();
            if (jodit.editor === doc.body) {
              jodit.s.setCursorIn(doc.body);
            }
          }
        })
        .on(jodit.ew, 'mousedown touchstart keydown keyup touchend click mouseup mousemove scroll', (e) => {
          jodit.events?.fire(jodit.ow, e);
        });
    }
  }

  function addTransferScrollBehavior(iframe, wysiwygId) {
    if (!iframe) {
      return;
    }

    const iframeWindow = iframe.contentWindow;
    const iframeDocument = iframe.contentDocument || iframeWindow.document;

    iframeWindow.addEventListener('wheel', (e) => {
      const atTop = iframeWindow.scrollY <= 0;
      const atBottom = iframeWindow.scrollY + iframeWindow.innerHeight >= iframeDocument.body.scrollHeight;

      if ((atTop && e.deltaY < 0) || (atBottom && e.deltaY > 0)) {
        window.parent.postMessage({ type: 'scroll-transfer', deltaY: e.deltaY, wysiwygId: wysiwygId }, '*');
        e.preventDefault();
      }
    }, { passive: false });
  }

})();
