AGN.Lib.CoreInitializer.new('trackable-link-extensions', function($scope = $(document)) {
  class TrackableLinkExtensionsTable extends AGN.Lib.InputTable {
    constructor($container) {
      super($container);
    };

    get rowTemplate() {
      return `
        <tr>
            <td><input type="text" class="form-control" data-name="name" value="{{- name }}" placeholder="${t('defaults.name')}" ${this.readonly ? 'disabled' : ''}></td>
            <td><input type="text" class="form-control" data-name="value" value="{{- value }}" placeholder="${t('defaults.value')}" ${this.readonly ? 'disabled' : ''}></td>
        </tr>
      `;
    }

    get headerTemplate() {
      return `
        <tr>
            <th>${t('defaults.name')}</th>
            <th>${t('defaults.value')}</th>
        </tr>
      `;
    }

    addActions() {
      super.addActions();
      AGN.Lib.Action.new({click: '[data-delete-all-extensions]'}, this.clean.bind(this), this.$container);
      AGN.Lib.Action.new({click: '[data-add-default-extensions]'}, this.addDefaultExtensions.bind(this), this.$container);
    }

    addDefaultExtensions() {
      const $lastRow = this.$table.find('tbody tr:last');
      if (this.isEmptyRow$($lastRow)) {
        $lastRow.remove();
      }
      _.each(this.config.defaultExtensions, extension => this.addRow(extension));
      this.addEmptyRow();
    }
  }

  _.each($scope.find('[data-trackable-link-extensions]'), function(el) {
    new TrackableLinkExtensionsTable($(el));
  });
});
