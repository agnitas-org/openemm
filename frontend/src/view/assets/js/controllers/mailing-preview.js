AGN.Lib.Controller.new('mailing-preview', function() {
    var Form = AGN.Lib.Form,
        Storage = AGN.Lib.Storage;

    var RECIPIENT_MODE, TARGET_MODE;
    var MAILING_ID;
    var needReload = false;

    this.addDomInitializer('mailing-preview', function($frame) {
        var config = this.config;
        RECIPIENT_MODE = config.RECIPIENT_MODE;
        TARGET_MODE = config.TARGET_MODE;
        MAILING_ID = config.MAILING_ID;

        restoreFields();
        var form = Form.get($('#container-preview'));
        if (form.getValue('reload') == 'false') {
            form.setValue('reload', true);

            form.submit().done(function() {
                $('[data-stored-field]').on('change', function() {
                    var $field = $(this);
                    Storage.saveChosenFields($field);
                    //necessary to prevent endless reloading after change fields with data-action='change-stored-header-data'
                    needReload = needReload || isTriggeredField($field.prop('name'));
                });

                form.initFields();
                form.initValidator();
                // form.initFields() changes fields displaying and its height, so view should be called to redraw preview block height.
                AGN.Lib.CoreInitializer.run('view');
                controlTestRunVisibility();
            });
        }
    });

    this.addAction({'click' : 'refresh-preview'}, function() {
        needReload = false;
        updatePreview();
    });

    this.addAction({'change': 'change-stored-header-data'}, function() {
        if (needReload) {
            needReload = false;
            updatePreview();
        }
    });

    this.addAction({'change': 'change-header-data'}, function() {
        updatePreview();
    });

    this.addAction({'click': 'toggle-tab-recipientMode'}, function() {
        changePreviewMode(RECIPIENT_MODE);
    });

    this.addAction({'click': 'toggle-tab-targetGroupMode'}, function(){
        changePreviewMode(TARGET_MODE);
    });

    function changePreviewMode(value) {
        var $field = $('[name="modeTypeId"]');
        if ($field.exists()) {
            $field.val(value);
            $field.trigger('change');
        }
    }

    function updatePreview(formCallback) {
        var form = Form.get($('#preview'));
        if (formCallback && typeof(formCallback) == 'function') {
            formCallback(form);
        }
        form.setValue('reload', false);
        form.setResourceSelectorOnce('#preview');
        form.submit();
    }

    function restoreFields() {
        var needReload = false;

        $("[data-stored-field]").each(function() {
            var $e = $(this),
                name = $e.prop('name');

            if (!isTriggeredField(name)) {
                if (Storage.restoreChosenFields($e)) {
                    needReload = true;
                }
            }
        });

        synchronizeModeWithActiveTab();

        Storage.restoreChosenFields($("[name='modeTypeId']"));
        Storage.restoreChosenFields($("[name='customerATID']"));
        Storage.restoreChosenFields($("[name='customerEmail']"));
        Storage.restoreChosenFields($("[name='targetGroupId']"));
    }

    function synchronizeModeWithActiveTab() {
        // Restore value by active tab
        var targetMode = Storage.get('toggle_tab#preview-targetModeContent');
        if (targetMode && !targetMode.hidden) {
            $("[name='modeTypeId']").val(TARGET_MODE);
            Storage.saveChosenFields($("[name='modeTypeId']"));
        }

        var recipientMode = Storage.get('toggle_tab#preview-recipientModeContent');
        if (recipientMode && !recipientMode.hidden) {
            $("[name='modeTypeId']").val(RECIPIENT_MODE);
            Storage.saveChosenFields($("[name='modeTypeId']"));
        }
    }

    function isTriggeredField(fieldName) {
        if (!fieldName) {
            return false;
        }

        return fieldName == 'customerATID' ||
            fieldName == 'customerEmail' ||
            fieldName == 'modeTypeId' ||
            fieldName == 'targetGroupId';
    }

    function controlTestRunVisibility() {
      controlTestRunContainerVisibility();
      controlAddToTestRunBtnVisibility();
    }

    function controlTestRunContainerVisibility(hide) {
      const $testRecipients = getTestRecipients$();
      if (!$testRecipients.exists()) {
          return;
      }

      const hidden = $testRecipients.val().length <= 0 || !!hide;
      $('#personalized-test-run-container').toggleClass('hidden', hidden);
    }

    function getTestRecipients$() {
      return $('#personalized-test-recipients');
    }

    function controlAddToTestRunBtnVisibility() {
      const previewRecipientEmail = getEmailOfCurrentPreviewRecipient();
      const $testRunRecipients = getTestRecipients$();
      const isVisible = $testRunRecipients.find('option[value="' + previewRecipientEmail + '"]').length
        && $testRunRecipients.val().indexOf(previewRecipientEmail) == -1;
      $('[data-action="add-to-personalized-test-run"]').toggle(!!isVisible);
    }

  function getEmailOfCurrentPreviewRecipient() {
    return $('[name="customerATID"]').find(':selected').data('email');
  }

  this.addAction({'change': 'change-personalized-test-recipients'}, function() {
    controlTestRunVisibility();
  });

  this.addAction({'click': 'add-to-personalized-test-run'}, function() {
    const email = getEmailOfCurrentPreviewRecipient();
    const $testRecipients = getTestRecipients$();
    $testRecipients.val(_.union($testRecipients.val(), [email]));
    AGN.Lib.CoreInitializer.run("select", $testRecipients);
    controlTestRunVisibility();
  });

    this.addAction({'click': 'personalized-test-run'}, function() {
      const testRecipients$ = getTestRecipients$();
      const recipientIds = testRecipients$.val();
      $
        .post(AGN.url("/mailing/send/" + MAILING_ID + "/personalized-test.action"), {mailingTestRecipients: recipientIds})
        .done(function(resp) {
          if (resp && resp.success === true) {
            testRecipients$.val([]);
            AGN.Lib.CoreInitializer.run("select", testRecipients$);
            controlTestRunContainerVisibility(true);
          }
          AGN.Lib.JsonMessages(resp.popups, true);
        })
    });
});
