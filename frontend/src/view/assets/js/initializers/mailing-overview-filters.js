AGN.Lib.DomInitializer.new('mailing-overview-filters', function() {
  $("#filtersDescription").html(AGN.Lib.Template.text('mailing-overview-filters', this.config));
  $('#mailing').after($('#mailings-overviewPreview'));
});
