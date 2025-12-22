AGN.Lib.Controller.new('blacklist-list', function () {

  this.addAction({ click: 'save' }, function () {
    $.ajax(this.el.data('url'), {
      type: 'POST',
      data: {
        email: $('#new-entry-email').val(),
        reason: $('#new-entry-reason').val()
      },
      success: resp => submitForm(resp)
    });
  });

  this.addAction({ click: 'save-edit' }, function () {
    const $el = this.el;
    AGN.Lib.Form.get($el).submit().done(resp => {
      submitForm(resp, () => AGN.Lib.Modal.getInstance($el).hide());
    });
  });

  function submitForm(resp, callback) {
    if (resp.success === true) {
      AGN.Lib.Form.get($('#blacklist-overview')).submit().done(() => AGN.Lib.JsonMessages(resp.popups, true));

      if (callback) {
        callback();
      }
    } else if (resp.popups) {
      AGN.Lib.JsonMessages(resp.popups, true);
    } else {
      AGN.Lib.RenderMessages($(resp));
    }
  }

});
