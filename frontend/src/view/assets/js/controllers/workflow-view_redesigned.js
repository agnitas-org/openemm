AGN.Lib.Controller.new('workflow-view', function() {
  const Def = AGN.Lib.WM.Definitions;
  const Editor = AGN.Lib.WM.Editor;
  const Node = AGN.Lib.WM.Node;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const MailingEditorHelper = AGN.Lib.WM.MailingEditorHelper;
  const Dialogs = AGN.Lib.WM.Dialogs;
  const Confirm = AGN.Lib.Confirm;
  const Utils = AGN.Lib.WM.Utils;
  const DateTimeUtils = AGN.Lib.WM.DateTimeUtils;
  const Modal = AGN.Lib.Modal;

  let editor;

  // Register custom connector type.
  if (!jsPlumb.Connectors['FixedBezierConnector']) {
    jsPlumbUtil.extend(AGN.Opt.Components.JsPlumbFixedBezierConnector, jsPlumb.Connectors.AbstractConnector);
    jsPlumb.Connectors['FixedBezierConnector'] = AGN.Opt.Components.JsPlumbFixedBezierConnector;
  }

  $(window).on('wheel mousewheel DOMMouseScroll', e => e.ctrlKey && e.preventDefault());
  $(window).on('resize viewportChanged', () => editor?.updateMinimap());
  $(document).on('keydown', onKeyDown);
  $(document).on('keyup', onKeyUp);
  $(document).on('mousedown', onMouseDown);
  $(document).on('mouseup', onMouseUp);

  function onMouseDown(e) {
    if (e.button === Def.MOUSE_BUTTON_MIDDLE) {
      editor.setPanningEnabled(true);
    }
  }

  function onMouseUp(e) {
    if (e.button === Def.MOUSE_BUTTON_MIDDLE) {
      editor.setPanningEnabled(false);
    }
  }
  
  function onKeyDown(e) {
    if ($(e?.target).is('input, textarea')) {
      return;
    }
    switch (e.keyCode) {
      case Def.KEY_SHIFT:
        editor.setMultiConnectionEnabled(true);
        break;
      case Def.KEY_CTRL:
        editor.setMouseWheelZoomEnabled(true);
        break;
    }
  }

  function onKeyUp(e) {
    if ($(e?.target).is('input, textarea')) {
      return;
    }
    switch (e.keyCode) {
      case Def.KEY_DELETE:
        editor.deleteSelected();
        break;
      case Def.KEY_SHIFT:
        editor.setMultiConnectionEnabled(false);
        break;
      case Def.KEY_ENTER:
        editor.editIcon();
        break;
      case Def.KEY_CTRL:
        editor.setMouseWheelZoomEnabled(false);
        break;
    }
  }

  this.addDomInitializer('workflow-view', function () {
    const config = this.config;
    Def.constants = config.constants;
    Def.workflowId = config.workflowId;
    Def.shortname = config.shortname;
    Def.statisticUrl = config.statisticUrl;
    Def.sentMailings = config.sentMailings || [];

    EditorsHelper.hideNodeEditors();

    const nodes = config.icons.map(Node.deserialize);
    const connections = Node.deserializeConnections(config.icons, Node.toMap(nodes));

    if (editor) {
      editor.recycle();
    }

    editor = new Editor(config.isEditable !== false, config.isContextMenuEnabled !== false, config.isFootnotesEnabled === true);

    editor.setOnInitialized(function () {
      editor.batch(function () {
        // Add all the nodes to the editor.
        nodes.forEach(function (node) {
          editor.add(node);
        });

        // Establish all the connections.
        connections.forEach(function (connection) {
          editor.connect(connection.source, connection.target);
        });
      });

      editor.updateNodeTitles();
      editor.updateFootnotes();

      if (Def.workflowId === 0) {
        Def.intialSchema = editor.serializeIcons();
      }
    });

    editor.setMinimapEnabled(config.isMinimapEnabled !== false);

    var slider = $('#slider');
    if (slider.exists()) {
      slider.slider({
        min: Def.MIN_ZOOM,
        max: Def.MAX_ZOOM,
        value: Def.DEFAULT_ZOOM,
        step: Def.ZOOM_STEP,
        create: function () {
          var slider = $(this).slider('instance');

          editor.setZoom(Def.DEFAULT_ZOOM);
          editor.setOnZoom(function (scale) {
            slider.value(scale);
          });
        },
        slide: function (event, ui) {
          editor.setZoom(ui.value);
        }
      });
    }

    if (config.fitPdfPage === true) {
      editor.fitPdfPage();
    }

    window.waitStatus = config.initializerFinishStatus || '';
    addShowStatisticListener(config.showStatisticWithoutSave);
  });

  function addShowStatisticListener(showStatisticWithoutSave) {
    $(".navbar-nav a[href*='statistic.action']").on('click', (ev) => {
      if (showStatisticWithoutSave) {
        return;
      }
      ev.preventDefault();
      saveWorkflowFormData(true, {showStatistic: true})
    });
  }

  this.addDomInitializer('mailing-data-transfer-modal', function ($e) {
    var $inputsPerParam = $e.find('input[data-mailing-param]');
    var $all = $e.find('#transferAllSettings');
    var checkedParamsMap = {};
    var checkedParams = [];

    $inputsPerParam.on('change', function () {
      var $checkbox = $(this);
      var param = $checkbox.data('mailing-param');

      if ($checkbox.prop('checked')) {
        checkedParamsMap[param] = true;
      } else {
        delete checkedParamsMap[param];
      }

      checkedParams = Object.keys(checkedParamsMap);

      $all.prop('checked', checkedParams.length == $inputsPerParam.length);
    });

    $all.on('change', function () {
      var isChecked = $all.prop('checked');

      $inputsPerParam.prop('checked', isChecked);

      if (isChecked) {
        $inputsPerParam.each(function () {
          checkedParamsMap[$(this).data('mailing-param')] = true;
        });
        checkedParams = Object.keys(checkedParamsMap);
      } else {
        checkedParamsMap = {};
        checkedParams = [];
      }
    });

    this.addAction({click: 'transfer-mailing-data'}, function () {
      Confirm.get($e).positive(checkedParams);
    });
  });

  this.addDomInitializer('own-workflow-expanding-modal', function ($e) {
    this.addAction({click: 'expand-own-workflow'}, function () {
      Confirm.get($e).positive({
        workflowId: $e.find('#workflow-select').val(),
        copyContent: $e.find('[name="copyContent"]').val() === 'true'
      });
    });
  });

  this.addDomInitializer('icon-comment-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('icon-comment', new AGN.Lib.WM.CommentEditor());
    this.addAction({click: 'icon-comment-editor-save'}, () => nodeEditor.saveEditor());
    this.addAction({click: 'icon-comment-editor-cancel'}, () => nodeEditor.cancelEditor());
  });

  this.addDomInitializer('parameter-editor-initializer', function () {
    EditorsHelper.registerEditor('parameter', new AGN.Lib.WM.ParameterNodeEditor(editor));
  });

  this.addDomInitializer('split-editor-initializer', function () {
    EditorsHelper.registerEditor('split', new AGN.Lib.WM.SplitNodeEditor(editor));
  });

  this.addDomInitializer('deadline-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('deadline', new AGN.Lib.WM.DeadlineNodeEditor());

    this.addAction({'change': 'deadline-editor-update'}, () => nodeEditor.updateVisibility());
    this.addAction({'change': 'deadline-editor-time-change'}, () => nodeEditor.onTimeChanged());
  });

  this.addDomInitializer('archive-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('archive', new AGN.Lib.WM.ArchiveNodeEditor(saveWorkflowFormData));
    this.addAction({click: 'archive-editor-new'}, () => nodeEditor.createNewArchive());
  });

  this.addDomInitializer('recipient-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('recipient', new AGN.Lib.WM.RecipientNodeEditor(saveWorkflowFormData));

    this.addAction({click: 'recipient-editor-create-new-target'}, () => nodeEditor.createNewTarget());

    this.addAction({click: 'recipient-editor-target-edit'}, function () {
      nodeEditor.editTarget(this.el.data('target-id'));
    });
  });

  this.addDomInitializer('export-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('export', new AGN.Lib.WM.ExportNodeEditor(this.config, saveWorkflowFormData));

    this.addAction({change: 'export-editor-change'}, () => nodeEditor.onChange());
    this.addAction({click: 'export-editor-new'}, () => nodeEditor.createNewAutoExport());
    this.addAction({click: 'export-editor-autoexport'}, () => nodeEditor.editAutoExport());
  });

  this.addDomInitializer('import-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('import', new AGN.Lib.WM.ImportNodeEditor(this.config, saveWorkflowFormData));

    this.addAction({change: 'import-editor-change'}, () => nodeEditor.onChange());
    this.addAction({click: 'import-editor-new'}, () => nodeEditor.createNewAutoImport());
    this.addAction({click: 'import-editor-update'}, () => nodeEditor.editAutoImport());
  });

  this.addDomInitializer('start-editor-initializer', function () {
    const nodeEditor = new AGN.Lib.WM.StartStopNodeEditor(editor, this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('start', nodeEditor);
    EditorsHelper.registerEditor('stop', nodeEditor);

    this.addAction({change: 'start-editor-execution-changed'}, () => nodeEditor.onExecutionChanged());
    this.addAction({change: 'start-editor-event-changed'}, () => nodeEditor.onStartEventChanged());
    this.addAction({change: 'start-editor-type-changed'}, () => nodeEditor.onStartTypeChanged());
    this.addAction({change: 'start-editor-rule-changed'}, () => nodeEditor.onRuleModeChanged());
    this.addAction({change: 'start-editor-reaction-changed'}, () => nodeEditor.onReactionChanged());
    this.addAction({change: 'start-editor-profile-field-changed'}, () => nodeEditor.onProfileFieldChanged());
    this.addAction({click: 'start-editor-add-rule'}, () => nodeEditor.addRule());
    this.addAction({click: 'start-editor-reminder-changed'}, () => nodeEditor.onReminderChanged());
    this.addAction({change: 'start-editor-schedule-reminder-date-changed'}, () => nodeEditor.onScheduleReminderDateChanged());
    
    this.addAction({change: 'start-editor-mailing-select'}, function () {
      nodeEditor.onMailingSelectChange(this.el.val());
    });
    
    this.addAction({click: 'start-editor-rule-remove'}, function () {
      nodeEditor.removeRuleRow(this.el.data('rule-index'));
    });
  });

  this.addDomInitializer('decision-editor-initializer', function () {
    const nodeEditor = EditorsHelper.registerEditor('decision', new AGN.Lib.WM.DecisionNodeEditor(editor, this.config));

    this.addAction({change: 'decision-editor-mailing-select'}, function () {
      nodeEditor.onMailingSelectChange(this.el.val());
    });

    this.addAction({click: 'decision-editor-rule-remove'}, function () {
      nodeEditor.removeRuleRow(this.el.data('rule-index'));
    });

    this.addAction({click: 'decision-editor-rule-add'}, () => nodeEditor.addRule());
    this.addAction({change: 'decision-editor-type-change'}, () => nodeEditor.onTypeChanged());
    this.addAction({change: 'decision-editor-criteria-change'}, () => nodeEditor.onCriteriaChanged());
    this.addAction({change: 'decision-editor-reaction-change'}, () => nodeEditor.onDecisionReactionChanged());
    this.addAction({change: 'decision-editor-profile-field-change'}, () => nodeEditor.onProfileFieldChanged());
  });

  this.addDomInitializer('mailing-editor-initializer', function () {
    const mailingEditorBase = new MailingEditorHelper(this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('mailing', new AGN.Lib.WM.MailingNodeEditor(mailingEditorBase));

    this.addAction({change: 'mailing-editor-base-status-change'}, function () {
      mailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'mailing-editor-base-select-change'}, function () {
      mailingEditorBase.onMailingSelectChange(this.el.val());
    });

    this.addAction({click: 'mailing-editor-base-toggle-settings'}, function () {
      const config = Utils.getConfigData(this.el);
      mailingEditorBase.toggleSendSettings(config.editorId);
    });
  });

  this.addDomInitializer('followup-mailing-editor-initializer', function () {
    const baseMailingData = this.config.baseMailingData;
    const followupMailingData = this.config.followupMailingData;
    baseMailingData.$dropdown = $('#followup-m-editor');

    const baseMailingEditorBase = new MailingEditorHelper(baseMailingData, saveWorkflowFormData);
    const followupMailingEditorBase = new MailingEditorHelper(followupMailingData, saveWorkflowFormData);

    baseMailingEditorBase.selectedFollowUpMailingId = 0;

    baseMailingEditorBase.findPreviousMailingIds = function () {
      const previousMailingIds = [];

      EditorsHelper.forEachPreviousNode(function (node) {
        const type = node.getType();

        if (Def.NODE_TYPES_MAILING.includes(type) && Def.NODE_TYPE_FOLLOWUP_MAILING != type) {
          previousMailingIds.push(node.getId());
        }
      });

      return previousMailingIds;
    };

    baseMailingEditorBase.getMailingsByWorkStatus = function (status, sort, order, selectedMailValue) {
      var mailingEditorBase = this;
      var $mailingsList = $(this.formNameJId + ' ' + this.selectNameJId);
      var previousMailingIds = _.uniq(this.findPreviousMailingIds()).toString();
      $mailingsList.attr('readonly', 'readonly');

      $.ajax({
        url: AGN.url('/workflow/getMailingsByWorkStatus.action'),
        data: {
          mailingTypes: this.mailingTypesForLoading.join(','),
          status: status,
          sort: sort,
          order: order,
          mailingId: this.selectedFollowUpMailingId,
          parentMailingId: $(this.formNameJId + ' ' + this.selectNameJId).val(),
          mailingStatus: 'W',
          takeMailsForPeriod: true,
          mailingsInCampaign: previousMailingIds
        },
        success: function (data) {
          //populate the drop-down list with mailings
          $mailingsList.html('');
          $mailingsList.append('<option value="0">' + t('workflow.defaults.no_mailing') + '</option>');
          for (var i = 0; i < data.length; i++) {
            var obj = data[i];
            var mailingFontColor = '';
            var selected = '';

            if (obj.WORK_STATUS == 'mailing.status.sent' || obj.WORK_STATUS == 'mailing.status.norecipients') {
              mailingFontColor = 'style=\'color: #808080;\'';
            }

            if (selectedMailValue == obj.MAILING_ID) {
              selected = 'selected';
            }

            $mailingsList.append('<option ' + mailingFontColor + ' value=\'' + obj.MAILING_ID + '\' status=\'' + obj.WORK_STATUS + '\' senddate=\'' + obj.SENDDATE + '\' ' + selected + '>' + obj.SHORTNAME + '</option>');
          }
          $mailingsList.removeAttr('readonly');
          mailingEditorBase.mailingsStatus = status;
          mailingEditorBase.mailingsSort = sort;
          mailingEditorBase.mailingsOrder = order;
          $mailingsList.val(mailingEditorBase.mailingId);

          EditorsHelper.initSelectWithValueOrChooseFirst($mailingsList, mailingEditorBase.mailingId);
        }
      });
    };

    followupMailingEditorBase.onMailingSelectChange = function (val) {
      baseMailingEditorBase.selectedFollowUpMailingId = val;
      this.setSelectMailingOptions(val);
    };

    EditorsHelper.registerEditor('followup_mailing', new AGN.Lib.WM.FollowupMailingNodeEditor(baseMailingEditorBase, followupMailingEditorBase, followupMailingData.disableFollowup === 'true'));

    this.addAction({change: 'followup-mailing-editor-base-status-change'}, function () {
      followupMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'followup-mailing-editor-base-select-change'}, function () {
      followupMailingEditorBase.onMailingSelectChange(this.el.val());
    });

    this.addAction({change: 'followup-base-mailing-editor-base-status-change'}, function () {
      baseMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'followup-base-mailing-editor-base-select-change'}, function () {
      baseMailingEditorBase.onMailingSelectChange(this.el.val());
    });
  });

  this.addDomInitializer('sms-mailing-editor-initializer', function () {
    const smsMailingEditorBase = new MailingEditorHelper(this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('mailing_mediatype_sms', new AGN.Lib.WM.SmsMailingNodeEditor(smsMailingEditorBase));

    this.addAction({change: 'sms-mailing-editor-base-status-change'}, function () {
      smsMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'sms-mailing-editor-base-select-change'}, function () {
      smsMailingEditorBase.onMailingSelectChange(this.el.val());
    });
  });

  this.addDomInitializer('post-mailing-editor-initializer', function () {
    const postMailingEditorBase = new MailingEditorHelper(this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('mailing_mediatype_post', new AGN.Lib.WM.PostMailingNodeEditor(postMailingEditorBase));

    this.addAction({change: 'post-mailing-editor-base-status-change'}, function () {
      postMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'post-mailing-editor-base-select-change'}, function () {
      postMailingEditorBase.onMailingSelectChange(this.el.val());
    });
  });

  this.addDomInitializer('action-mailing-editor-initializer', function () {
    const actionbasedMailingEditorBase = new MailingEditorHelper(this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('actionbased_mailing', new AGN.Lib.WM.ActionBasedMailingNodeEditor(actionbasedMailingEditorBase));
    this.addAction({change: 'action-mailing-editor-base-status-change'}, function () {
      actionbasedMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'action-mailing-editor-base-select-change'}, function () {
      actionbasedMailingEditorBase.onMailingSelectChange(this.el.val());
    });
  });

  this.addDomInitializer('date-based-mailing-editor-initializer', function () {
    const datebasedMailingEditorBase = new MailingEditorHelper(this.config, saveWorkflowFormData);
    EditorsHelper.registerEditor('datebased_mailing', new AGN.Lib.WM.DateBasedMailingNodeEditor(datebasedMailingEditorBase));

    this.addAction({change: 'date-mailing-editor-base-status-change'}, function () {
      datebasedMailingEditorBase.onMailingsStatusChange(this.el.val());
    });

    this.addAction({change: 'date-mailing-editor-base-select-change'}, function () {
      datebasedMailingEditorBase.onMailingSelectChange(this.el.val());
    });
  });

  this.addAction({change: 'save-node'}, function () {
    if (!EditorsHelper.isReadOnlyMode()) {
      EditorsHelper.getCurrentEditor().save();
    }
  });
  
  this.addAction({click: 'mailing-editor-edit'}, () => EditorsHelper.getCurrentEditor().editMailing());
    
  this.addAction({click: 'mailing-editor-new'}, () => EditorsHelper.getCurrentEditor().createNewMailing());

  this.addAction({click: 'chain-mode'}, function () {
    editor.setChainModeEnabled(!(editor.isChainMode() || editor.connectSelected()));
    $(this.el).toggleClass('active', editor.isChainMode());
  });

  this.addAction({click: 'delete-selected'}, () => editor.deleteSelected());
  this.addAction({click: 'show-grid' }, () => editor.toggleGrid());
  this.addAction({submission: 'workflow-save'}, () => saveWorkflowFormData(true, {}));
  this.addAction({click: 'align-all'}, () => editor.alignAll());
  this.addAction({click: 'undo'}, () => editor.undo());
  
  this.addAction({click: 'zoom-in'}, function () {
    const slider = $('#slider').slider('instance');
    slider.value(slider.value() + Def.ZOOM_STEP);
    editor.setZoom(slider.value());
  });

  this.addAction({click: 'zoom-out'}, function () {
    const slider = $('#slider').slider('instance');
    slider.value(slider.value() - Def.ZOOM_STEP);
    editor.setZoom(slider.value());
  });

  this.addAction({click: 'workflow-copy'}, function () {
    const workflowId = Def.workflowId;
    //checks whether at least one node has content
    const someNonEmptyNode = editor.getNodes().some(node => node.isFilled());

    Dialogs.confirmCopy(someNonEmptyNode).done(function (response) {
      window.location.href = AGN.url(`/workflow/copy.action?workflowId=${workflowId}&isWithContent=${response}`);
      return false;
    });
  });

  this.addAction({click: 'workflow-generate-pdf'}, function () {
    const isNewWorkflow = Def.workflowId <= 0 || !Def.shortname;
    const hasUnsavedChanges = editor.hasUnsavedChanges();

    if (isNewWorkflow || hasUnsavedChanges) {
      Modal.fromTemplate('save-before-pdf-modal');
    } else {
      window.location.href = AGN.url('/workflow/' + Def.workflowId + '/generatePDF.action');
      AGN.Lib.Loader.hide();
    }
  });

  this.addAction({click: 'workflow-activate'}, () => Dialogs.Activation(getMailingNames()).done(activate));
  this.addAction({click: 'workflow-deactivate'}, () => deactivate(true, {'status': Def.constants.statusInactive}));
  this.addAction({click: 'workflow-pause'}, () => saveWorkflowFormData(true, {'status': Def.constants.statusPaused}));
  this.addAction({click: 'workflow-unpause'}, () => unpause({'status': Def.constants.statusActive}));
  this.addAction({click: 'workflow-dry-run'}, function () {
    let isStartTesting = false;
    let newStatus = Def.constants.statusOpen;
    if (Def.constants.initialWorkflowStatus != Def.constants.statusTesting) {
      isStartTesting = true;
      newStatus = Def.constants.statusTesting;
    }
    Dialogs.confirmTestingStartStop(isStartTesting).done(() => saveWorkflowFormData(true, {'status': newStatus}));
  });
  
  function activate() {
    editor
      .getNodesByType(Def.NODE_TYPE_START)
      .filter(isStartNodeDateInPast)
      .forEach(adjustStartDate);
    saveWorkflowFormData(true, {'status': Def.constants.statusActive})
  }

  function isStartNodeDateInPast(startNode) {
    const startDate = _.clone(startNode.data.date);
    if (!startDate) {
      return false;
    }

    startDate.setHours(startNode.data.hour);
    startDate.setMinutes(startNode.data.minute);
    startDate.setSeconds(0);
    return startDate <= new Date();
  }
  
  function adjustStartDate(startNode) {
    const { hour, minute } = DateTimeUtils.getCurrentAdminTime() || {};
    if (!hour || !minute) {
      return;
    }
    startNode.data.date = new Date();
    const startDate = startNode.data.date;
    startDate.setHours(parseInt(hour, 10));
    startDate.setMinutes(parseInt(minute, 10));
    startDate.setSeconds(startDate.getSeconds() > 55 ? (startDate.getSeconds() + 120) : (startDate.getSeconds() + 60)); // add one-two minutes
    startNode.data.hour = startDate.getHours();
    startNode.data.minute = startDate.getMinutes();
    
    editor.updateNodeTitle(startNode, true);
    getWorkflowForm().setValueOnce('startTimeAdjusted', true);
  }

  function unpause(options) {
    Dialogs.Activation([], true).done(() => saveWorkflowFormData(true, options));
  }

  function deactivate(validate, options) {
    Dialogs.Deactivation().done(() => saveWorkflowFormData(validate, options));
  }

  function saveWorkflowFormData(validate, options) {
    if (validate && !isValidWorkflowBaseData()) {
      return;
    }
    const form = getWorkflowForm();
    options = $.extend({}, options);
    Object.keys(options).forEach(function (key) {
      form.setValueOnce(key, options[key]);
    })
    if (editor.gridBackgroundShown) {
      // since when opening the editor we do not know in which coordinate system the icons were located (default or grid),
      // then when saving we turn off the grid, which transforms the position values of the icons
      // and after saving they will not lose their location in the editor
      editor.toggleGrid();
    }
    form.setValueOnce('workflowSchema', editor.serializeIcons());
    form.setValueOnce('editorPositionLeft', -1); //todo: remove if not needed
    form.setValueOnce('editorPositionTop', -1); //todo: remove if not needed
    form.submit('static');
  }
  
  function getWorkflowForm() {
    return AGN.Lib.Form.get($('form#workflowForm'));
  }

  function getMailingNames() {
    return editor.getNodesByTypes(Def.NODE_TYPES_MAILING)
      .map(node => node.title)
      .filter(node => node.trim());
  }

  function isValidWorkflowBaseData() {
    if ($('#name').val().length < 3) {
      getWorkflowForm().showFieldError('shortname', t('error.workflow.shortName'));
      return false;
    }
    return true;
  }

  this.addDomInitializer('open-edit-icon-initializer', function ($e) {
    const data = $e.json();
    const nodeId = data.nodeId;
    const elementValue = data.elementValue;
    if (!nodeId || !elementValue) {
      return;
    }
    const node = Node.get($('#' + nodeId));
    if (!node) {
      return;
    }
    prepareNodeToOpenOnStart(node, elementValue);
    editor.editIcon(node);
    editor.updateNodeTitle(node, true);
  });

  function prepareNodeToOpenOnStart(node, elementValue) {
    switch (node.type) {
      case Def.NODE_TYPE_MAILING:
      case Def.NODE_TYPE_ACTION_BASED_MAILING:
      case Def.NODE_TYPE_MAILING_MEDIATYPE_SMS:
      case Def.NODE_TYPE_MAILING_MEDIATYPE_POST:
      case Def.NODE_TYPE_DATE_BASED_MAILING:
      case Def.NODE_TYPE_FOLLOWUP_MAILING:
        node.data.mailingId = elementValue;
        break;
      case Def.NODE_TYPE_RECIPIENT:
        if (elementValue != "0" && $.inArray(elementValue, node.data.targets) == -1) {
          node.data.targets.push(elementValue);
        }
        break;
      case Def.NODE_TYPE_ARCHIVE:
        node.data.campaignId = elementValue;
        break;
      case Def.NODE_TYPE_IMPORT:
      case Def.NODE_TYPE_EXPORT:
        node.data.importexportId = elementValue;
        break;
      default:
        console.debug("Unknown node type");
    }
  }
});
