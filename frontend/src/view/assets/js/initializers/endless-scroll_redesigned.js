/*doc
---
title: Endless scroll
name: endless-scroll
category: Components - Endless scroll
---

The `endless-scroll` directive can be used for loading records when the user scrolls to the end.

To use it, you need to specify the `data-endless-scroll="urlToLoad"` attribute for the scrolling element. A request with the `page` parameter will be sent to the specified URL, which will increase with each subsequent upload.

to stop further loading, the response must contain an element with the `data-endless-scroll-stop` attribute.

Basically, elements are loaded into the first div which is located inside `data-endless-scroll`, but if you need to specify a specific block, then it needs to be given the `data-endless-scroll-content` attribute.

If you want to display some loader when data loads, you need to add attribute `data-endless-scroll-opts="loader: 'loader-selector'"` to the element.

```htmlexample
<div class="tile" style="height: 200px">
    <div class="tile-header">
        <h1 class="tile-title">Scrollable tile</h1>
    </div>
    <div class="tile-body js-scrollable" data-endless-scroll="https://api.thecatapi.com/v1/images/search?limit=200" data-endless-scroll-opts="loader: '#endless-scroll-loader'">
        <div></div>
    </div>

    <div id="endless-scroll-loader" class="tile-footer tile-footer--loader hidden">
        <i class="icon icon-spinner icon-pulse"></i>
    </div>
</div>
```
*/

AGN.Lib.CoreInitializer.new('endless-scroll',  function ($scope = $(document)) {

  $scope.find('[data-endless-scroll]').each(function () {
    const $el = $(this);
    let options = {};

    if ( $el.is('[data-endless-scroll-opts]') ) {
      const elementOptions = AGN.Lib.Helpers.objFromString($el.data('endless-scroll-opts'));
      options = _.extend(options, elementOptions);
    }

    new AGN.Lib.EndlessScroll($el, $el.data('endless-scroll'), options);
  });

});
