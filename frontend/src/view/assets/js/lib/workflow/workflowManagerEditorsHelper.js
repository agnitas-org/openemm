(function($) {
  var EditorsHelper = function(campaignManager, nodeFactory, campaignManagerSettings) {

    this.editors = [];
    this.curEditor;
    this.curEditingNode;

    this.showEditDialog = function(node, isActivatedWorkflow) {
      if (node.type == "start" && !node.filled && campaignManager.nodeHasIncomingConnections(node)) {
        node.type = "stop";
      }
      if (node.type == "stop" && !node.filled && !campaignManager.nodeHasIncomingConnections(node)) {
        node.type = "start";
      }
      this.curEditingNode = node;
      this.curEditor = this.editors[node.type];
      this.curEditor.fillEditor(node);
      var self = this;

      this.getEditorPanel(node.type).dialog({
        open: function(event, ui) {
          var title = self.getEditorPanel(node.type).parent().find('.ui-dialog-title');
          title.empty();
          title.append($('<span class="dialog-title-image">' + self.curEditor.getTitle() + '</span>'));
          title.find(".dialog-title-image").css("background-image", "url(' " + campaignManagerSettings.imagePath + nodeFactory.getSmallImageForNode(node) + "')");

          if (node.type == nodeFactory.NODE_TYPE_RECIPIENT) {
            //only one mailinglist allowed per campaign. That's why we disable editing for the second and others recipient icons.
            if (node.isDependent == true) {
              self.getEditorPanel(node.type).parent().find(".recipient-editor-select").prop("disabled", true);
            } else {
              self.getEditorPanel(node.type).parent().find(".recipient-editor-select").prop("disabled", false);
            }
          }

          //reinit datepickers
          jQuery(event.target).find('.js-datepicker').each(function(index, element) {
            var $datepicker = jQuery(element).pickadate('picker');
            $datepicker.set('select', $datepicker.get('select'));
          });

          if (isActivatedWorkflow) {
            disableDialogItems(event);
          }

          // //reinit timepickers
          // jQuery(event.target).find('.js-timepicker').each(function (index, element) {
          //     var $timepicker = jQuery(element).pickatime('picker');
          //     $timepicker.set('select', $timepicker.get('select'));
          // });
        },
        close: function(event, ui) {
          if (self.curEditor.closeEditor != undefined) {
            self.curEditor.closeEditor();
          }
          ;
        },
        width: 650,
        height: "auto",
        minHeight: "auto",
        modal: true,
        resizable: false
      });
    };

    this.showIconCommentDialog = function(node) {
        this.curEditingNode = node;
        this.curEditor = this.editors["icon-comment"];
        this.curEditor.fillEditor(node);
        var self = this;
        this.getEditorPanel("icon-comment").dialog({
            open: function(event, ui) {
                self.getEditorPanel("icon-comment").parent().find('.ui-dialog-titlebar').hide();
            },
            width: 300,
            height: "auto",
            minHeight: "auto",
            modal: true,
            resizable: false,
            fluid: true,
            position: { my: "left top", at: "left bottom", of: node.element }
        });
    };

    this.saveIconComment = function(iconComment) {
        var currentNode = this.curEditingNode;
        var editorData = {
            iconComment: iconComment
        };
        jQuery.extend(currentNode.data, editorData);
        campaignManager.getCommentControls().addCommentEllipsis(currentNode);
    };

    this.saveCurrentEditor = function(leaveOpen, mailingEditorBase) {
      var self = this;
      var editorData = this.curEditor.saveEditor();
      // check to close editor dialog
      if (!leaveOpen) {
        this.cancelEditor();
      }

      if (this.isNodeIsMailing(this.curEditingNode)) {
        var mailingId = editorData.mailingId;
        if (mailingEditorBase) {
          mailingEditorBase.checkDifferentMailingLists(mailingId, function(mailingContent) {
            self.mailingSpecificSave(leaveOpen, mailingContent, editorData, mailingEditorBase);
          }, function(mailingContent) {
            self.curEditor.storedMailingContent = mailingContent;
            self.curEditor.storedMailingId = mailingId;
            self.curEditor.storedLeaveOpen = leaveOpen;
            self.curEditor.storedEditorData = editorData;
            mailingEditorBase.initOneMailinglistWarningDialog(mailingEditorBase);
          });
        }
      } else {
        if ((this.curEditingNode.type != "ownWorkflow") && (this.curEditingNode.type != "scBirthday")
          && (this.curEditingNode.type != "scDOI" && (this.curEditingNode.type != "scABTest"))) {
          campaignManager.saveSnapshot();
        }

        jQuery.extend(this.curEditingNode.data, editorData);

        if (!this.curEditingNode.filled && !this.isNodeIsMailing(this.curEditingNode)) {
          this.curEditingNode.filled = true;
        }

        if (this.curEditingNode.filled
          && typeof(this.curEditor.isSetFilledAllowed) == "function" && !this.curEditor.isSetFilledAllowed()
        ) {
          this.curEditingNode.filled = false;
        }

        campaignManager.updateNode(this.curEditingNode);
        campaignManager.updateParameterValueAfterDecision();
        campaignManager.callWorkflowManagerStateChangedCallback();
      }
    };

    this.isNodeIsMailing = function(node) {
      return node.type == "mailing" || node.type == "actionbased_mailing"
        || node.type == "datebased_mailing" || node.type == "followup_mailing"
    };

    /**
     * Save changes in node with type mailing or actionbased_mailing or datebased_mailing or followup_mailing
     * @param leaveOpen
     * @param mailingContent
     * @param editorData
     * @param mailingEditorBase
     */
    this.mailingSpecificSave = function(leaveOpen, mailingContent, editorData, mailingEditorBase) {
      campaignManager.saveSnapshot();
      jQuery.extend(this.curEditingNode.data, editorData);
      this.curEditingNode.data.iconTitle = this.curEditingNode.iconTitle;
      if (this.curEditingNode.filled
        && typeof(this.curEditor.isSetFilledAllowed) == "function" && !this.curEditor.isSetFilledAllowed()) {
        this.curEditingNode.filled = false;
      }
      campaignManager.updateNode(this.curEditingNode);

      if (this.isNodeIsMailing(this.curEditingNode)) {
        mailingEditorBase.trySupplementNodes(mailingContent);
      }

      campaignManager.updateParameterValueAfterDecision();
      campaignManager.callWorkflowManagerStateChangedCallback();
    };

    /**
     * Restore data that was stored before showing modal and save changes of mailing.
     */
    this.mailingSpecificSaveAfterMailinglistCheckModal = function(mailingEditorBase) {
      this.curEditor.storedMailingContent.mailinglistId = mailingEditorBase.configuredMailingData.mailinglistId;
      this.mailingSpecificSave(this.curEditor.storedLeaveOpen, this.curEditor.storedMailingContent, this.curEditor.storedEditorData, mailingEditorBase);
    };

    this.cancelEditor = function() {
      if (this.getEditorPanel(this.curEditingNode.type).dialog("instance")) {
        this.getEditorPanel(this.curEditingNode.type).dialog("close");
      }
    };

    this.getEditorPanel = function(type) {
      var editorType = (type == "stop") ? "start" : type;
      return jQuery("#" + editorType + "-editor");
    };

    this.formToObject = function(formName) {
      var formData = jQuery('form[name="' + formName + '"]').serializeArray();
      var result = {};
      jQuery.each(formData, function() {
        if (this.name.indexOf("[") != -1) {
          var arrName = this.name.substring(0, this.name.indexOf("["));
          if (result[arrName] == undefined) {
            result[arrName] = [];
          }
          var index = this.name.substring(this.name.indexOf("[") + 1, this.name.indexOf("]"));
          if (result[arrName][index] == undefined) {
            result[arrName][index] = {};
          }
          if (this.name.indexOf(".") != -1) {
            var propName = this.name.substring(this.name.indexOf(".") + 1);
            result[arrName][index][propName] = this.value;
          }
          else {
            result[arrName][index] = this.value;
          }
        }
        else {
          result[this.name] = this.value || '';
        }
      });
      // handle unselected checkboxes
      jQuery('form[name="' + formName + '"] [type="checkbox"]').each(function() {
        if (!jQuery(this).prop("checked")) {
          result[this.name] = false;
        }
      });
      return result;
    };

    this.fillFormFromObject = function(formName, data, namePrefix, defaultData) {
      if (typeof data === 'string') {
        this.fillFormFromObjectItem(formName, namePrefix, namePrefix, data || defaultData);
      } else {
        for(var nameKey in data) {
          var value = data[nameKey];
          if(!value && defaultData && defaultData[nameKey]) {
            value = defaultData[nameKey];
          }
          this.fillFormFromObjectItem(formName, namePrefix, nameKey, value);
        }
      }
    };

    this.fillFormFromObjectItem = function(formName, namePrefix, name, val) {
      if (Object.prototype.toString.call(val) === "[object Array]") {
        for(var i = 0; i < val.length; i++) {
          this.fillFormFromObject(formName, val[i], name + "[" + i + "]" + ".");
        }
      }
      else {
        var $el = jQuery('form[name="' + formName + '"] [name="' + namePrefix + name + '"]');
        var type = $el.attr('type');
        switch (type) {
          case 'checkbox':
            $el.prop('checked', Boolean(val));
            break;
          case 'radio':
            $el.filter('[value="' + val + '"]').prop('checked', true);
            break;
          default:
            var tagName = $el.prop("tagName");
            switch (tagName) {
              //since we started using select2 we should perform such initialization
              case 'SELECT' :
                $el.val(val);
                this.initSelectWithValueOrChooseFirst($el, val);
                break;
              default:
                $el.val(val);
            }
        }
      }
    };

    this.processForward = function(forwardName, elementId, multiSelectors, submitFn, extraForwardParams) {
      $("#forwardName").val(forwardName);
      $("#forwardParams").val(
        "nodeId=" + campaignManager.extractIdFromConnectionEnd(this.curEditingNode.elementJQ.attr("id"))
        + ";elementId=" + encodeURIComponent(elementId) + (extraForwardParams ? ";" + extraForwardParams : "")
      );

      if (this.curEditor.safeToSave) {
        this.saveCurrentEditor();
      }

      submitFn(false);
    };

    this.getFirstOptionValue = function($el) {
      return AGN.Lib.Select.get($el).getFirstValue();
    };

    this.resetSelect = function($el) {
      AGN.Lib.Select.get($el).clear();
    };

    this.initSelectWithFirstValue = function($el) {
      AGN.Lib.Select.get($el).selectFirstValue();
    };

    this.initSelectWithValueOrChooseFirst = function($el, selectedValue) {
      AGN.Lib.Select.get($el).selectValueOrSelectFirst(selectedValue)
    };

    this.deleteNode = function(nodeId) {
      campaignManager.deleteNode(campaignManager.getCMNodes().getNodeIdPrefix() + nodeId, false, false);
    };

    this.openNode = function(node, needCheckActivation) {
      campaignManager.editNode(node, needCheckActivation);
    };

  };

  function disableDialogItems(event) {
    var $target = $(event.target);

    $target.find(".disable-for-active").each(function(index, element) {
      $(element).attr('disabled', true);
    });

    $target.find(".hide-for-active").each(function(index, element) {
      $(element).hide();
    });

    $target.find(":input:enabled[type!='hidden']").not(':button, .select2, .js-select, .js-datepicker').each(function(index, element) {
      if ($(element).prop('type') == 'radio' || $(element).prop('type') == 'checkbox') {
        $(element).attr('disabled', true);
      } else {
        $(element).attr('readonly', true);
      }
    });

    $target.find(':input:enabled.select2, :input:enabled.js-select, .select2-container').not(".disabled, :disabled").each(function(index, element) {
      if ($(element).prop('type') == 'text' || $(element).is('input, select')) {
        $(element).attr('disabled', true);
      } else {
        $(element).select2('disable');
      }
    });

    $target.find('.js-datepicker').each(function(index, element) {
      $(element).attr('disabled', true);
    });
  }

  AGN.Lib.WM.EditorsHelper = EditorsHelper;
})(jQuery);
