(() => {
  const Def = AGN.Lib.WM.Definitions;
  const Helpers = AGN.Lib.Helpers;
  const DateFormat = AGN.Lib.DateFormat;

  AGN.Lib.WM.Utils = {
    checkActivation: function (withWarn) {
      const currentWorkflowStatus = $('#workflow-status').val();

      if (Def.constants.initialWorkflowStatus === Def.constants.statusActive &&
        (currentWorkflowStatus === Def.constants.statusActive || currentWorkflowStatus === Def.constants.statusTesting)) {

        if (withWarn) {
          AGN.Lib.Messages.warn('error.workflow.saveActivatedWorkflow');
        }
        return true;
      }
      return false;
    },

    isPausedWorkflow: function () {
      return $('#workflow-status').val() === Def.constants.statusPaused;
    },

    getConfigData: function ($e) {
      return Helpers.objFromString($e.data('config'));
    },


    mapAsOptionsHtml: function (options) {
      var html = '';

      Object.keys(options).forEach(function (key) {
        var attributes = 'value="' + key + '"';

        html += '<option ' + attributes + '>' + options[key] + '</option>';
      });

      return html;
    },

    arrayAsOptionsHtml: function (options) {
      var html = '';

      options.forEach(function (option) {
        var attributes = 'value="' + option.id + '"';

        var extras = option.data;
        if (extras) {
          Object.keys(extras).forEach(function (k) {
            attributes += ' data-' + k + '="' + extras[k].replace('"', '&quot;') + '"';
          });
        }

        var text = option.text
          .replace('<', '&lt;')
          .replace('>', '&gt;');

        html += '<option ' + attributes + '>' + text + '</option>';
      });

      return html;
    }
  };

  AGN.Lib.WM.DateTimeUtils = {
    toDateTime: function (date, hour, minute) {
      var value = null;
      if (!!date && date.getMonth) {
        value = new Date(date);

        if (!!hour && hour !== 0) {
          value.setHours(hour);
        }

        if (!!minute && minute !== 0) {
          value.setMinutes(minute);
        }
      }

      return value;
    },
    getDateTimeValue: function (date, hour, minute) {
      var value = this.toDateTime(date, hour, minute);
      return value != null ? value.getTime() : null;
    },
    getDateTimeStr: function (date, hour, minute, datePattern) {
      if (!date || !date.getMonth) {
        return "";
      }

      hour = hour || date.getHours();
      minute = minute || date.getMinutes();

      var dateTime = this.toDateTime(date, hour, minute);

      if (!!dateTime) {
        return DateFormat.format(dateTime, datePattern)
      } else {
        return "";
      }
    },
    getDateStr: function (date, datePattern) {
      return this.getDateTimeStr(date, null, null, datePattern);
    },
    getCurrentAdminTime() {
      return $.ajax({url: AGN.url('/workflow/getCurrentAdminTime.action'), async: false}).responseJSON;
    },
  }
})();
