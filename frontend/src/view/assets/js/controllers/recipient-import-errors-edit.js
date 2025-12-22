AGN.Lib.Controller.new('recipient-import-errors-edit', function () {

  this.addDomInitializer('recipient-import-errors-edit', function () {
    if (isCurrentPageReloaded()) {
      AGN.Lib.Page.reload(AGN.url('/recipient/import/chooseMethod.action?cancelImport=true'))
    } else {
      window.onbeforeunload = function () {
        return "If you leave the page, the import will be canceled automatically!";
      }
    }
  });

  function isCurrentPageReloaded() {
    if (typeof performance === 'undefined' || typeof performance.getEntriesByType !== 'function') {
      return false;
    }

    const entries = performance.getEntriesByType('navigation');
    if (entries.length > 0) {
      return entries[0].type === 'reload' && entries[0].name === window.location.href;
    }

    return false;
  }

  this.addAction({submitted: 'save-errors'}, function () {
    unbindUnloadEvents();
  });

  this.addAction({click: 'ignore-errors'}, function () {
    unbindUnloadEvents();
    AGN.Lib.Confirm.request(AGN.url('/recipient/import/errors/ignore.action'));
  });

  function unbindUnloadEvents() {
    window.onbeforeunload = null;
  }
});
