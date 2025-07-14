(() => {

  const MAILING_CARD_SELECTOR = '.mailing-card';

  class MailingsTile extends AGN.Lib.Dashboard.DraggableTile {

    static ID = 'mailings';

    constructor(controller) {
      super(controller);
      const self = this;

      controller.addAction({click: 'delete-recent-mailing'}, function() {
        self.deleteAction(this);
      });

      controller.addAction({click: 'copy-recent-mailing'}, function() {
        self.copyAction(this);
      });

      controller.addAction({change: 'change-mailings-preview'}, function () {
        const $tile = this.el.closest('.tile');
        const previewEnabled = !this.el.is(':checked');

        AGN.Lib.Storage.set('dashboard-mailings-preview', previewEnabled);
        AGN.Lib.CoreInitializer.run('truncated-text-popover');
        AGN.Lib.Scrollbar.get($tile.find('.tile-body')).update();
        AGN.Lib.Popover.toggleState($tile, !previewEnabled);
      });
    }

    copyAction(action) {
      action.event.preventDefault();
      const mailingId = this.getRecentMailingId($(action.el));
      AGN.Lib.Page.reload(AGN.url(`/mailing/${mailingId}/copy.action`));
    }

    deleteAction(action) {
      action.event.preventDefault();
      const $btn = $(action.el)
      $
        .get({
          url: AGN.url('/mailing/deleteMailings.action'),
          data: {
            bulkIds: this.getRecentMailingId($btn),
            toPage: 'dashboard'
          }
        })
        .done(resp => {
          AGN.Lib.Confirm.create(resp).done(resp => {
            AGN.Lib.RenderMessages($(resp));
            $btn.closest(MAILING_CARD_SELECTOR).remove();
          })
        });
    }

    getRecentMailingId($btn) {
      return $btn.closest(MAILING_CARD_SELECTOR).data('mailing-id');
    }

    get templateOptions() {
      return {
        ...super.templateOptions,
        isPreviewEnabled: AGN.Lib.Storage.get('dashboard-mailings-preview') || false
      };
    }
  }
  AGN.Lib.Dashboard.MailingsTile = MailingsTile;
})();
