(function(){

  var init,
      trigger,
      initClasses;



  init = function() {
     var $view = $('[data-view]:checked'),
         conf = AGN.Lib.Storage.get('view_' + $view.data('view'));

    if (conf) {
        $('[data-view][value="' + conf.view + '"]').prop('checked', true);
    } else {
      conf = {
        view: $view.val()
      }
    }

    initClasses(conf.view);
  }

  trigger = function($trigger) {
    var type = $trigger.val();

    initClasses(type);
    AGN.Lib.Storage.set('view_' + $trigger.data('view'), { view: type });
  }

  initClasses = function(type) {
    _.each($('[data-view-' + type + ']'), function(target) {
      var $target = $(target);
      $target.attr('class', $target.data('view-' + type));
    })

    // fix issues w/ element rerendering
    $(window).trigger('resize');
    AGN.Lib.CoreInitializer.run('load');
  }


  AGN.Lib.View = {
    init: init,
    trigger: trigger
  };

})();
