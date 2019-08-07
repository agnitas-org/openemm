(function() {
  var History = window.history;

  $(document).on('click', 'a[href^="#"]', function(e) {
    if (e.isDefaultPrevented()) {
      return;
    }
    e.preventDefault();

    setHash($(this).attr('href'));
  });

  $(document).on('click', '[data-one-time]', function() {
    var $this = $(this);

    setTimeout(function() {
      $this.attr('disabled', 'disabled');
      if ($this.is('a')) {
        $this.removeAttr('href');
      }
    }, 1);
  });

  function setHash(hash) {
    if (hash === '#') {
      // Never set an empty hash — such links used to prevent page reloading.
      return;
    }

    var address = window.location.href;
    var hashPos = address.indexOf('#');
    if (hashPos >= 0) {
      address = address.substring(0, hashPos);
    }
    History.replaceState(History.state || {}, document.title, address + hash);
  }

})();
