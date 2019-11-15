AGN.Lib.Controller.new('mailing-view-base', function() {
  var Form = AGN.Lib.Form,
    Tooltip = AGN.Lib.Tooltip;

  var config;
  var isChangeMailing = false;
  var targetGroupIds = [];

  this.addDomInitializer('mailing-view-base', function() {
    config = this.config;

    if (this.config.scrollTop > 0) {
      $('#gt-wrapper').animate({scrollTop: config.scrollTop}, 50);
    }

    targetGroupIds = $('#targetGroupIds').val() || [];

    $('#targetModeCheck')
      .closest('.form-group')
      .toggle(targetGroupIds.length > 1);

    updateGeneralMailingTypeView(config.mailingType);
  });

  this.addAction({change: 'selectTargetGroups'}, function() {
    var ids = this.el.val() || [];
    var $mode = $('#targetModeCheck');
    var $modeControls = $mode.closest('.form-group');

    // Was single and become multiple.
    if (ids.length > 1 && targetGroupIds.length <= 1) {
      $mode.prop('checked', true);
    }
    $modeControls.toggle(ids.length > 1);

    targetGroupIds = ids;
  });

  function getFieldsData(selectors) {
    var data = {};

    selectors.forEach(function(selector) {
      var $input = $(selector);
      if ($input.exists() && !$input.prop('disabled')) {
        var name = $input.prop('name');
        var value = $input.val();

        if (name) {
          data[name] = value;
        }
      }
    });

    return data;
  }

  this.addAction({click: 'calculateRecipients'}, function() {
    var $count = $('#calculatedRecipientsBadge');

      var fields = [
          '#settingsGeneralMailingList',
          '#settingsTargetgroupsListSplit',
          '#settingsTargetgroupsListSplitPart',
          '#lightWeightMailingList',
          '#assignTargetGroups',
          '#targetGroupIds',
          '#followUpType'
      ];

    if (config) {
      $.ajax(config.urls.MAILINGBASE, {
        type: 'POST',
        traditional: true,
        data: $.extend(getFieldsData(fields), {
          action: config.actions.ACTION_RECIPIENTS_CALCULATE,
          mailingID: config.mailingId,
          changeMailing: isChangeMailing,
          targetMode: $('#targetModeCheck').prop('checked') ? config.TARGET_MODE_AND : config.TARGET_MODE_OR
        })
      }).always(function(resp) {
        if (resp && resp.success === true) {
          $count.text(resp.count);
        } else {
          $count.text('?');
          AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
        }
      });
    }
  });

  function getPosition($e) {
    var $tile = $e.closest('.tile');
    if ($tile.exists()) {
      return $tile.position().top;
    } else {
      return $e.position().top;
    }
  }

  this.addAction({change: 'change-mailing-settings'}, function () {
      isChangeMailing = true;
  });

  this.addAction({click: 'deleteMailingParameter'}, function() {
    var form = Form.get(this.el);
    var position = getPosition(this.el);

    var $tr = this.el.closest('tr');
    Tooltip.remove($tr.all('[data-tooltip]'));
    $tr.remove();

    form.setValueOnce('scrollTop', Math.round(position));
    form.submit();
  });

  this.addAction({click: 'createMailingParameter'}, function() {
    var form = Form.get(this.el);
    var position = getPosition(this.el);

    form.setValueOnce('addParameter', true);
    form.setValueOnce('scrollTop', Math.round(position));

    form.submit();
  });

  this.addAction({change: 'change-general-mailing-type'}, function() {
    var self = this;
    var value = self.el.val();
    updateGeneralMailingTypeView(value);

  });

  function updateGeneralMailingTypeView(value) {
    if (config.followUpAllowed) {
      toggle(config.TYPE_FOLLOWUP == value, '#followUpControls');
    }

    toggle(config.TYPE_INTERVAL == value, '#mailingIntervalContainer');

    toggle(config.TYPE_DATEBASED == value, '#mailing-bcc-recipients');

    var hideTargets = config.TYPE_ACTIONBASED == value || (config.TYPE_DATEBASED == value && config.isWorkflowDriven);
    toggle(!hideTargets, '#mailingTargets');

  }

  function toggle(show, selector) {
    var container = $(selector);
    if (container) {
      if (show) {
        container.removeClass('hidden');
        container.show();
      } else {
        container.hide();
      }
    }
  }

});
