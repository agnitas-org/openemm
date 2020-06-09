/*doc
---
title: Load Directive
name: load-directive
parent: directives
---

The `load` directive can be used to load an area designated by a jquery selector in `data-load-target` via an ajax request. If `data-load-interval` is set the area will be updated until the server sends a response with a `data-load-stop` attribute.

`<div data-load="urlToLoad" data-load-target="#renderTarget" data-load-interval="5000"></div>`
*/

(function(){

  var Load = AGN.Lib.Load;

  AGN.Lib.CoreInitializer.new('load', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-load]'), function(el) {
      // queue after rendering has finished
      window.setTimeout(function() {
        Load.load($(el));
      }, 100)
    });
  });

})();
