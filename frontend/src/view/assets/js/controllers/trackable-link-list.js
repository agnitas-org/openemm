AGN.Lib.Controller.new('mailing-trackable-links', function () {
  const self = this;
  const Form = AGN.Lib.Form;
  let scrollToElemId;
  let KEEP_UNCHANGED;
  let SAVE_ALL_URL;
  

  this.addDomInitializer('mailing-trackable-links', function () {
    KEEP_UNCHANGED = this.config.KEEP_UNCHANGED;
    SAVE_ALL_URL = this.config.SAVE_ALL_URL;
    if (this.config.scrollToLinkId) {
      scrollTo($(`#link-${this.config.scrollToLinkId}`));
    }
  });

  AGN.Lib.Action.new({change: '[id^=link-], #tile-trackableLinkEditAll'}, function () {
    scrollToElemId = this.el.attr('id');
  });

  this.addAction({click: 'save-bulk-actions'}, function () {
    const form = Form.get($("#trackableLinksForm"));
    const bulkActionsFormData = $('#bulkActionsForm').serializeFormDataObject();
    form._data = {}

    $.each(bulkActionsFormData, function (k, v) {
      form.setValue(k, v);
    });
    form.setValue('modifyAllLinksExtensions', false);
    setExtensionsToForm(AGN.Lib.InputTable.get('#bulkActionExtensions').collect(), form);
    form.setActionOnce(SAVE_ALL_URL);
    form.submit();
    AGN.Lib.Modal.getWrapper(this.el).modal('hide');
  });

  this.addAction({submission: 'save-all'}, function () {
    this.event.preventDefault();
    const form = Form.get($('#trackableLinksForm'));
    form._data = {}

    form.setValue('modifyBulkLinksExtensions', false);
    keepBulkSettingsUnchanged(form);
    setExtensionsToForm(AGN.Lib.InputTable.get('#link-common-extensions').collect(), form);
    form.setActionOnce(SAVE_ALL_URL);
    form.submit();
  });
  
  this.addAction({scrollTo: 'scroll-to'}, function () {
    if (scrollToElemId) {
      scrollTo($(this.el).find('#' + scrollToElemId));
    }
  });

  function scrollTo($target) {
    if ($target && $target.length > 0) {
      const $scrollContainer = $target.closest('.table-wrapper__body');
      if ($scrollContainer.exists()) {
        const highestOffsetTop = $scrollContainer.find(':first-child').offset().top;
        const targetOffsetTop = $target.offset().top;
        $scrollContainer.scrollTop(0);
        $scrollContainer.scrollTop(targetOffsetTop - highestOffsetTop);
      }
    }
  }
  
  this.addAction({click: 'save-individual'}, function () {
    this.event.preventDefault();
    const form = Form.get($('#trackableLinkForm'));
    const extensions = AGN.Lib.InputTable.get("#individual-extensions").collect();
    setExtensionsToForm(extensions, form);
    form.submit();
  });

  this.addAction({'change': 'link-details-trackable'}, function () {
    self.runInitializer('trackableAction');
  });

  this.addInitializer('trackableAction', function () {
    const $trigger = $('[data-action="link-details-trackable"] :selected');
    const $linkAction = $('#linkAction');
    $linkAction.prop('disabled', $linkAction.prop('disabled') || $trigger.val() == 0);
  })

  function setExtensionsToForm(extensions, form) {
    _.each(extensions, function (extension, index) {
      form.setValue('extensions[' + index + '].name', extension.name);
      form.setValue('extensions[' + index + '].value', extension.value);
    })
  }

  function keepBulkSettingsUnchanged(form) {
    form.setValue('bulkUsage', KEEP_UNCHANGED);
    form.setValue('bulkAction', KEEP_UNCHANGED);
    form.setValue('bulkDeepTracking', KEEP_UNCHANGED);
    form.setValue('bulkStatic', KEEP_UNCHANGED);
  }
});
