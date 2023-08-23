AGN.Lib.Controller.new('trackable-link-list', function () {
  var scrollToElemId;
  var KEEP_UNCHANGED;
  AGN.Opt.DefaultExtensions = {};
  var TrackableLinkExtensions = new AGN.Lib.TrackableLinkExtensions();
  var SAVE_ALL_URL;

  this.addDomInitializer('trackable-link-list', function () {
    KEEP_UNCHANGED = this.config.KEEP_UNCHANGED;
    SAVE_ALL_URL = this.config.SAVE_ALL_URL;
    AGN.Opt.DefaultExtensions = this.config.defaultExtensions;
    if (this.config.scrollToLinkId) {
      scrollTo($('#link-' + this.config.scrollToLinkId));
    }
  });

  this.addDomInitializer('trackable-link-extensions', function () {
    TrackableLinkExtensions.load(this.config.extensions, getExtensionsTable());
  });

  AGN.Lib.Action.new({change: '[id^=link-], #tile-trackableLinkEditAll'}, function () {
    scrollToElemId = this.el.attr('id');
  });

  this.addAction({scrollTo: 'scroll-to'}, function () {
    if (scrollToElemId) {
      scrollTo($(this.el).find('#' + scrollToElemId));
    }
  });

  this.addAction({
    click: 'save-bulk-actions',
    enterdown: 'description-enterdown, bulkActionExtensionsEnterdown'
  }, function () {
    var form = AGN.Lib.Form.get($("#trackableLinkForm"));
    var bulkActionsFormData = $('#bulkActionsForm').serializeFormDataObject();
    form._data = {}

    $.each(bulkActionsFormData, function (k, v) {
      form.setValue(k, v);
    });
    form.setValue('modifyAllLinksExtensions', false);
    setExtensionsToForm(form);
    form.setActionOnce(SAVE_ALL_URL);
    form.submit();
    AGN.Lib.Modal.getWrapper(this.el).modal('hide');
  });

  this.addAction({click: 'save', enterdown: 'settingsExtensionsEnterdown'}, function () {
    this.event.preventDefault();
    var form = AGN.Lib.Form.get($('#trackableLinkForm'));
    form._data = {}

    form.setValue('modifyBulkLinksExtensions', false);
    keepBulkSettingsUnchanged(form);
    setExtensionsToForm(form);
    form.setActionOnce(SAVE_ALL_URL);
    form.submit();
  });

  function scrollTo($target) {
    if ($target && $target.length > 0) {
      var $scrollContainer = $target.closest('[data-sizing="scroll"]');
      if ($scrollContainer.length > 0) {
        var highestOffsetTop = $scrollContainer.find(':first-child').offset().top;
        var targetOffsetTop = $target.offset().top;
        $scrollContainer.scrollTop(0);
        $scrollContainer.scrollTop(targetOffsetTop - highestOffsetTop);
      }
    }
  }

  function setExtensionsToForm(form) {
    _.each(TrackableLinkExtensions.collect(), function (extension, index) {
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

  function getExtensionsTable() {
    return isModalOpened() ? $('#bulkActionExtensions tbody') : $('#settingsExtensions tbody');
  }

  function isModalOpened() {
    return $('.modal').is(':visible');
  }
});
