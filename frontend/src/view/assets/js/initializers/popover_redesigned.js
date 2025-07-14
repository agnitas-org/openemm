/*doc
---
title: Popovers
name: popovers
category: Components - Popovers
---
*/

/*doc
---
title: Popover
name: popover
parent: popovers
---

By setting `data-popover` a popover will automatically be generated for the element, which is shown on hover.

```htmlexample
<div class="d-flex gap-2">
  <button class="btn btn-primary btn-icon" data-popover="Add">
    <i class="icon icon-plus"></i>
  </button>

  <button class="btn btn-danger btn-icon" data-popover="Remove">
    <i class="icon icon-trash-alt"></i>
  </button>
</div>
```

You can specify custom options for your popover using `[data-popover-options="{optionsJson}"]`.

```htmlexample
<a href="#" class="btn btn-primary" data-popover="Content text" data-popover-options='{"title": "Title", "popperConfig": {"placement": "bottom-end"}}'>
    Hover me
</a>
```

If you want to use some mustache template as content for popover you can do this like this:

```htmlexample
<a href="#" class="btn btn-primary" data-popover data-popover-options='{"html": true, "templateName": "styleguide-popover-template"}'>
    Hover me
</a>

<script id="styleguide-popover-template" type="text/x-mustache-template">
    <div class="d-flex flex-column gap-1">
      <h1>Popover content</h1>
      <h2>Popover content</h2>
      <h3>Popover content</h3>
    </div>
</script>
```
*/

AGN.Lib.CoreInitializer.new('popover', ['table-row-actions'], function ($scope = $(document)) {

  const Popover = AGN.Lib.Popover;
  const ATTR_NAME = 'data-popover';

  Popover.validate();

  $scope.all(`[${ATTR_NAME}]`).each(function () {
    const $e = $(this);
    const options = $.extend(
      {content: $.trim($e.attr(ATTR_NAME))},
      $e.data('popover-options')
    );

    if (options.templateName) {
      if (options.html) {
        initContentFunction($e, options);
      } else {
        options.content = AGN.Lib.Template.text(options.templateName);
      }
    }

    if (options.content) {
      const popover = Popover.getOrCreate($e, options);

      if (options.disabled) {
        popover.disable();
      }
    }
  });

  function initContentFunction($e, options) {
    let content = null;

    options.content = () => {
      if (content) {
        return content;
      }

      const $template = AGN.Lib.Template.dom(options.templateName);

      AGN.Lib.Helpers.imagesLoaded($template.all('img'))
        .done(loadedImages => {
          content = $template;

          if (loadedImages.length) {
            const popover = Popover.get($e);
            if (popover) {
              popover.setContent();
              if ($e.is(':hover')) {
                $e.popover('show');
              }
            }
          }
        });

      return content;
    };
  }

});
