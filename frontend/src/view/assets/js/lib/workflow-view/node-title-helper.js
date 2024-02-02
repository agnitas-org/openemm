(function () {

  var Def = AGN.Lib.WM.Definitions,
    DateTimeUtils = AGN.Lib.WM.DateTimeUtils;

  //entity names map is loading on the fly by entity type if nothing is still loaded
  var ENTITY_NAMES_BY_TYPE = {};

  var NodeTitleHelper = function(){};

  function getEntityNameByType(type, id, url) {
    if (id <= 0) {
      return '';
    }

    //preload data id map is empty
    if (_.isEmpty(ENTITY_NAMES_BY_TYPE[type])) {
      $.ajax({url: AGN.url(url), async: false}).done(function (resp) {
        ENTITY_NAMES_BY_TYPE[type] = _.extend({}, resp);
      });
    }
    var name = ENTITY_NAMES_BY_TYPE[type][id];
    return !!name ? name : '';
  }

  NodeTitleHelper.getWebFormName = function (webFormId) {
    return getEntityNameByType('form', webFormId, "/workflow/ajax/getWebFormNames.action");
  };

  NodeTitleHelper.getTargetName = function (targetId) {
    return getEntityNameByType('target', targetId, "/workflow/ajax/getTargetNames.action");
  };

  NodeTitleHelper.getMailingName = function (mailingId) {
    return getEntityNameByType('mailing', mailingId, "/workflow/ajax/getMailingNames.action");
  };

  NodeTitleHelper.getArchiveName = function (archiveId) {
    return getEntityNameByType('archive', archiveId, "/workflow/ajax/getArchiveNames.action");
  };

  NodeTitleHelper.getAutoExportName = function (autoExportId) {
    return getEntityNameByType('autoExport', autoExportId, "/workflow/ajax/getAutoExportNames.action");
  };

  NodeTitleHelper.getAutoImportName = function (autoImportId) {
    return getEntityNameByType('autoImport', autoImportId, "/workflow/ajax/getAutoImportNames.action");
  };

  NodeTitleHelper.getMailinglistName = function (mailinglistId) {
    return getEntityNameByType('mailinglist', mailinglistId, "/workflow/ajax/getMailinglistNames.action");
  };

  function getReactionImage(reaction) {
    switch (reaction) {
      case Def.constants.reactionOpened:
        return "reaction_opened.png";
      case Def.constants.reactionNotOpened:
        return "reaction_notopened.png";
      case Def.constants.reactionClicked:
        return "reaction_clicked.png";
      case Def.constants.reactionNotClicked:
        return "reaction_notclicked.png";
      case Def.constants.reactionBought:
        return "reaction_bought.png";
      case Def.constants.reactionNotBought:
        return "reaction_notbought.png";
      case Def.constants.reactionChangeOfProfile:
        return "reaction_profilechange.png";
      case Def.constants.reactionWaitingForConfirm:
        return "reaction_wfc.png";
      case Def.constants.reactionOptIn:
        return "reaction_optin.png";
      case Def.constants.reactionOptOut:
        return "reaction_optout.png";
      case Def.constants.reactionClickedLink:
        return "reaction_clicked.png";
      case Def.constants.reactionOpenedAndClicked:
        return "reaction_profilechange.png";
      case Def.constants.reactionOpenedOrClicked:
        return "reaction_profilechange.png";
    }
  }

  function getReactionName(reaction) {
    switch (reaction) {
      case Def.constants.reactionOpened:
        return t('workflow.reaction.opened');
      case Def.constants.reactionNotOpened:
        return t('workflow.reaction.not_opened');
      case Def.constants.reactionClicked:
        return t('workflow.reaction.clicked');
      case Def.constants.reactionNotClicked:
        return t('workflow.reaction.not_clicked');
      case Def.constants.reactionBought:
        return t('workflow.reaction.bought');
      case Def.constants.reactionNotBought:
        return t('workflow.reaction.not_bought');
      case Def.constants.reactionChangeOfProfile:
        return t('workflow.reaction.change_of_profile');
      case Def.constants.reactionWaitingForConfirm:
        return t('workflow.reaction.waiting_for_confirm');
      case Def.constants.reactionOptIn:
        return t('workflow.reaction.opt_in');
      case Def.constants.reactionOptOut:
        return t('workflow.reaction.opt_out');
      case Def.constants.reactionClickedLink:
        return t('workflow.reaction.clicked_on_link');
      case Def.constants.reactionOpenedAndClicked:
        return t('workflow.reaction.opened_and_clicked');
      case Def.constants.reactionOpenedOrClicked:
        return t('workflow.reaction.opened_or_clicked');
    }
  }

  function getDelayUnitName(delayValue, delayUnit) {
    if (delayValue > 1) {
      switch (delayUnit) {
        case Def.constants.deadlineTimeUnitMinute:
          return t('workflow.deadline.minutes');
        case Def.constants.deadlineTimeUnitHour:
          return t('workflow.defaults.hours');
        case Def.constants.deadlineTimeUnitDay:
          return t('workflow.defaults.days');
        case Def.constants.deadlineTimeUnitWeek:
          return t('workflow.defaults.weeks');
        case Def.constants.deadlineTimeUnitMonth:
          return t('workflow.defaults.months');
      }
    } else {
      switch (delayUnit) {
        case Def.constants.deadlineTimeUnitMinute:
          return t('workflow.deadline.minute');
        case Def.constants.deadlineTimeUnitHour:
          return t('workflow.defaults.hour');
        case Def.constants.deadlineTimeUnitDay:
          return t('workflow.defaults.day');
        case Def.constants.deadlineTimeUnitWeek:
          return t('workflow.defaults.week');
        case Def.constants.deadlineTimeUnitMonth:
          return t('workflow.defaults.month');
      }
    }
  }

  function getDecisionCriteriaName(value) {
    switch (value) {
      case Def.constants.decisionAOCriteriaClickRate:
        return t('workflow.defaults.ckickrate');
      case Def.constants.decisionAOCriteriaOpenrate:
        return t('workflow.opening_rate');
      default:
        return t('workflow.statistic.revenue');
    }
  }

  function getCloseParenthesis(value) {
    switch (Number(value)) {
      case 1:
        return ')';
      case 0:
        return '';
    }
  }

  function getOpenParenthesis(value) {
    switch (Number(value)) {
      case 1:
        return '(';
      case 0:
        return '';
    }
  }

  function getProfileFieldValue(field, value) {
    if (field && Def.GENDER_PROFILE_FIELD === field.toLowerCase()) {
      return Def.constants.genderOptions[!value ? 0 : value];
    } else {
      return _.isEmpty(value) ? '' : value;
    }
  }

  function getRulesDescription(profileField, rules) {
    var rulesMsg = '';

    rules.forEach(function(rule) {
      rulesMsg +=
        (_.isEmpty(rulesMsg) ? '' : ' ' + rule.chainOperator + ' ') +
        getOpenParenthesis(rule.parenthesisOpened) + profileField + ' ' +
        Def.constants.operatorsMap[rule.primaryOperator] + ' ' +
        getProfileFieldValue(profileField, rule.primaryValue) +
        getCloseParenthesis(rule.parenthesisClosed);
    });

    return t('workflow.start.rule') + (!!rulesMsg ? ': ' + rulesMsg : '');
  }

  function getTargetsDescription(targets, option) {
    var targetNames = [];
    for (var i = 0; i < Def.TITLE_MAX_TARGETS && i < targets.length; i++) {
      targetNames.push(NodeTitleHelper.getTargetName(targets[i]));
    }

    var separator = ', ';
    if (Def.constants.targetOptions[option]) {
      separator = ' ' + Def.constants.targetOptions[option] + ' ';
    }

    if (targets.length > Def.TITLE_MAX_TARGETS) {
      return targetNames.join(separator) + ' ' + t('defaults.andMore', targets.length - Def.TITLE_MAX_TARGETS) + '...';
    } else {
      return targetNames.join(separator);
    }
  }

  function getDateDescription(date, hour, minute) {
    if (date) {
      if (arguments.length === 1) {
        return DateTimeUtils.getDateStr(date, Def.constants.localeDateTimePattern);
      } else {
        return DateTimeUtils.getDateTimeStr(date, hour, minute, Def.constants.localeDateTimePattern);
      }
    } else {
      return '';
    }
  }

  function getDelayDescription(value, timeUnit) {
    return value + ' ' + getDelayUnitName(value, timeUnit);
  }

  function getReactionDescription(reaction, mailingId) {
    return t('workflow.reaction.title') + ': ' + getReactionName(reaction) + ',\n' +
      t('workflow.defaults.mailing') + ': ' + NodeTitleHelper.getMailingName(mailingId);
  }

  function getAutoOptimizationDescription(criteria, threshold, date) {
    var description = getDecisionCriteriaName(criteria) + (_.isEmpty(threshold) ? '' : ': ' + threshold) + '\n';

    if (!!date) {
      description += t('workflow.defaults.date') + ': ' + getDateDescription(date);
    }

    return description;
  }

  function getDateBasedStartDescription(field, operator, value) {
    return field + ' ' + Def.constants.operatorsMap[operator] + (!value ? '' : ' ' + value);
  }

  function getFollowupMailingDescription(followUpMailingName, baseMailingName, reaction) {
    if (followUpMailingName) {
      if (baseMailingName) {
        return baseMailingName + ' (' + getReactionName(reaction) + ') ' + followUpMailingName;
      } else {
        return followUpMailingName;
      }
    }
  }

  function getRecipientDescription(mailinglistId, targets, targetsOption, showMailingList) {
    var title = '';
    if(showMailingList) {
      title += t('workflow.mailinglist.short') + ': ' + NodeTitleHelper.getMailinglistName(mailinglistId);
    }

    if (!_.isEmpty(targets)) {
      if(!_.isEmpty(title)) {
        title += '\n';
      }
      title += t('workflow.target.short') + ': ' + getTargetsDescription(targets, targetsOption);
    }

    return title;
  }

  NodeTitleHelper.positionTitle = function(node, anchorsInUse) {
    if (anchorsInUse[Def.BOTTOM]) {
        if (anchorsInUse[Def.LEFT]) {
            if (anchorsInUse[Def.RIGHT]) {
                if (anchorsInUse[Def.TOP]) {
                    node.positionTitle(Def.BOTTOM);
                } else {
                    node.positionTitle(Def.TOP);
                }
            } else {
                node.positionTitle(Def.RIGHT);
            }
        } else {
            node.positionTitle(Def.LEFT);
        }
    } else {
        node.positionTitle(Def.BOTTOM);
    }
  };

  var getStartNodeTitle = function(data) {
    var startType = data.startType;
    var title = '';

    //start on date
    if (Def.constants.startTypeDate === startType) {
      title = t('workflow.start.start_date') + ':\n' + getDateDescription(data.date, data.hour, data.minute);
    }
    //start on event
    else if (Def.constants.startTypeEvent === startType) {
      var eventType = data.event;

      //reaction based start
      if (Def.constants.startEventReaction === eventType) {
        title = t('workflow.start.start_event') + ':\n' + getReactionName(data.reaction) + ': (' + getDateDescription(data.date, data.hour, data.minute) + ')';
      }
      //date based start
      else if (Def.constants.startEventDate === eventType) {
        title = t('workflow.start.start_event') + ':\n' +
          getDateBasedStartDescription(data.dateProfileField, data.dateFieldOperator, data.dateFieldValue) + ':\n' +
          '(' + getDateDescription(data.date, data.hour, data.minute) + ')';
      }
    }

    return title;
  }

  var getStopNodeTitle = function(data, node, isNormalWorkflow) {
    var endType = data.endType;
    var title = '';

    //date based end
    if (Def.constants.endTypeDate === endType) {
      title = t('workflow.stop.end_date') + ':\n' + getDateDescription(data.date, data.hour, data.minute);
    }
    //automatic end
    else if (Def.constants.endTypeAutomatic === endType) {
      title = isNormalWorkflow ? t('workflow.stop.automatic_end') : t('workflow.stop.open_end');
    }

    return title;
  }

  var getDeadlineNodeTitle = function(data) {
    var deadlineType = data.deadlineType;
    var title = '';

    if (Def.constants.deadlineTypeFixedDeadline === deadlineType) {
      title = t('workflow.deadline.title') + ':\n' + getDateDescription(data.date, data.hour, data.minute);
    } else if (Def.constants.deadlineTypeDelay === deadlineType) {
      title = t('workflow.defaults.delay') + ':\n' + getDelayDescription(data.delayValue, data.timeUnit);
    }

    return title;
  }

  var getDecisionNodeTitle = function(data) {
    var decisionType = data.decisionType;
    var title = '';
    if (Def.constants.decisionTypeAutoOptimization === decisionType) {
      title = t('workflow.mailing.autooptimization') + ':\n' + getAutoOptimizationDescription(data.aoDecisionCriteria, data.threshold, data.decisionDate);
    } else {
      if (Def.constants.decisionReaction === data.decisionCriteria) {
        title = t('workflow.decision') + ',\n' + getReactionDescription(data.reaction, data.mailingId);
      } else {
        title = t('workflow.decision') + ',\n' + getRulesDescription(data.profileField, data.rules);
      }
    }

    return title;
  }

  var getFollowUpMailingNodeTitle = function(data, node) {
    var baseMailingName, followUpMailingName;
    var title = node.getTitle();

    baseMailingName = NodeTitleHelper.getMailingName(data.baseMailingId) || '?';
    followUpMailingName = NodeTitleHelper.getMailingName(data.mailingId) || '?';

    var description = getFollowupMailingDescription(followUpMailingName, baseMailingName, data.decisionCriterion);
    if (description) {
      //don't update title if updated description is empty
      title = description;
    }

    return title;
  }

  var MAILING_TITLE_CONFIG = {
    title: function(data) {
      return NodeTitleHelper.getMailingName(data.mailingId)
    }
  }

  var TITLE_CONFIG = {
    '*': {
      overlayImage: {},
      overlayTitle: '',
      title: ''
    },
    start: {
      title: getStartNodeTitle,
      isImageAvailable: function(data) {
        return Def.constants.startTypeEvent === data.startType && Def.constants.startEventReaction === data.event;
      },
      overlayImage: function(data) {
        if (this.isImageAvailable(data)) {
          return {visible: true, image: getReactionImage(data.reaction), title: getReactionName(data.reaction)}
        } else {
          return {};
        }
      },
    },
    stop: {
      title: getStopNodeTitle,
    },
    deadline: {
      title: getDeadlineNodeTitle,
    },
    parameter: {
      title: '',
      overlayTitle: function(data) {
        return data.value;
      }
    },
    decision: {
      title: getDecisionNodeTitle,
      branches: {
        title: function () {
          return {
            positive: t('workflow.defaults.yes'),
            negative: t('workflow.defaults.no')
          };
        }
      }
    },
    recipient: {
      title: function(data, node) {
        return getRecipientDescription(data.mailinglistId, data.targets, data.targetsOption, !node.isInRecipientsChain());
      }
    },
    archive: {
      title: function(data) {
        return NodeTitleHelper.getArchiveName(data.campaignId)
      }
    },
    form: {
      title: function(data) {
        return NodeTitleHelper.getWebFormName(data.userFormId);
      }
    },
    mailing: MAILING_TITLE_CONFIG,
    mailing_mediatype_sms: MAILING_TITLE_CONFIG,
    mailing_mediatype_post: MAILING_TITLE_CONFIG,
    actionbased_mailing: MAILING_TITLE_CONFIG,
    datebased_mailing: MAILING_TITLE_CONFIG,
    followup_mailing: {
      title: getFollowUpMailingNodeTitle
    },
    import: {
      title: function(data) {
        return NodeTitleHelper.getAutoImportName(data.importexportId);
      }
    },
    export: {
      title: function(data) {
        return NodeTitleHelper.getAutoExportName(data.importexportId);
      }
    }
  };

  function ignoreUpdateTitle(node) {
    var type = node.getType();
    if (Def.NODE_TYPES_MAILING.includes(type) || Def.NODE_TYPES_IMPORT_EXPORT.includes(type)) {
      //if title exists don't update for mailing and import/export nodes
      return !!node.getTitle();
    }

    return false;
  }

  NodeTitleHelper.getDecisionBranchesLabels = function() {
    return TITLE_CONFIG.decision.branches.title();
  }

  NodeTitleHelper.updateTitle = function(node, isNormalWorkflow, forceUpdate) {
    if (!forceUpdate && ignoreUpdateTitle(node)) {
      return;
    }

    if (!node.isFilled()) {
      //reset all titles items
      node.setTitle('');
      node.setOverlayImage({});
      node.setOverlayTitle('');
      return;
    }

    node.setTitle(generateNodeTitle(node, isNormalWorkflow));
    node.setOverlayImage(generateOverlayImage(node));
    node.setOverlayTitle(generateOverlayTitle(node));

  };

  function generateOverlayImage(node) {
    return getConfiguration(node.getType(), 'overlayImage', node.getData());
  }

  function generateOverlayTitle(node) {
    return getConfiguration(node.getType(), 'overlayTitle', node.getData());
  }

  function generateNodeTitle(node, isNormalWorkflow) {
    return getConfiguration(node.getType(), 'title', node.getData(), node, isNormalWorkflow);
  }

  function getConfiguration(type, configName) {
    var configs = TITLE_CONFIG[type];
    if (!configs.hasOwnProperty(configName)) {
      configs = TITLE_CONFIG['*'];
    }

    var config = configs[configName];

    var args = Array.prototype.slice.call(arguments, 2);
    return _.isFunction(config) ? config.apply(configs, args) : config;
  }

  AGN.Lib.WM.NodeTitleHelper = NodeTitleHelper;

})();
