AGN.Lib.Controller.new('mailing-send', function () {

  const Form = AGN.Lib.Form,
    Confirm = AGN.Lib.Confirm,
    Page = AGN.Lib.Page,
    Template = AGN.Lib.Template,
    Messages = AGN.Lib.Messages;

  const RECIPIENT_TEST_RUN_OPTION = 'RECIPIENT';
  const TARGET_TEST_RUN_OPTION = 'TARGET';
  const SEND_TO_SELF_TEST_RUN_OPTION = 'SEND_TO_SELF';

  let $testRecipientsTable;
  let approvalOptions;
  let config;

  const Helpers = {
    hideGreenMarks: function () {
      const $transmissionMark = $('.transmission-mark');
      $transmissionMark.parent().find('button').removeClass('hidden');
      $transmissionMark.remove();
    },
    updateWorkStatus: function (workStatus, tooltip) {
      const $statusIcon = Template.dom('mailing-workstatus-icon', {workStatus, tooltip});
      $('#workstatus-icon').replaceWith($statusIcon);
      AGN.Lib.CoreInitializer.run('tooltip', $statusIcon);
    }
  };

  this.addDomInitializer("test-mailing", function () {
    $testRecipientsTable = $('#test-recipients-table');
    drawTestRecipientsTable(this.config.testRecipients);

    approvalOptions = JSON.parse(this.config ? this.config.approvalOptions : '[]');
    controlTestRunOptionsDisplaying();
    $('#get-approval-switch').on('change', function () {
      controlTestRunOptionsDisplaying();
    });
    controlTestRunRecipientsDisplaying();
    $('#test-run-options').on('change', function () {
      controlTestRunRecipientsDisplaying();
    });
  });

  function controlTestRunOptionsDisplaying() {
    const filterOptions = $('#get-approval-switch').prop('checked');
    const $optionsSelect = $("#test-run-options");
    $optionsSelect.find("option:not([value='" + approvalOptions.join("']):not([value='") + "']")
      .attr('disabled', filterOptions);
    if (filterOptions && approvalOptions.indexOf($optionsSelect.val()) === -1) {
      $optionsSelect.val($optionsSelect.find("option:not(:disabled):first").val()).trigger('change')
    }
  }

  function controlTestRunRecipientsDisplaying() {
    const $testRunDropdown = $('#test-run-options');
    if (!$testRunDropdown.exists()) {
      return;
    }
    const isSingleRecipientOption = $testRunDropdown.val() == RECIPIENT_TEST_RUN_OPTION;
    const isTargetIdRunOption = $testRunDropdown.val() == TARGET_TEST_RUN_OPTION;
    const isSendToSelfRunOption = $testRunDropdown.val() == SEND_TO_SELF_TEST_RUN_OPTION;
    const newTargetElementsHidden = !isSingleRecipientOption || !$('#save-target-toggle').prop('checked');

    $testRecipientsTable.parent().toggleClass('hidden', (!isSingleRecipientOption));
    $('#adminSendButton').parent().toggleClass('hidden', isSingleRecipientOption || isTargetIdRunOption || isSendToSelfRunOption);
    $('#testTargetSaveButton').parent().toggleClass('hidden', newTargetElementsHidden);
    $('#test-run-target-name-input').toggleClass('hidden', newTargetElementsHidden);
    // prevent hidden fields submit
    $('#testRunOptionSelect').prop('disabled', !isTargetIdRunOption);
    $testRecipientsTable.find('input').prop('disabled', !isSingleRecipientOption);
  }

  this.addDomInitializer("send-mailing", function () {
    config = this.config;
    const decimalSeparator = window.adminLocale === 'en-US' ? ',' : '.';

    _.each($('.commaSplitInput'), e => {
      const $e = $(e);
      const separatedText = $e.val().replace(/\B(?=(\d{3})+(?!\d))/g, decimalSeparator);

      $e.val(separatedText);
    });

    clearMaxRecipientsValueIfZero();

    if (config.approximateMaxDeliverySize > config.errorSizeThreshold) {
      Messages.warnText(Template.text('delivery-size-error-msg'));
    }
  });

  this.addAction({click: 'check-links'}, function () {
    $.post(config.urls.CHECK_LINKS).done(resp => AGN.Lib.RenderMessages($(resp)));
  });

  this.addAction({click: 'send-world'}, function () {
    if (config.approximateMaxDeliverySize > config.errorSizeThreshold) {
      Messages.alertText(Template.text('delivery-size-error-msg'));
      return;
    }

    const formSubmissionCallback = () => {
      const form = Form.get(this.el);
      form.setActionOnce(AGN.url('/mailing/send/confirm-send-world.action'));
      form.submit('confirm').done(resp => {
        Confirm.create(resp)
          .done(resp => form.updateHtml(resp));
      });
    };

    if (config.approximateMaxDeliverySize > config.warningSizeThreshold) {
      Confirm.from('warning-mailing-size-modal').done(formSubmissionCallback);
      return;
    }

    formSubmissionCallback();
  });

  const drawTestRecipientsTable = function (recipients) {
    if (!recipients.length) {
      const $row = Template.dom('test-recipient-row', {value: '', newRow: true, sent: false});
      $testRecipientsTable.append($row);
      AGN.runAll($row);

      return;
    }

    for (let i = 0; i < recipients.length; i++) {
      const isLastRow = i === recipients.length - 1;
      const $row = Template.dom('test-recipient-row', {value: recipients[i], newRow: isLastRow, sent: true});
      $testRecipientsTable.append($row);
      AGN.runAll($row);
    }
  }

  this.addAction({change: 'save-interval-settings'}, function () {
    const form = Form.get(this.el);
    form.setActionOnce(AGN.url('/mailing/send/interval.action'));
    form.submit();
  });

  this.addAction({'click': 'resume-sending'}, function () {
    const $el = this.el;
    const link = $el.data("link");

    $.post(link).done(resp => {
      Page.render(resp);
      $el.parent().remove();
    });
  });

  this.addDomInitializer("status-mail-recipients", function () {
    const recipients = this.config.recipients || '';
    const $container = $('#status-emails-block');

    recipients.split(' ')
      .filter(email => email)
      .forEach(email => $container.append(Template.text('status-mail-recipient-row', {email, newRow: false})));

    $container.append(Template.text('status-mail-recipient-row', {email: '', newRow: true}));
  });

  this.addAction({
    click: 'status-mail-recipient-row-delete'
  }, function () {
    const $row = this.el.closest('[data-status-mail-recipient-row]');
    const mailingId = $row.data('mailing-id');

    $row.remove();
    saveStatusEmails(mailingId, collectStatusEmails());
  });

  this.addAction({
    click: 'status-mail-recipient-row-add',
    enterdown: 'status-mail-recipient-change'
  }, function () {
    this.event.preventDefault();

    const $row = this.el.closest('[data-status-mail-recipient-row]');
    const $emailInput = $row.find('input');
    const email = $emailInput.val();

    if (email) {
      saveStatusEmails($row.data('mailing-id'), collectStatusEmails(true), () => {
        $row.before(Template.text('status-mail-recipient-row', {email: email, newRow: false}));
        $emailInput.val('');
      });
    }
  });

  this.addAction({enterdown: 'send-mailing'}, function () {
    this.event.preventDefault();
    $('#send-btn').trigger('click');
  });

  const collectStatusEmails = (includeNewRow = false) => {
    let $inputs = $('[data-status-mail-recipient-row]').find('input');

    if (!includeNewRow) {
      $inputs = $inputs.not(':last');
    }

    return $inputs.map(function () {
      return $(this).val().trim();
    }).get().join(' ');
  }

  const saveStatusEmails = (mailingId, emails, callback) => {
    $.post(AGN.url(`/mailing/send/${mailingId}/save-statusmail-recipients.action`), {emails})
      .done(resp => {
        if (resp.success && callback) {
          callback();
        }
        AGN.Lib.JsonMessages(resp.popups);
      });
  }

  this.addDomInitializer('delivery-status-view', function () {
    if (!this.config.isTransmissionRunning) {
      Helpers.hideGreenMarks();
    }

    const workStatus = this.config.workStatus;
    const workStatusTooltip = this.config.workStatusTooltip;
    if (workStatus && workStatusTooltip) {
      Helpers.updateWorkStatus(workStatus, workStatusTooltip);
    }

    if (this.config.deliveryStat) {
      const config = _.extend(this.config.deliveryStat, this.config.deliveryStatExtraInfo);
      renderButtonsBasedOnDeliveryStatus(config);
    }
  });

  function renderButtonsBasedOnDeliveryStatus(config) {
    const $buttons = Template.dom('delivery-status-buttons', config);
    $('.delivery-status-action').remove();
    $('.header__actions').append($buttons);
  }

  this.addAction({'click': 'start-delivery'}, function () {
    if ($('#test-run-options').val() == RECIPIENT_TEST_RUN_OPTION) {
      const existsAnyEmailAddress = $('input[name="mailingTestRecipients"]')
        .get()
        .some(input => $(input).val().trim());

      if (!existsAnyEmailAddress) {
        Messages.alert('error.enterEmailAddresses');
        return;
      }
    }

    $('#adminSendButton').addClass('disabled');
    $('#testSendButton').addClass('disabled');

    const form = Form.get(this.el);
    form.setActionOnce(this.el.data('url'));
    form.submit();
  });

  this.addAction({click: 'add-test-recipient', enterdown: 'new-test-recipient'}, function () {
    this.event.preventDefault();

    const $currentRow = this.el.closest('[data-test-recipient-row]');
    const $currentInput = this.el.is('input') ? this.el : $currentRow.find('input');
    const $newRow = Template.dom('test-recipient-row', {value: $currentInput.val() || '', newRow: false, sent: false});

    $currentRow.before($newRow);
    $currentInput.val('');

    AGN.runAll($newRow);

    $currentInput.focus();
    changeSaveTestRunTargetBtnState(false);
  });

  this.addAction({click: 'remove-test-recipient'}, function () {
    const $tr = this.el.closest('[data-test-recipient-row]');
    $tr.remove();
    changeSaveTestRunTargetBtnState(false);
  });

  this.addAction({input: 'new-test-recipient'}, function () {
    changeSaveTestRunTargetBtnState(false);
  });

  this.addAction({input: 'edit-test-recipient', enterdown: 'edit-test-recipient'}, function () {
    this.event.preventDefault();
    changeSaveTestRunTargetBtnState(false);
  });

  this.addAction({input: 'edit-test-run-target-name'}, function () {
    changeSaveTestRunTargetBtnState(false);
  });

  this.addAction({enterdown: 'edit-test-run-target-name'}, function () {
    this.event.preventDefault();
  });

  function changeSaveTestRunTargetBtnState(saved) {
    const $testRunTargetBtn = $('#testTargetSaveButton');
    const $btnIcon = $testRunTargetBtn.find('i');
    if (saved) {
      $btnIcon.hide();
    }
    $testRunTargetBtn.toggleClass('btn-primary', !saved, 500);
    $testRunTargetBtn.toggleClass('btn-success', saved, 500);
    if (saved) {
      $testRunTargetBtn.css({'cursor': 'not-allowed', 'pointer-events': 'none'});
    } else {
      $testRunTargetBtn.css({'cursor': '', 'pointer-events': ''});
    }
    $btnIcon.toggleClass('icon-save', !saved);
    $btnIcon.toggleClass('icon-check', saved);
    $btnIcon.show(500);
  }

  this.addAction({click: 'save-target-for-test-run'}, function () {
    if (!isValidTestTargetNameField()) {
      return;
    }
    saveTargetName();
  });

  function isValidTestTargetNameField() {
    const $targetName = $('input[name="targetName"]');
    const form = Form.get($targetName);
    const errors = AGN.Lib.Validator.get('length').errors($targetName, {min: 3, required: true});

    if (!errors.length) {
      form.cleanFieldFeedback();
      return true;
    }

    errors.forEach(error => form.showFieldError$($targetName, error.msg));
    return false;
  }

  function saveTargetName() {
    $.ajax({
      type: 'POST',
      url: AGN.url("/mailing/send/test/saveTarget.action"),
      data: {
        'mailingTestRecipients': collectTestRunEmails(),
        'targetName': $('input[name="targetName"]').val().trim()
      }
    }).done(resp => {
      if (resp.success === true) {
        changeSaveTestRunTargetBtnState(true);
      } else {
        AGN.Lib.JsonMessages(resp.popups);
      }
    });
  }

  function collectTestRunEmails() {
    return $('input[name="mailingTestRecipients"]')
      .map(function () {
        return this.value.trim();
      })
      .get()
      .filter(email => email);
  }

  this.addAction({focusout: 'max-recipients-change'}, function () {
    clearMaxRecipientsValueIfZero();
  });

  function clearMaxRecipientsValueIfZero() {
    const $maxRecipients = $('#maxRecipients');

    if ($maxRecipients.exists() && $maxRecipients.val() === '0') {
      $maxRecipients.val('');
    }
  }

  this.addAction({change: 'prioritization-toggle'}, function () {
    toggleButton($(this.el), 'isPrioritizationDisallowed')
  });

  this.addAction({change: 'encrypted-send-toggle'}, function () {
    toggleButton($(this.el), 'isEncryptedSend')
  });

  this.addAction({change: 'sendStatusOnErrorOnly-toggle'}, function () {
    toggleButton($(this.el), 'statusOnErrorEnabled')
  });

  function toggleButton($toggle, propertyName) {
    const isChecked = $toggle.prop('checked');

    // Disable toggle button until changes are saved.
    $toggle.prop('disabled', true);

    const failed = () => {
      // Failed to save changes, revert initial toggle button state.
      $toggle.prop('checked', !isChecked);
      Messages.defaultError();
    }

    const data = {};
    data[propertyName] = $toggle.is(':checked')

    $.ajax({
      type: 'POST',
      url: $toggle.data('url'),
      data: data
    }).done(resp => {
      if (resp && resp.success) {
        Messages.defaultSaved();
      } else {
        failed();
      }
    })
      .fail(failed)
      .always(() => $toggle.prop('disabled', false));
  }
});