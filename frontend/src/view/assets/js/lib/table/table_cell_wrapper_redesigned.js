(() => {

  const Template = AGN.Lib.Template;

  AGN.Lib.TableCellWrapper = function (options, data) {
    if (options?.viewLinkTemplate && !options.isRestoreMode()) {
      const cell = document.createElement('a');

      if (!Template.exists(options.viewLinkTemplate)) {
        Template.register(options.viewLinkTemplate, options.viewLinkTemplate);
      }

      cell.href = AGN.url(Template.text(options.viewLinkTemplate, data).trim());
      cell.classList.add('ag-grid-cell-link');

      if (options.viewInModal) {
        cell.setAttribute('data-confirm', '')
      }

      return cell;
    }

    return document.createElement('div');
  };
})();
