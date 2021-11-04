AGN.Lib.Controller.new('targets-list', function() {

  this.addAction({'change': 'check-favorites'}, function() {
    AGN.Lib.Loader.prevent();
    
    $.ajax({
      type: 'GET',
      url: AGN.url(getFavoritesActionAddress(this.el))
    });
  });
  
  function getFavoritesActionAddress(el) {
    var targetId = el.data('target-id');
    var favorite = el.is(':checked');
    return '/target/' + targetId + '/' + (favorite ? 'addToFavorites' : 'removeFromFavorites') + '.action';
  }
});
