(() => {

  class PreviewTable {

    static init($el) {
      const conf = AGN.Lib.Storage.get(PreviewTable.#getStorageKey($el));
      if (!conf) {
        return;
      }

      $el.find(`input[value="${conf.type}"]`).prop('checked', true);
      PreviewTable.#toggleUI($el);
    }

    static toggle($el) {
      PreviewTable.#toggleUI($el);
      AGN.Lib.Storage.set(PreviewTable.#getStorageKey($el), {
        type: $el.find('input:checked').val()
      });
    }

    static #toggleUI($el) {
      const type = $el.find('input[type="radio"]:checked').val();

      const isPreviewEnabled = type === 'preview';
      const isVisualListEnabled = type === 'visual-list';

      const $table = $($el.data('preview-table'));

      $table.toggleClass('table--preview', isPreviewEnabled);
      $table.toggleClass('table--visual-list', isVisualListEnabled);
      AGN.Lib.Popover.toggleState($table, !isPreviewEnabled && !isVisualListEnabled);
    }

    static #getStorageKey($toggle) {
      return `table-type-${$toggle.data('preview-table')}`;
    }
  }

  AGN.Lib.PreviewTable = PreviewTable;

})();
