AGN.Lib.CoreInitializer.new('loader', function ($scope = $(document)) {

  const Template = AGN.Lib.Template;

  _.each($scope.find('[data-custom-loader]'), el => {
    const loaderName = $(el).data('custom-loader');
    if (loaderName === 'processing') {
      initProcessingLoader(el);
    }
  });

  function initProcessingLoader(el) {
    const $container = $($(el).data('resource-selector')) || $(el);
    const $loader = Template.dom('processing-loader', { targetForm: `#${$(el).attr('id')}` });

    $(el).on({
      'form:loadershow': () => $container.html($loader),
      'form:loaderhide': () => $loader.remove()
    });
  }
});
