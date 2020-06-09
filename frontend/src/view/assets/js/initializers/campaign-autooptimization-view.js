(function(){

  function extractDateTimeToResultField(dateSelector, timeSelector, resultFieldSelector, datePattern) {
    var dateValue = $(dateSelector).pickadate('picker').get('select', datePattern);
    var timeValue = $(timeSelector).val();
    var dateTime = "";

    if (dateValue && timeValue) {
      dateTime = dateValue + " " + timeValue;
    }

    $(resultFieldSelector).val(dateTime);
  }

  function initDateTimeSelectorsGroup(dateSelector, timeSelector, resultFieldSelector, datePattern) {
    $(dateSelector).pickadate('picker').on({
      set: function() {
        extractDateTimeToResultField(dateSelector, timeSelector, resultFieldSelector, datePattern);
      }
    });

    $(timeSelector).on('change', function() {
      extractDateTimeToResultField(dateSelector, timeSelector, resultFieldSelector, datePattern);
    });
  }

  AGN.Lib.DomInitializer.new('campaign-autooptimization-view', function() {
    $('form#optimizationForm').submit(false);

    initDateTimeSelectorsGroup("#testMailingsSendDate", "#testMailingsSendTime", "input[name='testMailingsSendDateAsString']", this.config.datePattern);
    initDateTimeSelectorsGroup("#resultSendDate", "#resultSendTime", "input[name='resultSendDateAsString']", this.config.datePattern);
  });

})();
