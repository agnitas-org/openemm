(function () {

  const DEFAULT_ROW_TEMPLATE = _.template(`
      {{ const disabled = isDisabled ? 'disabled' : '';}}
      <div class="col-12" data-email-list-manager-row data-action="change-trigger">
        <div class="row g-1">
            <div class="col">
                <input type="text" value="{{= value}}" class="form-control" size="42" maxlength="99" {{- disabled }} />
            </div>
            {{ if (!isDisabled) { }}
                <div class="col-auto">
                     <button type="button" class="btn btn-primary btn-icon" data-action="create-email" data-email-manager-row-add>
                        <i class="icon icon-plus"></i>
                    </button>
                </div>
            {{ } }}
        </div>
      </div>
  `);

  const DELETE_BTN_TEMPLATE = _.template(`
     <button type="button" class="btn btn-danger btn-icon" data-action="delete-email" data-email-manager-row-delete>
      <i class="icon icon-trash-alt"></i>
    </button>
  `);

  class EmailsListManager {
    constructor(scope, data, options) {
      this.scope = scope;
      this.data = data;
      this.options = $.extend({
        rowTemplate: DEFAULT_ROW_TEMPLATE,
        rowSelector: '[data-email-list-manager-row]',
        addBtnSelector: '[data-email-manager-row-add]',
        deleteBtnSelector: '[data-email-manager-row-delete]',
        rowDataMapper: data => {
          return {value: data, isDisabled: options.disabled};
        }
      }, options);

      _.each(this.data, rowData => this.#addFieldRow(this.scope, rowData, null, this.options));
    }

    static initialize(scope, data, options) {
      return new EmailsListManager(scope, data, options);
    }

    createRowAfter(target) {
      this.#addFieldRow(this.scope, "", $(target), this.options)
    }

    #addFieldRow(scope, rowData, $target, options) {
      const content = options.rowTemplate(options.rowDataMapper(rowData))

      if ($target && $target.exists()) {
        $target.closest(options.rowSelector).after(content);
      } else {
        scope.append(content);
      }

      this.#updateRemoveButtons(scope, options);
    }

    deleteRow(target) {
      $(target).closest(this.options.rowSelector).remove();
      this.#updateRemoveButtons(this.scope, this.options);
    }

    getJsonData() {
      const values = [];
      _.each(this.scope.find(`${this.options.rowSelector} input`), rowInput => {
        const value = rowInput.value;
        if (value.trim() !== '') {
          values.push(value);
        }
      });
      return values;
    }

    #updateRemoveButtons(scope, options) {
      const $rows = scope.find(options.rowSelector);
      if ($rows.length === 1) {
        $rows.find(options.deleteBtnSelector).remove();
      } else {
        _.each($rows, (row, index) => {
          const $row = $(row);
          const $deleteBtn = $row.find(options.deleteBtnSelector);

          if (index != $rows.length - 1 && !$deleteBtn.exists()) {
            $row.find('.btn').replaceWith($(DELETE_BTN_TEMPLATE({})));
          }
        });
      }
    }
  }

  AGN.Lib.EmailsListManager = EmailsListManager;
})();
