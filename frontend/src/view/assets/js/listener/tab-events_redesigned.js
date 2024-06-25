(function () {

  const Tile = AGN.Lib.Tile;
  const Tab = AGN.Lib.Tab;
  const Form = AGN.Lib.Form;

  $(document).on('click', '[data-toggle-tab]', function (e) {
    const $this = $(this);
    const $tileTrigger = Tile.trigger($this);
    const toggleMethod = $this.data('toggle-tab-method') || 'show';

    if ($this.is('[data-form-submit]')) {
      const $form = Form.getWrapper($this);
      if ($form.exists() && !Form.get($form).valid()) {
        e.preventDefault();
        return false;
      }
    }

    Tile.show($tileTrigger);
    Tab[toggleMethod]($this);
    AGN.Lib.Scrollbar.get($this)?.update();

    e.preventDefault();
    return false;
  });

})();
