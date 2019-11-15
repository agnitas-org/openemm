AGN.Lib.Controller.new('recipient-view', function() {
  var RESULT_OK = 'OK';
  var RESULT_USED = 'USED';
  var RESULT_BLACKLISTED = 'BLACKLISTED';

  var Form = AGN.Lib.Form,
    Confirm = AGN.Lib.Confirm,
    Template = AGN.Lib.Template;

  var checker;

  this.addDomInitializer('recipient-view', function($e) {
    var form = Form.get($e);

    checker = new AddressChecker(this.config.urls.CHECK_ADDRESS, function(result) {
      switch (result) {
        case RESULT_OK:
          form.cleanFieldError('email');
          break;

        case RESULT_USED:
          form.showFieldError('email', t('error.inUse'), true);
          break;

        case RESULT_BLACKLISTED:
          form.showFieldError('email', t('error.isBlacklisted'), true);
          break;
      }
    });

    $('#recipient-email').on('change input', _.debounce(function() {
      checker.check($(this).val());
    }, 300));
  });

  this.addAction({
    submission: 'recipient-save'
  }, function() {
    var form = Form.get(this.el);
    var address = $('#recipient-email').val();

    if (checker && address && address.trim()) {
      checker.jqxhr(address.trim())
        .done(function(resp) {
          if (resp.isBlacklisted === true) {
            Confirm.create(Template.text('email-confirmation-modal', {question: t('recipient.blacklisted.question')}))
              .done(function() {
                if (resp.inUse === true) {
                  Confirm.create(Template.text('email-confirmation-modal', {question: t('recipient.duplicate.question')}))
                    .done(function() { form.submit(); });
                } else {
                  form.submit();
                }
              });
          } else if (resp.inUse === true) {
            Confirm.create(Template.text('email-confirmation-modal', {question: t('recipient.duplicate.question')}))
              .done(function() { form.submit(); });
          } else {
            form.submit();
          }
        }).fail(function() {
          form.submit();
        });
    } else {
      form.submit();
    }
  });

  function AddressChecker(url, callback) {
    this.url = url;
    this.running = false;
    this.callback = $.isFunction(callback) ? callback : $.noop;
    this.lastAddress = null;
    this.lastResult = null;
    this.scheduledCheck = null;
  }

  AddressChecker.prototype.jqxhr = function(address) {
    return $.get(this.url, {email: address});
  };

  AddressChecker.prototype.scheduleCheck = function(address) {
    var self = this;

    if (self.scheduledCheck) {
      clearTimeout(self.scheduledCheck);
    }

    if (self.running) {
      // Simply make the same call later — prevent ajax mess.
      self.scheduledCheck = setTimeout(function() { self.scheduleCheck(address); }, 300);
    } else {
      self.running = true;
      self.lastAddress = null;
      self.lastResult = null;

      try {
        self.jqxhr(address)
          .always(function() {
            self.running = false;
          }).done(function(resp) {
            self.lastAddress = address;
            self.lastResult = (resp.isBlacklisted === true ? RESULT_BLACKLISTED : (resp.inUse === true ? RESULT_USED : RESULT_OK));
            self.report();
          });
      } catch (e) {
        self.running = false;
      }
    }
  };

  AddressChecker.prototype.check = function(address) {
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
  };

  AddressChecker.prototype.report = function(result) {
    if (arguments.length) {
      this.callback.call(null, result);
    } else {
      if (this.lastAddress) {
        this.report(this.lastResult);
      }
    }
  };

});
