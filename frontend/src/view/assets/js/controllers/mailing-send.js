AGN.Lib.Controller.new('mailing-send', function() {
    const Form = AGN.Lib.Form,
        Modal = AGN.Lib.Modal,
        Confirm = AGN.Lib.Confirm,
        Page = AGN.Lib.Page,
        Template = AGN.Lib.Template;

    const RECIPIENT_TEST_RUN_OPTION = 'RECIPIENT';
    const TARGET_TEST_RUN_OPTION = 'TARGET';
    const SEND_TO_SELF_TEST_RUN_OPTION = 'SEND_TO_SELF';

    const Helpers = {
        disableSendButtons: function () {
            $('#test-send-controls-group #adminSendButton').addClass('disabled');
            $('#test-send-controls-group #testSendButton').addClass('disabled');
        },
        enableSendButtons: function () {
            $('#test-send-controls-group #adminSendButton').removeClass('disabled');
            $('#test-send-controls-group #testSendButton').removeClass('disabled');
        },
        hideGreenMarks: function () {
            $('.transmission-mark').remove();
            $('#recipient-add-btn, .recipient-remove-btn').removeClass('hidden');
        },
        updateWorkStatus: function (workStatus, tooltip) {
            const $statusIcon = $(Template.text('mailing-workstatus-icon', {workstatus: workStatus, tooltip: tooltip}));
            $('#workstatus-icon').replaceWith($statusIcon);
        }
    };

    this.addAction({
        'click': 'configure-delivery-mailing-size-warning'
    }, function() {
        const $e = this.el;

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
        'click': 'resume-sending'
    }, function() {
        const $e = this.el;
        const link = $e.data("link");

        const jqxhr = $.post(link);
        jqxhr.done(function (resp) {
            Page.render(resp);
            $e.closest('.form-group').remove();
        });
    });

    this.addAction({
        'click': 'configure-delivery'
    }, function() {
        this.el.prop('disabled', true);
        Page.reload(this.el.data('url'), true);
        this.el.prop('disabled', false);
    });

    this.addAction({
        'click': 'start-delivery'
    }, function() {
        if ($('#test-run-options').val() == RECIPIENT_TEST_RUN_OPTION) {
            var isTestRecipientsRequired = true;

            $('input[name="mailingTestRecipients"]').each(function() {
                const address = $(this).val();
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

        const form = Form.get(this.el);
        form.setActionOnce(this.el.data('url'));
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

    this.addDomInitializer('delivery-status-view', function() {
        if (!this.config.isTransmissionRunning) {
            Helpers.hideGreenMarks();
        }

        const workStatus = this.config.workStatus;
        const workStatusTooltip = this.config.workStatusTooltip;
        if (workStatus && workStatusTooltip) {
            Helpers.updateWorkStatus(workStatus, workStatusTooltip);
        }
    });

    this.addAction({click: 'add-test-recipient', enterdown: 'new-test-recipient'}, function() {
        this.event.preventDefault();

        const $currentRow = this.el.closest('tr');
        const $currentInput = this.el.is('input') ? this.el : $currentRow.find('input');
        const $newRow = Template.dom('test-recipient-row', {value: $currentInput.val() || ''});

        $currentRow.before($newRow);
        $currentInput.val('');

        AGN.runAll($newRow);

        $currentInput.focus();
        changeSaveTestRunTargetBtnState(false);
    });

    this.addAction({input: 'new-test-recipient'}, function() {
        changeSaveTestRunTargetBtnState(false);
    });
    
    this.addAction({input: 'edit-test-recipient', enterdown: 'edit-test-recipient'}, function() {
        this.event.preventDefault();
        changeSaveTestRunTargetBtnState(false);
    });
    
    this.addAction({input: 'edit-test-run-target-name'}, function() {
        changeSaveTestRunTargetBtnState(false);
    });
    
    this.addAction({enterdown: 'edit-test-run-target-name'}, function() {
        this.event.preventDefault();
    });

    this.addAction({click: 'remove-test-recipient'}, function() {
        const $tr = this.el.closest('tr');
        $tr.remove();
        changeSaveTestRunTargetBtnState(false);
    });


  var approvalOptions;

  this.addDomInitializer("test-run-recipients", function () {
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
    if (!$testRunDropdown.length) {
      return;
    }
    const isSingleRecipientOption = $testRunDropdown.val() == RECIPIENT_TEST_RUN_OPTION;
    const isTargetIdRunOption = $testRunDropdown.val() == TARGET_TEST_RUN_OPTION;
    const isSendToSelfRunOption = $testRunDropdown.val() == SEND_TO_SELF_TEST_RUN_OPTION;
    const newTargetElementsHidden = !isSingleRecipientOption || !$('#save-target-toggle').prop('checked');
    $('#test-recipients-table').toggleClass('hidden', (!isSingleRecipientOption));
    $('#adminSendButton').toggleClass('hidden', isSingleRecipientOption || isTargetIdRunOption || isSendToSelfRunOption);
    $('#testTargetSaveButton').toggleClass('hidden', newTargetElementsHidden);
    $('#test-run-target-name-input').toggleClass('hidden', newTargetElementsHidden);
    // prevent hidden fields submit
    $('#adminTargetGroupSelect').prop('disabled', !isTargetIdRunOption);
    $('#test-recipients-table input').prop('disabled', !isSingleRecipientOption);
  }

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

    function isValidTestTargetNameField() {
        const $targetName = $('input[name="targetName"]');
        const errors = AGN.Lib.Validator.get('length').errors($targetName, {min: 3, required: true});
        const form = Form.get($targetName);
        if (!errors.length) {
            form.cleanErrors();
            return true;
        }
        errors.forEach(function(error) {
            form.showFieldError$($targetName, error.msg);
        })
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
        }).done(function(resp) {
            if (resp.success === true) {
                changeSaveTestRunTargetBtnState(true);
            } else {
                AGN.Lib.JsonMessages(resp.popups);
            }
        });
    }

    this.addAction({click: 'save-target-for-test-run'}, function() {
        if (!isValidTestTargetNameField()) {
            return;
        }
        saveTargetName();
    });

    function collectTestRunEmails() {
        return $('input[name="mailingTestRecipients"]')
            .map(function() {
                return this.value.trim();
            }).get()
            .filter(function(email) {
                return email
            });
    }

    function initializeFormFields($form) {
        const form = AGN.Lib.Form.get($form);
        form.initFields();
    }

    this.addAction({change: 'prioritization-toggle'}, function() {
        toggleButton($(this.el), 'isPrioritizationDisallowed')
    });

    this.addAction({change: 'encrypted-send-toggle'}, function() {
        toggleButton($(this.el), 'isEncryptedSend')
    });

    this.addAction({change: 'sendStatusOnErrorOnly-toggle'}, function() {
        toggleButton($(this.el), 'statusOnErrorEnabled')
    });

    function toggleButton($toggle, propertyName) {
        const isChecked = $toggle.prop('checked');

        // Disable toggle button until changes are saved.
        $toggle.prop('disabled', true);

        function failed() {
            // Failed to save changes, revert initial toggle button state.
            $toggle.prop('checked', !isChecked);
            AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
        }

        const data = {};
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

    AGN.Lib.Controller.new('mailing-deletion-modal', function() {
        this.addDomInitializer('mailing-deletion-modal', function() {
            this.el.find('.js-confirm-positive').on("click", function () {
                const $deliveryStatusBox = $('#delivery-status-box');
                if ($deliveryStatusBox.exists()) {
                    AGN.Lib.Load.get($deliveryStatusBox).stop();
                }
            });
        });
    });
});

AGN.Lib.Controller.new('delivery-settings-view', function() {
    this.addDomInitializer('delivery-settings-view', function() {
        var decimalSeparator;

        if (this.config.adminLocale === 'en_US') {
            decimalSeparator = ",";
        } else {
            decimalSeparator = ".";
        }

        const $maxRecipients = $('#maxRecipients');
        $maxRecipients.val('');

        $maxRecipients.focusout(function(){
            if ($(this).val() === '0') {
                $maxRecipients.val('');
            }
        });

        $('#sendBtn').mousedown(function() {
            if ($maxRecipients.val() === '') {
                $maxRecipients.val('0');
            }
        });

        _.each($('.commaSplitLabel'), function(elem) {
            const $e = $(elem);
            const separatedText = $e.text().replace(/\B(?=(\d{3})+(?!\d))/g, decimalSeparator);

            $e.text(separatedText);
        });
    });
});