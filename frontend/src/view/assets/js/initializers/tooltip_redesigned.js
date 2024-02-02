/*doc
---
title: Tooltip Directive
name: tooltip-directive
parent: directives
---

By setting `data-tooltip` a tooltip will automatically be generated for the element, which is shown on hover. You can pass options via the `data-tooltip-options` attribute.

Available options can be looked up on the <a href="http://getbootstrap.com/javascript/#tooltips-options">Bootstrap Tooltip Doc</a>

Use `data-tooltip-style` attribute to append CSS-classes to root tooltip div element.

Use `data-tooltip-src` attribute to generate a tooltip content (title option) from a mustache template.

```htmlexample
  <div class="form-group">
    <div class="col-sm-4">
      <label class="control-label">First name</label>
    </div>
    <div class="col-sm-4">
      <input type="text" class="form-control" name="firstname" data-tooltip="" data-tooltip-src="custom-tooltip-1" data-tooltip-options="html: true"/>
    </div>
  </div>

  <div class="form-group">
    <div class="col-sm-4">
      <label class="control-label">Last name</label>
    </div>
    <div class="col-sm-4">
      <input type="text" class="form-control" name="lastname" data-tooltip="" data-tooltip-src="custom-tooltip-1" data-tooltip-options="html: true"/>
    </div>
  </div>

  <script id="custom-tooltip-1" type="text/x-mustache-template">
    <span>A tooltip for <strong>{{= element.attr('name') }}</strong></span>
  </script>
```
*/


(function(){

  const Tooltip = AGN.Lib.Tooltip;

  AGN.Lib.CoreInitializer.new('tooltip', function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    $('.tooltip').remove();

    $scope.find('[data-tooltip], [data-tooltip-help]').each(function() {
      const $e = $(this);
      Tooltip.create($e, Tooltip.options($e));
    });
  });

})();
