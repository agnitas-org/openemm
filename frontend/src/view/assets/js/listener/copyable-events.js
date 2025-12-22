/*doc
---
title: Clipboard
name: copyable
category: Components - Copyable
---

A text which is a part of a web page (no matter visible or hidden) can be copied to clipboard by clicking at certain
element (button, text or pretty much anything else).

There are two modes of the feature: copying a content of certain input (like `<input>` or `<select>`)
and copying a predefined content (`[data-copyable-value]` attribute). Both are covered below.

The clickable area is always determined by an element having `data-copyable` attribute:

# Content of certain input

To use this mode you need to add `[data-copyable-target]` attribute to the same element which has `[data-copyable]`.
The value of `[data-copyable-target]` should contain a CSS selector of the input or text element to be used.

```html_example
<div class="d-flex gap-1">
    <input type="text" class="form-control" id="name"/>
    <span data-copyable="" data-copyable-target="#name">
        <a href="#" class="btn btn-icon btn-info"><i class="icon icon-copy"></i></a>
    </span>
</div>
```

# Predefined content

This option is even easier. You only need to add `[data-copyable-value]` attribute. The attribute's value will be stored in clipboard when clickable area is clicked.

```html_example
<div class="copy-box">
    <span>CTOKEN:</span>
    <span id="company-token-label">eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9</span>
    <button class="icon-btn" data-copyable data-copyable-target="#company-token-label">
        <i class="icon icon-copy"></i>
    </button>
</div>
```
*/

(() => {

  const Tooltip = AGN.Lib.Tooltip;

  AGN.Lib.Action.new({click: '[data-copyable]'}, function () {
    let value = undefined;
    let $tooltipTarget = this.el;

    if (this.el.is('[data-copyable-value]')) {
      value = this.el.data('copyable-value');
    } else {
      const selector = this.el.data('copyable-target');
      const $target = selector ? $(selector) : this.el;

      if ($target.exists()) {
        value = $target.val() || $target.text();
        $tooltipTarget = $target;
      }
    }

    if (value !== undefined) {
      AGN.Lib.Clipboard.set(value, (v, success) => {
        if (success) {
          const existingTooltip = Tooltip.get($tooltipTarget);
          const copiedMsg = t('clipboard.copied.tooltip');

          if (existingTooltip) {
            Tooltip.setContent($tooltipTarget, copiedMsg);
          } else {
            Tooltip.create($tooltipTarget, {
              title: copiedMsg,
              trigger: 'manual'
            });
          }

          Tooltip.setShown($tooltipTarget);

          const currentTimeoutId = this.el.data('tooltip-timeout-id');
          if (currentTimeoutId) {
            // Make sure to clear former timeout (if any).
            clearTimeout(currentTimeoutId);
          }

          const timeoutId = setTimeout(() => {
            if (existingTooltip) {
              Tooltip.restoreContent($tooltipTarget);
              Tooltip.setShown($tooltipTarget, false);
            } else {
              Tooltip.remove($tooltipTarget);
            }

            this.el.removeData('tooltip-timeout-id');
          }, 2500);

          this.el.data('tooltip-timeout-id', timeoutId);
        }
      });
    }
  });
})();
