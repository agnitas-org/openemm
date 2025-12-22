/*doc
---
title: Scrollbar
name: scrollbar
category: Components - Scrollbar
---

Adding the `.js-scrollable` class to an element will create perfect scrollbar for this container.

```htmlexample
  <div class="tile" style="height: 200px">
      <div class="tile-header">
          <h1 class="tile-title text-truncate">Tile</h1>
      </div>

      <div class="tile-body js-scrollable">
          <h1>Content1</h1>
          <h1>Content2</h1>
          <h1>Content3</h1>
          <h1>Content4</h1>
          <h1>Content5</h1>
          <h1>Content6</h1>
          <h1>Content7</h1>
          <h1>Content8</h1>
          <h1>Content9</h1>
          <h1>Content10</h1>
      </div>
  </div>
```

*/

;(() => {

  const Scrollbar = AGN.Lib.Scrollbar;

  AGN.Lib.CoreInitializer.new('scrollable', ['table'], function($scope = $(document)) {
    _.each($scope.find('.js-scrollable, .table-wrapper__body, .ag-body-viewport'), el => {
      const $el = $(el);
      const scrollbar = Scrollbar.get($el, false);

      if (AGN.Lib.Helpers.isMobileView()) {
        scrollbar?.destroy();
        return;
      }

      if (scrollbar) {
        scrollbar.update();
      } else {
        new Scrollbar($el);
      }
    });
  });

})();
