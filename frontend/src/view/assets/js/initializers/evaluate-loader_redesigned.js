AGN.Lib.CoreInitializer.new('evaluate-loader', function($scope = $(document)) {
  const Modal = AGN.Lib.Modal;
  const Page = AGN.Lib.Page;
  
  $scope.on('click', '[data-evaluate-loader]', function (e) {
    const $el = $(this);
    e.preventDefault();
    let $modal;
    
    return $.ajax({
      beforeSend: () => $modal = Modal.fromTemplate(getModalTemplateName($el)),
      xhr: showProgress,
      url: $el.prop('href'),
      type: 'POST',
      data: $el.is('[data-form-target]') ? AGN.Lib.Form.get($($el.data('form-target'))).params() : ''
    }).done(data => Page.render(data))
      .fail(data => Page.render(data))
      .always(() => Modal.getInstance($modal).hide());
  });

  function showProgress() {
    const xhr = new XMLHttpRequest();
    xhr.addEventListener('progress', (event) => {
      if (event.lengthComputable) {
        $("#progress-text").html(event.loaded + " bytes");
      }
    });
    return xhr;
  }

  function getModalTemplateName($el) {
    return $el.data('evaluate-loader') || 'evaluate-loader-template';
  }
});
