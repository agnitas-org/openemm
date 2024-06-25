(function(){

  var Confirm;

  Confirm = function($modal, deferred) {
    this.$modal = $modal;
    this.deferred = deferred || $.Deferred();
  };

  // static method
  // gets the instance of a confirm
  Confirm.get = function($needle) {
    var $confirm = Confirm.getWrapper($needle);

    return $confirm.data('_confirm');
  };

  // static method
  // create a new confirm
  Confirm.create = function(html, deferred) {
    var confirm,
        modal = AGN.Lib.Modal.create(html);

    confirm = new Confirm(modal, deferred);

    // if response contains only errors scripts, then modal will not be created
    if (modal) {
      modal.data('_confirm', confirm);
    }

    return confirm.promise();
  };

  // static method
  // create a new confirm
  Confirm.createFromTemplate = function(conf, template, deferred) {
    var confirm,
        modal = AGN.Lib.Modal.fromTemplate(template, conf);

    confirm = new Confirm(modal, deferred);
    modal.data('_confirm', confirm);

    return confirm.promise();
  };

  Confirm.request = function(source) {
    var deferred = $.Deferred();
    var jqxhr;

    if (_.isString(source)) {
      jqxhr = $.get(source);
    } else {
      jqxhr = source;
    }

    jqxhr.done(function(resp) {
      var $resp = $(resp),
        $modal = $resp.all('.modal');

      if ($modal.length === 1) {
        var confirm = new Confirm($modal, deferred);
        $modal.data('_confirm', confirm);
        AGN.Lib.Modal.create($modal);
      } else {
        AGN.Lib.Page.render(resp);
        deferred.reject();
      }
    }).fail(function() {
      deferred.reject();
    });

    return deferred.promise();
  };

  // static method
  // gets the jquery wrapped modal element
  Confirm.getWrapper = function($needle) {
    var $confirm;

    $confirm = $($needle.data('confirm-target'));

    if ( $confirm.length == 0 ) {
       $confirm = $needle.closest('.modal');
    }

    return $confirm;
  };

  Confirm.prototype.promise = function() {
    return this.deferred.promise();
  };

  // positive callback (user has accepted)
  // pass in the response from the server
  Confirm.prototype.positive = function(resp) {
    this.deferred.resolve(resp);
    this.$modal.modal('hide');
    this.$modal.remove();
  };

  // negative callback (user has not accepted / closed the modal)
  Confirm.prototype.negative = function(resp) {
    this.deferred.reject(resp);
    this.$modal.modal('hide');
    this.$modal.remove();
  };


  AGN.Lib.Confirm = Confirm;

})();
