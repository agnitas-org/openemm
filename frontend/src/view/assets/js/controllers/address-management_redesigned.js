AGN.Lib.Controller.new('address-management', function () {

  const Table = AGN.Lib.Table;
  const Messages = AGN.Lib.Messages;
  const Confirm = AGN.Lib.Confirm;

  const CATEGORY_ATTR_NAME = 'data-entry-category';
  let email;

  this.addDomInitializer('address-management', function () {
    email = this.config.email;
  });

  this.addAction({click: 'delete-all'}, function () {
    const affectedCategories = getNotEmptyCategories();
    Confirm.from('confirm-delete-all-modal', {affectedCategories}).done(() => {
      $.post(AGN.url(`/address-management/entries/deleteAll.action?email=${email}`))
        .done(resp => AGN.Lib.Form.get(this.el).updateHtml(resp));
    });
  });

  function getNotEmptyCategories() {
    return $('.btn-category:not([disabled])').map(function () {
      return $(this).data('entry-category-tab');
    }).get();
  }

  this.addAction({click: 'update-all'}, function () {
    createConfirmForReplace(newEmail => {
      $.post(AGN.url(`/address-management/entries/replaceAll.action?email=${email}&newEmail=${newEmail}`))
        .done(resp => AGN.Lib.Form.get(this.el).updateHtml(resp));
    });
  });

  this.addAction({click: 'delete-entry'}, function () {
    const category = getCategory(this.el);
    const rowNode = Table.get(this.el).findRowByElement(this.el);

    Confirm.from('confirm-single-delete-modal', {category, data: rowNode.data}).done(() => {
      requestDeleteEntries(
        [getEntryData(this.el)],
        () => removeTableRow(this.el)
      )
    });
  });

  this.addAction({click: 'bulk-delete'}, function () {
    const table = Table.get(this.el);

    const rows = table.api.getSelectedRows();
    const category = getCategory(this.el);
    const entries = getSelectedRowsData(rows, category);

    Confirm.from('confirm-bulk-delete-modal', {category}).done(() => {
      requestDeleteEntries(
        entries,
        removedEntries => {
          const rowsForDeletion = rows.filter(r => removedEntries.find(e => e.id === r.id && e.companyId === r.companyId));
          removeRowsFromTable(rowsForDeletion, table);
        }
      )
    });
  });

  function requestDeleteEntries(entries, callback) {
    $.ajax({
      url: AGN.url(`/address-management/entries/delete.action?email=${email}`),
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(entries),
      dataType: 'json',
      success: resp => {
        if (resp.success) {
          callback(resp.data);
        }

        AGN.Lib.JsonMessages(resp.popups, true);
      },
      error: Messages.defaultError
    });
  }

  this.addAction({click: 'replace-email'}, function () {
    const $newEmail = $('#new-email-address');
    const email = $newEmail.val();

    if (AGN.Lib.Helpers.isValidEmail(email)) {
      Confirm.get(this.el).positive(email);
    } else {
      AGN.Lib.Form.showFieldError$($newEmail, t('import.columnMapping.error.invalidEmail'));
    }
  });

  this.addAction({click: 'single-replace'}, function () {
    createConfirmForReplace(email => {
      requestReplaceEmails(
        email,
        [getEntryData(this.el)],
        () => removeTableRow(this.el)
      );
    });
  });

  this.addAction({click: 'bulk-replace'}, function () {
    createConfirmForReplace(email => {
      const table = Table.get(this.el);

      const rows = table.api.getSelectedRows();
      const entries = getSelectedRowsData(rows, getCategory(this.el));

      requestReplaceEmails(
        email,
        entries,
        replacedEntries => {
          const rowsForDeletion = rows.filter(r => replacedEntries.find(e => e.id === r.id && e.companyId === r.companyId));
          removeRowsFromTable(rowsForDeletion, table);
        }
      );
    });
  });

  function removeRowsFromTable(rows, table) {
    const category = getCategory(table.$el);
    table.api.applyTransaction({remove: rows});
    decreaseCategoryEntriesCounter(category, rows.length);
  }

  function createConfirmForReplace(callback) {
    Confirm.from('replace-email-modal').done(callback)
  }

  function requestReplaceEmails(newEmail, entries, callback) {
    $.ajax({
      url: AGN.url(`/address-management/entries/replace.action?oldEmail=${email}&email=${newEmail}`),
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(entries),
      dataType: 'json',
      success: resp => {
        if (resp.success) {
          // In case if searching email still matches to new email we should prevent row remove
          if (!newEmail.toLowerCase().includes(email.toLowerCase())) {
            callback(resp.data);
          }
        }

        AGN.Lib.JsonMessages(resp.popups, true);
      },
      error: Messages.defaultError
    });
  }

  function removeTableRow($el) {
    const table = Table.get($el);
    const row = table?.findRowByElement($el);
    if (row) {
      const category = getCategory($el);
      table.api?.applyTransaction({remove: [row.data]});
      decreaseCategoryEntriesCounter(category);
    }
  }

  function getEntryData($el) {
    return {
      id: $el.data('entry-id'),
      companyId: $el.data('entry-cid'),
      category: getCategory($el)
    }
  }

  function getSelectedRowsData(rows, category) {
    return rows.map(r => {
      return {
        id: r.id,
        companyId: r.companyId,
        category: category
      }
    });
  }

  function decreaseCategoryEntriesCounter(category, value = 1) {
    const $btn = $(`[data-entry-category-tab=${category}]`);
    const $counter = $btn.find('span');
    const newCount = parseInt($counter.text()) - value;

    $counter.text(newCount);
    $btn.prop('disabled', newCount === 0);
  }

  function getCategory($el) {
    return $el.closest(`[${CATEGORY_ATTR_NAME}]`).attr(CATEGORY_ATTR_NAME);
  }
});
