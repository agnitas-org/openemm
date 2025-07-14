(() => {

  const Form = AGN.Lib.Form;

  $(document).on('click', '[data-toggle-tab]', function (e) {
    const $this = $(this);

    if ($this.is('[data-form-submit]')) {
      const $form = Form.getWrapper($this);
      if ($form.exists() && !Form.get($form).valid()) {
        e.preventDefault();
        return false;
      }
    }

    AGN.Lib.Tab.show($this);
    AGN.Lib.Scrollbar.get($this)?.update();

    e.preventDefault();
    return false;
  });
})();
