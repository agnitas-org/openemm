(() => {

  class MailingsTile extends AGN.Lib.Dashboard.DraggableTile {

    static MAILING_CARD_SELECTOR = '.mailing-card';
    static ID = 'mailings';

    constructor(controller) {
      super(controller);
      const self = this;

      controller.addAction({'click': 'delete-recent-mailing'}, function() {
        self.deleteAction(this);
      });

      controller.addAction({'click': 'copy-recent-mailing'}, function() {
        self.copyAction(this);
      });
    }

    copyAction(action) {
      action.event.preventDefault();
      const mailingId = this.getRecentMailingId($(action.el));
      AGN.Lib.Page.reload(AGN.url('/mailing/{mailing-id}/copy.action'.replace('{mailing-id}', mailingId)));
    }

    deleteAction(action) {
      action.event.preventDefault();
      const $btn = $(action.el)
      $
        .get({
          url: AGN.url(`/mailing/deleteMailings.action?bulkIds=${this.getRecentMailingId($btn)}`),
          data: {toPage: 'dashboard'}
        })
        .done(resp => AGN.Lib.Confirm.create(resp)
          .done((resp) => {
            AGN.Lib.RenderMessages($(resp));
            $btn.closest(MailingsTile.MAILING_CARD_SELECTOR).remove()
          }));
    }

    getRecentMailingId($btn) {
      return $btn.closest(MailingsTile.MAILING_CARD_SELECTOR).data('mailing-id');
    }
  }
  AGN.Lib.Dashboard.MailingsTile = MailingsTile;
})();
