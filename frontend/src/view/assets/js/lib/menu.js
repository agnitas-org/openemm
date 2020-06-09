(function(){

  var Menu;

  Menu = {
    init: function() {
      var conf = AGN.Lib.Storage.get('menu_state') || 'open';

      if  (conf == 'open') {
        this.openDirectly();
      } else {
        this.closeDirectly();
      }

      this.updateInits();
    },

    isOpen: function() {
      return $('body').hasClass('menu-is-active');
    },

    openDirectly: function() {
      AGN.Lib.Helpers.disableCSSAnimations();
      this.open();
      AGN.Lib.Helpers.enableCSSAnimations();
    },

    closeDirectly: function() {
      AGN.Lib.Helpers.disableCSSAnimations();
      this.close();
      AGN.Lib.Helpers.enableCSSAnimations();
    },

    close: function() {
      AGN.Lib.Storage.set('menu_state', 'close');
      $('body').removeClass('menu-is-active');
      this.updateInits();
    },

    open: function() {
      AGN.Lib.Storage.set('menu_state', 'open');
      $('body').addClass('menu-is-active');
      this.updateInits();
    },

    updateInits: function() {
      window.setTimeout(function() {
        AGN.Lib.CoreInitializer.run(['truncate', 'scrollable', 'dropdown-expand']);
      }, 600);
    }
  };

  AGN.Lib.Menu = Menu;

})();
