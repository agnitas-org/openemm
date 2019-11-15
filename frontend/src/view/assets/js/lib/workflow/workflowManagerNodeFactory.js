(function() {

  var DateTimeUtils = AGN.Lib.WM.DateTimeUtils,
    NodeFactory = {

    NODE_TYPE_START: "start",
    NODE_TYPE_STOP: "stop",
    NODE_TYPE_DECISION: "decision",
    NODE_TYPE_DEADLINE: "deadline",
    NODE_TYPE_PARAMETER: "parameter",
    NODE_TYPE_REPORT: "report",
    NODE_TYPE_RECIPIENT: "recipient",
    NODE_TYPE_ARCHIVE: "archive",
    NODE_TYPE_FORM: "form",
    NODE_TYPE_MAILING: "mailing",
    NODE_TYPE_OWN_WORKFLOW: "ownWorkflow",
    NODE_TYPE_SC_BIRTHDAY: "scBirthday",
    NODE_TYPE_SC_DOI: "scDOI",
    NODE_TYPE_SC_ABTEST: "scABTest",
    NODE_TYPE_ACTION_BASED_MAILING: "actionbased_mailing",
    NODE_TYPE_DATE_BASED_MAILING: "datebased_mailing",
    NODE_TYPE_FOLLOWUP_MAILING: "followup_mailing",
    NODE_TYPE_IMPORT: "import",
    NODE_TYPE_EXPORT: "export",

    NODE_TYPE_START_ID: 0,
    NODE_TYPE_STOP_ID: 1,
    NODE_TYPE_DECISION_ID: 2,
    NODE_TYPE_DEADLINE_ID: 3,
    NODE_TYPE_PARAMETER_ID: 4,
    NODE_TYPE_REPORT_ID: 5,
    NODE_TYPE_RECIPIENT_ID: 6,
    NODE_TYPE_ARCHIVE_ID: 7,
    NODE_TYPE_FORM_ID: 8,
    NODE_TYPE_MAILING_ID: 9,
    NODE_TYPE_OWN_WORKFLOW_ID: 10,
    NODE_TYPE_SC_BIRTHDAY_ID: 11,
    NODE_TYPE_SC_DOI_ID: 12,
    NODE_TYPE_SC_ABTEST_ID: 13,
    NODE_TYPE_ACTION_BASED_MAILING_ID: 14,
    NODE_TYPE_DATE_BASED_MAILING_ID: 15,
    NODE_TYPE_FOLLOWUP_MAILING_ID: 16,
    NODE_TYPE_IMPORT_ID: 17,
    NODE_TYPE_EXPORT_ID: 18,

    reactionRegistry: [],

    createNode: function(type, x, y, element, id, campaignManager) {
      var node = {
        id: id,
        x: x,
        y: y,
        type: type,
        data: {},
        filled: false,
        element: element,
        elementJQ: jQuery(element),
        usedAnchors: [],
        isExpandable: false,
        iconTitle: "",
        editable: true
      };

      campaignManager.setCampaignActionsType(node);

      switch (node.type) {
        case "scABTest":
        case "scBirthday":
        case "scDOI":
          node.filled = true;
          node.isExpandable = true;
          node.data = {
            copyContent: true,
            created: false
          };
          break;
        case "ownWorkflow":
          node.isExpandable = true;
          node.data = {
            ownWorkflowId: 0,
            copyContent: true,
            created: false
          };
          break;
        case "start":
          node.data = {
            startType: null,
            date: null,
            hour: 0,
            minute: 0,
            sendReminder: false,
            remindAdminId: 0,
            remindAtOnce: false,
            scheduleReminder: false,
            remindSpecificDate: false,
            remindDate: new Date(),
            remindHour: 0,
            remindMinute: 0,
            event: null,
            reaction: null,
            mailingId: 0,
            profileField: "",
            useRules: false,
            rules: [],
            executeOnce: true,
            comment: "",
            recipients: "",
            adminTimezone: ""
          };
          break;
        case "stop":
          node.data = {
            endType: null,
            date: null,
            hour: 0,
            minute: 0,
            sendReminder: false,
            remindAdminId: 0,
            remindAtOnce: false,
            scheduleReminder: false,
            remindSpecificDate: false,
            remindDate: new Date(),
            remindHour: 0,
            remindMinute: 0,
            event: null,
            reaction: null,
            mailingId: 0,
            profileField: "",
            useRules: false,
            rules: [],
            executeOnce: true,
            comment: "",
            recipients: "",
            adminTimezone: ""
          };
          break;

        case "decision":
          node.data = {
            decisionType: null,
            decisionCriteria: null,
            reaction: null,
            mailingId: 0,
            linkId: 0,
            profileField: "",
            aoDecisionCriteria: null,
            threshold: "",
            decisionDate: new Date(),
            rules: [],
            includeVetoed: true
          };
          break;

        case "deadline":
          node.data = {
            deadlineType: null,
            date: new Date(),
            timeUnit: null,
            delayValue: 0,
            hour: 0,
            minute: 0,
            useTime: false//,
            //remindAdminId: 0,
            //sendReminder: false
          };
          break;

        case "parameter":
          node.data = {
            value: 0
          };
          break;

        case "report":
          node.data = {
            reports: []
          };
          break;

        case "recipient":
          //this attribute will be true if recipient icon is supplemented in that sequence "...->mailing->deadline->recipient->....->mailing"
          node.isDependent = false;
          node.data = {
            mailinglistId: 0,
            targets: [],
            targetsOption: null
          };
          break;
        case "archive":
          node.data = {
            campaignId: 0,
            archived: false
          };
          break;
        case "form":
          node.data = {
            userFormId: 0,
            formType: "form"
          };
          break;
        case "optin_form":
          node.data = {
            userFormId: 0,
            formType: "optin_form"
          };
          node.type = "form";
          break;
        case "fullview":
          node.data = {
            userFormId: 0,
            formType: "fullview"
          };
          node.type = "form";
          break;
        case "optout_form":
          node.data = {
            userFormId: 0,
            formType: "optout_form"
          };
          node.type = "form";
          break;
        case "mailing":
        	node.data = {
        	  mailingId: 0,
        	  skipEmptyBlocks: true,
        	  doubleCheck: true
        	};
        	break;
        case "actionbased_mailing":
        case "datebased_mailing":
          node.data = {
            mailingId: 0
          };
          break;
        case "followup_mailing":
          node.data = {
            baseMailingId: 0,
            mailingId: 0,
            decisionCriterion: "OPENED"
          };
          break;
        case "import":
        case "export":
          node.data = {
            importexportId: 0,
            errorTolerant: false
          };
          break;
      }
      return node;
    },

    createNodeFromData: function(nodeData, element, campaignManager) {
      var node = {
        id: nodeData.id,
        x: nodeData.x,
        y: nodeData.y,
        type: nodeData.type,
        data: {},
        filled: nodeData.filled,
        element: element,
        elementJQ: jQuery(element),
        isExpandable: false,
        usedAnchors: [],
        iconTitle: nodeData.iconTitle
      };

      campaignManager.setCampaignActionsType(node);

      var keys = Object.keys(nodeData);
      for(var i = 0; i < keys.length; i++) {
        var key = keys[i];
        if (key != 'x' && key != 'y' && key != 'type' && key != 'filled') {
          if (key == 'date' || key == 'remindDate' || key == 'decisionDate') {
            if (nodeData[key]) {
              if (nodeData[key].toString().indexOf('-') != -1) {
                var parts = nodeData[key].toString().split('-');
                node.data[key] = new Date(parts[0], parts[1] - 1, parts[2]);
              } else {
                node.data[key] = new Date(nodeData[key]);
              }
            }
          } else {
            node.data[key] = nodeData[key];
          }
        }
      }

      if (node.type == "ownWorkflow" || node.type == "scBirthday" || node.type == "scDOI" || node.type == "scABTest") {
        node.isExpandable = true;
      }
      return node;
    },

    prepareNodeForSubmission: function(clientNode) {
      var node = {
        x: clientNode.x,
        y: clientNode.y,
        type: clientNode.type,
        filled: clientNode.filled,
        iconTitle: clientNode.iconTitle
      };

      if (!!clientNode.data) {
        var keys = Object.keys(clientNode.data);
        for(var i = 0; i < keys.length; i++) {
          node[keys[i]] = clientNode.data[keys[i]];
        }
      }

      if (!!clientNode.value) {
        node.value = clientNode.value;
      }
      if (!!clientNode.mailinglistId) {
        node.mailinglistId = clientNode.mailinglistId;
      }

      node.date = DateTimeUtils.getDateTimeValue(node.date, node.hour, node.minute);
      node.remindDate = DateTimeUtils.getDateTimeValue(node.remindDate, node.remindHour, node.remindMinute);
      node.decisionDate = DateTimeUtils.getDateTimeValue(node.decisionDate);

      node['id'] = clientNode.id;
      return node;
    },

    getFixedType: function(type) {
      return (type == "stop") ? "start" : type;
    },

    getImage: function(type) {
      return "icon_" + this.getFixedType(type) + "_l.png";
    },

    getEmptyImage: function(type) {
      return "icon_" + this.getFixedType(type) + "_g.png";
    },

    getSmallImage: function(type) {
      return "icon_" + this.getFixedType(type) + "_s.png";
    },

    getImageForNode: function(node) {
      if (node.type == "form") {
        return this.getImage(node.data.formType);
      }
      return this.getImage(node.type);
    },

    getEmptyImageForNode: function(node) {
      if (node.type == "form") {
        return this.getEmptyImage(node.data.formType);
      }
      return this.getEmptyImage(node.type);
    },

    getSmallImageForNode: function(node) {
      if (node.type == "form") {
        return this.getSmallImage(node.data.formType);
      }
      return this.getSmallImage(node.type);
    },

    getReactionImage: function(reaction) {
      return this.reactionRegistry[reaction].image;
    },

    getReactionName: function(reaction) {
      return this.reactionRegistry[reaction].name;
    }
  };

  AGN.Lib.WM.NodeFactory = NodeFactory;
})();