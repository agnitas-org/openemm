(() => {
  const Popover = AGN.Lib.Popover;
  const Template = AGN.Lib.Template;

  class MailingPopover extends Popover {
    constructor(mailing) {
      super();
      this.mailing = mailing;
      this.mailing.thumbnailUrl = AGN.url(this.mailingThumbnailSrc);
      Popover.getOrCreate(mailing.$el, {...this.options, content: this.content});
    }

    get content() {
      return Template.dom("calendar-mailing-popover-content", this.mailing);
    }

    get options() {
      return {
        trigger: 'hover',
        html: true,
        customClass: 'dashboard-mailing-popover',
        popperConfig: {
          placement: "bottom",
        },
      };
    }

    get mailingThumbnailSrc() {
      if (this.mailing.post) {
        return "/assets/core/images/facelift/post_thumbnail.jpg";
      }
      if (this.mailing.thumbnailComponentId) {
        return "/sc?compID=" + this.mailing.thumbnailComponentId;
      }
      return "/assets/core/images/facelift/no_preview.svg";
    }
  }

  AGN.Lib.Dashboard.MailingPopoverUxUpdateRollback = MailingPopover;
})(jQuery);
