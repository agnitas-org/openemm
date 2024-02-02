(() => {

  class PreviewTable {

    static init() {
      const $toggle = $('[data-preview-table]');
      const conf = AGN.Lib.Storage.get(PreviewTable.#getStorageKey($toggle));

      if (conf) {
        $toggle.prop('checked', !conf.preview);
        PreviewTable.#toggleUI($toggle);
      }
    }

    static toggle($toggle) {
      PreviewTable.#toggleUI($toggle)
      AGN.Lib.Storage.set(PreviewTable.#getStorageKey($toggle), {
        preview: PreviewTable.#isPreviewEnabled($toggle)
      });
    }

    static #toggleUI($toggle) {
      const tableSelector = $toggle.data('preview-table');
      $(tableSelector).toggleClass('table-preview', PreviewTable.#isPreviewEnabled($toggle));
    }

    static #isPreviewEnabled($toggle) {
      return !$toggle.is(':checked');
    }

    static #getStorageKey($toggle) {
      return `preview-table-${$toggle.data('preview-table')}`;
    }
  }

  AGN.Lib.PreviewTable = PreviewTable;

})();
