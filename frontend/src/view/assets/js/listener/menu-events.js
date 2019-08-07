(function(){

  var Action = AGN.Lib.Action,
      Menu   = AGN.Lib.Menu;

  Action.new({
    'click':  '.menu-close',
  }, function() {
    Menu.close();
  });

  Action.new({
    'click':  '.menu-open',
  }, function() {
    Menu.open();
  });

  Action.new({
    'click':  '.menu-open',
  }, function() {
    Menu.open();
  });

  $(document).on('click', 'main', function() {
    if (Menu.isOpen()) {
      Menu.close();
    }
  });

  $(document).on('')

})();
