AGN.Lib.Controller.new('mailing-settings-view', function() {
  var Form = AGN.Lib.Form;

  var scrollOffset = 0;

  var config;
  var isChangeMailing = false;
  var targetGroupIds = [];
  var mailingListSelect;
  var lastMailingListId = 0;

  var saveDirtyState = false;
  var mediaTypePriorityChanged = false;
  var backupFieldsDirtyData = {};

  var refreshTargetModeLabel = function() {
    var $targetModeDesc = $('#target-mode-desc')
    if ($("[name='targetMode']:checked").val() == config.TARGET_MODE_OR) {
      $targetModeDesc.text(t('mailing.default.targetmode_or'));
    } else {
      $targetModeDesc.text(t('mailing.default.targetmode_and'));
    }
  }

  const updateCharCounter = function($el) {
    const count = $el.val().length;

    $('[data-char-counter-for="' + $el.attr('id') + '"]')
        .find('span:first')
        .text(t('fields.content.charactersEntered', count));
  }

  this.addDomInitializer('mailing-settings-view', function() {
    config = this.config;

    targetGroupIds = $('#targetGroupIds').val() || [];

    $('#targetModeAndBtn')
      .closest('.form-group')
      .toggle(targetGroupIds.length > 1);
    mailingListSelect = $('#settingsGeneralMailingList');
    
    mailingListSelect.select2("readonly", false);
    if (lastMailingListId  > 0) {
      lastMailingListId = mailingListSelect.select2("val", lastMailingListId);
    }
    if (config.selectedRemovedMailinglistId) {
        showRemovedMailinglistError();
    }
    updateGeneralMailingTypeView(config.mailingType);
    if (!config.isMailingGrid || config.mailingId || config.isCopying) {
      redrawMediatypeTiles();
    }
    configureFormChangesTracking();
    refreshTargetModeLabel();
    scrollPage();
    if ($('#email-tile').length) {
      updateCharCounter($('#emailSubject'));
    } 
  });
  
  function showRemovedMailinglistError() {
    var form = Form.get($('#mailingSettingsForm'));
    form.showFieldError('mailinglistId', 'Mailing list removed', true);
  }

  function scrollPage() {
    if (scrollOffset > 0) {
      var wrapper = $('#gt-wrapper');
      if (wrapper.exists()) {
        wrapper.animate({scrollTop: scrollOffset}, 50);
      } else {
        $(document).scrollTop(scrollOffset + 50);
      }
    } else {
      var shortname = $('#mailingSettingsForm').find('[name="shortname"]');
      if ($(shortname).exists()) {
        $(shortname).focus();
      }
    }
    scrollOffset = 0;
  }

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

  function configureFormChangesTracking() {
    var $form = $('#mailingSettingsForm');
    if ($form.dirty('refreshEvents') === null) {
      $form.dirty({
        preventLeaving: true,
        leavingMessage: t('grid.layout.leaveQuestion'),
        onDirty: function() {
          if (isDirtyOnlyNonEditableFields($form) && !mediaTypePriorityChanged) {
            $form.dirty('setAsClean');
          }
        }
      });
    }

    if (saveDirtyState) {
      $form.dirty('restoreData', backupFieldsDirtyData);
    }

    if (!saveDirtyState || isDirtyOnlyNonEditableFields($form) && !mediaTypePriorityChanged) {
      $form.dirty('setAsClean');
      backupFieldsDirtyData = $form.dirty('backupData');
    }

    mediaTypePriorityChanged = false;
    saveDirtyState = false;
  }
    
  window.onbeforeunload = function() {
    //prevent show loader if form is dirty
    if (!$('#mailingSettingsForm').dirty('isDirty') === true) {
        AGN.Lib.Loader.show();
    }
  };
  
  this.addAction({change: 'selectTargetGroups'}, function() {
    var ids = this.el.val() || [];
    var $mode = $('#targetModeAndBtn');
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
      if ($input.exists() && $input.closest('.form-group').is(':visible') && !$input.prop('disabled')) {
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
          '#altgIds',
          '#followUpType'
      ];

    if (config) {
      $.ajax({
        type: 'POST',
        url: AGN.url("/mailing/ajax/" + config.mailingId + "/calculateRecipients.action"),
        traditional: true,
        data: $.extend(getFieldsData(fields), {
          changeMailing: isChangeMailing,
          targetMode: $('[name="targetMode"]:checked').val(),
          isWmSplit: config.wmSplit
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

    if (config.selectedRemovedMailinglistId) {
        mailingListSelect.find('option[value="' + config.selectedRemovedMailinglistId + '"]').remove();
        var form = Form.get($(this.el));
        form.cleanFieldError('mailinglistId');
    }
  });
  
  this.addAction({change: 'change-target-mode'}, refreshTargetModeLabel);

  function setMailingListSelectByFollowUpMailing() {
    var selectedParentMailingId = $('#lightWeightMailingList').val();
    var selectedParentMailingMailingListId = $('#parentmailing-' + selectedParentMailingId + '-mailinglist')
      .attr('data-mailing-list-id');
    mailingListSelect.select2("val", selectedParentMailingMailingListId);
    mailingListSelect.select2("readonly", true);
  }

  var isFollowUpMailing = function(mailingType) {
    return config.FOLLOWUP_MAILING_TYPE == mailingType;
  };

  var isActionBasedMailing = function(mailingType) {
    return config.ACTIONBASED_MAILING_TYPE == mailingType;
  };

  var isDateBasedMailing = function(mailingType) {
    return config.DATEBASED_MAILING_TYPE == mailingType;
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

    toggle(isDateBasedMailing(mailingType), '#mailing-bcc-recipients');

    var showTargetGroups = true;
    if (isActionBasedMailing(mailingType)) {
      showTargetGroups = config.campaignEnableTargetGroups;
    }
    if (isDateBasedMailing(mailingType)) {
      showTargetGroups = !config.workflowDriven;
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

  this.addAction({change: 'change-mediatype'}, function() {
    var $el = $(this.el);
    var $form = Form.getWrapper($el);
    backupFieldsDirtyData = $form.dirty('backupData');
    saveDirtyState = true;
    var active = isMediatypeListElemActive($el);
    redrawMediatypeCheckboxes($el, active);
    redrawMediatypeTiles();
  });

  function isMediatypeListElemActive($el) {
    return $el.find('input:checkbox:first').is(':checked');
  }

  function prioritizeMediatypeCheckboxes() {
    var checkboxesContainer = $('#tile-mediaTypes').find('.list-group');
    var checkboxes = getSortedMediatypeCheckboxes();
    checkboxesContainer.empty();
    checkboxes.forEach(function(item) {
      checkboxesContainer.append(item);
    });
  }

  function getSortedMediatypeCheckboxes() {
    return $('#tile-mediaTypes').find('.list-group li').toArray().sort(function(a, b) {
      var $a = $(a);
      var $b = $(b);
      if (!isMediatypeListElemActive($a) && isMediatypeListElemActive($b)) return 1;
      if (isMediatypeListElemActive($a) && !isMediatypeListElemActive($b)) return -1;
      return $a.data('priority') - $b.data('priority');
    });
  }
  
  function redrawMediatypeCheckboxes($el, active) {
    prioritizeMediatypeCheckboxes();
    displayArrows($el, active);
  }

  function redrawEmailTile() {
    $('#frame-tile').remove();
    $('#email-tile').remove();
    var $emailCheckboxItem = $('#tile-mediaTypes').find('[data-mediatype="email"]');
    if (isMediatypeListElemActive($emailCheckboxItem) || config.isMailingGrid) {
      var $baseInfoTile = $('#base-info-tile');
      var $emailTile = AGN.Lib.Template.dom('email-tile-template');
      var $frameTile = AGN.Lib.Template.dom('frame-tile-template');
      $baseInfoTile.after($emailTile);
      $baseInfoTile.after($frameTile);
    }
  }

  function redrawMediatypeTiles() {
    redrawEmailTile();
    var $mediatypesTile = $('#mediatypes-tile');
    $('#tile-mediaTypes').find('.list-group li').toArray()
      .filter(function(el) {
        return $(el).data('mediatype') !== 'email' && $(el).data('mediatype') !== 'post';
      })
      .reverse()
      .forEach(function(el) {
        var $el = $(el);
        var mediatype = $el.data('mediatype');
        $('#' + mediatype + '-tile').remove();
        if (isMediatypeListElemActive($el)) {
          var $tile = AGN.Lib.Template.dom(mediatype + '-tile-template');
          $mediatypesTile.after($tile);
        }
      });
    AGN.Lib.Form.get($('#mailingSettingsForm')).initFields();
    AGN.Lib.CoreInitializer.run('tab-toggle');
    AGN.Lib.CoreInitializer.run('ace');
    AGN.Lib.CoreInitializer.run('select');
  }

  function displayArrows($el, active) {
    if (active) {
      $el.find('label').after("\
          <div class='list-group-item-controls'>\
              <a href='#' data-action='prioritise-mediatype-down'>\
                <i class='icon icon-chevron-circle-down'></i>\
              </a>\
              <a href='#' data-action='prioritise-mediatype-up'>\
               <i class='icon icon-chevron-circle-up'></i>\
              </a>\
          </div>\
      ");
    } else {
      $el.find('.list-group-item-controls').remove();
    }
  }

  this.addAction({click: 'prioritise-mediatype-up'}, function() {
    prioritiseMediatypes($(this.el).closest('li'), true);
  });
  
  this.addAction({click: 'prioritise-mediatype-down'}, function() {
    prioritiseMediatypes($(this.el).closest('li'), false);
  });

  function moveMediatypeUp($current) {
    var $prev = $current.prev();
    var currentPriority = $current.attr('data-priority');
    $current.attr('data-priority', $prev.attr('data-priority'));
    $prev.attr('data-priority', currentPriority);
    $prev.before($current);
  }

  function moveMediatypeDown($current) {
    var $next = $current.next()
    var currentPriority = $current.attr('data-priority');
    $current.attr('data-priority', $next.attr('data-priority'));
    $next.attr('data-priority', currentPriority);
    $next.after($current);
  }

  function isMediatypeCanBeMoved($el, up) {
    return (up && $el.prev().length)
      || (!up && $el.next().length && isMediatypeListElemActive($el.next()));
  }

  function prioritiseMediatypes($el, up) {
    var $form = Form.getWrapper($el);
    backupFieldsDirtyData = $form.dirty('backupData');
    mediaTypePriorityChanged = true;
    saveDirtyState = true;
    if (isMediatypeCanBeMoved($el, up)) {
      if (up) {
        moveMediatypeUp($el);
      } else {
        moveMediatypeDown($el);
      }
      redrawMediatypeTiles();
    }
  }
  
  this.addAction({click: 'save'}, function() {
    var $form = $('#mailingSettingsForm');
    var form = Form.get($form);
    
    $('#tile-mediaTypes').find('.list-group li').toArray()
          .forEach(function(el) {
            var $el = $(el);
            var mediatype = $el.data('mediatype');
            form.setValueOnce(mediatype + 'Mediatype.priority', $el.data('priority'))
          });
    
    setParamsToForm(form);
    form.submit();
  });
  
  function setParamsToForm(form) {
    _.each(collectParamsFromTable(), function (param, index) {
      form.setValueOnce('params[' + index + '].name', param.name);
      form.setValueOnce('params[' + index + '].value', param.value);
      form.setValueOnce('params[' + index + '].description', param.description);
    })
  }
  
  function collectParamsFromTable() {
    var $table = $('#mailingParamsTable tbody');
    if ($table && $table.length) {
      return _.map($table.find('[data-param-row]'), function(row) {
        var $row = $(row);
        return {
          name: getParamValInRowByCol($row, 'name'),
          value: getParamValInRowByCol($row, 'value'),
          description: getParamValInRowByCol($row, 'description')
        };
      }).filter(function(param) {
        return param.name || param.value || param.description;
      })
    }
    return [];
  }
  
  function getParamValInRowByCol($row, col) {
    return $row.find('[data-param-' + col + ']').val();
  }

  this.addAction({
    input: 'count-text-chars',
    'editor:create': 'count-textarea-chars',
    'editor:change': 'count-textarea-chars'
  }, function() {
    updateCharCounter(this.el);
  });
});
