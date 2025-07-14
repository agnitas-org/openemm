AGN.Lib.Controller.newExtended('mailing-settings-view', 'mailing-settings-base-view', function () {

  const Form = AGN.Lib.Form;
  const Select = AGN.Lib.Select;
  const Modal = AGN.Lib.Modal;
  const Messages = AGN.Lib.Messages;
  const Messaging = AGN.Lib.Messaging;

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
  let $form;

  Messaging.subscribe('mailing-settings:mailinglistChanged', mailinglistId => {
    if (!isFollowUpMailing($('#settingsGeneralMailType').val())) {
      lastMailingListId = mailinglistId;
    }
  });

  this.addDomInitializer('mailing-settings-view', function () {
    config = this.config;
    addRecipientsCountListener();
    $form = $('#mailingSettingsForm');
    lastSelectedTargetsIds = $('#targetGroupIds').val() || [];
    displayTargetModeToggle(lastSelectedTargetsIds.length > 1);
    mailingListSelect = Select.get($('#settingsGeneralMailingList'));
    mailingListSelect.setReadonly(false);

    if (lastMailingListId > 0) {
      lastMailingListId = mailingListSelect.selectOption(lastMailingListId);
      mailingListSelect.$el.trigger('change');
    }

    updateGeneralMailingTypeView(config.mailingType);

    configureFormChangesTracking();
    refreshTargetModeLabel();

    onMediatypesUpdate();

    if (config.isNewUx) {
      initMediatypesSortable()
    }
  });

  function initMediatypesSortable() {
    getMediatypes$().sortable({
      delay: 10,
      scroll: true,
      scrollSensitivity: 30,
      scrollSpeed: 5,
      handle: ".mediatype-order-handle",
      cursor: "move",
      items: '.mediatype-item',
      stop: () => _.each($('.mediatype-item .input-group span:first-child'), (el, i) => $(el).text(i + 1))
    });
  }

  function getMediatypes$() {
    return $('#mediatypes-list');
  }

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

  function isDirtyOnlyNonEditableFields() {
    const dirtyFields = $form.dirty('showDirtyFields');
    if (dirtyFields.length > 0) {
      const filtered = dirtyFields
        .filter(function (_, e) {
          //filter non editable fields
          const $el = $(e);
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
      onDirty: function () {
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

  window.onbeforeunload = function () {
    //prevent show loader if form is dirty
    if (!$form.dirty('isDirty') === true) {
      AGN.Lib.Loader.show();
    }
  };

  function displayTargetModeToggle(show) {
    $('#target-mode-box').toggle(show);
    $('#regular-targets-box').toggleClass('border rounded p-2', show);
  }

  this.addAction({change: 'selectTargetGroups'}, function () {
    const newTargetIds = this.el.val() || [];

    if (newTargetIds.length > 1 && lastSelectedTargetsIds.length <= 1) { // was single and become multiple
      $('#target-mode-and-btn').prop('checked', true);
    }
    displayTargetModeToggle(newTargetIds.length > 1);
    lastSelectedTargetsIds = newTargetIds;
  });

  function getFieldsData(selectors) {
    const data = {};

    selectors.forEach(selector => {
      const $input = $(selector);
      if ($input.exists() && $input.is(':visible') && !$input.prop('disabled')) {
        const name = $input.prop('name');

        if (name) {
          data[name] = $input.val();
        }
      }
    });

    return data;
  }

  this.addAction({click: 'calculateRecipients'}, function () {
    if (config) {
      lastDataForCountingRecipients = getDataForCountingRecipients();

      $.ajax({
        type: 'POST',
        url: AGN.url("/mailing/ajax/" + config.mailingId + "/calculateRecipients.action"),
        traditional: true,
        data: lastDataForCountingRecipients
      }).always(function (resp) {
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
    return $.extend(getFieldsData(recipientsCountRelatedFields), {
      changeMailing: isChangeMailing,
      targetMode: getTargetModeVal(),
      isWmSplit: config.wmSplit
    });
  }

  function setRecipientsCountValue(value) {
    $('#number-of-recipients').val(value);
  }

  this.addAction({change: 'change-general-mailing-type'}, function () {
    const self = this;
    const mailingType = self.el.val();
    mailingListSelect.setReadonly(false);

    if (lastMailingListId > 0) {
      mailingListSelect.selectOption(lastMailingListId);
    }
    updateGeneralMailingTypeView(mailingType);
  });

  this.addAction({change: 'change-target-mode'}, refreshTargetModeLabel);

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
    let showFollowUpControls = false;
    if (config.followUpAllowed) {
      if (isFollowUpMailing(mailingType)) {
        Messaging.send('mailing-settings:updateMailinglistForFollowUp');
        showFollowUpControls = true;
      }
      toggle(showFollowUpControls, '#followUpControls');
      $("#followUpControls").all(":input").each((i, el) => $(el).prop("disabled", !showFollowUpControls));
    }

    const showBccRecipients = isDateBasedMailing(mailingType);
    toggle(showBccRecipients, '#mailing-bcc-recipients');
    $("#mailing-bcc-recipients").prop("disabled", !showBccRecipients);

    const showTargetGroups = isActionBasedMailing(mailingType) ? config.campaignEnableTargetGroups : true;
    toggle(showTargetGroups, '#regular-targets-box, #calculate-recipients-box');
  }

  function toggle(show, selector) {
    const $container = $(selector);
    if ($container) {
      if (show) {
        $container.removeClass('hidden');
        $container.show();
      } else {
        $container.hide();
      }
    }
  }

  this.addAction({change: 'change-mediatype'}, function () {
    const $el = $(this.el);
    const active = isMediatypeListElemActive($el);
    redrawMediatypeCheckboxes($el, active);
    const showEditFrameContentBtn = $('#email-mediatype-switch, #sms-mediatype-switch').is(':checked');
    $('#edit-frame-content-btn').toggle(showEditFrameContentBtn);
  });

  function isMediatypeListElemActive($el) {
    return $el.find('input:checkbox:first').is(':checked');
  }

  function prioritizeMediatypeCheckboxes() {
    const checkboxesContainer = getMediatypes$();
    const checkboxes = getSortedMediatypeCheckboxes();
    checkboxesContainer.empty();
    checkboxes.forEach(function (item) {
      checkboxesContainer.append(item);
    });
  }

  function getSortedMediatypeCheckboxes() {
    return $('#mediatypes-list li').toArray().sort(function (a, b) {
      const $a = $(a);
      const $b = $(b);
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
    onMediatypesUpdate();
  }

  function hideFirstAndLastMediatypeControlArrows() { // css :first-of-type now working after mediatypes dom manipulation
    const $list = getMediatypes$();
    $list.find('a').css('opacity', '1');
    $list.find('.icon-angle-up:first, .icon-angle-down:last').css('opacity', '0');
  }

  function onMediatypesUpdate() {
    hideFirstAndLastMediatypeControlArrows();
    if (config.isNewUx) {
      toggleMediatypeOrderDragButtons();
      updateMediatypeSequenceLabels()
    }
    Form.get(getMediatypes$()).initFields();
  }

  function toggleMediatypeOrderDragButtons() {
    const show = getMediatypes$().find('.form-check-input').toArray().every(mt => mt.checked);
    getMediatypes$().find('.mediatype-order-handle').toggle(show);
  }

  function getMediatypeControlArrows() {
    return `
          <div class='list-group-item-controls'>
              <a href='#' class='icon icon-angle-down fs-2' data-action='prioritise-mediatype-down'></a>
              <a href='#' class='icon icon-angle-up fs-2' data-action='prioritise-mediatype-up'></a>
          </div>`;
  }

  this.addAction({click: 'prioritise-mediatype-up'}, function () {
    prioritiseMediatypes($(this.el).closest('li'), true);
  });

  this.addAction({click: 'prioritise-mediatype-down'}, function () {
    prioritiseMediatypes($(this.el).closest('li'), false);
  });

  function moveMediatypeUp($current) {
    const $prev = $current.prev();
    const currentPriority = $current.attr('data-priority');
    $current.attr('data-priority', $prev.attr('data-priority'));
    $prev.attr('data-priority', currentPriority);
    $prev.before($current);
  }

  function moveMediatypeDown($current) {
    const $next = $current.next();
    const currentPriority = $current.attr('data-priority');
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
        if (config.isNewUx) {
          $el.prev().before($el);
        } else {
          moveMediatypeUp($el);
        }
      } else {
        if (config.isNewUx) {
          $el.next().after($el);
        } else {
          moveMediatypeDown($el);
        }
      }
      $form.dirty("setAsDirty");
      onMediatypesUpdate();
    }
  }

  function updateMediatypeSequenceLabels() {
    _.each($('.mediatype-item .input-group span:first-child'), (el, i) => $(el).text(i + 1))
  }


  this.addAction({submission: 'save'}, saveMailing);

  function saveMailing() {
    const form = Form.get($form);
    setMediatypesPrioritiesToForm(form);
    setParamsToForm(form);
    form.submit();
  }

  function setMediatypesPrioritiesToForm(form) {
    if (config.isNewUx) {
      _.each($('#mediatypes-list li'), function (el, i) {
        form.setValueOnce($(el).data('mediatype') + 'Mediatype.priority', i);
      })
    } else {
      $('#mediatypes-list li').toArray().forEach(function (el) {
        const $el = $(el);
        const mediatype = $el.data('mediatype');
        form.setValueOnce(mediatype + 'Mediatype.priority', $el.data('priority'))
      });
    }
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

  // todo remove when ux update will be tested and old redesigned version will be removed
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

  this.addAction({'editor:change': 'validate-on-change'}, function () {
    Form.get(this.el).validateField(this.el);
  });

});
