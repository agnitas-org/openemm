AGN.Lib.Controller.new('blacklist-list', function () {

  this.addAction({ click: 'save' }, function () {
    $.post(this.el.data('url'), {
      email: $('#new-entry-email').val(),
      reason: $('#new-entry-reason').val()
    })
    .done(resp => submitTableForm(resp))
    .fail(jqXHR => AGN.Lib.RenderMessages($(jqXHR.responseText)))
  });

  this.addAction({ click: 'save-edit' }, function () {
    const $el = this.el;
    AGN.Lib.Form.get($el).submit().done(resp => {
      submitTableForm(resp, () => AGN.Lib.Modal.getInstance($el).hide());
    });
  });

  function submitTableForm(resp, callback) {
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
