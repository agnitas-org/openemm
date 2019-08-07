(function(){

  var Action = AGN.Lib.Action;

  // toggle password to input
  Action.new({
    'click':  '[data-password]',
  }, function() {
    var target  = this.el.data('password'),
        text    = this.el.data('password-toggle'),
        $target = $(target);

    this.el.data('password-toggle', this.el.text());
    this.el.text(text);

    if ($target.prop('type') === 'password') {
        $target.prop('type', 'text');
    } else {
        $target.prop('type', 'password');
    }

    this.event.preventDefault();

  });

})();
