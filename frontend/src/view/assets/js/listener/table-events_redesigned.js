(() => {

  const Helpers = AGN.Lib.Helpers;
  const Confirm = AGN.Lib.Confirm;
  const Form = AGN.Lib.Form;

  // avoid loading the Single View
  // when inputs or links are clicked
  $(document).on('click', '.js-table a, .js-table input, .input-table input[type="checkbox"], .js-table select, .js-table .select2, .js-table button, .js-table td:has(input[type="checkbox"]), .js-table .disabled', function (e) {
    e.stopPropagation();
  });

  $(document).on('click', '[data-table-sort]', function (e) {
    const $this = $(this);
    // not <a> was clicked, so skip
    if (e.target !== $this.get(0)) {
      return;
    }

    const href = $this.attr('href');

    if (Form.getWrapper($this).exists()) {
      const form = Form.get($this);
      const params = Helpers.paramsFromUrl(href);

      // feed the link params to the form
      _.each(params, (field, fieldName) => form.setValue(fieldName, field));

      form.submit();
    } else {
      $.get(href).done(resp => AGN.Lib.Page.render(resp, false));
    }

    e.preventDefault();
  });

  $(document).on('click', '[data-paginate]', function (e) {
    const $this = $(this);
    const form = Form.get($this);
    const params = Helpers.paramsFromUrl($this.attr('href'));

    // feed the link params to the form
    _.each(params, (field, fieldName) => form.setValue(fieldName, field));

    form.submit();
    e.preventDefault();

    const $tile = $this.closest('.tile');
    if ($tile.exists()) {
      const top = $tile.offset().top;
      if (window.scrollY > top) {
        window.scrollTo(0, top - 150);
      }
    } else {
      window.scrollTo(0, 0);
    }
  });

  $(document).on('click', '.js-row-delete', function (e) {
    const $this = $(this);
    const href = $this.is('button') ? $this.data('url') : $this.attr('href');
    const form = Form.get($this);

    if (href) {
      $.get(href).done(resp => {
        const $modal = $(resp).all('.modal');

        if ($modal.exists()) {
          Confirm.create(resp).done(resp => {
            $this.closest('tr').remove();
            form.updateHtml(resp);
          });
        } else {
          form.updateHtml(resp);
        }
      });
    }

    e.preventDefault();
  });

  // load the resource when
  // a table row is clicked
  $(document).on('click', '.js-table tr:not(.disabled)', function (e) {
    if (AGN.Lib.Helpers.isMobileView()) {
      return;
    }

    const $row = $(this);
    const target = $row.data('link');

    if (typeof (target) !== 'undefined' && target !== '#' && !$row.closest('.table-row-wrapper').exists()) {
      showRemoteModal($row, target);
    }
  });

  function showRemoteModal($el, href) {
    const form = Form.get($el)
    $.get(href).done(resp => {
      const $modal = $(resp).all('.modal');
      if ($modal.exists()) {
        Confirm.create(resp).done(resp => form.updateHtml(resp));
      } else {
        form.updateHtml(resp);
      }
    });
  }

  $(document).on('click', 'td:has(input[type="checkbox"])', function (e) {
    e.preventDefault();

    const $targets = $(this).find('input[type="checkbox"]');

    _.each($targets, target => {
      const $target = $(target);
      if (!$target.prop('disabled')) {
        $target.prop('checked', !$target.prop('checked'));
        $target.trigger('change');
      }
    });
  });

  $(document).on('click', '.disabled', function (e) {
    e.preventDefault();
  });

  $(document).on('click', '[data-toggle-table-truncation]', function () {
    const $tableWrapper = $(this).closest('.table-wrapper');
    const $table = $tableWrapper.find('table, .ag-body');
    $table.toggleClass('no-truncate');
    AGN.Lib.CoreInitializer.run(['truncated-text-popover', 'scrollable'], $tableWrapper);
    AGN.Lib.Storage.set('truncate-table-text', !$table.hasClass('no-truncate'));
  });

  AGN.Lib.Action.new({change: '[data-preview-table]'}, function () {
    AGN.Lib.PreviewTable.toggle(this.el);
    AGN.Lib.CoreInitializer.run(['scrollable', 'truncated-text-popover', 'table', 'table-cols-resizer'], this.el.closest('.table-wrapper'));
  });

  $(document).on('change', '[data-bulk-checkbox]', function () {
    const $tableWrapper = $(this).closest('.table-wrapper');
    const $mainCheckbox = $tableWrapper.find('[data-bulk-checkboxes]');
    const $checkboxes = $tableWrapper.find('[data-bulk-checkbox]:not(:disabled)');

    const checkedCount = $checkboxes.filter(':checked').length;
    const totalCheckboxes = $checkboxes.length;

    $mainCheckbox.prop('indeterminate', checkedCount > 0 && checkedCount < totalCheckboxes);
    $mainCheckbox.prop('checked', checkedCount === totalCheckboxes);

    updateTableBulkActions($tableWrapper);
  });

  $(document).on('change', '[data-bulk-checkboxes]', function () {
    const $el = $(this);
    const $checkboxes = $el.closest('.table-wrapper').find('[data-bulk-checkbox]:not(:disabled)');

    $checkboxes.prop('checked', $el.prop('checked')).trigger('change');
  });

  $(document).on('table:updateBulkActions', '.table-wrapper', function (e, selectedRowsCount, availableActions) {
    updateTableBulkActions($(this), selectedRowsCount, availableActions);
  });

  function updateTableBulkActions($tableWrapper, selectedRowsCount, availableActions) {
    const $bulkActions = $findBulkActions($tableWrapper);
    if (!$bulkActions.exists()) {
      return;
    }

    if (selectedRowsCount === null || selectedRowsCount === undefined) {
      const $selectedRows = $tableWrapper.find('[data-bulk-checkbox]:checked').closest('tr');

      availableActions = findAllowedBulkActionsNames($selectedRows);
      selectedRowsCount = $selectedRows.length;
    }

    $bulkActions.find('.bulk-actions__selected span').text(selectedRowsCount);
    updateBulkActionsButtonsVisibility($bulkActions, availableActions || []);
    $bulkActions.toggleClass('hidden', selectedRowsCount === 0);
  }

  function findAllowedBulkActionsNames($rows) {
    return $rows
      .find('[data-bulk-action]')
      .filter((_, btn) => !$(btn).prop('disabled') && !$(btn).hasClass('disabled'))
      .map((_, btn) => $(btn).data('bulk-action'))
      .get();
  }

  function updateBulkActionsButtonsVisibility($bulkActions, availableActions) {
    if (!availableActions.length) {
      return;
    }

    const actions = new Set(availableActions);

    $bulkActions.find('[data-bulk-action]').each(function () {
      const $btn = $(this);
      $btn.toggleClass('hidden', !actions.has($btn.data('bulk-action')));
    });
  }

  function $findBulkActions($tableWrapper) {
    return $tableWrapper.find('.bulk-actions');
  }

})();
