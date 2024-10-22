/*doc
---
title: Input table
name: input-table
category: Components - Input table
---

Use `[data-input-table]` in order to create a stack of rows inputs in the form of a table with the ability to add and delete rows
(the <i class="icon icon-plus"></i></a> and <i class="icon icon-trash-alt"></i> buttons are added automatically).
Empty rows can't be added. Only last one row can be empty.
The focus jumps to the next input on `Enter key` pressed.

Mandatory elements

element                                                     |description                                                     |
------------------------------------------------------------|----------------------------------------------------------------| 
`script[type="application/json"][data-config]`              |pass fields config {<b>data</b>: json, <b>readonly</b>: boolean}| 
`script[type="text/x-mustache-template"][data-row-template]`|specifying mustache template for a single row                   |


Below are some methods you can call:

method          |description            |
----------------|-----------------------|
<i>`get(el)`</i>|get InputTable instance|
`collect()`     |get input data as json |
`clean()`       |remove all rows        |

```htmlexample

<h2 class="mt-3">Default example<h2>

<div data-input-table>
    <script data-config type="application/json">
        {
          "data": [{"name":"first","value":"one","option":1},{"name":"second","value":"two","option":2},{"name":"third","value":"three","option":3}],
          "readonly": false
        }
    </script>
    <script data-row-template type="text/x-mustache-template">
        <tr>
           <td><input type="text" class="form-control" data-name="name" value="{{- name }}" placeholder='Name'></td>
           <td><input type="text" class="form-control" data-name="value" value="{{- value }}" placeholder='Value'></td>
           <td>
              <select data-name="option" class="form-control">
                  <option val="0" {{- option === 1 ? 'selected' : ''}}>Select option</option>
                  <option val="1" {{- option === 1 ? 'selected' : ''}}>Option 1</option>
                  <option val="2" {{- option === 2 ? 'selected' : ''}}>Option 2</option>
                  <option val="3" {{- option === 3 ? 'selected' : ''}}>Option 3</option>
              </select>
            </td>
        </tr>
    </script>
</div>

<h2 class="mt-3">Readonly example<h2>

<div data-input-table>
    <script data-config type="application/json">
        {
          "data": [{"name":"first","value":"one","option":1},{"name":"second","value":"two","option":2},{"name":"third","value":"three","option":3}],
          "readonly": true
        }
    </script>
    <script data-row-template type="text/x-mustache-template">
        <tr>
           <td><input type="text" class="form-control" data-name="name" value="{{- name }}" placeholder='Name' readonly></td>
           <td><input type="text" class="form-control" data-name="value" value="{{- value }}" placeholder='Value' readonly></td>
           <td>
              <select data-name="option" class="form-control" readonly>
                  <option val="0" {{- option === 1 ? 'selected' : ''}}>Select option</option>
                  <option val="1" {{- option === 1 ? 'selected' : ''}}>Option 1</option>
                  <option val="2" {{- option === 2 ? 'selected' : ''}}>Option 2</option>
                  <option val="3" {{- option === 3 ? 'selected' : ''}}>Option 3</option>
              </select>
            </td>
        </tr>
    </script>
</div>
```
 */

