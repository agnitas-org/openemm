AGN.Lib.Controller.new('mailing-settings-view', function() {
  var Form = AGN.Lib.Form;

  var scrollOffset = 0;

  var config;
  var isChangeMailing = false;
  var targetGroupIds = [];
  var mailingListSelect;
  var lastMailingListId = 0;
  var lastDataForCountingRecipients;

  var mediaTypePriorityChanged = false;

  var refreshTargetModeLabel = function() {
    var $targetModeDesc = $('#target-mode-desc')
    if ($("[name='targetMode']:checked").val() == config.TARGET_MODE_OR) {
      $targetModeDesc.text(t('mailing.default.targetmode_or'));
    } else {
      $targetModeDesc.text(t('mailing.default.targetmode_and'));
    }
  }
  
  var collectMailingParams = function() {
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

  const updateCharCounter = function($el) {
    if (!$el.length) {
      return;
    }

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
      updateCharCounter($('#pre-header'));
    }
    AGN.Opt.collectMailinParams = collectMailingParams;
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
    const $form = $('#mailingSettingsForm');

    $form.dirty('destroy');
    $form.dirty({
      preventLeaving: true,
      leavingMessage: t('grid.layout.leaveQuestion'),
      onDirty: function() {
        if (isDirtyOnlyNonEditableFields($form) && !mediaTypePriorityChanged) {
          $form.dirty('setAsClean');
        }
      }
    });

    if (isDirtyOnlyNonEditableFields($form) && !mediaTypePriorityChanged) {
      $form.dirty('setAsClean');
    }

    mediaTypePriorityChanged = false;
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
    if (config) {
      lastDataForCountingRecipients = getDataForCountingRecipients();

      $.ajax({
        type: 'POST',
        url: AGN.url("/mailing/ajax/" + config.mailingId + "/calculateRecipients.action"),
        traditional: true,
        data: lastDataForCountingRecipients
      }).always(function(resp) {
        if (resp && resp.success === true) {
          setRecipientsCountValue(resp.count);
        } else {
          setRecipientsCountValue('?');
          AGN.Lib.Messages(t('defaults.error'), t('defaults.error'), 'alert');
        }
      });
    }
  });

  this.addAction({change: 'change-mailing-settings'}, function () {
      isChangeMailing = true;
      resetRecipientsCountIfNecessary();
  });

  function resetRecipientsCountIfNecessary() {
    if (!lastDataForCountingRecipients) {
      return;
    }

    const actualDataForCountingRecipients = getDataForCountingRecipients();

    if (!_.isEqual(actualDataForCountingRecipients, lastDataForCountingRecipients)) {
      lastDataForCountingRecipients = null;
      setRecipientsCountValue('?');
    }
  }

  function getDataForCountingRecipients() {
    const fields = [
      '#settingsGeneralMailingList',
      '#settingsTargetgroupsListSplit',
      '#settingsTargetgroupsListSplitPart',
      '#lightWeightMailingList',
      '#assignTargetGroups',
      '#targetGroupIds',
      '#altgIds',
      '#followUpType'
    ];

    return  $.extend(getFieldsData(fields), {
      changeMailing: isChangeMailing,
      targetMode: $('[name="targetMode"]:checked').val(),
      isWmSplit: config.wmSplit
    });
  }

  function setRecipientsCountValue(value) {
    const $count = $('#calculatedRecipientsBadge');
    $count.text(value);
  }

  this.addAction({change: 'set-parent-mail-mailinglist'}, function () {
    setMailingListSelectByFollowUpMailing();
  });

  this.addAction({change: 'scroll-to'}, function () {
      var position = getPosition(this.el);
      scrollOffset = Math.round(position);
  });

  function getPosition($e) {
    var $tile = $e.closest('.tile');
    if ($tile.exists()) {
      return $tile.position().top;
    } else {
      return $e.position().top;
    }
  }

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
    const mailinglistId = this.el.select2("val");
    const mailingType = $('#settingsGeneralMailType').val();
    if (!isFollowUpMailing(mailingType)) {
      lastMailingListId = mailinglistId;
    }

    if (config.selectedRemovedMailinglistId) {
        mailingListSelect.find('option[value="' + config.selectedRemovedMailinglistId + '"]').remove();
        var form = Form.get($(this.el));
        form.cleanFieldError('mailinglistId');
    }

    // TODO: remove condition after GWUA-5688 will be successfully tested
    if (config.allowedMailinglistsAddresses) {
      const mailinglistData = config.mailinglists.find(function (data) {
        return data.id == mailinglistId;
      });

      if (mailinglistData) {
        if (mailinglistData.senderEmail) {
          $('#emailSenderMail').val(mailinglistData.senderEmail).trigger('change');
        }

        if (mailinglistData.replyEmail) {
          $('#emailReplyEmail').val(mailinglistData.replyEmail);
        }

        if (mailinglistData.senderEmail && mailinglistData.replyEmail) {
          AGN.Lib.Messages(t('defaults.info'), t('mailing.default.sender_and_reply_emails_changed'), 'info');
        } else if (mailinglistData.senderEmail) {
          AGN.Lib.Messages(t('defaults.info'), t('mailing.default.sender_email_changed'), 'info');
        } else if (mailinglistData.replyEmail) {
          AGN.Lib.Messages(t('defaults.info'), t('mailing.default.reply_email_changed'), 'info');
        }
      }
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

  function removeEmailTileIfNecessary() {
    if (config.isMailingGrid) {
      return;
    }

    const $emailCheckboxItem = $('#tile-mediaTypes').find('[data-mediatype="email"]');
    if (!isMediatypeListElemActive($emailCheckboxItem)) {
      $('#frame-tile').remove();
      $('#email-tile').remove();
    }
  }

  function renderEmailTileIfNotExists() {
    const tilesExist = $('#frame-tile').length && $('#email-tile').length;

    if (!tilesExist) {
      const $emailTile = $('#email-tile-template').html();
      const $frameTile = $('#frame-tile-template').html();

      const $baseInfoTile = $('#base-info-tile');

      $baseInfoTile.after($emailTile);
      $baseInfoTile.after($frameTile);
    }
  }

  function redrawMediatypeTiles() {
    renderEmailTileIfNotExists();
    removeEmailTileIfNecessary();
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
    mediaTypePriorityChanged = true;

    if (isMediatypeCanBeMoved($el, up)) {
      if (up) {
        moveMediatypeUp($el);
      } else {
        moveMediatypeDown($el);
      }

      $form.dirty("setAsDirty")

      redrawMediatypeTiles();
    }
  }

  this.addAction({submission: 'save'}, function () {
    const form = Form.get(this.el);

    $('#tile-mediaTypes').find('.list-group li').toArray()
        .forEach(function(el) {
          const $el = $(el);
          const mediatype = $el.data('mediatype');
          form.setValueOnce(mediatype + 'Mediatype.priority', $el.data('priority'))
        });

    setParamsToForm(form);
    form.submit();
  });

  function setParamsToForm(form) {
    _.each(collectMailingParams(), function (param, index) {
      form.setValueOnce('params[' + index + '].name', param.name);
      form.setValueOnce('params[' + index + '].value', param.value);
      form.setValueOnce('params[' + index + '].description', param.description);
    })
  }
  
  function getParamValInRowByCol($row, col) {
    return $row.find('[data-param-' + col + ']').val();
  }

  this.addAction({
    input: 'count-text-chars',
    textarea: 'count-text-chars',
    'editor:create': 'count-textarea-chars',
    'editor:change': 'count-textarea-chars'
  }, function() {
    updateCharCounter(this.el);
  });
});
