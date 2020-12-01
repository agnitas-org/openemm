(function(){

  AGN.Lib.CoreInitializer.new('dark-theme', function() {
    if($('body').hasClass('dark-theme')) {
      $('iframe').each(function() {
        var $iframe = $(this);
        $iframe.on('load.iframe', function() {
          $iframe.contents().find('body').addClass('dark-theme');
        });
      });
    }
  });

})();