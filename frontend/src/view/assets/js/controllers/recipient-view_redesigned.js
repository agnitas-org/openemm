AGN.Lib.Controller.new('recipient-view', function () {

  const RESULT_OK = 'OK';
  const RESULT_USED = 'USED';
  const RESULT_BLACKLISTED = 'BLACKLISTED';

  const Form = AGN.Lib.Form;

  let checker;

  this.addDomInitializer('recipient-view', function () {
    const form = Form.get(this.el);

    checker = new AddressChecker(AGN.url(`/recipient/${this.config.id}/checkAddress.action`), result => {
      switch (result.status) {
        case RESULT_OK:
          form.cleanFieldError('email');
          break;

        case RESULT_USED:
          form.showFieldError('email', AGN.Lib.Template.text('duplicated-email-block', result), true);
          break;

        case RESULT_BLACKLISTED:
          form.showFieldError('email', t('error.isBlacklisted'), true);
          break;
      }
    });

    $('#recipient-email').on('change input', _.debounce(function () {
      checker.check($(this).val());
    }, 300));

    updateBindingsUserTypesProps();
  });

  function updateBindingsUserTypesProps() {
    $('.not-allowed-usertype').each(function () {
      const $el = $(this);

      if ($el.prop('selected')) {
        $el.parent('select').prop('disabled', true);
      } else {
        $el.prop('disabled', true);
      }
    });
  }

  this.addAction({submission: 'recipient-save'}, function () {
    const form = Form.get(this.el);
    const address = $('#recipient-email').val();

    this.event.preventDefault();

    const checkAltgMatchCallback = () => {
      $.post(AGN.url('/recipient/checkAltgMatch.action'), form.params())
        .done(response => {
          if (response.popups && response.popups.alert && response.popups.alert.length > 0) {
            AGN.Lib.JsonMessages(response.popups);
          } else if (!response.success || response.success.length === 0) {
            showConfirmModal(t('recipient.hide.question'), () => {
              form.setActionOnce(AGN.url('/recipient/saveAndBackToList.action'));
              form.submit();
            })
          } else {
            form.submit();
          }
        })
        .fail(() => form.submit())
    }

    if (checker && address && address.trim()) {
      checker.jqxhr(address.trim())
        .done(resp => {
          if (resp.isBlacklisted === true) {
            showConfirmModal(t('recipient.blacklisted.question'), () => {
              if (resp.inUse === true) {
                showConfirmModal(t('recipient.duplicate.question'), checkAltgMatchCallback);
              } else {
                checkAltgMatchCallback();
              }
            });
          } else if (resp.inUse === true) {
            showConfirmModal(t('recipient.duplicate.question'), checkAltgMatchCallback);
          } else {
            checkAltgMatchCallback();
          }
        }).fail(checkAltgMatchCallback);
    } else {
      checkAltgMatchCallback();
    }
  });

  function showConfirmModal(question, callback) {
    return AGN.Lib.Confirm.from('recipient-confirmation-modal', {question}).done(callback);
  }

  this.addAction({change: 'activate-mediatype'}, function () {
    const mediatypeCode = this.el.data('mediatype');
    const isChecked = this.el.is(":checked");

    this.el.closest('[data-mailinglist-tile]').find(`.icon-mediatype-${mediatypeCode}`).toggleClass('text-primary', isChecked);
    this.el.closest('.tile').find('[data-binding-usertype]').prop('disabled', !isChecked);
  });

  this.addAction({focusout: 'hide-datasource-popover'}, function () {
    AGN.Lib.Popover.hide(this.el);
  });

  class AddressChecker {
    constructor(url, callback) {
      this.url = url;
      this.running = false;
      this.callback = $.isFunction(callback) ? callback : $.noop;
      this.lastAddress = null;
      this.lastResult = null;
      this.scheduledCheck = null;
    }

    jqxhr(address) {
      return $.get(this.url, {email: address});
    }

    scheduleCheck(address) {
      if (this.scheduledCheck) {
        clearTimeout(this.scheduledCheck);
      }

      if (this.running) {
        // Simply make the same call later — prevent ajax mess.
        this.scheduledCheck = setTimeout(() => this.scheduleCheck(address), 300);
      } else {
        this.running = true;
        this.lastAddress = null;
        this.lastResult = null;

        try {
          this.jqxhr(address)
            .always(() => this.running = false)
            .done(resp => {
              this.lastAddress = address;
              this.lastResult = {
                status: (resp.isBlacklisted === true ? RESULT_BLACKLISTED : (resp.inUse === true ? RESULT_USED : RESULT_OK)),
                recipientID: resp.existingRecipientId || 0
              };
              this.report();
            });
        } catch (e) {
          this.running = false;
        }
      }
    }

    check(address) {
      address = address?.trim() || '';

      if (!address) {
        this.report(RESULT_OK);
      } else if (this.lastAddress === address) {
        this.report();
      } else {
        this.scheduleCheck(address);
      }
    }

    report(result) {
      if (arguments.length) {
        this.callback.call(null, result);
      } else {
        if (this.lastAddress) {
          this.report(this.lastResult);
        }
      }
    }
  }
});
