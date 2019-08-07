(function(){

  var Loader = {},
      hiding,
      requestCount = 0,
      prevent = false;

  Loader.show = function() {
    window.clearTimeout(hiding);

    if (!prevent) {
      requestCount += 1;
      $('.loader').show();
    } else {
      prevent = false;
    }
  }

  Loader.hide = function() {
    requestCount -= 1;

    if (requestCount <= 0) {
      hiding = window.setTimeout(function() {
        $('.loader').hide();
      }, 5);

      requestCount = 0;
    }
  }

  Loader.prevent = function() {
    prevent = true;
  }


  AGN.Lib.Loader = Loader;


})();
