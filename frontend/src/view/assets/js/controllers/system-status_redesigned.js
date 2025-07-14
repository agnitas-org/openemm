AGN.Lib.Controller.new('system-status', function () {

  this.addAction({click: 'cancel-imports'}, function () {
    AGN.Lib.Confirm.from('cancel-running-imports-modal')
      .done(() => {
        $.post(AGN.url('/serverstatus/killRunningImports.action'))
          .fail(() => AGN.Lib.Messages.defaultError());
      });
  });
});