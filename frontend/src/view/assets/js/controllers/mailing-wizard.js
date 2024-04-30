AGN.Lib.Controller.new('mailing-wizard', function() {

  this.addAction({change: 'selectTargetGroups'}, function() {
    var $el = $(this.el);
    var ids = $el.val() || [];
    var $mode = $('#targetModeCheck');
    var $modeControls = $mode.closest('.form-group');

    // Was single and become multiple.
    $mode.prop('checked', ids.length > 1);
    $modeControls.toggle(ids.length > 1);
  });

});