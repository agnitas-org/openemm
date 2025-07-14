(() => {
  const Form = AGN.Lib.Form;

  $(document).on('click', '[data-evaluate-loader]', function (e) {
    const $el = $(this);
    e.preventDefault();
    let $modal;

    return $.ajax({
      beforeSend: () => $modal = AGN.Lib.Modal.fromTemplate(getModalTemplateName($el)),
      xhr: showProgress,
      url: $el.prop('href'),
      type: 'POST',
      data: $el.is('[data-form-target]') ? Form.get($($el.data('form-target'))).params() : ''
    }).done(resp => {
      if (typeof resp === 'object') {
        if (resp.success === false) {
          AGN.Lib.JsonMessages(resp.popups);
        } else {
          downloadFile(resp.data, $el);
        }
      } else {
        AGN.Lib.Page.render(resp)
      }
    })
      .fail(data => AGN.Lib.Page.render(data))
      .always(() => AGN.Lib.Modal.getInstance($modal).hide());
  });

  function showProgress() {
    const xhr = new XMLHttpRequest();
    xhr.addEventListener('progress', (event) => {
      if (event.lengthComputable) {
        $("#progress-text").html(`${event.loaded} bytes`);
      }
    });
    return xhr;
  }

  function getModalTemplateName($el) {
    return $el.data('evaluate-loader') || 'evaluate-loader-template';
  }

  function downloadFile(url, $el) {
    const $form = Form.getWrapper($el);
    const wasDirty = $form.dirty('isDirty');
    $form.dirty('setAsClean'); // prevent dirty alert

    const unloadHandler = window.onbeforeunload;
    window.onbeforeunload = null;
    window.location = AGN.url(url);
    window.onbeforeunload = unloadHandler;

    if (wasDirty) {
      $form.dirty('setAsDirty');
    }
  }
})();
