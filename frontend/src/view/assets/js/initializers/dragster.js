;(function(){

  AGN.Initializers.Dragster = function($scope) {

    $('.dropzone').each(function() {
      new Dragster(this)
    })
  }

})();
