AGN.Lib.Controller.new('mailing-preview', function () {
  const Form = AGN.Lib.Form;
  const Storage = AGN.Lib.Storage;
  const Messages = AGN.Lib.Messages;
  const Helpers = AGN.Lib.Helpers;
  const Select = AGN.Lib.Select;

  const TRIGGERED_FIELDS =  ['customerATID', 'customerEmail', 'modeType', 'targetGroupId'];

  let MAILING_ID;
  let needReload = false;

  this.addDomInitializer('mailing-preview', function ($frame) {
    MAILING_ID = this.config.MAILING_ID;

    restoreFields();
    var form = Form.get(this.el);
    if (form.getValue('reload') == 'false') {
      form.setValue('reload', true);

      form.submit().done(() => {
        $('[data-stored-field]').on('change', function () {
          const $field = $(this);
          Storage.saveChosenFields($field);
          //necessary to prevent endless reloading after change fields with data-action='change-stored-header-data'
          needReload = needReload || isTriggeredField($field);
        });

        form.initFields();
        form.initValidator();
        controlTestRunVisibility();
      });
    }
  });

  this.addAction({change: 'refresh-preview', click: 'update-preview'}, function () {
    needReload = false;
    updatePreview();
  });

  this.addAction({'change': 'change-stored-header-data'}, function () {
    if (needReload) {
      needReload = false;
      updatePreview();
    }
  });

  this.addAction({'change': 'change-header-data'}, function () {
    updatePreview();
  });

  function updatePreview() {
    const form = Form.get($('#preview-form'));
    form.setValue('reload', false);
    form.submit();
  }

  function restoreFields() {
    $("[data-stored-field]").each(function () {
      Storage.restoreChosenFields($(this))
    });
  }

  function isTriggeredField($field) {
    const fieldName = $field.prop('name');
    return fieldName && TRIGGERED_FIELDS.includes(fieldName);
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
    const previewRecipientEmail = getEmailOfCurrentPreviewRecipient();
    const isVisible = Select.get(getTestRecipients$()).getSelectedValue().indexOf(previewRecipientEmail) === -1;
    $('[data-action="add-to-personalized-test-run"]').toggle(!!isVisible);
  }

  function getEmailOfCurrentPreviewRecipient() {
    if (isRecipientChosenByInput()) {
      return $('#customerEmail').val();
    }
    return $('[name="customerATID"]').find(':selected').data('email');
  }

  function isRecipientChosenByInput() {
    return $('[name="modeType"]').val() === 'MANUAL';
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
});
