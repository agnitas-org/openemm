/*doc
---
title: Load
name: load
category: Components - Load
---

The `load` directive can be used to load an area designated by a jquery selector in `[data-load-target]` via an ajax request.
If `[data-load-interval]` is set the area will be updated until the server sends a response with a `[data-load-stop]` attribute.

`<div data-load="urlToLoad" data-load-target="#renderTarget" data-load-interval="5000"></div>`
*/
AGN.Lib.CoreInitializer.new('load', function ($scope = $(document)) {
  _.each($scope.find('[data-load]'), el => {
    // queue after rendering has finished
    window.setTimeout(() => AGN.Lib.Load.load($(el)), 100)
  });
});
