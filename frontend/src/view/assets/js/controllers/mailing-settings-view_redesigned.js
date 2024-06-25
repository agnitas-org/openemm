AGN.Lib.Controller.new('mailing-settings-view', function() {
  const Form = AGN.Lib.Form;
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  const Modal = AGN.Lib.Modal;
  const Messages = AGN.Lib.Messages;

  const recipientsCountRelatedFields = [
    '#settingsGeneralMailingList',
    '#lightWeightMailingList',
    '#assignTargetGroups',
    '#targetGroupIds',
    '#altgIds',
    '#followUpType'
  ];
  
  let config;
  let isChangeMailing = false;
  let lastSelectedTargetsIds = [];
  let mailingListSelect;
  let lastMailingListId = 0;
  let lastDataForCountingRecipients;
  let mediaTypePriorityChanged = false;
  let editWithCampaignBtn;
  let $form;

  this.addDomInitializer('mailing-settings-view', function() {
    config = this.config;
    addRecipientsCountListener();
    editWithCampaignBtn = Template.text('edit-with-campaign-btn');
    $form = $('#mailingSettingsForm');
    lastSelectedTargetsIds = $('#targetGroupIds').val() || [];
    displayTargetModeToggle(lastSelectedTargetsIds.length > 1);
    mailingListSelect = Select.get($('#settingsGeneralMailingList'));
    mailingListSelect.setReadonly(false);
    if (lastMailingListId > 0) {
      lastMailingListId = mailingListSelect.selectOption(lastMailingListId);
    }
    if (config.selectedRemovedMailinglistId) {
      showRemovedMailinglistError();
    }
    updateGeneralMailingTypeView(config.mailingType);

    configureFormChangesTracking();
    refreshTargetModeLabel();

    decorateWorkflowDrivenInputs();
    hideFirstAndLastMediatypeControlArrows();
  });

  function addRecipientsCountListener() {
    $(recipientsCountRelatedFields.join(',')).on('change', function () {
      isChangeMailing = true;
      if (!lastDataForCountingRecipients) {
        return;
      }
      const actualDataForCountingRecipients = getDataForCountingRecipients();
      if (!_.isEqual(actualDataForCountingRecipients, lastDataForCountingRecipients)) {
        lastDataForCountingRecipients = null;
        setRecipientsCountValue('?');
      }
    });
  }

  function refreshTargetModeLabel() {
    const isTargetModeOR = getTargetModeVal() === config.TARGET_MODE_OR;
    $('#target-mode-description').text(t(`mailing.default.targetmode_${isTargetModeOR ? 'or' : 'and'}`));
  }

  function getTargetModeVal() {
    return $("[name='targetMode']:checked").val();
  }
  
  function getInputAddonWithCampaignBtn() {
    return $('<div>', {
      class: 'input-group-text input-group-text--disabled border-end-0 pe-0',
      html: editWithCampaignBtn
    });
  }

  function decorateWorkflowDrivenInputs() {
    _.each($('[data-workflow-driven="true"]'), (el) => decorateWorkflowDrivenInput($(el)));
  }

  function decorateWorkflowDrivenInput($el) {
    if ($el.is('input[type="text"]')) {
      decorateWorkflowDrivenTextInput($el);
    }
    if ($el.is('input[type="checkbox"]')) {
      decorateWorkflowDrivenCheckboxInput($el);
    }
    if ($el.is('select')) {
      decorateWorkflowDrivenSelect($el);
    }
    $el.prop('disabled', true);
  }

  function decorateWorkflowDrivenTextInput($el) {
    $el
      .css('border-left', '0')
      .wrap('<div class="input-group">')
      .before(getInputAddonWithCampaignBtn(editWithCampaignBtn));
    AGN.Lib.CoreInitializer.run("tooltip", $el.parent());
  }

  function decorateWorkflowDrivenCheckboxInput($el) {
    $el.next().after(editWithCampaignBtn);
    AGN.Lib.CoreInitializer.run("tooltip", $el.parent());
  }

  function decorateWorkflowDrivenSelect($el) {
    const $select2 = $el.next('.select2-container');
    if ($el.prop('multiple')) {
      $select2.find('.select2-selection__rendered').hide();
      $select2.find('.select2-search__field').val(t('mailing.default.editWithCampaign'));
      $select2.find('.select2-search--inline').prepend(editWithCampaignBtn);
    } else {
      $select2.find('.select2-selection').prepend(editWithCampaignBtn);
    }
    AGN.Lib.CoreInitializer.run("tooltip", $select2);
  }
  
  function showRemovedMailinglistError() {
    const form = Form.get($form);
    form.showFieldError('mailinglistId', t('fields.mailinglist.errors.removed'), true);
  }

  function isDirtyOnlyNonEditableFields() {
    const dirtyFields = $form.dirty('showDirtyFields');
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
    $form.dirty('destroy');
    $form.dirty({
      preventLeaving: true,
      leavingMessage: t('grid.layout.leaveQuestion'),
      onDirty: function() {
        if (isDirtyOnlyNonEditableFields() && !mediaTypePriorityChanged) {
          $form.dirty('setAsClean');
        }
      }
    });

    if (isDirtyOnlyNonEditableFields() && !mediaTypePriorityChanged) {
      $form.dirty('setAsClean');
    }

    mediaTypePriorityChanged = false;
  }
    
  window.onbeforeunload = function() {
    //prevent show loader if form is dirty
    if (!$form.dirty('isDirty') === true) {
        AGN.Lib.Loader.show();
    }
  };

  function displayTargetModeToggle(show) {
    $('#target-mode-box').toggle(show);
    $('#regular-targets-box').toggleClass('border rounded p-2', show);
  }

  this.addAction({change: 'selectTargetGroups'}, function() {
    const newTargetIds = this.el.val() || [];
    
    if (newTargetIds.length > 1 && lastSelectedTargetsIds.length <= 1) { // was single and become multiple
      $('#target-mode-and-btn').prop('checked', true);
    }
    displayTargetModeToggle(newTargetIds.length > 1);
    lastSelectedTargetsIds = newTargetIds;
  });

  function getFieldsData(selectors) {
    var data = {};

    selectors.forEach(function(selector) {
      var $input = $(selector);
      if ($input.exists() && $input.is(':visible') && !$input.prop('disabled')) {
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
          Messages.defaultError();
        }
      });
    }
  });

  function getDataForCountingRecipients() {
    return  $.extend(getFieldsData(recipientsCountRelatedFields), {
      changeMailing: isChangeMailing,
      targetMode: getTargetModeVal(),
      isWmSplit: config.wmSplit
    });
  }

  function setRecipientsCountValue(value) {
    $('#number-of-recipients').val(value);
  }

  this.addAction({change: 'set-parent-mail-mailinglist'}, function () {
    setMailingListSelectByFollowUpMailing();
  });

  this.addAction({change: 'change-general-mailing-type'}, function() {
    const self = this;
    const mailingType = self.el.val();
    mailingListSelect.setReadonly(false);

    if (lastMailingListId > 0) {
      mailingListSelect.selectOption(lastMailingListId);
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
        mailingListSelect.$findOption(config.selectedRemovedMailinglistId).remove();
        const form = Form.get($(this.el));
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
          Messages.info('mailing.default.sender_and_reply_emails_changed');
        } else if (mailinglistData.senderEmail) {
          Messages.info('mailing.default.sender_email_changed');
        } else if (mailinglistData.replyEmail) {
          Messages.info('mailing.default.reply_email_changed');
        }
      }
    }
  });
  
  this.addAction({change: 'change-target-mode'}, refreshTargetModeLabel);

  function setMailingListSelectByFollowUpMailing() {
    const selectedParentMailingId = $('#lightWeightMailingList').val();
    const selectedParentMailingMailingListId = $('#parentmailing-' + selectedParentMailingId + '-mailinglist')
      .attr('data-mailing-list-id');
    mailingListSelect.selectOption(selectedParentMailingMailingListId);
    mailingListSelect.setReadonly(true);
  }

  function isFollowUpMailing(mailingType) {
    return config.FOLLOWUP_MAILING_TYPE == mailingType;
  }

  function isActionBasedMailing(mailingType) {
    return config.ACTIONBASED_MAILING_TYPE == mailingType;
  }

  function isDateBasedMailing(mailingType) {
    return config.DATEBASED_MAILING_TYPE == mailingType;
  }

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

    const showTargetGroups = isActionBasedMailing(mailingType) ? config.campaignEnableTargetGroups : true;
    toggle(showTargetGroups, '#regular-targets-box, #calculate-recipients-box');
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
  });

  function isMediatypeListElemActive($el) {
    return $el.find('input:checkbox:first').is(':checked');
  }

  function prioritizeMediatypeCheckboxes() {
    var checkboxesContainer = $('#mediatypes-list');
    var checkboxes = getSortedMediatypeCheckboxes();
    checkboxesContainer.empty();
    checkboxes.forEach(function(item) {
      checkboxesContainer.append(item);
    });
  }

  function getSortedMediatypeCheckboxes() {
    return $('#mediatypes-list li').toArray().sort(function(a, b) {
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
  
  function displayArrows($el, active) {
    if (active) {
      $el.find('.form-switch').after(getMediatypeControlArrows());
    } else {
      $el.find('.list-group-item-controls').remove();
    }
    hideFirstAndLastMediatypeControlArrows();
  }

  function hideFirstAndLastMediatypeControlArrows() {
    let $list = $('.list-group');
    $list.find('a').css('opacity', '1');
    $list.find('.icon-angle-up:first, .icon-angle-down:last').css('opacity', '0');
    Form.get($list).initFields();
  }

  function getMediatypeControlArrows() {
    return `
          <div class='list-group-item-controls'>
              <a href='#' class='icon icon-angle-down fs-2' data-action='prioritise-mediatype-down'></a>
              <a href='#' class='icon icon-angle-up fs-2' data-action='prioritise-mediatype-up'></a>
          </div>`;
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
    mediaTypePriorityChanged = true;

    if (isMediatypeCanBeMoved($el, up)) {
      if (up) {
        moveMediatypeUp($el);
      } else {
        moveMediatypeDown($el);
      }

      $form.dirty("setAsDirty")

      hideFirstAndLastMediatypeControlArrows();
    }
  }

  this.addAction({submission: 'save'}, saveMailing);

  function saveMailing() {
    const form = Form.get($form);
    setMediatypesPrioritiesToForm(form);
    setParamsToForm(form);
    form.submit();
  }

  function setMediatypesPrioritiesToForm(form) {
    $('#mediatypes-list li').toArray().forEach(function (el) {
      const $el = $(el);
      const mediatype = $el.data('mediatype');
      form.setValueOnce(mediatype + 'Mediatype.priority', $el.data('priority'))
    });
  }

  function setParamsToForm(form) {
    const $mailingParamsTable = $('#mailingParamsTable');
    if (!$mailingParamsTable.exists()) {
      return;
    }
    _.each($mailingParamsTable.data('table').collect(), function (param, index) {
      form.setValueOnce('params[' + index + '].name', param.name);
      form.setValueOnce('params[' + index + '].value', param.value);
      form.setValueOnce('params[' + index + '].description', param.description);
    })
  }
  
  this.addAction({click: 'edit-content-modal'}, function () {
    const showText = $("#email-mediatype-switch").prop('checked') || config.isMailingGrid;
    const showHtml = showText && !$("#emailMailFormat").val() == 0 && !config.isMailingGrid;
    const showSms = $("#sms-mediatype-switch").prop('checked');
    
    AGN.Lib.Modal.fromTemplate("modal-editor", {showText, showHtml, showSms});
  });
  
  this.addAction({click: 'save-content'}, function () {
    let $contentForm = $('#content-form');
    if (!Form.get($contentForm).validate()) {
      return;
    }
    Modal.getInstance($contentForm).hide();
    saveMailing();
  });
});
