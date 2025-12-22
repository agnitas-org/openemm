AGN.Lib.Controller.new('navbar', function() {

  this.addAction({
    mouseenter: 'expand-navbar-tab', mouseleave: 'expand-navbar-tab'
  }, function() {
    this.el.toggleClass('hovered');
  });

});