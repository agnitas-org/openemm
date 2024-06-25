AGN.Lib.Controller.new('recipient-view', function () {

  const RESULT_OK = 'OK';
  const RESULT_USED = 'USED';
  const RESULT_BLACKLISTED = 'BLACKLISTED';

  const Form = AGN.Lib.Form
  const Confirm = AGN.Lib.Confirm;
  const Template = AGN.Lib.Template;

  let checker;
  let isActiveSaveSubmenu = false;
  let config;

  this.addDomInitializer('recipient-view', function ($e) {
    const form = Form.get($e);
    config = this.config;

    checker = new AddressChecker(config.urls.CHECK_ADDRESS, function (result) {
      switch (result.status) {
        case RESULT_OK:
          form.cleanFieldError('email');
          break;

        case RESULT_USED:
          const btnUrl = config.urls.EXISTING_USER_URL_PATTERN.replace(':recipientID:', result.recipientID);
          const messageErrorInUseUser = `
            <div class="duplicated-recipient-block">
                ${t('error.inUse')}
                <a href="${btnUrl}" class="btn btn-sm btn-inverse text-nowrap">${t('recipient.existing.btn')}</a>
            </div>
          `;

          form.showFieldError('email', messageErrorInUseUser, true);
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

  this.addAction({
    submission: 'recipient-save'
  }, function () {
    const form = Form.get(this.el);
    const address = $('#recipient-email').val();
    this.event.preventDefault();
    const checkAltgMatchCallback = () => {
      $.post(config.urls.CHECK_MATCH_ALTG, form.params())
        .done(function (response) {
          if (response.popups && response.popups.alert && response.popups.alert.length > 0) {
            AGN.Lib.JsonMessages(response.popups);
          } else if (!response.success || response.success.length === 0) {
            Confirm.create(createConfirmModal(t('recipient.hide.question')))
              .done(() => {
                form.setActionOnce(config.urls.SAVE_AND_BACK_TO_LIST);
                form.submit();
              })
          } else {
            form.submit();
          }
        })
        .fail(() => {
          form.submit();
        })
    }

    if (checker && address && address.trim()) {
      checker.jqxhr(address.trim())
        .done(resp => {
          if (resp.isBlacklisted === true) {
            Confirm.create(createConfirmModal(t('recipient.blacklisted.question')))
              .done(() => {
                if (resp.inUse === true) {
                  Confirm.create(createConfirmModal(t('recipient.duplicate.question')))
                    .done(checkAltgMatchCallback);
                } else {
                  checkAltgMatchCallback();
                }
              });
          } else if (resp.inUse === true) {
            Confirm.create(createConfirmModal(t('recipient.duplicate.question')))
              .done(checkAltgMatchCallback);
          } else {
            checkAltgMatchCallback();
          }
        }).fail(checkAltgMatchCallback);
    } else {
      checkAltgMatchCallback();
    }
  });

  function createConfirmModal(question) {
    return Template.text('recipient-confirmation-modal', {question: question});
  }

  this.addAction({
    change: 'activate-mediatype'
  }, function () {
    const mediatypeCode = this.el.data('mediatype');
    const isChecked = this.el.is(":checked");

    this.el.closest('[data-mailinglist-tile]').find(`.icon-mediatype-${mediatypeCode}`).toggleClass('text-primary', isChecked);
    this.el.closest('.tile').find('[data-binding-usertype]').prop('disabled', !isChecked);
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
      const self = this;

      if (self.scheduledCheck) {
        clearTimeout(self.scheduledCheck);
      }

      if (self.running) {
        // Simply make the same call later — prevent ajax mess.
        self.scheduledCheck = setTimeout(function () {
          self.scheduleCheck(address);
        }, 300);
      } else {
        self.running = true;
        self.lastAddress = null;
        self.lastResult = null;

        try {
          self.jqxhr(address)
            .always(function () {
              self.running = false;
            }).done(function (resp) {
            self.lastAddress = address;
            self.lastResult = {
              status: (resp.isBlacklisted === true ? RESULT_BLACKLISTED : (resp.inUse === true ? RESULT_USED : RESULT_OK)),
              recipientID: resp.existingRecipientId || 0
            };
            self.report();
          });
        } catch (e) {
          self.running = false;
        }
      }
    }

    check(address) {
      address = address ? address.trim() : '';

      if (address) {
        if (this.lastAddress == address) {
          this.report();
        } else {
          this.scheduleCheck(address);
        }
      } else {
        this.report(RESULT_OK);
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
