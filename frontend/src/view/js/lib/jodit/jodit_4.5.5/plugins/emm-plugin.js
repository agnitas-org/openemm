(() => {

  const IMAGES_MAP_CACHE = new Map();

  const TOOLBARS = {
    Full: ['cut', 'copy', 'undo', 'redo', 'find', 'selectall', 'spellcheck', 'link', 'image', 'table', 'hr', 'symbols', 'font',
      'fontsize', 'paragraph', 'brush', 'bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'eraser',
      'align', 'ul', 'ol', 'outdent', 'indent', 'emm-ai-text', 'emm-tag', 'emoji'],
    Trimmed: ['cut', 'copy', 'undo', 'redo', 'find', 'link', 'symbols', 'bold', 'italic', 'underline', 'strikethrough',
      'superscript', 'subscript', 'eraser', 'outdent', 'indent', 'emm-ai-text', 'emm-tag', 'emoji'],
    EMM: ['cut', 'copy', 'undo', 'redo', 'find', 'selectall', 'link', 'image', 'table', 'hr', 'symbols', 'font', 'fontsize',
      'paragraph', 'brush', 'bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'eraser', 'align',
      'ul', 'ol', 'outdent', 'indent', 'emm-ai-text', 'emm-tag', 'emoji'],
    EMC: ['cut', 'copy', 'undo', 'redo', 'find', 'selectall', 'link', 'symbols', 'paragraph', 'brush', 'bold', 'italic',
      'underline', 'strikethrough', 'superscript', 'subscript', 'eraser', 'emm-ai-text', 'emm-tag', 'emoji']
  }

  Jodit.modules.Icon.set('robot', `<i class="icon icon-robot"></i>`);

  Jodit.plugins.add('emm', editor => {
    editor.options.buttons = TOOLBARS[editor.options.emmToolbarType] || TOOLBARS.EMM;
    if (!editor.options.emmShowAiTextGeneration) {
      editor.options.buttons = editor.options.buttons.filter(btn => btn !== 'emm-ai-text');
    }

    editor.options.controls['emm-ai-text'] = {
      icon: 'robot',
      tooltip: t('ai.textGeneration'),
      exec: editor => {
        AGN.Lib.Confirm.request(AGN.url('/wysiwyg/dialogs/aiTextGenerationRedesigned.action'))
          .done(text => insertContent(editor, text));
      }
    }

    editor.options.controls['emm-tag'] = {
      icon: `<svg><use href="${AGN.url(`/assets/core/images/wysiwyg/wysiwyg-icon-sprite.svg#agn_tag`, true)}"></use></svg>`,
      tooltip: t('wysiwyg.dialogs.agn_tags.tooltip'),
      exec: editor => {
        AGN.Lib.Confirm.request(AGN.url('/wysiwyg/dialogs/agn-tagsRedesigned.action'))
          .done(code => insertContent(editor, code));
      }
    }

    const mailingId = editor.options.emmMailingId;
    if (!IMAGES_MAP_CACHE.get(mailingId)) {
      IMAGES_MAP_CACHE.set(mailingId, getMailingImages(mailingId));
    }

    if (mailingId) {
      const imagePopupMethod = Jodit.defaultOptions.controls.image.popup;

      editor.options.controls.image.popup = function (editor, current, close) {
        const popup = imagePopupMethod.apply(this, Array.prototype.slice.apply(arguments));
        const topicId = `image-browser:selectedLink:${editor.id}`;

        AGN.Lib.Messaging.unsubscribe(topicId);

        if (!popup.querySelector('.jodit-ui-button--browse-server')) {
          createBrowseServerBtn(popup);
        }

        AGN.Lib.Messaging.subscribe(topicId, link => {
          $('.jodit-ui-input__input[name="url"]').val(link);
        })

        return popup;
      }

      function createBrowseServerBtn(popup) {
        const link = AGN.url(`/wysiwyg/image-browser.action?redesigned=true&mailingID=${mailingId}&editorId=${editor.id}`);

        const insertBtn = popup.querySelector('.jodit-ui-form .jodit-ui-button');

        const btn = insertBtn.cloneNode(true);
        btn.classList.add('jodit-ui-button--browse-server');
        btn.querySelector('.jodit-ui-button__text').textContent = 'Browse server';
        btn.style.marginLeft = '5px';
        btn.setAttribute('type', 'button');
        btn.setAttribute('data-popup', '');
        btn.setAttribute('data-popup-options', `width: 1200, height: 630, url: ${link}`);

        insertBtn.parentNode.appendChild(btn);

        return btn;
      }
    }

    function processContent(content) {
      content = processUrls(content, mailingId);
      return AGN.Lib.Helpers.unescapeAgnTags(content);
    }

    if (editor.options.editHTMLDocumentMode) {
      editor.events.on('beforeGetNativeEditorValue', function (options) {
        if (options?.fromEmmPlugin) {
          return '';
        }

        // hack to invoke original event handler
        const content = editor.e.fire('beforeGetNativeEditorValue', {fromEmmPlugin: true})
        // prevents invoking of original event handler
        editor.e.stopPropagation('beforeGetNativeEditorValue');

        return processContent(content);
      });
    } else {
      editor.events.on('beforeGetValueFromEditor', () => processContent(editor.editor.innerHTML));
    }

    editor.events.on('beforeSetValueToEditor', function (html) {
      if (!html) {
        return html;
      }

      html = replaceAgnImageTags(html, mailingId);
      return AGN.Lib.Helpers.escapeAgnTags(html);
    });
  });

  function insertContent(editor, content) {
    if (content) {
      editor.s.insertHTML(content);
      editor.synchronizeValues(); // For history module we need to synchronize values between textarea and editor
    }
  }

  function replaceAgnImageTags(data, mailingId) {
    const imagesUrlsMap = IMAGES_MAP_CACHE.get(mailingId);
    if (!imagesUrlsMap) {
      return data;
    }
    const imgTags = data.match(/\[agnIMAGE\s+name=(?:'.+\.\w+'|".+\.\w+")]/g);
    if (!imgTags) {
      return data;
    }

    imgTags.forEach(tagText => {
      const imgName = tagText.match(/name=['"](.+\.\w+)['"]/)[1];
      if (imgName) {
        const imageUrl = imagesUrlsMap[imgName];
        if (imageUrl) {
          data = data.replace(tagText, encodeURI(imageUrl));
        }
      }
    });

    return data;
  }

  function getMailingImages(mailingId) {
    let result;
    $.ajax({
      url: AGN.url('/wysiwyg/images/names-urls.action'),
      type: 'GET',
      async: false,
      data: {mi: mailingId},
      success: json => result = json
    });

    return result;
  }

  function processUrls(content, mailingId) {
    const imagesUrlsMap = IMAGES_MAP_CACHE.get(mailingId);
    if (!imagesUrlsMap) {
      return content;
    }
    const urls = content.match(/https?:\/\/[\w\-._~:\/?#@!$&'()*+,;=%]+/g);
    if (!urls) {
      return content;
    }
    const urlsImagesNamesMap = {};
    Object.keys(imagesUrlsMap).forEach(key => {
      urlsImagesNamesMap[imagesUrlsMap[key]] = key;
    });
    urls.forEach(url => {
      let normalizedUrl = url.replace(/(\/mediapool_element)(\/\d+)?(\/\d+\/\d+\/0\/\d+)(\.\w+)?/, function (match, $1, $2, $3) {
        return $1 + $3;
      });

      try {
        normalizedUrl = decodeURI(normalizedUrl);
      } catch (e) {
        console.error('Error with encoding URL: ' + normalizedUrl);
      }

      normalizedUrl = normalizedUrl.replaceAll('&amp;', '&');
      const imageName = urlsImagesNamesMap[normalizedUrl];
      if (imageName) {
        content = content.replace(url, `[agnIMAGE name="${imageName}"]`);
      }
    });

    return content.replaceAll('&amp;', '&');
  }
})();
