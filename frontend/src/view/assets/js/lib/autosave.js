(function() {

  function checkPossibleRestoration(scopeId, check, restore) {
    var bundle = AGN.Lib.Storage.get('autosave#' + scopeId);

    if (bundle) {
      var result = check ? check(bundle.values, bundle.timestamp) : bundle.values;

      if (result) {
        var dialog = AGN.Lib.Template.text('autosave-restore', {
          modalClass: '',
          title: t('autosave.confirm.title'),
          content: t('autosave.confirm.question', new Date(bundle.timestamp)),
          negative: t('autosave.discard'),
          positive: t('autosave.restore')
        });

        AGN.Lib.Confirm.create(dialog)
          .done(function() {
            restore(result === true ? bundle.values : result, bundle.timestamp);
            AGN.Lib.Messages(t('autosave.success.title'), t('autosave.success.message'), 'success');
          })
          .fail(function() {
            AGN.Lib.Storage.delete('autosave#' + scopeId);
          });
      }
    }
  }

  function scheduleAutoSave(scopeId, save, period) {
    var doSave = function() {
      var values = save();

      if (values) {
        AGN.Lib.Storage.set('autosave#' + scopeId, {
          timestamp: new Date().getTime(),
          values: values
        });
      }

      return values !== false;
    };

    if (period > 0) {
      var timerHandle = setInterval(function () {
        doSave() || clearInterval(timerHandle);
      }, period * 1000);
    }

    var doSaveOnUnauthorized = function() {
      doSave() || $(document).unbind('ajax:unauthorized', doSaveOnUnauthorized);
    };

    $(document).on('ajax:unauthorized', doSaveOnUnauthorized);
  }

  AGN.Lib.AutoSave = {
    initialize: function(scopeId, save, check, restore, period) {
      checkPossibleRestoration(scopeId, check, restore);
      scheduleAutoSave(scopeId, save, period);
    }
  };

})();
