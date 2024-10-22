(function () {
  const Popover = AGN.Lib.Popover;
  
  class NodePopover {
    constructor(node) {
      this.node = node;
      this.getMailingThumbnailWithCache = _.memoize(this.getMailingThumbnail.bind(this));
      this.#initPopover();
    }

    #initPopover() {
      this.getPopoverContent().then(content => this.createPopover(content));
      this.setEnabled(true);
    }
    
    createPopover(content) {
      if (!content) {
        return;
      }
      this.popover = Popover.getOrCreate(this.node.get$(), {
        trigger: 'hover',
        html: true,
        content
      });
    }

    remove() {
      if (this.popover) {
        this.popover.hide();
      }
      Popover.remove(this.node.get$());
      this.popover = null;
    }
    
    update() {
      this.remove();
      this.#initPopover();
    }

    async getPopoverContent() {
      const thumbnailEl = await this.getThumbnail();
      const comment = this.node.getComment();
      if (!thumbnailEl && !comment?.trim()) {
        return null;
      }
      return $('<div>')
        .addClass('d-flex flex-column align-items-center')
        .append(thumbnailEl)
        .append($('<div>').text(comment));
    }

    async getThumbnail() {
      const mailingId = this.node.getData().mailingId;
      if (!this.node.isMailingNode() || !mailingId) {
        return ''
      }
      return this.getMailingThumbnailWithCache(mailingId);
    }

    async getMailingThumbnail(mailingId) {
      const thumbnailSrc = await this.getMailingThumbnailSrc(mailingId);
      if (!thumbnailSrc) {
        return '';
      }
      const img = new Image();
      img.src = thumbnailSrc;
      img.style.width = '200px';
      return img;
    }

    async getMailingThumbnailSrc(mailingId) {
      const componentId = await $.get(AGN.url(`/workflow/mailing/${mailingId}/thumbnail.action`));
      return componentId > 0 ? AGN.url('/sc?compID=' + componentId) : '';
    }

    setEnabled(isEnabled) {
      this.isEnabled = isEnabled;
      if (!this.popover) {
        return;
      }
      if (isEnabled) {
        this.popover.enable();
      } else {
        this.popover.disable();
      }
    }
  }
  
  AGN.Lib.WM.NodePopover = NodePopover;
})();
