/*doc
---
title: Tooltips
name: tooltips
category: Components - Tooltips
---
*/

/*doc
---
title: Tooltip
name: tooltip
parent: tooltips
---

By setting `data-tooltip` a tooltip will automatically be generated for the element, which is shown on hover. You can pass options via the `data-tooltip-options` attribute.

Available options can be looked up on the <a href="https://getbootstrap.com/docs/5.2/components/tooltips/" target="_blank"><i>Bootstrap Tooltip docs</i></a>.

Use `data-tooltip-style` attribute to append CSS-classes to root tooltip div element.

Use `data-tooltip-src` attribute to generate a tooltip content (title option) from a mustache template.

```htmlexample
<div class="d-flex gap-3">
  <div>
      <label class="form-label">First name</label>
      <input type="text" class="form-control" name="firstname" data-tooltip="" data-tooltip-src="custom-tooltip" data-tooltip-options="html: true"/>
  </div>
  <div>
      <label class="form-label">Last name</label>
      <input type="text" class="form-control" name="lastname" data-tooltip="" data-tooltip-src="custom-tooltip" data-tooltip-options="html: true"/>
  </div>
</div>
  
<script id="custom-tooltip" type="text/x-mustache-template">
  <span>A tooltip for <strong>{{= element.attr('name') }}</strong></span>
</script>
```
*/

AGN.Lib.CoreInitializer.new('tooltip', function($scope = $(document)) {

  const Tooltip = AGN.Lib.Tooltip;

  $('.tooltip').remove();

  $scope.all('[data-tooltip]').each(function() {
    const $e = $(this);
    Tooltip.create($e, Tooltip.options($e));
  });

});
