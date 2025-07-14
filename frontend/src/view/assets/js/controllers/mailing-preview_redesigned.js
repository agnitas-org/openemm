AGN.Lib.Controller.new('mailing-preview', function () {
  const Form = AGN.Lib.Form;
  const Messages = AGN.Lib.Messages;
  const Helpers = AGN.Lib.Helpers;
  const Select = AGN.Lib.Select;

  let MAILING_ID;

  this.addDomInitializer('mailing-preview', function () {
    MAILING_ID = this.config.MAILING_ID;
    const form = Form.get(this.el);
    form.initFields();
    form.initValidator();
    controlTestRunVisibility();
    if (isMobileSizeChosen(this.config, form)) {
      loadIframeMediapoolImagesForMobile();
    }
  });

  function loadIframeMediapoolImagesForMobile() {
    $('[name="previewFrame"]')
      .hide()
      .on('load', addMobileParameterForMediapoolImagesSrc)
      .show();
  }

  function addMobileParameterForMediapoolImagesSrc() {
    $(this.contentDocument || this.contentWindow.document)
      .find('img')
      .filter((i, img) => $(img).attr('src')?.includes('mediapool_element'))
      .each(function () {
        const originalSrc = $(this).attr('src');
        $(this).attr('src', `${originalSrc}${originalSrc.includes('?') ? '&' : '?'}forMobile=true`);
      });
  }

  function isMobileSizeChosen(config, form) {
    return config.MOBILE_SIZES.includes(parseInt(form.getValue('size')));
  }

  this.addAction({change: 'refresh-preview', click: 'update-preview'}, function () {
    updatePreview();
  });

  this.addAction({'change': 'change-stored-header-data'}, function () {
    updatePreview();
  });

  this.addAction({'change': 'change-header-data'}, function () {
    updatePreview();
  });

  function updatePreview() {
    const form = Form.get($('#preview-form'));
    form.submit();
  }

  function controlTestRunVisibility() {
    controlTestRunContainerVisibility();
    controlAddToTestRunBtnVisibility();
  }

  this.addAction({'click' : 'change-preview-customer-options'}, function() {
    controlAddToTestRunBtnVisibility();
  });
  
  function controlTestRunContainerVisibility() {
    const $testRecipients = getTestRecipients$();
    if (!$testRecipients.exists()) {
      return;
    }

    const hidden = $testRecipients.val().length <= 0;
    $('#personalized-test-run-container').toggleClass('hidden', hidden);
  }

  function getTestRecipients$() {
    return $('#personalized-test-recipients');
  }

  function controlAddToTestRunBtnVisibility() {
    const isVisible = !isRecipientChosenByTargetGroup() && currentRecipientAlreadyChosenForTestRun();
    $('[data-action="add-to-personalized-test-run"]').toggle(!!isVisible);
  }

  function currentRecipientAlreadyChosenForTestRun() {
    const previewRecipientEmail = getEmailOfCurrentPreviewRecipient();
    return Select.get(getTestRecipients$()).getSelectedValue().indexOf(previewRecipientEmail) === -1;
  }

  function getEmailOfCurrentPreviewRecipient() {
    if (isRecipientChosenByInput()) {
      return $('#customerEmail').val();
    }
    return $('[name="customerATID"]').find(':selected').data('email');
  }

  function getCurrentMode() {
    return $('[name="modeType"]').val();
  }

  function isRecipientChosenByInput() {
    return getCurrentMode() === 'MANUAL';
  }

  function isRecipientChosenByTargetGroup() {
    return getCurrentMode() === 'TARGET_GROUP';
  }

  function validateEmailOfCurrentPreviewRecipient() {
    return new Promise(function (resolve, reject) {
      const email = getEmailOfCurrentPreviewRecipient();
      if (!Helpers.isValidEmail(email)) {
        Messages.alert('defaults.invalidEmail');
        return;
      }
      $.get(AGN.url(`/mailing/preview/${MAILING_ID}/isRecipient.action`), {email})
        .done(resp => {
          if (resp.success) {
            resolve(email);
          } else {
            AGN.Lib.JsonMessages(resp.popups, true);
            reject();
          }
        });
    });
  }
  
  this.addAction({
    input: 'change-personalized-test-recipients',
    change: 'change-personalized-test-recipients'
  }, function() {
    controlTestRunVisibility();
  });

  this.addAction({'click': 'add-to-personalized-test-run'}, function () {
    validateEmailOfCurrentPreviewRecipient()
      .then(email => {
        const $testRecipients = getTestRecipients$();
        const testRecipientsSelect = Select.get($testRecipients);
        if (!testRecipientsSelect.hasOption(email)) {
          testRecipientsSelect.addOption(email);
        }
        testRecipientsSelect.selectOptions(_.union($testRecipients.val(), [email]));
        controlTestRunVisibility();
      })
  });

  this.addAction({'click': 'personalized-test-run'}, function () {
    const testRecipients$ = getTestRecipients$();
    const recipientIds = testRecipients$.val();
    $.post(AGN.url(`/mailing/send/${MAILING_ID}/personalized-test.action`), {mailingTestRecipients: recipientIds})
      .done((resp) => {
        if (resp && resp.success === true) {
          testRecipients$.val([]);
          AGN.Lib.CoreInitializer.run("select", testRecipients$);
          controlTestRunContainerVisibility();
        }
        AGN.Lib.JsonMessages(resp.popups, true);
      })
  });

  this.addAction({'click': 'download-attachment'}, function () {
    const $tempAnchor = $('<a>').attr('href', $('#attachments').val());
    $('body').append($tempAnchor);
    $tempAnchor[0].click();
    $tempAnchor.remove();
  });
});
