AGN.Lib.Controller.new('device-statistic', function() {
  var HTML_URL_PREFIX,
    htmlBlockSelector,
    exportBlockSelector,
    typeFieldName;

  this.addDomInitializer("device-statistic-initializer", function() {
    var config = this.config;
    var active = config.activeStatisticId;
    HTML_URL_PREFIX = config.HTML_URL;
    htmlBlockSelector = config.htmlBlockSelector;
    exportBlockSelector = config.exportBlockSelector;
    typeFieldName = config.typeFieldName;

    _.each($('[data-action="switch-device-tab"]'), function(el){
      if ($(el).data('type') === active) {
        $(el).trigger("click");
      }
    })
  });

  this.addAction({click: 'switch-device-tab'}, function(){
    var type = $(this.el).data('type');
    $(htmlBlockSelector).find('iframe').attr('src', getHtmlLink(type));
    $(exportBlockSelector).html(getExportLink(type));
    $('[name="' + typeFieldName + '"]').val(type);

  });

  function getExportLink(type) {
    return AGN.Lib.Template.text("statistic-csv-view", {type: type});
  }

  function getHtmlLink(type) {
    return HTML_URL_PREFIX.replace('{stat-type}', type);
  }

});
