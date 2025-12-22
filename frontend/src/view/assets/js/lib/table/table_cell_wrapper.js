(() => {

  const Template = AGN.Lib.Template;

  AGN.Lib.TableCellWrapper = function (viewLinkTemplate, viewInModal, isRestoreMode, data) {
    if (viewLinkTemplate && !isRestoreMode) {
      const cell = document.createElement('a');

      if (!Template.exists(viewLinkTemplate)) {
        Template.register(viewLinkTemplate, viewLinkTemplate);
      }

      cell.href = AGN.url(Template.text(viewLinkTemplate, data).trim());
      cell.classList.add('ag-grid-cell-link');

      if (viewInModal) {
        cell.setAttribute('data-confirm', '')
      }

      return cell;
    }

    return document.createElement('div');
  };
})();
