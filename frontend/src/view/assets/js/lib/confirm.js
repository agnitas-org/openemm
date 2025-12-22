(() => {

  class Confirm {
    constructor($modal, deferred) {
      this.$modal = $modal;
      this.deferred = deferred || $.Deferred();
    }

    promise() {
      return this.deferred.promise();
    }

    // positive callback (user has accepted)
    // pass in the response from the server
    positive(resp) {
      this.deferred.resolve(resp);
      this.$modal.modal('hide');
      this.$modal.remove();
    }

    // negative callback (user has not accepted / closed the modal)
    negative(resp) {
      this.deferred.reject(resp);
      this.$modal.modal('hide');
      this.$modal.remove();
    }

    static get($needle) {
      const $confirm = Confirm.getWrapper($needle);
      return $confirm.data('_confirm');
    }

    static getWrapper($needle) {
      return $needle.closest('.modal');
    }

    static create(html, deferred) {
      const modal = AGN.Lib.Modal.create(html);
      const confirm = new Confirm(modal, deferred);

      // if response contains only errors scripts, then modal will not be created
      if (modal) {
        modal.data('_confirm', confirm);
      }

      return confirm.promise();
    }

    static from(template, conf = {}, deferred) {
      const modal = AGN.Lib.Modal.fromTemplate(template, conf);

      const confirm = new Confirm(modal, deferred);
      modal.data('_confirm', confirm);

      return confirm.promise();
    }

    static request(source) {
      const deferred = $.Deferred();
      let jqxhr;

      if (_.isString(source)) {
        jqxhr = $.get(source);
      } else {
        jqxhr = source;
      }

      jqxhr.done(resp => {
        const $modal = $(resp).all('.modal');

        if ($modal.length === 1) {
          const confirm = new Confirm($modal, deferred);
          $modal.data('_confirm', confirm);
          AGN.Lib.Modal.create($modal);
        } else if (typeof resp === 'object' && 'success' in resp) { // BooleanResponseDto
          AGN.Lib.JsonMessages(resp.popups, true);
          resp.success ? deferred.resolve() : deferred.reject();
        } else {
          AGN.Lib.Page.render(resp);
          deferred.reject();
        }
      }).fail(() => deferred.reject());

      return deferred.promise();
    }
  }

  AGN.Lib.Confirm = Confirm;

})();
