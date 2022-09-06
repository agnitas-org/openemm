AGN.Lib.Controller.new('mailing-send', function() {
  var Form = AGN.Lib.Form,
    Modal = AGN.Lib.Modal,
    Confirm = AGN.Lib.Confirm,
    Page = AGN.Lib.Page,
    Template = AGN.Lib.Template;

  var ADMIN_TARGET_SINGLE_RECIPIENT = -1;

  var Helpers = {
    disableSendButtons: function() {
      $('#test-send-controls-group #adminSendButton').addClass('disabled');
      $('#test-send-controls-group #testSendButton').addClass('disabled');
    },
    enableSendButtons: function() {
      $('#test-send-controls-group #adminSendButton').removeClass('disabled');
      $('#test-send-controls-group #testSendButton').removeClass('disabled');
    },
    hideTestSendConfigBlock: function() {
      $('#test-send-controls-group').hide();
    },
    showTestSendConfigBlock: function() {
      $('#test-send-controls-group').show();
    },
    hideTestBlockMessage: function() {
      $('#test-send-status-block').hide();
    },
    showTestBlockMessage: function() {
      $('#test-send-status-block').show();
    }
  };

  this.addDomInitializer("test-run-recipients-select", function () {
    updateTestRunButtons(this.el);
  });

  this.addAction({
    'click': 'resume-sending'
  }, function() {
    var $e = this.el;
    var link = $e.data("link");

    var jqxhr = $.post(link);
    jqxhr.done(function (resp) {
      Page.render(resp);
      $e.closest('.form-group').remove();
    });
  });

  this.addAction({
    'click': 'configure-delivery-mailing-size-warning'
  }, function() {
    var $e = this.el;

    Confirm.createFromTemplate({}, 'warning-mailing-size-modal').done(function() {
      $e.prop('disabled', true);
      Page.reload($e.data('url'));
    });
  });

  this.addAction({
    'click': 'configure-delivery-mailing-size-error'
  }, function() {
    Modal.createFromTemplate({}, 'error-mailing-size-modal');
  });

  this.addAction({
    'click': 'configure-delivery'
  }, function() {
    this.el.prop('disabled', true);
    Page.reload(this.el.data('url'), true);
    this.el.prop('disabled', false);
  });

  this.addAction({
    'click': 'send-admin'
  }, function() {
    var action = this.el.data('action-value'),
        actionUrl = this.el.data('base-url'),
        form   = Form.get(this.el);

    Helpers.disableSendButtons();

    var jqxhr = $.post(actionUrl, {action:action, mailingID:form.getValue('mailingID')});

    jqxhr.done(function (resp) {
      form.updateHtml(resp);
    });
  });

  this.addAction({
    'click': 'send-test'
  }, function() {
    var action = this.el.data('action-value'),
        actionUrl = this.el.data('base-url'),
        form   = Form.get(this.el);

    if ($('#adminTargetGroupSelect').val() == ADMIN_TARGET_SINGLE_RECIPIENT) {
      var isTestRecipientsRequired = true;

      $('input[name="mailingTestRecipients"]').each(function() {
        var address = $(this).val();
        if (address && address.trim()) {
          isTestRecipientsRequired = false;
        }
      });

      if (isTestRecipientsRequired) {
        AGN.Lib.Messages(t('defaults.error'), t('error.enterEmailAddresses'), 'alert');
        return;
      }
    }

    Helpers.disableSendButtons();

    form.setValue('action', action);

    var jqxhr = $.post(actionUrl, $('#testDeliveryForm').serialize());

    jqxhr.done(function (resp) {
      form.updateHtml(resp);
    });
  });

  this.addAction({
    'click': 'send-world'
  }, function() {
    handleMailingAction(this.el);
  });

  this.addAction({
    'click': 'cancel-mailing'
  }, function() {
    handleMailingAction(this.el);
  });

  this.addAction({
    'click': 'resume-mailing'
  }, function() {
    handleMailingAction(this.el);
  });

  function handleMailingAction($el) {
    var baseUrl = $el.data('base-url'),
        form   = Form.get($el);

    form.jqxhr().done(function(resp) {
      form.updateHtml(resp);

      var History = window.history;
      History.replaceState(History.state || {}, document.title, baseUrl);
    });
  }

  this.addAction({
    'click': 'check-links'
  }, function() {
    var action = this.el.data('action-value'),
        actionUrl = this.el.data('base-url'),
        form   = Form.get(this.el);

    form.setValue('action', action);
    Helpers.disableSendButtons();

    var jqxhr = $.post(actionUrl, $('#testDeliveryForm').serialize());
    jqxhr.done(function (resp) {
      form.updateHtml(resp);
    });
  });

  this.addAction({
    'click': 'recipients-row-remove'
  }, function() {
    var emailAddresses,
      $emailAddressFields,
      $row = this.el.closest('tr'),
      $emailAddresses = $('[name="statusmailRecipients"]');

    $row.remove();
    $emailAddressFields = $('[name^="statusmailRecipient_"]');

    emailAddresses = _.map( $emailAddressFields, function(field) {
      return $(field).val();
    });

    $emailAddresses.val(emailAddresses.join(' '));
    Form.get(this.el).submit();
  });

  this.addAction({
    'click': 'recipients-row-add',
    'keydown': 'recipients-row-field'
  }, function() {

    if (this.event.type == 'keydown' && this.event.keyCode != 13) {
      return;
    }

    var emailAddresses,
      $email = $('#newStatusMail'),
      $emailAddressFields = $('[name^="statusmailRecipient_"]'),
      $emailAddresses = $('[name="statusmailRecipients"]');

    emailAddresses = _.map( $emailAddressFields, function(field) {
      return $(field).val();
    });

    emailAddresses.push($email.val());
    $emailAddresses.val(emailAddresses.join(' '));
    Form.get(this.el).submit().done(function() {
      $('#newStatusMail').trigger("focus");
    });
  });

  this.addInitializer('statusmailRecipients', function($scope) {
    var emailAddresses,
        $emailAddresses = $('[name="statusmailRecipients"]'),
        $target = $('#statusEmailContainer');

    $('.js-recipients-row').remove();

    if ($emailAddresses.val() == "") {
      return;
    }

    emailAddresses = $emailAddresses.val() || "";
    emailAddresses = emailAddresses.split(' ');

    _.each(emailAddresses, function(email) {
      if (email == "") {
          return;
      }

      $target.prepend(Template.text('recipients-row', { email: email }));
    })
  });

  this.addDomInitializer('transmission-status', function() {
    if (this.config.isRunning) {
      Helpers.hideTestSendConfigBlock();
      Helpers.showTestBlockMessage();
    } else {
      Helpers.showTestSendConfigBlock();
      Helpers.hideTestBlockMessage();
    }
  });

  this.addAction({click: 'add-test-recipient', enterdown: 'new-test-recipient'}, function() {
    this.event.preventDefault();

    var $currentRow = this.el.closest('tr');
    var $currentInput = this.el.is('input') ? this.el : $currentRow.find('input');
    var $newRow = Template.dom('test-recipient-row', {value: $currentInput.val() || ''});

    $currentRow.before($newRow);
    $currentInput.val('');

    AGN.runAll($newRow);

    $currentInput.focus();
  });

  this.addAction({enterdown: 'edit-test-recipient'}, function() {
    this.event.preventDefault();
  });

  this.addAction({click: 'remove-test-recipient'}, function() {
    var $tr = this.el.closest('tr');
    $tr.remove();
  });

  this.addAction({change: 'admin-target-group'}, function() {
    updateTestRunButtons(this.el);
  });

  function updateTestRunButtons(testRunDropdown) {
    if(testRunDropdown) {
      var isSingleRecipientValue = testRunDropdown.val() == ADMIN_TARGET_SINGLE_RECIPIENT;
      $('#test-recipients-table').toggleClass('hidden', (!isSingleRecipientValue));
      $('#adminSendButton').toggleClass('hidden', isSingleRecipientValue);
    }
  }

  var STORAGE_TIME_OF_SETTINGS_CACHE_MS = 60000;
  var settingsReceiptDate;
  var settingsResponse;

  this.addAction({click: 'save-security-settings'}, function() {
    var form = AGN.Lib.Form.get(this.el);
    var requiredAutoImportId = form.getValue('autoImportId');

    form.submit().done(function(resp) {
      if(resp.success === true) {
        AGN.Lib.JsonMessages(resp.popups);
        $('#close-security-settings').click();
        settingsReceiptDate -= STORAGE_TIME_OF_SETTINGS_CACHE_MS;
      } else {
        AGN.Lib.JsonMessages(resp.popups, true);
      }

      $("#activateMailingForm input[name='autoImportId']").val(requiredAutoImportId);
    });
  });

  this.addAction({click: 'load-security-settings'}, function() {
    var currentDate = new Date();

    if (!settingsReceiptDate || currentDate - settingsReceiptDate >= STORAGE_TIME_OF_SETTINGS_CACHE_MS) {
      var href = $(this.el).attr('href');

      if (href) {
        var jqxhr = $.get(href);
        jqxhr.done(function(resp) {
          Page.render(resp);
          settingsResponse = resp;
          settingsReceiptDate = new Date();

          initializeFormFields($('#security-settings-form'));
        });
      }
    } else {
      Page.render(settingsResponse);
      initializeFormFields($('#security-settings-form'));
    }
  });

  function initializeFormFields($form) {
    var form = AGN.Lib.Form.get($form);
    form.initFields();
  }

  this.addAction({change: 'prioritization-toggle'}, function() {
    toggleButton($(this.el), 'isPrioritizationDisallowed')
  });

  this.addAction({change: 'sendStatusOnErrorOnly-toggle'}, function() {
    toggleButton($(this.el), 'statusOnErrorEnabled')
  });

  function toggleButton($toggle, propertyName) {
    var isChecked = $toggle.prop('checked');

    // Disable toggle button until changes are saved.
    $toggle.prop('disabled', true);

    function failed() {
      // Failed to save changes, revert initial toggle button state.
      $toggle.prop('checked', !isChecked);
      AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
    }

    var data = {};
    data[propertyName] = $toggle.is(':checked')

    $.ajax({
      type: 'POST',
      url: $toggle.data('url'),
      data: data
    }).done(function(resp) {
      if (resp && resp.success) {
        AGN.Lib.Messages(t('defaults.success'), t('defaults.saved'), 'success');
      } else {
        failed();
      }
    }).fail(failed).always(function() {
      // Enable toggle button back.
      $toggle.prop('disabled', false);
    });
  }
});
