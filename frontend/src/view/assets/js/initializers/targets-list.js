AGN.Lib.Controller.new('targets-list', function() {

  this.addAction({'change': 'check-favorites'}, function() { // favorite for company
    AGN.Lib.Loader.prevent();
    $.post(AGN.url(getFavoritesActionAddress(this.el)));
  });
  
  function getFavoritesActionAddress(el) {
    const targetId = el.data('target-id');

    if (el.is(':checked')) {
      return `/target/${targetId}/addToFavorites.action`;
    }
    return `/target/${targetId}/removeFromFavorites.action`;
  }

  this.addAction({'change': 'check-admin-favorites'}, function() { // favorite for user
    AGN.Lib.Loader.prevent();
    const targetId = this.el.data('target-id');
    const favorite = this.el.is(':checked');

    $.ajax({
      type: favorite ? 'POST' : 'DELETE',
      url: AGN.url(`/target/${targetId}/favorite.action`)
    });
  });
});
