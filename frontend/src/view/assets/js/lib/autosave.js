(() => {

  function getStorageKey(scopeId) {
    return `autosave#${scopeId}`;
  }

  function checkPossibleRestoration(scopeId, check, restore) {
    const storageKey = getStorageKey(scopeId);
    const bundle = AGN.Lib.Storage.get(storageKey);

    if (bundle) {
      const result = check ? check(bundle.values, bundle.timestamp) : bundle.values;

      if (result) {
        const dialog = AGN.Lib.Template.text('autosave-restore', {
          title: t('autosave.confirm.title'),
          content: t('autosave.confirm.question',
            new Date(bundle.timestamp).toLocaleString(window.adminLocale,
              { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit'}))
        });

        return AGN.Lib.Confirm.create(dialog)
          .done(() => {
            restore(result === true ? bundle.values : result, bundle.timestamp);
            AGN.Lib.Messages(t('autosave.success.title'), t('autosave.success.message'), 'success');
          })
          .always(() => AGN.Lib.Storage.delete(storageKey))
          .promise();
      } else {
        AGN.Lib.Storage.delete(storageKey);
      }
    }
    return $.Deferred().reject().promise();
  }

  function scheduleAutoSave(scopeId, save, period) {
    const doSave = () => {
      const values = save();

      if (values) {
        AGN.Lib.Storage.set(getStorageKey(scopeId), {
          timestamp: new Date().getTime(),
          values: values
        });
      }

      return values !== false;
    };

    if (period > 0) {
      const timerHandle = setInterval(() => {
        doSave() || clearInterval(timerHandle);
      }, period * 1000);
    }

    const doSaveOnUnauthorized = function () {
      doSave() || $(document).unbind('ajax:unauthorized', doSaveOnUnauthorized);
    };

    $(document).on('ajax:unauthorized', doSaveOnUnauthorized);
  }

  AGN.Lib.AutoSave = {
    initialize(scopeId, save, check, restore, period) {
      const promise = checkPossibleRestoration(scopeId, check, restore);
      scheduleAutoSave(scopeId, save, period);
      return promise;
    },
    clear(scopeId) {
      AGN.Lib.Storage.delete(getStorageKey(scopeId));
    }
  };

})();