(() => {
  const Templates = AGN.Opt.Templates;
  const addBtnTemplate = _.template(Templates['plus-btn']);
  const deleteBtnTemplate = _.template(Templates['trash-btn']);

  class InputTable {
    constructor($container, data = [], readonly) {
      const $config = $container.find('script[data-config]');
      this.config = $config.exists() ? $config.json() : {readonly, data};
      this.readonly = this.config.readonly;
      this.$container = $container;
      this.rowTemplateMustache = _.template(this.rowTemplate);
      this.renderTable(this.config.data);
      this.addActions();
      this.$container.data('table', this);
    };

    static get(container) {
      return $(container).data('table');
    }

    get rowTemplate() {
      if (this._rowTemplate?.length) {
        return this._rowTemplate;
      }
      this._rowTemplate = this.$container.find('[data-row-template]').html();
      return this._rowTemplate;
    }

    get headerTemplate() {
      if (this._headerTemplate?.length) {
        return this._headerTemplate;
      }
      this._headerTemplate = this.$container.find('[data-input-table-header-template]').html();
      return this._headerTemplate;
    }

    get $rowTemplate() {
      return $(this.rowTemplate);
    }

    renderTable(data, initial = true) {
      if (initial) {
        this.$table = $('<table>').addClass('input-table').append($('<tbody>'));
        if (this.readonly) {
          this.$table.addClass('input-table--readonly');
        }

        if (this.headerTemplate) {
          const $header = $(_.template(this.headerTemplate)());
          if (!this.readonly) {
            $header.append('<th></th>');
          }
          this.$table.prepend($('<thead>').append($header));
        }

        this.$container.prepend(this.$table);
      }

      _.each(data, (rowData) => this.addRow(rowData));
      if (!this.readonly) {
        this.addEmptyRow();
      }
    }

    addActions() {
      const self = this;
      this.addAction({click: '[data-add-row]'}, this.addEmptyRow.bind(this));
      this.addAction({enterdown: 'input'}, function() {
        this.event.preventDefault();
        self.getNextInputToFocus(this.el).focus();
      });

      this.addAction({click: '[data-delete-row]'}, function () {
        self.removeRow(this.el);
      });
    }
    
    removeRow($rowEl) {
      $rowEl.closest('tr').remove();
      this.$container.trigger('input-table:row-removed');
    }

    addAction(events, handler) {
      AGN.Lib.Action.new(events, handler, this.$table);
    }

    collect(includeLastRow = true) {
      let rows = this.$table.find('tr').toArray();
      if (!includeLastRow) {
        rows = rows.slice(0, -1);
      }

      return rows
        .filter(row => !this.isEmptyRow$($(row)))
        .map(row => this.getRowData($(row)));
    };

    getRowData($row) {
      return $row.find(':input[data-name]').toArray().reduce((acc, input) => {
        acc[$(input).data('name')] = this.getInputVal($(input));
        return acc;
      }, {});
    }

    getInputVal($input) {
      if ($input.is(':checkbox')) {
        return $input.is(":checked");
      }
      return $input.val()?.trim();
    }

    addRow(rowData) {
      if (!this.isUniqueRow(rowData)) {
        return;
      }
      const $row = $(this.rowTemplateMustache(rowData));
      if (!this.readonly) {
        const button = this.generateRowBtn(rowData);
        $row.append($('<td>').html(button));
      }
      this.$table.append($row);
      AGN.runAll($row);
      AGN.Lib.Form.get(this.$table).initFields();
    }

    getNextInputToFocus($current) {
      const $currentRow = $current.closest('tr');
      if (this.isEmptyRow$($currentRow)) { // if row is empty focus on first input back
        return $currentRow.find(':input[type="text"]:first');
      }
      const $inputsInRow = $currentRow.find(':input[type="text"]');
      const currentIndex = $inputsInRow.index($current);
      let $nextInput = $inputsInRow.eq(currentIndex + 1);
      if ($nextInput.length) {
        return $nextInput;
      }
      $nextInput = $currentRow.next().find(':input[type="text"]:first');
      if ($nextInput.length) {
        return $nextInput;
      }
      this.addEmptyRow();
      return this.$table.find('tbody tr:last input[type="text"]:first')
    }

    generateRowBtn(data) {
      return this.#isEmptyRow(data) ? this.generateAddBtn() : this.generateDeleteBtn();
    }

    generateAddBtn() {
      return addBtnTemplate({attrs: `data-add-row data-tooltip='${t('defaults.add')}'`});
    }

    generateDeleteBtn() {
      return deleteBtnTemplate({attrs: `data-delete-row data-tooltip='${t('defaults.clear')}'`});
    }

    #isEmptyRow(data) {
      return this.getRowEntries(data).every(([, val]) => this.#isBlank(val));
    }

    getNames() {
      return this.$rowTemplate.find('[data-name]').toArray().map(input => $(input).data('name'));
    }

    getRowEntries(data) {
      const names = this.getNames();
      return Object.entries(data)
        .filter(([key]) => names.includes(key));
    }

    isEmptyRow$($row) {
      return _.every($row.find(':input:not([type="hidden"])'), input => {
        const $input = $(input);
        return $input.prop('disabled') || this.#isBlank($input.val())
      });
    }

    #isBlank(value) {
      return value === null || value === undefined || String(value).trim() === '';
    }

    addEmptyRow() {
      const $lastRow = this.$table.find('tbody tr:last');
      if (!$lastRow.length || !this.isEmptyRow$($lastRow)) {
        this.replaceNewButtonWithDeleteButton();
        this.addRow(this.getNames().reduce((obj, key) => {
          obj[key] = '';
          return obj;
        }, {}));
      }
      this.focusOnLastRow();
    }

    focusOnLastRow() {
      this.$table.find('tbody tr:last input:first').focus();
    }

    isUniqueRow(data) {
      if (this.#isEmptyRow(data)) {
        return true;
      }
      const rowEntries = this.getRowEntries(data);
      return !this.$table.find('tbody tr').toArray().some(row => {
        return rowEntries.every(([name, val]) => $(row).find(`[data-name="${name}"]`).val() === val);
      });
    }

    replaceNewButtonWithDeleteButton() {
      const newBtn = this.$table.find('[data-add-row]');
      const $deleteBtn = $(this.generateDeleteBtn());
      newBtn.after($deleteBtn);
      newBtn.remove();

      AGN.Lib.CoreInitializer.run('tooltip', $deleteBtn);
    }

    #replaceLastDeleteButtonWithNewButton() {
      const $addBtn = $(this.generateAddBtn());

      this.$table.find('[data-delete-row]:last')
        .after($addBtn)
        .remove();

      AGN.Lib.CoreInitializer.run('tooltip', $addBtn);
    }
    
    removeLastRow() {
      if (this.$container.find('tbody tr').length > 1) {
        this.$container.find('tbody tr:last').remove();
        this.#replaceLastDeleteButtonWithNewButton();
      }
    }

    clean() {
      this.$table.find('tbody tr').remove();
      this.addEmptyRow();
    }

    update(data) {
      this.$table.find('tbody tr').remove();
      this.renderTable(data, false);
    }

    get readonlyAttr() {
      return this.readonly ? 'readonly' : '';
    }
  }
  AGN.Lib.InputTable = InputTable;
})();
