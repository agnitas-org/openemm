/*doc
---
title: Colorpicker
name: colorpicker
category: Components - Colorpicker
---

Adding the `.js-colorpicker` class to an input will integrate a colorpicker for that field.

```htmlexample
<div class="form-column-2">
    <div class="colorpicker-container">
        <input type="text" id="color-input-example-1" class="form-control js-colorpicker" value="#0056B9">
    </div>
    <div class="input-group">
        <span class="input-group-text">Chose color</span>
        <input type="text" class="form-control js-colorpicker" data-name="color" value="#FFD800">
    </div>
</div>
```
*/

AGN.Lib.CoreInitializer.new('colorpicker', function ($scope = $(document)) {
  const Colorpicker = AGN.Lib.Colorpicker;

  $scope.find('.js-colorpicker').each(function () {
    const $el = $(this);

    if (!Colorpicker.get($el)) {
      new Colorpicker($el);
    }
  });

});
