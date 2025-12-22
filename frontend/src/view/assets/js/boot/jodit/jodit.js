(() => {

  if (!window.Jodit) {
    return;
  }

  const options = {
    width: '100%',
    height: '100%',
    showWordsCounter: false,
    showCharsCounter: false,
    showPlaceholder: false,
    askBeforePasteHTML: false,
    toolbarAdaptive: false,
    addNewLineOnDBLClick: false,
    hidePoweredByJodit: true,
    extraPlugins: ['emm', 'emoji'],
    cleanHTML: {
      timeout: 500,
      removeEmptyTags: false,
      fillEmptyParagraph: false,
      replaceNBSP: false,
      removeEmptyElements: false,
      removeEmptyAttributes: false,
      removeOnError: false,
      replaceOldTags: false
    },
    link: {
      processPastedLink: false,
    },
    colors: {
      full: ['#000000', '#993300', '#333300', '#003300', '#003366', '#000080', '#333399', '#333333', '#800000', '#FF6600', '#808000', '#808080', '#008080', '#0000FF', '#666699', '#808080', '#FF0000', '#FF9900', '#99CC00', '#339966', '#33CCCC', '#3366FF', '#800080', '#999999', '#FF00FF', '#FFCC00', '#FFFF00', '#00FF00', '#00FFFF', '#00CCFF', '#993366', '#C0C0C0', '#FF99CC', '#FFCC99', '#FFFF99', '#CCFFCC', '#CCFFFF', '#99CCFF', '#CC99FF', '#FFFFFF']
    },
    colorPickerDefaultTab: 'color',
    wrapNodes: {
      emptyBlockAfterInit: false
    },
    enter: 'br',
    enterBlock: 'div',
    iframe: true,
    iframeSandbox: 'allow-forms allow-same-origin',
    iframeDefaultSrc: '<html><head><title></title></head><body></body></html>',
    iframeStyle: '',
    iframeTitle: '',
    iframeCSSLinks: [AGN.url('/assets/jodit.css')],
    controls: {
      font: {
        list: {
          'Comic Sans MS,cursive': 'Comic Sans MS'
        }
      }
    }
  };

  _.merge(Jodit.defaultOptions, options);

  // sorting list of fonts
  Jodit.defaultOptions.controls.font.list = Object.fromEntries(Object.entries(Jodit.defaultOptions.controls.font.list)
    .sort(([, valueA], [, valueB]) => valueA.localeCompare(valueB)));

  function extendLinkPlugin() {
    const originalFormTemplate = Jodit.defaultOptions.link.formTemplate;
    const formTemplateFn = editor => {
      const html = originalFormTemplate(editor);

      $(html.container).prepend(`
        <div class="jodit-ui-block jodit-ui-block_align_left">
          <div class="jodit-ui-input jodit-ui-block__link-type">
            <span class="jodit-ui-input__label">${t('wysiwyg.dialogs.link.type')}</span>
            <div class="jodit-ui-input__wrapper">
              <select name="link_type" class="jodit-select">
                <option value="">URL</option>
                <option value="email">${t('wysiwyg.dialogs.link.email_type')}</option>
                <option value="phone">${t('wysiwyg.dialogs.link.phone_type')}</option>
              </select>
            </div>
          </div>
        </div>
      
        <div class="jodit-ui-block jodit-ui-block_align_left" style="display: none">
          <div class="jodit-ui-input jodit-ui-block__phone-number">
            <span class="jodit-ui-input__label">${t('wysiwyg.dialogs.link.phone_number')} *</span>
            <div class="jodit-ui-input__wrapper">
              <input class="jodit-ui-input__input" name="phone-number" type="tel">
            </div>
          </div>
        </div>
      
        <div class="jodit-ui-block jodit-ui-block_align_left" style="display: none">
          <div class="jodit-ui-input jodit-ui-block__email-address">
            <span class="jodit-ui-input__label">${t('wysiwyg.dialogs.link.email_address')} *</span>
            <div class="jodit-ui-input__wrapper">
              <input class="jodit-ui-input__input" name="email-address" type="text">
            </div>
          </div>
        </div>
      
        <div class="jodit-ui-block jodit-ui-block_align_left" style="display: none">
          <div class="jodit-ui-input jodit-ui-block__email-subject">
            <span class="jodit-ui-input__label">${t('wysiwyg.dialogs.link.subject')}</span>
            <div class="jodit-ui-input__wrapper">
              <input class="jodit-ui-input__input" name="email-subject" type="text">
            </div>
          </div>
        </div>
      
        <div class="jodit-ui-block jodit-ui-block_align_left" style="display: none">
          <div class="jodit-ui-input jodit-ui-block__email-body">
            <span class="jodit-ui-input__label">${t('wysiwyg.dialogs.link.content')}</span>
            <div class="jodit-ui-input__wrapper">
              <input class="jodit-ui-input__input" name="email-body" type="text">
            </div>
          </div>
        </div>
      `);

      return html;
    }

    Jodit.defaultOptions.link.formTemplate = formTemplateFn;

    const linkPlugin = Jodit.plugins.get('link');
    const originalGenerateForm = linkPlugin.prototype.__generateForm;
    const originalAfterInit = linkPlugin.prototype.afterInit;

    linkPlugin.prototype.afterInit = function (... args) {
      originalAfterInit.apply(this, args);
      const [jodit] = args;
      jodit.e.on('dblclick.link', e => {
        if ($(e.target).is('a')) {
          $(jodit.container)
            .find('.jodit-toolbar-button_link button:visible')
            .first()
            .trigger('click')
        }
      });
    }

    linkPlugin.prototype.__generateForm = function (...args) {
      const res = originalGenerateForm.apply(this, args);
      const originalValidateFn = res.validate;
      const $form = $(res.container);

      const $phoneNumber = $form.find('.jodit-ui-block__phone-number input');
      const $emailAddress = $form.find('.jodit-ui-block__email-address input');
      const $emailSubject = $form.find('.jodit-ui-block__email-subject input');
      const $emailBody = $form.find('.jodit-ui-block__email-body input');
      const $url = $form.find('.jodit-ui-block__url input');
      const $linkType = $form.find('.jodit-ui-block__link-type select');
      const $submitBtn = $form.find('button[type="submit"]');
      const $emailFields = [$emailAddress, $emailSubject, $emailBody];

      res.validate = function(... args) {
        const isValid = originalValidateFn.apply(this, args);
        if (!isValid) {
          return false;
        }

        const showFieldError = ($field, text) => {
          AGN.Lib.Form.cleanFieldFeedback$($field);
          AGN.Lib.Form.appendFeedbackMessage($field, text);
          AGN.Lib.Form.markField($field);
        }

        if ($linkType.val() === 'email') {
          const email = $emailAddress.val();
          if (!AGN.Lib.Helpers.isValidEmail(email)) {
            showFieldError($emailAddress, t('defaults.invalidEmail'));
            return false;
          }
        } else if ($linkType.val() === '') {
          const url = $url.val();
          if (!AGN.Lib.Helpers.isUrl(url)) {
            showFieldError($url, t('url.invalid'));
            return false;
          }
        }

        return true;
      }

      function updateSubmitBtn() {
        $submitBtn.prop('disabled',
          ($emailAddress.is(':visible') && !$emailAddress.val()) ||
          ($phoneNumber.is(':visible') && !$phoneNumber.val())
        );
      }

      function toggleInput($input, show = false) {
        $input.closest('.jodit-ui-block').toggle(show);
      }

      $linkType.on('change', function () {
        [... $emailFields, $phoneNumber].forEach($e => toggleInput($e));
        toggleInput($url, true);

        const linkType = $(this).val();

        if (linkType === 'phone') {
          toggleInput($phoneNumber, true);
          toggleInput($url);
        } else if (linkType === 'email') {
          $emailFields.forEach($e => toggleInput($e, true));
          toggleInput($url);
        } else {
          $url.val('');
        }

        updateSubmitBtn();
      });

      function updateMailLink() {
        const email = $emailAddress.val().trim();
        const subject = $emailSubject.val().trim();
        const body = $emailBody.val().trim();

        let link = '';

        if (email) {
          link = `mailto:${email}`;
          const params = [];

          if (subject) params.push(`subject=${encodeURIComponent(subject)}`);
          if (body) params.push(`body=${encodeURIComponent(body)}`);

          if (params.length) {
            link += `?${params.join('&')}`;
          }
        }

        $url.val(link);
      }

      [$emailAddress, $emailSubject, $emailBody].forEach($e =>
        $e.on('input', () => {
          updateMailLink();
          updateSubmitBtn();
        })
      )

      $phoneNumber.on('input', function () {
        const number = $(this).val();

        $url.val(number.length ? `tel:${number}` : '')
        updateSubmitBtn();
      });

      const url = $url.val();

      if (url.startsWith('tel:')) {
        $phoneNumber.val(url.substring(4).trim());
        $linkType.val('phone').trigger('change');
      } else if (url.startsWith('mailto:')) {
        const mailto = url.substring(7);

        const [emailPart, queryPart] = mailto.split('?', 2);

        $emailAddress.val(emailPart);

        if (queryPart) {
          const params = new URLSearchParams(queryPart);
          $emailSubject.val(params.get('subject') || '');
          $emailBody.val(params.get('body') || '');
        }

        $linkType.val('email').trigger('change');
      }

      return res;
    };
  }

  extendLinkPlugin();

})();
