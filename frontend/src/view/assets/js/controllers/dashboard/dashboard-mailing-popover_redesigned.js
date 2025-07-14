(() => {
  const Popover = AGN.Lib.Popover;
  const Template = AGN.Lib.Template;
  const NO_PREVIEW_IMG = "/assets/core/images/facelift/no_preview.svg";
  const CONTENT_LOADER_PH = "---";
  const POPOVER_SHOW_DELAY = 200;

  class MailingPopover extends Popover {
    constructor(mailing, showLoader) {
      super();
      this.showLoader = showLoader;
      this.mailing = mailing;
      const thumbnail = AGN.url(this.mailingThumbnailSrc(mailing));
      const $content = this.content(this.showLoader ? this.loaderInfo : this.loadedInfo(mailing, thumbnail));
      this.popover = Popover.getOrCreate(this.mailing.$el, this.options($content));

      function generateGetBoundingClientRect(x = 0, y = 0) {
        return () => ({
          width: 0,
          height: 0,
          top: y,
          right: x,
          bottom: y,
          left: x,
        });
      }

      const updatePosition = ({clientX: x, clientY: y}) =>  {
        if (this.popover?._popper?.state?.elements?.reference) {
          this.popover._popper.state.elements.reference = virtualElement;
        }
        virtualElement.getBoundingClientRect = generateGetBoundingClientRect(x, y);
        this.popover?._popper?.update();
      }

      const virtualElement = {
        getBoundingClientRect: generateGetBoundingClientRect(),
      };

      this.mailing.$el.off('mouseenter').on('mouseenter', (e) => {
        if (this.disabled) {
          return;
        }
        const timeout = setTimeout(() => {
          this.popover.show();

          if (this.showLoader) {
            AGN.Lib.Loader.prevent();
            $
              .get(AGN.url("/calendar/mailingsPopoverInfo.action"), {mailingIds: this.mailing.id})
              .done(popoverInfo => {
                const thumbnail = AGN.url(this.mailingThumbnailSrc(popoverInfo[0]));
                let img = new Image();
                img.src = thumbnail;
                img.onload = () => {
                  this.$loadedContent = this.content(this.loadedInfo(popoverInfo[0], thumbnail));
                  $content.replaceWith(this.$loadedContent);
                  this.showLoader = false;
                };
            });
          }
        }, POPOVER_SHOW_DELAY);

        this.mailing.$el.on('mousemove', updatePosition);
        this.mailing.$el.on('mouseleave', ()=> {
          this.mailing.$el.off('mousemove', updatePosition);
          this.popover.hide();
          clearTimeout(timeout);
          if (this.$loadedContent?.length) {
            this.popover.setContent({
              '.popover-body': this.$loadedContent
            });
          }
        });
      });
    }

    loadedInfo(mailing, thumbnail) {
      return {
        ...mailing,
        isSent: this.mailing.isSent,
        thumbnailUrl: thumbnail,
        showLoader: false
      };
    }

    disable() {
      this.disabled = true;
    }

    enable() {
      this.disabled = false;
    }

    content(info) {
      const content = Template.dom("calendar-mailing-popover-content", info);
      AGN.runAll(content);
      return content;
    }

    get loaderInfo() {
      return {
        shortname: this.mailing.shortname,
        thumbnailUrl: AGN.url(NO_PREVIEW_IMG),
        subject: CONTENT_LOADER_PH,
        description: CONTENT_LOADER_PH,
        mailinglist: CONTENT_LOADER_PH,
        sendDate: CONTENT_LOADER_PH,
        sentCount: CONTENT_LOADER_PH,
        isSent: this.mailing.isSent,
        openers: CONTENT_LOADER_PH,
        clickers: CONTENT_LOADER_PH,
        showLoader: true,
      }
    }

    options(content) {
      return {
        content: content,
        html: true,
        trigger: 'manual',
        customClass: 'dashboard-mailing-popover',
        popperConfig: (defaultBsPopperConfig) => ({
          ...defaultBsPopperConfig,
          placement: 'bottom-start',
          modifiers: [
            ...(defaultBsPopperConfig.modifiers || []),
            {
              name: 'offset',
              options: {
                offset: ({ placement, reference, popper }) => {
                  return ['bottom-end', 'top-end'].includes(placement) ? [-15, 15] : [15, 15];
                },
              },
            },
            {
              name: 'flip',
              options: {
                enabled: true,
                fallbackPlacements: ['bottom-end', 'top-start', 'top-end'], // Try to flip to the opposite side if needed
              }
            }
          ],
        }),
      };
    }

    mailingThumbnailSrc(popoverInfo) {
      if (popoverInfo.post) {
        return "/assets/core/images/facelift/post_thumbnail.jpg";
      }
      if (popoverInfo.thumbnailComponentId) {
        return "/sc?compID=" + popoverInfo.thumbnailComponentId;
      }
      return NO_PREVIEW_IMG;
    }
  }

  AGN.Lib.Dashboard.MailingPopover = MailingPopover;
})(jQuery);
