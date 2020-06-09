(function(){

  var Tile = AGN.Lib.Tile,
      Tab  = AGN.Lib.Tab,
      Form  = AGN.Lib.Form;

  $(document).on('click', '[data-toggle-tab]', function(e) {
    var $this = $(this),
        $tileTrigger = Tile.trigger($this),
        toggleMethod = $this.data('toggle-tab-method') || 'show';


    if ($this.is('[data-form-submit]')) {
      var $form = Form.getWrapper($this);
      if ($form.exists() && !Form.get($form).valid()) {
        e.preventDefault();
        return false;
      }
    }

    Tile.show($tileTrigger);
    Tab[toggleMethod]($this);

    e.preventDefault();
    return false;
  });

})();
