/*doc
---
title: Clipboard
name: copyable-directives
category: Javascripts - Clipboard
---

A text which is a part of a web page (no matter visible or hidden) can be copied to clipboard by clicking at certain
element (button, text or pretty much anything else).

There are two modes of the feature: copying a content of certain input (like `<input>` or `<select>`)
and copying a predefined content (`data-copyable-value` attribute). Both are covered below.

The clickable area is always determined by an element having `data-copyable` attribute:

```html
<div data-copyable="">
  <!-- Something here -->
</div>
```

# Content of certain input

To use this mode you need to add `data-copyable-target` attribute to the same element which has `data-copyable`.
The value of `data-copyable-target` should contain a CSS selector of the input element to be used.

```html_example
<input type="text" id="name"/>

<span data-copyable="" data-copyable-target="#name">
  <i class="icon icon-copy"></i>
</span>
```

# Predefined content

This option is even easier. You only need to add `data-copyable-value` attribute. The attribute's value will be stored in clipboard when clickable area is clicked.

```html_example
<span data-copyable="" data-copyable-value="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9">
  Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 <i class="icon icon-copy"></i>
</span>
```
*/

(function() {
  var Action = AGN.Lib.Action,
    Clipboard = AGN.Lib.Clipboard,
    Tooltip = AGN.Lib.Tooltip;

  Action.new({'click': '[data-copyable]'}, function() {
    var value = undefined;

    if (this.el.is('[data-copyable-value]')) {
      value = this.el.data('copyable-value');
    } else {
      var selector = this.el.data('copyable-target');
      var $target;

      if (selector) {
        $target = $(selector);
      } else {
        $target = this.el;
      }

      if ($target.exists()) {
        value = $target.val();
      }
    }

    if (value !== undefined) {
      var $element = this.el;

      Clipboard.set(value, function(v, success) {
        if (success) {
          Tooltip.create($element, {
            title: t('tooltip.copy.clipboard'),
            trigger: 'manual'
          });

          Tooltip.setShown($element);

          var currentTimeoutId = $element.data('tooltip-timeout-id');
          if (currentTimeoutId) {
            // Make sure to clear former timeout (if any).
            clearTimeout(currentTimeoutId);
          }

          var timeoutId = setTimeout(function() {
            Tooltip.remove($element);
            $element.removeData('tooltip-timeout-id');
          }, 2500);

          $element.data('tooltip-timeout-id', timeoutId);
        }
      });
    }
  });
})();
