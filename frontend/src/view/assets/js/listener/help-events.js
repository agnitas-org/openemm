(function(){

  var Help = AGN.Lib.Help;

  $(document).on('click', '[data-help]', function(e) {
    Help.show($(this));
    e.preventDefault();
  });

})();
