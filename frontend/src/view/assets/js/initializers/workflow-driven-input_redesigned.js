;(() => {

  AGN.Lib.CoreInitializer.new('workflow-driven-input', function($scope = $(document)) {
    if (!AGN.Lib.Template.exists('edit-with-campaign-btn')) {
      return;
    }

    _.each($scope.all(':input[data-workflow-driven="true"]'), el=> {
      const $el = $(el);

      if ($el.is('input[type="text"]')) {
        decorateWorkflowDrivenTextInput($el);
      }
      if ($el.is('input[type="checkbox"]')) {
        decorateWorkflowDrivenCheckboxInput($el);
      }
      if ($el.is('select')) {
        decorateWorkflowDrivenSelect($el);
      }
      $el.prop('disabled', true);
    });
  });

  function decorateWorkflowDrivenTextInput($el) {
    $el
      .css('border-left', '0')
      .wrap('<div class="input-group">')
      .before(getInputAddonWithCampaignBtn(getWorkflowIcon()));
    AGN.Lib.CoreInitializer.run("tooltip", $el.parent());
  }

  function decorateWorkflowDrivenCheckboxInput($el) {
    $el.next().after(getWorkflowIcon());
    AGN.Lib.CoreInitializer.run("tooltip", $el.parent());
  }

  function decorateWorkflowDrivenSelect($el) {
    const $select2 = $el.next('.select2-container');
    if ($el.prop('multiple')) {
      $select2.find('.select2-selection__rendered').hide();
      $select2.find('.select2-search__field').val(t('mailing.default.editWithCampaign'));
      $select2.find('.select2-search--inline').prepend(getWorkflowIcon());
    } else {
      $select2.find('.select2-selection').prepend(getWorkflowIcon());
    }
    AGN.Lib.CoreInitializer.run("tooltip", $select2);
  }

  function getInputAddonWithCampaignBtn() {
    return $('<div>', {
      class: 'input-group-text input-group-text--disabled border-end-0 pe-0',
      html: getWorkflowIcon()
    });
  }

  function getWorkflowIcon() {
    return AGN.Lib.Template.text('edit-with-campaign-btn')
  }

})();
