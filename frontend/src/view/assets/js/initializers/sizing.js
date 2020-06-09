/*doc
---
title: Full Screen View
name: fullscreen
category: Javascripts - Full Screen View
---

A container can be stretched to fill the available screen area by using the `data-sizing` statement.

```htmlexample
<div class="tile" data-sizing="container">
  <div class="tile-header" data-sizing="top">
    <h2 class="headline">
      Top Element
    </h2>
  </div>

  <div class="tile-content" >

    <div class="row">
      <div class="col-sm-8">
        <div data-sizing="scroll">
          <p style="font-size: 200px;">Scrollable Element which fills the available screen area</p>
        </div>
      </div>

      <div class="col-sm-4">
        <div data-sizing="scroll">
          <p style="font-size: 200px;">Scrollable Element which fills the available screen area</p>
        </div>
      </div>
    </div>

  </div>

  <div class="tile-footer" data-sizing="bottom">
    <p>Bottom Element</p>
  </div>
</div>
```
*/

AGN.Lib.CoreInitializer.new('sizing', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  var windowHeight = $(window).height();

  _.each($scope.find('[data-sizing="container"]'), function(cont) {
    var $cont = $(cont),
        $topEls = $cont.find('[data-sizing="top"]'),
        $bottomEls = $cont.find('[data-sizing="bottom"]'),
        $els = $cont.find('[data-sizing="scroll"]'),
        offsetCont, topElsHeight, bottomElsHeight, elsHeight, offsetBottom;

      offsetCont = $cont.offset().top;

      // sum height of all top elements
      topElsHeight = _.reduce(
        _.map($topEls, function(el) {
         return $(el).outerHeight();
        }), function(sum, addend) {
        return sum + addend;
      }) || 0;

      // sum height of all bottom elements
      bottomElsHeight = _.reduce(
        _.map($bottomEls, function(el) {
         return $(el).outerHeight();
        }), function(sum, addend) {
        return sum + addend;
      }) || 0;

      offsetBottom = $cont.data('sizing-offset') || 20;

      elsHeight = windowHeight - offsetCont - topElsHeight - bottomElsHeight - offsetBottom;

      _.each($els, function(el) {
        var $el = $(el),
            offset;

        offset = $el.data('sizing-offset') || 0;

        $el.css({
          'height': elsHeight - offset,
          'overflow-y': 'auto'
        });
        $el.trigger('scrollTo');
      });
  })

});
