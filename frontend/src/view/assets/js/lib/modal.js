(function(){

  var Modal;

  Modal = function() {

  };

  Modal.create = function(src, conf) {
    var $modal = null,
      $resp = null;

    if (src instanceof $) {
      $resp = src;
    } else {
      $resp = $(src);
    }

    var $modals = $resp.filter('.modal');
    var $scripts = $resp.filter('script');

    if ($modals) {
        // Multiple modals at once are not allowed
        $modal = $($modals[0]);
        $modal.data('_modal', conf);

        // Some scripts are placed outside modals (an elements having .model class), we have to put them in
        $scripts.each(function() {
            $(this).appendTo($modal);
        });

        // Construct a dialog
        $modal = $modal.modal();

        AGN.Lib.Controller.init($modal);
        AGN.runAll($modal);
    }
    return $modal;
  };

  Modal.createFromTemplate = function(conf, template) {
    template = AGN.Opt.Templates[template] || AGN.Opt.Templates['modal'];

    return Modal.create(_.template(template)(conf), conf);
  };

  // static method
  // gets the instance of a confirm
  Modal.get = function($needle) {
    var $modal = Modal.getWrapper($needle);
    return $modal.data('_modal');
  };

  // static method
  // gets the jquery wrapped modal element
  Modal.getWrapper = function($needle) {
    return $needle.closest('.modal');
  };

  AGN.Lib.Modal = Modal;

})();
