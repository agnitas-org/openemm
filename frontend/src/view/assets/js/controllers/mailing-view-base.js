AGN.Lib.Controller.new('mailing-view-base', function() {
  var Form = AGN.Lib.Form;

  var scrollOffset = 0;

  var config;
  var isChangeMailing = false;
  var targetGroupIds = [];
  var mailingListSelect;
  var lastMailingListId = 0;

  var saveDirtyState = false;
  var backupFieldsDirtyData = {};

  this.addDomInitializer('mailing-view-base', function() {
    config = this.config;

    if (scrollOffset > 0) {
      var wrapper = $('#gt-wrapper');
      if (wrapper.exists()) {
          wrapper.animate({scrollTop: scrollOffset}, 50);
      } else {
        $(document).scrollTop(scrollOffset + 50);
      }
    } else {
      var shortname = $('#mailingBaseForm').find('[name="shortname"]');
      if ($(shortname).exists()) {
          $(shortname).focus();
      }
    }
    scrollOffset = 0;

    targetGroupIds = $('#targetGroupIds').val() || [];

    $('#targetModeCheck')
      .closest('.form-group')
      .toggle(targetGroupIds.length > 1);
    mailingListSelect = $('#settingsGeneralMailingList');

    mailingListSelect.select2("readonly", false);
    if (lastMailingListId  > 0) {
      lastMailingListId = mailingListSelect.select2("val", lastMailingListId);
    }

    updateGeneralMailingTypeView(config.mailingType);
  });

  function isDirtyOnlyNonEditableFields($form) {
    var dirtyFields = $form.dirty('showDirtyFields');
    if (dirtyFields.length > 0) {
      var filtered = dirtyFields
        .filter(function(_, e) {
          //filter non editable fields
          var $el = $(e);
          return !($el.prop('readonly') === true || $el.is(':disabled') || $el.prop('type') === 'hidden');
        });

      return filtered.length === 0;
    } else {
      return false;
    }
  }

  function configureFormChangesTracking($form) {
    if ($form.dirty('refreshEvents') === null) {
      $form.dirty({
        preventLeaving: true,
        leavingMessage: t('grid.layout.leaveQuestion'),
        onDirty: function() {
          if (isDirtyOnlyNonEditableFields($form)) {
            $form.dirty('setAsClean');
          }
        }
      });
    }

    if (saveDirtyState) {
      $form.dirty('restoreData', backupFieldsDirtyData);
    }

    if (!saveDirtyState || isDirtyOnlyNonEditableFields($form)) {
      $form.dirty('setAsClean');
      backupFieldsDirtyData = $form.dirty('backupData');
    }

    saveDirtyState = false;
  }

  this.addDomInitializer('mailing-view-base-form', function() {
    var $form = $(this.el);
    configureFormChangesTracking($form);

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

  this.addAction({change: 'set-parent-mail-mailinglist'}, function () {
    setMailingListSelectByFollowUpMailing();
  });

  this.addAction({change: 'scroll-to'}, function () {
      var position = getPosition(this.el);
      scrollOffset = Math.round(position);
  });

  this.addAction({change: 'change-general-mailing-type'}, function() {
    var self = this;
    var mailingType = self.el.val();
    mailingListSelect.select2("readonly", false);

    if (lastMailingListId > 0) {
      mailingListSelect.select2("val", lastMailingListId);
    }

    updateGeneralMailingTypeView(mailingType);
  });

  this.addAction({change: 'save-mailing-list-id'}, function() {
    var mailingType = $('#settingsGeneralMailType').val();
    if (!isFollowUpMailing(mailingType)) {
      lastMailingListId = this.el.select2("val");
    }
  });

  function setMailingListSelectByFollowUpMailing() {
    var selectedParentMailingId = $('#lightWeightMailingList').val();
    var selectedParentMailingMailingListId = $('#parentmailing-' + selectedParentMailingId + '-mailinglist')
      .attr('data-mailing-list-id');
    mailingListSelect.select2("val", selectedParentMailingMailingListId);
    mailingListSelect.select2("readonly", true);
  }

  var isFollowUpMailing = function(mailingType) {
    return config.TYPE_FOLLOWUP == mailingType;
  };

  var isIntervalMailing = function(mailingType) {
    return config.TYPE_INTERVAL == mailingType;
  };

  var isActionBasedMailing = function(mailingType) {
    return config.TYPE_ACTIONBASED == mailingType;
  };

  var isDateBasedMailing = function(mailingType) {
    return config.TYPE_DATEBASED == mailingType;
  };

  function updateGeneralMailingTypeView(mailingType) {
    var showFollowUpControls = false;
    if (config.followUpAllowed) {
      if (isFollowUpMailing(mailingType)) {
        setMailingListSelectByFollowUpMailing();
        showFollowUpControls = true;
      }
      toggle(showFollowUpControls, '#followUpControls');
    }

    toggle(isIntervalMailing(mailingType), '#mailingIntervalContainer');

    toggle(isDateBasedMailing(mailingType), '#mailing-bcc-recipients');

    var showTargetGroups = true;
    if (isActionBasedMailing(mailingType)) {
      showTargetGroups = config.campaignEnableTargetGroups;
    }
    if (isDateBasedMailing(mailingType)) {
      showTargetGroups = !config.isWorkflowDriven;
    }

    toggle(showTargetGroups, '#mailingTargets');
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

  this.addAction({change: 'change-media'}, function() {
    var $el = $(this.el);

    var form = AGN.Lib.Form.get($el);
    var $form = AGN.Lib.Form.getWrapper($el);
    backupFieldsDirtyData = $form.dirty('backupData');

    form.setValueOnce('action', config.actions.ACTION_VIEW_WITHOUT_LOAD);
    form.setValueOnce($el.prop('name'), $el.is(':checked'));

    form.setResourceSelectorOnce('#mailingBaseForm');

    $.post(form.getAction(), form.params())
      .done(function (resp) {
        saveDirtyState = true;
        form.updateHtml(resp);
      });
  });

  this.addAction({click: 'prioritise-media'}, function() {
    var $el = $(this.el);

    var form = AGN.Lib.Form.get($el);
    var $form = AGN.Lib.Form.getWrapper($el);
    backupFieldsDirtyData = $form.dirty('backupData');

    var data = _.extend({}, AGN.Lib.Helpers.objFromString($el.data('config')));
    form.setValueOnce('action', data.action);
    form.setValueOnce('activeMedia', data.activeMedia);

    form.setResourceSelectorOnce('#mailingBaseForm');
    $.post(form.getAction(), form.params())
      .done(function(resp) {
        saveDirtyState = true;
        form.updateHtml(resp);
      });
  });

  window.onbeforeunload = function() {
    //prevent show loader if form is dirty
    if (!$('#mailingBaseForm').dirty('isDirty') === true) {
        AGN.Lib.Loader.show();
    }
  };
});
