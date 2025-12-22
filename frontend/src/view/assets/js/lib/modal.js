(() => {

  class Modal {
    static create(src, conf) {
      let $modal = null,
        $resp;

      if (src instanceof $) {
        $resp = src;
      } else {
        $resp = $(src);
      }

      const $modals = $resp.filter('.modal');
      const $scripts = $resp.filter('script');

      if ($modals.exists()) {
        // Multiple modals at once are not allowed
        $modal = $($modals[0]);
        $modal.data('_modal', conf);

        // Some scripts are placed outside modals (an elements having .model class), we have to put them in
        $scripts.each(function () {
          $(this).appendTo($modal);
        });

        // Construct a dialog.
        // Disable focus as it was causing issues with not being able to focus on the search field in select2
        new bootstrap.Modal($modal, {focus: false}).show();

        AGN.Lib.RenderMessages($resp);
        AGN.Lib.Controller.init($modal);
        AGN.runAll($modal);
      } else {
        AGN.Lib.RenderMessages($resp);
      }

      return $modal;
    }

    static fromTemplate(template, conf = {}) {
      return Modal.create(AGN.Lib.Template.text(template, conf), conf);
    }

    // gets the instance of a confirm
    static get($needle) {
      const $modal = Modal.getWrapper($needle);
      return $modal.data('_modal');
    }

    // gets the jquery wrapped modal element
    static getWrapper($needle) {
      return $needle.closest('.modal');
    }

    static getInstance($needle) {
      const $modal = Modal.getWrapper($needle);
      return bootstrap.Modal.getInstance($modal);
    }
  }

  AGN.Lib.Modal = Modal;

})();
