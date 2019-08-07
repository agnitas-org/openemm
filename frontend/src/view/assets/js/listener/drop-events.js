;(function(){

  $(document).on('dragster:enter', function(e){
    $(e.target).addClass('drag-over');
  });


  $(document).on('dragster:leave', function(e){
    $(e.target).removeClass('drag-over');
  });

})();
