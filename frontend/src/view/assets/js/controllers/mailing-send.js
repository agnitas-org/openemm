AGN.Lib.Controller.new('mailing-send', function() {
  var Form = AGN.Lib.Form,
    Modal = AGN.Lib.Modal,
    Confirm = AGN.Lib.Confirm,
    Page = AGN.Lib.Page,
    Template = AGN.Lib.Template,
    Tooltip = AGN.Lib.Tooltip;

  var ADMIN_TARGET_SINGLE_RECIPIENT = -1;

  var Helpers = {
    disableSendButtons: function() {
      $('#test-send-controls-group').hide();
    },
    enableSendButtons: function() {
      $('#test-send-controls-group').show();
    }
  };

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
    Page.reload(this.el.data('url'));
  });

  this.addAction({
    'click': 'start-delivery'
  }, function() {
    var action = this.el.data('action-value'),
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
    form.submit();
  });

  this.addAction({
    'click': 'check-links'
  }, function() {
    var action = this.el.data('action-value'),
        form   = Form.get(this.el);

    Helpers.disableSendButtons();
    form.setValue('action', action);
    form.submit();
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
      $('#newStatusMail').focus();
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
      Helpers.disableSendButtons();
    } else {
      Helpers.enableSendButtons();
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
    Tooltip.remove($tr.all('[data-tooltip]'));
    $tr.remove();
  });

  this.addAction({change: 'admin-target-group'}, function() {
    var isSingleRecipientValue = this.el.val() == ADMIN_TARGET_SINGLE_RECIPIENT;
    $('#test-recipients-table').toggleClass('hidden', (!isSingleRecipientValue));
    $('#adminSendButton').toggleClass('hidden', isSingleRecipientValue);
  });

  this.addAction({change: 'prioritization-toggle'}, function() {
    var $toggle = this.el;
    var isChecked = $toggle.prop('checked');

    // Disable toggle button until changes are saved.
    $toggle.prop('disabled', true);

    function failed() {
      // Failed to save changes, revert initial toggle button state.
      $toggle.prop('checked', !isChecked);
      AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
    }

    $.ajax({
      type: 'POST',
      url: $toggle.data('url'),
      data: {
        isPrioritizationDisallowed: $toggle.is(':checked')
      }
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
  });
});
