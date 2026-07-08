AGN.Lib.Controller.new('mailloop-reply-view', function () {

  this.addDomInitializer('mailloop-reply-view', function () {
    const $newBadge = AGN.Lib.Template.dom('mailoop-reply-new-status-badge');
    $(`[data-reply-status-badge="${this.config.id}"]`).replaceWith($newBadge);
    AGN.Lib.CoreInitializer.run('tooltip', $newBadge);
  });

});
