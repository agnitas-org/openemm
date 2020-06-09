(function($) {

  var DateTimeUtils = AGN.Lib.WM.DateTimeUtils,
    CampaignManagerNodes = function CampaignManagerNodes(data) {

    /*******************************************************************************************************************
     * "Constants"
     ******************************************************************************************************************/

    // --- positions --- //

    this.LEFT = "left";
    this.TOP = "top";
    this.RIGHT = "right";
    this.BOTTOM = "bottom";

    // --- EndOf positions --- //

    /*******************************************************************************************************************
     * Private members
     ******************************************************************************************************************/

    var self = this;

    var nodes = {};
    var nextNodeIndex = 0;
    var iconNodesClass = "iconNode";

    var campaignManager = data.campaignManager;
    var scale = data.campaignManagerScale;
    var nodeFactory = data.nodeFactory;

    var nodeSizeInCoordinate = scale.pxToCoordinate(scale.getScaledNodeSize()) - 1;

    var allUsedEntity = data.allUsedEntity;

    this.openNodeId = null;
    this.openNodeValue = null;

    this.nodesContainerDOMId = data.nodesContainerDOMId;
    this.autoOptData = $.extend({resultMailingId: 0}, data.autoOptData);
    this.localeDateNTimePattern = data.localeDateNTimePattern;

    this.MAX_NUMBER_OF_TARGETS_FOR_DISPLAY = 2;
    this.MAX_NUMBER_OF_REPORTS_FOR_DISPLAY = 2;

    this.REACTION_OPENED = "OPENED";
    this.REACTION_NOT_OPENED = "NOT_OPENED";
    this.REACTION_CLICKED = "CLICKED";
    this.REACTION_NOT_CLICKED = "NOT_CLICKED";
    this.REACTION_BOUGHT = "BOUGHT";
    this.REACTION_NOT_BOUGHT = "NOT_BOUGHT";

    this.TITLE_ELLIPSIS_LENGTH = 14;

    var getIconNodesClass = function() {
      return iconNodesClass;
    };

    var getReactionIconPosition = function(usedAnchors, cmScale, imageScale) {
      var top = 0;
      var left = cmScale.getScaledNodeSize() - cmScale.getScaledNodeSize() * imageScale / 100;
      return {"top": top, "left": left};
    };

    this.getIconExtraInfoPosition = function(node, cmScale) {
      var top, left = 0;

      if ((node.type == nodeFactory.NODE_TYPE_START || node.type == nodeFactory.NODE_TYPE_STOP)
        && (node.data.startType == constants.startTypeEvent && node.data.event == constants.startEventReaction)
      ) {
        if (node.usedAnchors.indexOf(self.BOTTOM) == -1 && node.usedAnchors.indexOf(self.TOP) == -1) {
          top = cmScale.getScaledNodeSize();
          left = 0;
          return {"top": top, "left": left};
        } else if (node.usedAnchors.indexOf(self.LEFT) == -1 && node.usedAnchors.indexOf(self.RIGHT) == -1) {
          //if by-side orientation available
          top = cmScale.getScaledNodeSize() * 0.3;
          left = cmScale.getScaledNodeSize() * -1;
          return {"top": top, "left": left};
        }
      }

      if (node.usedAnchors.indexOf(self.BOTTOM) == -1) {
        top = cmScale.getScaledNodeSize();
        left = 0;
      } else if (node.usedAnchors.indexOf(self.LEFT) == -1) {
        top = cmScale.getScaledNodeSize() * 0.3;
        left = cmScale.getScaledNodeSize() * -1;
      } else if (node.usedAnchors.indexOf(self.RIGHT) == -1) {
        top = cmScale.getScaledNodeSize() * 0.3;
        left = cmScale.getScaledNodeSize();
      } else if (node.usedAnchors.indexOf(self.TOP) == -1) {
        top = cmScale.getScaledNodeSize() * -0.4;
        left = 0;
      } else {
        //bottom by default
        top = cmScale.getScaledNodeSize();
        left = 0;
      }

      return {"top": top, "left": left};
    };

    /*******************************************************************************************************************
     * Public members
     ******************************************************************************************************************/

    // when dropping node to non empty cell, there are to many questions and to many ways to go with
    // better just to decline drop into filled cell
    this.isEmptyCell = function(x, y, selectedNodes) {
      return this.isEmptyArea(x, y, 1, 1, selectedNodes);
    };

    this.isEmptyArea = function(x, y, width, height, selectedNodes) {
      var nodes = self.getNodesFromRegion(x, y, x + Math.abs(width) * nodeSizeInCoordinate, y + Math.abs(height) * nodeSizeInCoordinate, 0);
      if ((0 == Object.keys(nodes).length) && (0 == selectedNodes.length)) {
        return true;
      } else {
        var count = 0;

        _.each(nodes, function(node) {
          for(var j = 0; j < selectedNodes.length; j++) {
            if (selectedNodes[j].DOMElement.id == node.element.id) {
              count++;
              break;
            }
          }
        });

        return (Object.keys(nodes).length == count);
      }
    };

    this.count = function() {
      return Object.keys(nodes).length;
    };

    this.createNodeElement = function(type, index) {
      var nodeId = this.getNodeIdPrefix() + index;
      var image = nodeFactory.getEmptyImage(type);
      var container = $("#" + this.nodesContainerDOMId);
      var element = $(
        '<div class="' + getIconNodesClass() + '" id="' + nodeId + '">' +
        '<img class="node-image" src="' + campaignManagerSettings.imagePath + image + '"/>' +
        '</div>'
      );
      container.append(element);
      element.data("index", index).disableSelection().find("img").disableSelection();
      return element.get(0);
    };

    this.addLabelsToNodeElement = function(element, nodeData) {
      //add just one label and prioritize by insert order
      var appendLabelIfNotEmpty = function(element, labelContent, classes) {
        var $el = $(element);
        if (!!labelContent) {
          $el.addClass(classes);
          $el.append($('<div class="icon-label">' + labelContent + '</span></div>'));
          return true;
        }
        return false;
      };

      var autoOptWinner = this.autoOptData.resultMailingId;
      var labelContent = nodeFactory.getAutoOptWinnerLabelContent(autoOptWinner, nodeData.type, nodeData.mailingId);
      if (appendLabelIfNotEmpty(element, labelContent, 'icon-autoopt-winner')) {
        return;
      }
    };

    this.addNode = function(type, x, y) {
      if (this.isEmptyCell(x, y, [])) {
        var newIndex = this.generateId();
        var element = this.createNodeElement(type, newIndex);
        nodes[newIndex] = nodeFactory.createNode(type, x, y, element, newIndex, campaignManager);
        return nodes[newIndex];
      }
      else {
        return false;
      }
    };

    this.addNodeSequence = function(types, x, y) {
      function calculate(n) {
        if (n > 1) {
          return n * 2 - 1;
        } else {
          return n;
        }
      }

      if (types.length > 0 && this.isEmptyArea(x, y, calculate(types.length), 1, [])) {
        return types.map(function(type, index) {
          var newIndex = self.generateId();
          var element = self.createNodeElement(type, newIndex);
          var offsetX = (calculate(index + 1) - 1) * nodeSizeInCoordinate;
          return nodes[newIndex] = nodeFactory.createNode(type, x + offsetX, y, element, newIndex, campaignManager);
        });
      } else {
        return false;
      }
    };

    this.generateId = function() {
      return ++nextNodeIndex;
    };

    this.restoreNode = function(nodeData) {
      if ((nodeData.id + '').indexOf('new') == -1) {
        if (nextNodeIndex < nodeData.id) {
          nextNodeIndex = nodeData.id + 1;
        }
      } else {
        var s = nodeData.id.split('-');
        var i = parseInt(s[s.length - 1]);
        if (i > nextNodeIndex) {
          nextNodeIndex = i;
        }
      }
      var index = nodeData.id;
      var element = this.createNodeElement(nodeData.type, index);
      this.addLabelsToNodeElement(element, nodeData);
      nodes[index] = nodeFactory.createNodeFromData(nodeData, element, campaignManager);
      return nodes[index];
    };

    this.normalizeNodes = function() {
      // remove 1/1 offset before calculating minX minY
      self.addIndexOffset(-1, -1);

      var minX = false;
      var minY = false;
      for(var i in nodes) {
        if (minX === false || minX > nodes[i].x) {
          minX = nodes[i].x;
        }
        if (minY === false || minY > nodes[i].y) {
          minY = nodes[i].y;
        }
      }
      if (minX || minY) {
        self.addIndexOffset(
          -minX,
          -minY
        );
      }
      // create offset so that the workflow will have margin at top/left = 1 grid cell
      self.addIndexOffset(1, 1);

      return {
        collapsedX: -minX,
        collapsedY: -minY
      };
    };

    this.getNodes = function() {
      return nodes;
    };

    this.updateNode = function(index, properties) {
      $.extend(nodes[index], properties);
    };

    this.getIconNodesSelector = function() {
      return "." + getIconNodesClass();
    };

    this.getNodeIdPrefix = function() {
      return "campaignManagerNode-";
    };

    this.getNewNodeIdPrefix = function() {
      return "new-";
    };

    this.deleteNode = function(nodeId) {
      for(i in nodes) {
        if (nodes[i].element.id == nodeId) {

          // delete HTML element
          $(nodes[i].element).remove();

          // delete node
          delete nodes[i];

          break;
        }
      }
    };

    this.getNodesMap = function() {
      var map = {};

      _.each(nodes, function(node) {
        map[node.element.id] = node;
      });

      return map;
    };

    this.getNodeById = function(nodeId) {
      for(var i in nodes) {
        if (nodes[i].element.id === nodeId) {
          return nodes[i];
        }
      }
      return null;
    };

    this.addIndexOffset = function(indexOffsetX, indexOffsetY) {
      for(i in nodes) {
        this.updateNode(i, {x: nodes[i].x + indexOffsetX, y: nodes[i].y + indexOffsetY});
      }
    };

    this.getNodesFromRegion = function(minXCoord, minYCoord, maxXCoord, maxYCoord, delta) {
      var foundNodes = {};
      for(i in nodes) {
        if (((nodes[i].x >= minXCoord) &&
          (nodes[i].x <= maxXCoord) &&
          (nodes[i].y >= minYCoord) &&
          (nodes[i].y <= maxYCoord))
          ||
          ((nodes[i].x + nodeSizeInCoordinate + delta >= minXCoord) &&
            (nodes[i].x + nodeSizeInCoordinate + delta <= maxXCoord) &&
            (nodes[i].y >= minYCoord) &&
            (nodes[i].y <= maxYCoord))
          ||
          ((nodes[i].x >= minXCoord) &&
            (nodes[i].x <= maxXCoord) &&
            (nodes[i].y + nodeSizeInCoordinate + delta >= minYCoord) &&
            (nodes[i].y + nodeSizeInCoordinate + delta <= maxYCoord))
          ||
          ((nodes[i].x + nodeSizeInCoordinate + delta >= minXCoord) &&
            (nodes[i].x + nodeSizeInCoordinate + delta <= maxXCoord) &&
            (nodes[i].y + nodeSizeInCoordinate + delta >= minYCoord) &&
            (nodes[i].y + nodeSizeInCoordinate + delta <= maxYCoord))) {
          foundNodes[i] = nodes[i];
        }
      }
      return foundNodes;
    };

    this.updateNodeView = function(node, cmScale) {
      // update bounds
      node.element.style.top = cmScale.coordinateToPx(node.y) + "px";
      node.element.style.left = cmScale.coordinateToPx(node.x) + "px";
      node.element.style.width = cmScale.getScaledNodeSize() + "px";
      node.element.style.height = cmScale.getScaledNodeSize() + "px";
      if (node.filled) {
        // if the node is filled - change the icon to active one
        this.setActiveImage(node);
      } else {
        this.setNotActiveImage(node);
      }
      self.updateNodeAdditionalViewElements(node, cmScale);

    };

    this.setActiveImage = function(node) {
      node.elementJQ.find(".node-image").attr("src", campaignManagerSettings.imagePath + nodeFactory.getImageForNode(node));
    };

    this.setNotActiveImage = function(node) {
      node.elementJQ.find(".node-image").attr("src", campaignManagerSettings.imagePath + nodeFactory.getEmptyImageForNode(node));
    };

    this.updateNodeAdditionalViewElements = function(node, cmScale) {
      // if the node is not filled - don't display any additional data
      if (!node.filled
        && node.type != nodeFactory.NODE_TYPE_MAILING
        && node.type != nodeFactory.NODE_TYPE_ACTION_BASED_MAILING
        && node.type != nodeFactory.NODE_TYPE_DATE_BASED_MAILING
        && node.type != nodeFactory.NODE_TYPE_FOLLOWUP_MAILING
        && (node.type != nodeFactory.NODE_TYPE_IMPORT || (node.type == nodeFactory.NODE_TYPE_IMPORT && node.data.importexportId == 0))
        && (node.type != nodeFactory.NODE_TYPE_EXPORT || (node.type == nodeFactory.NODE_TYPE_EXPORT && node.data.importexportId == 0)
        )
      ) {
        return;
      }

      // calculate the font-size and text position according to current scale
      var fontSize = cmScale.getCurrentScale() * 90;

      var position = self.getIconExtraInfoPosition(node, cmScale);
      var textTop = position.top;
      var textLeft = position.left;

      // remove old additional view elements
      node.elementJQ.find(".icon-extra-info").each(function() {
        $(this).remove();
      });

      _.each(node.elementJQ.find('.icon-footnote-number'), function(elem) {
        var $elem = $(elem);
        var currentFontSize = cmScale.getCurrentScale() * 110;
        var padding = cmScale.getCurrentScale() * 5.2;
        $elem.css("font-size", currentFontSize + "%");
        $elem.css("padding", "0 " + padding + "px");
      });

      // start/stop icon
      if (node.type == nodeFactory.NODE_TYPE_START || node.type == nodeFactory.NODE_TYPE_STOP) {
        var startStopDateStr = DateTimeUtils.getDateTimeStr(node.data.date, node.data.hour, node.data.minute, this.localeDateNTimePattern);

        // add start date
        if (node.type == nodeFactory.NODE_TYPE_START && node.data.startType == constants.startTypeDate) {
          var dateMessage = t('workflow.start.start_date');
          node.elementJQ.append($("<div class='icon-extra-info'>" + dateMessage + ":<br>" + startStopDateStr + "</div>"));
        } else if (node.type == nodeFactory.NODE_TYPE_STOP && node.data.endType == constants.endTypeDate) {
          dateMessage = t('workflow.stop.end_date');
          node.elementJQ.append($("<div class='icon-extra-info'>" + dateMessage + ":<br>" + startStopDateStr + "</div>"));
        }
        // add start event and start-reaction icon
        else if (node.data.startType == constants.startTypeEvent && node.data.event == constants.startEventReaction) {
          var eventMessage = (node.type == nodeFactory.NODE_TYPE_START) ? t('workflow.start.start_event') : t('workflow.stop.end_event');
          var reactionName = nodeFactory.getReactionName(node.data.reaction);
          node.elementJQ.append($("<div class='icon-extra-info'>" + eventMessage + ":<br>" + reactionName + "<br>(" + startStopDateStr + ")</div>"));

          var reactionImage = nodeFactory.getReactionImage(node.data.reaction);
          node.elementJQ.append($("<img class='icon-extra-info' title='" + reactionName + "'/>"));
          node.elementJQ.find("img.icon-extra-info").attr("src", campaignManagerSettings.imagePath + reactionImage);
          var imageScale = 60;
          node.elementJQ.find("img.icon-extra-info").css("height", "" + imageScale + "%");
          node.elementJQ.find("img.icon-extra-info").css("width", "" + imageScale + "%");

          //find position
          var position = getReactionIconPosition(node.usedAnchors, cmScale, imageScale);
          node.elementJQ.find("img.icon-extra-info").css("top", position.top + "px");
          node.elementJQ.find("img.icon-extra-info").css("left", position.left + "px");
        }
        // add date event description
        else if (node.data.startType == constants.startTypeEvent && node.data.event == constants.startEventDate) {
          var dateEvent = node.data.dateProfileField + " " + constants.operatorsMap[node.data.dateFieldOperator] +
            " " + (node.data.dateFieldValue == null ? "" : node.data.dateFieldValue);
          var dateEventMessage = t('workflow.start.start_event');
          node.elementJQ.append($("<div class='icon-extra-info'>" + dateEventMessage + ":<br>" + dateEvent + "<br>(" + startStopDateStr + ")</div>"));
        }
        // open end
        else if (node.data.endType == constants.endTypeAutomatic) {
          var stopTitle = campaignManager.isNormalCampaignActionsType() ? t('workflow.stop.automatic_end') : t('workflow.stop.open_end');
          node.elementJQ.append($("<div class='icon-extra-info'>" + stopTitle + "</div>"));
        }
      }

      // deadline
      else if (node.type === nodeFactory.NODE_TYPE_DEADLINE) {
        // add deadline date
        if (node.data.deadlineType === constants.deadlineTypeFixedDeadline) {
          var decisionDateStr = DateTimeUtils.getDateTimeStr(node.data.date, node.data.hour, node.data.minute, this.localeDateNTimePattern);;
          node.elementJQ.append($("<div class='icon-extra-info'>" + t('workflow.deadline.title') + ":<br>" + decisionDateStr + "</div>"));
        }
        // add delay
        else if (node.data.deadlineType === constants.deadlineTypeDelay) {
          var message = t('workflow.defaults.delay') + ": <br>" + node.data.delayValue + " ";
            if (node.data.delayValue > 1) {
                switch (node.data.timeUnit) {
                    case constants.deadlineTimeUnitMinute:
                        message += t('workflow.deadline.minutes');
                        break;
                    case constants.deadlineTimeUnitHour:
                        message += t('workflow.defaults.hours');
                        break;
                    case constants.deadlineTimeUnitDay:
                        message += t('workflow.defaults.days');
                        break;
                    case constants.deadlineTimeUnitWeek:
                        message += t('workflow.defaults.weeks');
                        break;
                    case constants.deadlineTimeUnitMonth:
                        message += t('workflow.defaults.months');
                        break;
                }
            } else {
                switch (node.data.timeUnit) {
                    case constants.deadlineTimeUnitMinute:
                        message += t('workflow.deadline.minute');
                        break;
                    case constants.deadlineTimeUnitHour:
                        message += t('workflow.defaults.hour');
                        break;
                    case constants.deadlineTimeUnitDay:
                        message += t('workflow.defaults.day');
                        break;
                    case constants.deadlineTimeUnitWeek:
                        message += t('workflow.defaults.week');
                        break;
                    case constants.deadlineTimeUnitMonth:
                        message += t('workflow.defaults.month');
                        break;
                }
            }
          node.elementJQ.append($("<div class='icon-extra-info'>" + message + "</div>"));
        }
      }

      // parameter
      else if (node.type == nodeFactory.NODE_TYPE_PARAMETER) {
        node.elementJQ.append($("<div class='icon-extra-info icon-extra-info-center'>" + node.data.value + "</div>"));
        textTop = cmScale.getScaledNodeSize() * 0.36;
        node.elementJQ.find("div.icon-extra-info-center").css("top", textTop + "px");
        node.elementJQ.find("div.icon-extra-info-center").css("left", "0px");

        if (node.statisticsList != undefined) {
          node.elementJQ.append($("<div class='icon-extra-info stat-value'><span class='node-stats'>" + node.statisticsList[0] + "</span></div>"));
          node.elementJQ.find("div.stat-value").css("top", position.top + "px");
          node.elementJQ.find("div.stat-value").css("left", position.left + "px");
        }
      }

      // decision
      else if (node.type == nodeFactory.NODE_TYPE_DECISION) {
        var textValue = "";
        if (node.data.decisionType == constants.decisionTypeAutoOptimization) {
          textValue = t('workflow.mailing.autooptimization') + ", ";
          switch (node.data.aoDecisionCriteria) {
              case constants.decisionAOCriteriaClickRate:
                textValue += t('workflow.defaults.ckickrate');
                break;
              case constants.decisionAOCriteriaOpenrate:
                textValue += t('workflow.opening_rate');
                break;
              default:
                textValue += t('workflow.statistic.revenue');
          }
          if (node.data.threshold && node.data.threshold != "") {
            textValue += ": " + node.data.threshold;
          }

          if (!!node.data.decisionDate) {
            var dateStr = DateTimeUtils.getDateTimeStr(node.data.decisionDate, null, null, this.localeDateNTimePattern);;
            textValue += ", " + t('workflow.defaults.date') + ": " + dateStr;
          }
        }
        else {
          textValue = t('workflow.decision') + ", ";
          if (node.data.decisionCriteria == constants.decisionReaction) {
            var reactionName = nodeFactory.getReactionName(node.data.reaction);
            textValue += t('workflow.reaction.title') + ": " + reactionName + ", " +
              t('workflow.defaults.mailing') + ": " + allUsedEntity.allMailings[node.data.mailingId];
          }
          else {
            var parenthesisOpenedValues = {0: "", 1: "("};
            var parenthesisClosedValues = {0: "", 1: ")"};
            var ruleString = "";
            for(var i = 0, n = node.data.rules.length; i < n; i++) {
              if (ruleString.length != 0) {
                ruleString += " " + constants.chainOperatorOptions[node.data.rules[i].chainOperator] + " ";
              }
              ruleString += parenthesisOpenedValues[node.data.rules[i].parenthesisOpened];
              ruleString += node.data.profileField + " ";
              ruleString += constants.operatorsMap[node.data.rules[i].primaryOperator] + " ";
              if (node.data.profileField == "GENDER") {
                ruleString += constants.genderOptions[node.data.rules[i].primaryValue];
              } else {
                ruleString += node.data.rules[i].primaryValue;
              }
              ruleString += parenthesisClosedValues[node.data.rules[i].parenthesisClosed];
            }
            textValue += t('workflow.start.rule') + ": " + ruleString;
          }
        }
        node.elementJQ.append($("<div class='icon-extra-info'>" + textValue + "</div>"));
      }

      // recipient
      else if (node.type == nodeFactory.NODE_TYPE_RECIPIENT) {
        var textToShow = t('workflow.mailinglist.short') + ": " + allUsedEntity.allMailinglists[node.data.mailinglistId];
        if (node.data.targets && node.data.targets.length > 0) {
          textToShow += "<br>" + t('workflow.target.short') + ": ";
          for(i = 0; i < node.data.targets.length; i++) {
            textToShow += allUsedEntity.allTargets[node.data.targets[i]];
            if (i == self.MAX_NUMBER_OF_TARGETS_FOR_DISPLAY - 1 && node.data.targets.length > self.MAX_NUMBER_OF_TARGETS_FOR_DISPLAY) {
              textToShow += ", ...";
              break;
            } else if (i < node.data.targets.length - 1) {
              switch (String(node.data.targetsOption)) {
                case 'ALL_TARGETS_REQUIRED':
                  textToShow += " ∩ ";
                  break;
                case 'NOT_IN_TARGETS':
                  textToShow += " ≠ ";
                  break;
                case 'ONE_TARGET_REQUIRED':
                  textToShow += " ∪ ";
                  break;
                default:
                  textToShow += ", ";
              }
            }
          }
        }
        node.elementJQ.append($("<div class='icon-extra-info'>" + textToShow + "</div>"));

        //fix label top position if it is situated above the icon
        if (textLeft == 0 && textTop < 1) {
          node.elementJQ.find("div.icon-extra-info").css("font-size", fontSize + "%");
          var newTextTop = -1 * node.elementJQ.find("div.icon-extra-info").height();
          if (textTop > newTextTop) {
            textTop = newTextTop;
          }
        }
      }

      // report
      else if (node.type == nodeFactory.NODE_TYPE_REPORT) {
        var reportsStr = "";
        for(var i = 0; i < node.data.reports.length; i++) {
          reportsStr += allUsedEntity.allReports[node.data.reports[i]];
          if (i == self.MAX_NUMBER_OF_REPORTS_FOR_DISPLAY - 1 && node.data.reports.length > self.MAX_NUMBER_OF_REPORTS_FOR_DISPLAY) {
            reportsStr += ", ...";
            break;
          } else if (i < node.data.reports.length - 1) {
            reportsStr += ",<br>";
          }
        }
        node.elementJQ.append($("<div class='icon-extra-info'>" + t('workflow.defaults.report') + ":<br>" + reportsStr + "</div>"));
      }

      else if (node.type == nodeFactory.NODE_TYPE_ARCHIVE) {
        node.elementJQ.append($("<div class='icon-extra-info'>" + allUsedEntity.allCampaigns[node.data.campaignId] + "</div>"));
      }

      //forms
      else if (node.type == nodeFactory.NODE_TYPE_FORM) {
        node.elementJQ.append($("<div class='icon-extra-info'>" + allUsedEntity.allUserForms[node.data.userFormId] + "</div>"));
      }

      else if (node.type == nodeFactory.NODE_TYPE_MAILING ||
        node.type == nodeFactory.NODE_TYPE_ACTION_BASED_MAILING ||
        node.type == nodeFactory.NODE_TYPE_DATE_BASED_MAILING) {
        var textToShow = "";
        if (node.iconTitle) {
          textToShow = node.iconTitle;
        } else if (node.filled && node.data.mailingId > 0) {
          textToShow = allUsedEntity.allMailings[node.data.mailingId];
        }
        node.elementJQ.append($("<div class='icon-extra-info'>" + textToShow + "</div>"));
      }

      else if (node.type == nodeFactory.NODE_TYPE_FOLLOWUP_MAILING) {
        var textToShow = "";
        var textToShowDetailed = "";
        var baseTitle, followupTitle;

        if (node.iconTitle) {
          var mailingNames = node.iconTitle.split(":/");

          if (mailingNames.length) {
            if (mailingNames.length > 1) {
              baseTitle = mailingNames[0];
              followupTitle = mailingNames[1];
            } else {
              followupTitle = mailingNames[0];
            }
          }
        } else if (node.filled && node.data.mailingId > 0) {
          baseTitle = allUsedEntity.allMailings[node.data.baseMailingId] || '?';
          followupTitle = allUsedEntity.allMailings[node.data.mailingId] || '?';
        }

        if (followupTitle) {
          if (baseTitle) {
            var cutBaseTitle = baseTitle;
            var cutFollowupTitle = followupTitle;

            if (cutBaseTitle.length > self.TITLE_ELLIPSIS_LENGTH) {
              cutBaseTitle = cutBaseTitle.substring(0, self.TITLE_ELLIPSIS_LENGTH) + "...";
            }
            if (cutFollowupTitle.length > self.TITLE_ELLIPSIS_LENGTH) {
              cutFollowupTitle = cutFollowupTitle.substring(0, self.TITLE_ELLIPSIS_LENGTH) + "...";
            }

            var decisionCriterionText = getDecisionCriterionDescription(node.data.decisionCriterion);
            textToShow = cutBaseTitle + " (" + decisionCriterionText + ") " + cutFollowupTitle;
            textToShowDetailed = baseTitle + " (" + decisionCriterionText + ") " + followupTitle;
          } else {
            textToShow = followupTitle;
          }
        }

        node.elementJQ.append($("<div class='icon-extra-info'>" + textToShow + "</div>"));

        if (textToShowDetailed) {
          node.elementJQ.on({
            mouseover: function() {
              node.elementJQ.find(".icon-extra-info").html(textToShowDetailed);
            },
            mouseleave: function() {
              node.elementJQ.find(".icon-extra-info").html(textToShow);
            }
          });
        }
      }

      //import
      else if (node.type == nodeFactory.NODE_TYPE_IMPORT) {
        node.elementJQ.append($("<div class='icon-extra-info'>" + allUsedEntity.allAutoImports[node.data.importexportId] + "</div>"));
      }

      //export
      else if (node.type == nodeFactory.NODE_TYPE_EXPORT) {
        node.elementJQ.append($("<div class='icon-extra-info'>" + allUsedEntity.allAutoExports[node.data.importexportId] + "</div>"));
      }

      var extraInfo = node.elementJQ.find("div.icon-extra-info");
      extraInfo.css("font-size", fontSize + "%");
      if (node.type != nodeFactory.NODE_TYPE_PARAMETER) {
        extraInfo.css("left", textLeft + "px");
        if (textTop < 0 && textLeft == 0) {
          extraInfo.css("top", -extraInfo.height() + "px");
        }
        else {
          extraInfo.css("top", textTop + "px");
        }
      }

      // shorten the width of label in necessary
      if (node.type != nodeFactory.NODE_TYPE_PARAMETER) {
        var labelDiv = node.elementJQ.find("div.icon-extra-info");
        var originalContent = labelDiv.html();
        var shortened = shortenLabelWidth(labelDiv, cmScale.getScaledNodeSize());
      }

      // if the label width was shortened - we need to create a popup
      // with full version of label to show it by mouseover
      if (shortened) {
        labelDiv.mouseover(function() {
          $("#icon-label-popup-holder").append($("<div id='node-label-fulltext-popup'>" + originalContent + "</div>"));
          var popup = $("#node-label-fulltext-popup");
          popup.offset({
            top: labelDiv.offset().top,
            left: labelDiv.offset().left + cmScale.getScaledNodeSize()
          });
        });
        labelDiv.mouseout(function() {
          $("#node-label-fulltext-popup").remove();
        });
      }

      if (!!node.statisticsList && node.type != nodeFactory.NODE_TYPE_PARAMETER && node.type != nodeFactory.NODE_TYPE_DECISION) {
        for(i = 0; i < node.statisticsList.length; i++) {
          var extraInfo = node.elementJQ.find("div.icon-extra-info");
          extraInfo.append($("<br><span class='node-stats'>" + node.statisticsList[i] + "</span>"));
          if (textTop < 0 && textLeft == 0) {
            extraInfo.css("top", -extraInfo.height() + "px");
          }
        }
      }

      campaignManager.updateLabelConnection(node);
    };

    function getDecisionCriterionDescription(criterion) {
      switch (String(criterion)) {
        case self.REACTION_OPENED:
          return t('workflow.reaction.opened');
        case self.REACTION_NOT_OPENED:
          return t('workflow.reaction.not_opened');
        case self.REACTION_CLICKED:
          return t('workflow.reaction.clicked');
        case self.REACTION_NOT_CLICKED:
          return t('workflow.reaction.not_clicked');
        case self.REACTION_BOUGHT:
          return t('workflow.reaction.bought');
        case self.REACTION_NOT_BOUGHT:
          return t('workflow.reaction.NotBought');
      }

      return '';
    }

    function shortenLabelWidth(textDiv, maxWidth) {
      var shortened = false;

      // calculate the width of icon label
      // if the label is wider than icon - perform label shortening
      if (getLabelFullWidth(textDiv) > maxWidth) {
        var current = textDiv;
        var html = current.html();

        // split the text by lines forced by BR tag
        var forcedLines = html.split("<br>");
        for(var lineIndex = 0; lineIndex < forcedLines.length; lineIndex++) {
          current.text("");
          var height = current.height();
          var currentLine = "";
          var currentContent = "";

          // split each forced line by words
          var words = forcedLines[lineIndex].split(" ");
          for(var wordIndex = 0; wordIndex < words.length; wordIndex++) {
            if (wordIndex > 0) {
              current.text(current.text() + " ");
            }
            // add words to div (word-by-word)
            current.text(current.text() + words[wordIndex]);
            if (currentLine != "") {
              currentLine = currentLine + " ";
            }
            currentLine = currentLine + words[wordIndex];

            // if the height became bigger that means that new line appeared with adding of current word
            if (current.height() > height) {
              height = current.height();
              // if the current line is wider than icon - shorten the current line
              if (getLabelFullWidth(current) > maxWidth) {
                shortened = true;
                currentLine = getTextToFit(current, currentLine, maxWidth);
              }
              if (currentContent != "") {
                currentContent = currentContent + " ";
              }
              current.text(currentContent + currentLine);
              currentContent = current.text();
              currentLine = "";
            }
          }
          // put the shortened text back to lines array
          forcedLines[lineIndex] = current.text();
        }

        // clear the content of div and generate it again from array of shortened lines
        current.html("");
        for(lineIndex = 0; lineIndex < forcedLines.length; lineIndex++) {
          current.html(current.html() + forcedLines[lineIndex]);
          if (lineIndex < forcedLines.length - 1) {
            current.html(current.html() + "<br>");
          }
        }
      }
      return shortened;
    }

    // get the text with maximum number of symbols that can fit into div (with "..." at the end)
    var getTextToFit = function(div, textFull, maxSize) {
      var savedText = div.text();
      for(var i = self.TITLE_ELLIPSIS_LENGTH; i > 0; i--) {
        var tryText = textFull.substr(0, i) + "...";
        div.text(tryText);
        if (getLabelFullWidth(div) <= maxSize) {
          div.text(savedText);
          return tryText;
        }
      }
      div.text(savedText);
      return "";
    };

    // calculates the width of label (including the text that goes with overflow)
    var getLabelFullWidth = function(div) {
      var htmlContent = div.html();
      div.html("<span class='span-bounder'>" + htmlContent + "</span>");
      var labelWidth = div.find(".span-bounder").width();
      div.html(htmlContent);
      return labelWidth;
    };

    this.resetUsedAnchors = function() {
      for(var i in nodes) {
        nodes[i].usedAnchors = [];
      }

      return nodes;
    };

    this.getNodesByTypeList = function(nodeTypes) {
      var foundNodes = [];
      for(var i in nodes) {
        if ($.inArray(nodes[i].type, nodeTypes) != -1) {
          foundNodes.push(nodes[i]);
        }
      }
      return foundNodes;
    };


    this.getNodesByType = function(nodeType) {
      return self.getNodesByTypeList([nodeType]);
    };

    this.openNodeIcon = function() {
      if (self.openNodeId != null) {
        if (self.openNodeId != 0) {
          var openNodeId = campaignManager.getCMNodes().getNodeIdPrefix() + self.openNodeId;
          var openNode = campaignManager.getCampaignManagerNodes().getNodeById(openNodeId);
          if (openNode != null && self.openNodeValue != null) {
            if ((openNode.type == nodeFactory.NODE_TYPE_MAILING) || (openNode.type == nodeFactory.NODE_TYPE_ACTION_BASED_MAILING)
              || (openNode.type == nodeFactory.NODE_TYPE_DATE_BASED_MAILING) || (openNode.type == nodeFactory.NODE_TYPE_FOLLOWUP_MAILING)) {
              openNode.data.mailingId = self.openNodeValue;
            } else if ((openNode.type == nodeFactory.NODE_TYPE_RECIPIENT) && (self.openNodeValue != "0") && ($.inArray(self.openNodeValue, openNode.data.targets) == -1)) {
              openNode.data.targets.push(self.openNodeValue);
            } else if (openNode.type == nodeFactory.NODE_TYPE_ARCHIVE) {
              openNode.data.campaignId = self.openNodeValue;
            } else if (openNode.type == nodeFactory.NODE_TYPE_FORM) {
              openNode.data.userFormId = self.openNodeValue;
            } else if (openNode.type == nodeFactory.NODE_TYPE_REPORT && self.openNodeValue != "0" && $.inArray(self.openNodeValue, openNode.data.reports) == -1) {
              openNode.data.reports.push(self.openNodeValue);
            } else if (openNode.type == nodeFactory.NODE_TYPE_IMPORT || openNode.type == nodeFactory.NODE_TYPE_EXPORT) {
              openNode.data.importexportId = self.openNodeValue;
            }
          }
          var openNodeElement = $("#" + openNodeId);
          if (openNodeElement.length > 0) {
            self.openNodeId = null;
            self.openNodeValue = null;
            openNodeElement.dblclick();
          }
        }
      }
    };

    this.setOpenNode = function(openNodeId, openNodeValue) {
      this.openNodeId = openNodeId;
      this.openNodeValue = openNodeValue;
    }
  };

  AGN.Lib.WM.CampaignManagerNodes = CampaignManagerNodes;
})(jQuery);
