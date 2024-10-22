(function() {
  const Def = AGN.Lib.WM.Definitions;
  const Confirm = AGN.Lib.Confirm;

  const Dialogs = {
    Activation: activationDialog,
    Deactivation: deactivationDialog,
    confirmTestingStartStop: confirmTestingStartStop,
    confirmMailingDataTransfer: confirmMailingDataTransfer,
    confirmOwnWorkflowExpanding: confirmOwnWorkflowExpanding,
    createAutoOpt: createAutoOpt,
    confirmCopy: confirmCopy,
    connectionNotAllowed: connectionNotAllowed,
    createMailing: createMailing,
    mailingInUseDialog: mailingInUseDialog,
    simpleDialog: simpleDialog,
  };

  function activationDialog(mailings, isUnpause) {
    const confirm = Confirm.createFromTemplate({ rows: mailings.length }, 'activate-campaign-dialog');
    if (!isUnpause) {
      createDialogMailingsTable(mailings);
    }
    return confirm;
  }

  function createDialogMailingsTable(mailings) {
    const columns = getDialogMailingsTableCols();
    const options = getDialogMailingsTableOptions();
    new AGN.Lib.Table($('#activate-modal .table-wrapper'), columns, mailings, options);
  }

  function getDialogMailingsTableCols() {
    return [
      {
        field: "name",
        headerName: t('defaults.name'),
        filter: false,
        suppressMovable: true,
        sortable: true,
        sort: "asc",
        resizable: false,
        cellRenderer: 'NotEscapedStringCellRenderer'
      }
    ];
  }

  function getDialogMailingsTableOptions() {
    return {
      pagination: false,
      singleNameTable: true,
      showRecordsCount: "simple"
    };
  }

  function mailingInUseDialog(mailingNameTitle) {
    const message = t('workflow.mailing.copyQuestion');
    const btnText = `${t('workflow.button.copy')} ${t('workflow.defaults.and')} ${t('workflow.defaults.edit').toLowerCase()}`;
    return simpleDialog(mailingNameTitle, message, btnText);
  }

  function deactivationDialog() {
    const title = t('workflow.inactivating.title');
    const message = t('workflow.inactivating.question');
    return simpleDialog(title, message);
  }

  function confirmMailingDataTransfer(paramsToAsk) {
    return Confirm.from('mailing-data-transfer-modal', {Def: Def, paramsToAsk: paramsToAsk});
  }

  function confirmOwnWorkflowExpanding() {
    return Confirm.from('own-workflow-expanding-modal');
  }

  function createAutoOpt() {
    return Confirm.from('create-auto-opt-modal');
  }
  
  function confirmTestingStartStop(isStartTesting) {
    return Confirm.from('testing-modal', {startTesting: isStartTesting, shortname: Def.shortname});
  }

  function confirmCopy(hasContent) {
    return Confirm.from('workflow-copy-modal', {hasContent: hasContent === true});
  }

  function connectionNotAllowed() {
    const title = t('workflow.dialog.connectionNotAllowedTitle');
    const message = t('workflow.dialog.connectionNotAllowedMessage');
    return simpleDialog(title, message);
  }

  function simpleDialog(title, message, btnText = t('defaults.ok')) {
    return Confirm.from('workflow-simple-dialog-modal', {title, message, btnText});
  }

  function createMailing() {
    return Confirm.from('create-mailing-modal');
  }

  AGN.Lib.WM.Dialogs = Dialogs;
})();
