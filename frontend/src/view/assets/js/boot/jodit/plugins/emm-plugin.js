(() => {

  if (!window.Jodit) {
    return;
  }

  Jodit.modules.Icon.set('wrap-text-in-p', `<i class="icon icon-code"></i>`);

  const IMAGES_MAP_CACHE = new Map();

  const TOOLBARS = {
    Full: [
      {buttons: ['cut', 'copy', 'paste']},
      {buttons: ['undo', 'redo']},
      {buttons: ['find', 'selectall', 'spellcheck']},
      {buttons: ['link', 'image', 'table', 'hr', 'symbols']},
      {buttons: ['font', 'fontsize', 'paragraph', 'brush']},
      {buttons: ['bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript']},
      {buttons: ['eraser', 'align']},
      {buttons: ['ul', 'ol']},
      {buttons: ['outdent', 'indent']},
      {buttons: ['emm-tag', 'emoji', 'wrap-text-in-p']},
    ],
    Trimmed: [
      {buttons: ['cut', 'copy', 'paste']},
      {buttons: ['undo', 'redo']},
      {buttons: ['find', 'link', 'symbols']},
      {buttons: ['bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript']},
      {buttons: ['eraser', 'outdent', 'indent']},
      {buttons: ['emm-tag', 'emoji', 'wrap-text-in-p']},
    ],
    EMM: [
      {buttons: ['cut', 'copy', 'paste']},
      {buttons: ['undo', 'redo']},
      {buttons: ['find', 'selectall']},
      {buttons: ['link', 'image', 'table', 'hr', 'symbols']},
      {buttons: ['font', 'fontsize', 'paragraph', 'brush']},
      {buttons: ['bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript']},
      {buttons: ['eraser', 'align']},
      {buttons: ['ul', 'ol']},
      {buttons: ['outdent', 'indent']},
      {buttons: ['emm-tag', 'emoji', 'wrap-text-in-p']},
    ],
    EMC: [
      {buttons: ['cut', 'copy', 'paste']},
      {buttons: ['undo', 'redo']},
      {buttons: ['find', 'selectall']},
      {buttons: ['link', 'symbols']},
      {buttons: ['font', 'fontsize', 'paragraph', 'brush', 'align']},
      {buttons: ['ul', 'ol']},
      {buttons: ['bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript']},
      {buttons: ['eraser', 'emm-tag', 'emoji', 'wrap-text-in-p']},
    ]
  }

  Jodit.plugins.add('emm', editor => {
    editor.options.buttons = TOOLBARS[editor.options.emmToolbarType] || TOOLBARS.EMM;

    editor.options.controls['emm-tag'] = {
      icon: `<svg><use href="${AGN.url(`/assets/core/images/wysiwyg/wysiwyg-icon-sprite.svg#agn_tag`, true)}"></use></svg>`,
      tooltip: t('wysiwyg.dialogs.agn_tags.tooltip'),
      exec: editor => {
        AGN.Lib.Confirm.request(AGN.url('/wysiwyg/dialogs/agn-tags.action'))
          .done(code => insertContent(editor, code));
      }
    }

    editor.options.controls['wrap-text-in-p'] = {
      icon: 'wrap-text-in-p',
      tooltip: t('wysiwyg.wrapTextInP'),
      isDisabled: editor => editor.s.isCollapsed()
        || Boolean(editor.s.current() && Jodit.modules.Dom.closest(editor.s.current(), 'p', editor.editor)),
      exec: editor => {
        editor.s.commitStyle({ element: 'p' })
        editor.synchronizeValues();
      }
    }

    const mailingId = editor.options.emmMailingId;
    if (!IMAGES_MAP_CACHE.get(mailingId)) {
      IMAGES_MAP_CACHE.set(mailingId, getMailingImages(mailingId));
    }

    if (mailingId && !window.isOpenEmm) {
      const imagePopupMethod = Jodit.defaultOptions.controls.image.popup;

      editor.options.controls.image.popup = function (editor, current, close) {
        const popup = imagePopupMethod.apply(this, Array.prototype.slice.apply(arguments));
        const topicId = `image-browser:selectedLink:${editor.id}`;

        AGN.Lib.Messaging.unsubscribe(topicId);

        if (!popup.querySelector('.jodit-ui-button--browse-server')) {
          createBrowseServerBtn(popup);
        }

        AGN.Lib.Messaging.subscribe(topicId, link => {
          const img = editor.s.insertImage(link);
          editor.e.fire('openImageProperties', img);
        })

        return popup;
      }

      function createBrowseServerBtn(popup) {
        const link = AGN.url(`/wysiwyg/image-browser.action?mailingID=${mailingId}&editorId=${editor.id}`);

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
      if (!content) {
        return content;
      }

      content = processUrls(content, mailingId);
      return AGN.Lib.Helpers.unescapeAgnTags(content);
    }

    if (editor.options.editHTMLDocumentMode) {
      editor.events.on('beforeGetNativeEditorValue', function (options) {
        if (options?.fromEmmPlugin) {
          return '';
        }

        // hack to invoke original event handler
        let content = editor.e.fire('beforeGetNativeEditorValue', {fromEmmPlugin: true})
        // prevents invoking of original event handler
        editor.e.stopPropagation('beforeGetNativeEditorValue');

        content = AGN.Lib.Helpers.prettifyHtml(content);

        return processContent(content);
      });

      /**
       * Removes content from the temporary `div.jodit-wysiwyg` created during Jodit initialization.
       *
       * Although Jodit is configured to use an iframe editor (flags iframe and editHTMLDocumentMode),
       * it briefly creates a content-editable div, which can cause instructions like <meta> or <script> tags
       * from the user's content to be injected into the DOM end then executed.
       * This function clears the div to prevent unintended execution
       * before the `iframe.jodit-wysiwyg_iframe` is fully set up.
       */
      editor.events.on('beforeSetNativeEditorValue', function (data) {
        if ((editor.options.iframe || editor.options.editHTMLDocumentMode) && !editor.history.canUndo()) {
          data.value = '';
        }
      });

      editor.events.on('beforeSetElementValue', function(data) {
        if (editor._initialRawValue === undefined) {
          editor._initialRawValue = editor.element.value;
          editor._initialHasHtmlStructure = isFullHtmlDocument(editor._initialRawValue);
          editor._initialBodyHtml = extractBodyHtml(editor._initialRawValue);
          editor._initialHasDefaultHead = editor._initialHasHtmlStructure
            && hasEmptyTitleHead(editor._initialRawValue);
        }

        if (data.value === editor._initialRawValue) {
          return;
        }

        const doc = parseHtml(data.value);

        const linksToRemove = editor.o.iframeCSSLinks
          .map(cssLink => doc.head.querySelector(`link[rel="stylesheet"][href="${cssLink}"]`))
          .filter(el => !!el);

        if (linksToRemove.length) {
          linksToRemove.forEach(el => {
            const nextElement = el.nextSibling;
            if (nextElement?.nodeType === Node.TEXT_NODE && nextElement.textContent.trim() === '') {
              nextElement.remove();
            }
            el.remove();
          });

          data.value = `${editor.o.iframeDoctype}\n${doc.documentElement.outerHTML}`;
        }

        const bodyHtml = doc.body.innerHTML.trim();

        if (editor._initialRawValue.trim() === '' && isBodyEmpty(bodyHtml)) {
          data.value = '';
        } else if (editor._initialHasHtmlStructure) {
          if (editor._initialBodyHtml.trim() === bodyHtml) {
            data.value = editor._initialRawValue;
          } else if (editor._initialHasDefaultHead && isBodyEmpty(bodyHtml)) {
            data.value = '';
          }
        } else if (isBodyEmpty(bodyHtml)) {
          data.value = '';
        }
      });

      function extractBodyHtml(str) {
        return parseHtml(str).body.innerHTML;
      }

      function isBodyEmpty(bodyHtml) {
        return bodyHtml === '' || bodyHtml === '<br>';
      }

      function hasEmptyTitleHead(str) {
        const doc = parseHtml(str);

        const head = doc.head;
        if (!head) {
          return false;
        }

        const headChildren = Array.from(head.children);
        if (headChildren.length !== 1) {
          return false;
        }

        const title = headChildren[0];
        if (title.tagName.toLowerCase() !== "title") {
          return false;
        }

        return title.textContent.trim() === '';
      }

      function isFullHtmlDocument(str) {
        const hasTags =
          /<html[\s\S]*?>/i.test(str) &&
          /<head[\s\S]*?>/i.test(str) &&
          /<body[\s\S]*?>/i.test(str);

        if (!hasTags) {
          return false;
        }

        const doc = parseHtml(str);
        return !!(doc.documentElement && doc.head && doc.body);
      }

      function parseHtml(html) {
        return new DOMParser().parseFromString(html, "text/html");
      }
    } else {
      editor.events.on('afterGetValueFromEditor', data => {
        data.value = processContent(data.value);
      });
    }

    editor.events.on('processPaste', function (e, text) {
      if (e.clipboardData?.types?.includes('text/html')) {
        return text
          // remove custom attributes (anything not href= or src=)
          .replace(/<(\w+)([^>]*)>/g, (match, tag, attrs) => {
            const cleaned = attrs.replace(/\s+(?!href=|src=)[\w:-]+(?:=(["']).*?\1)?/g, '');
            return `<${tag}${cleaned}>`;
          })
          // remove empty SVGs
          .replace(/<path[^>]*>\s*<\/path>/g, '').replace(/<svg[^>]*>\s*<\/svg>/g, '');
      }

      return undefined;
    });

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

    return AGN.Lib.Helpers.replaceAgnTags(data, (tag, tagHtml) => {
      if (tag.name === 'agnIMAGE' && tag.attributes['name']) {
        const imageUrl = imagesUrlsMap[tag.attributes.name];
        if (imageUrl) {
          return encodeURI(imageUrl);
        }
      }

      return tagHtml;
    });
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
