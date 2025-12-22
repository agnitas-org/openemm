/*doc
---
title: Popout window
name: popout window
parent: iframe
---

Use `[data-popout-window]` to append 'Pop-out this window' button to the element footer (caption).

<small class="text-secondary">Initially designed for iframe.</small>

```htmlexample
<iframe src="https://www.agnitas.de/" data-popout-window style="height: 500px;"></iframe>
```
*/

AGN.Lib.CoreInitializer.new('popout-window', function ($scope = $(document)) {
  _.each($scope.find('[data-popout-window]'), el => {
    const $el = $(el);
    const src = $el.attr('src');
    if (!src) {
      return;
    }

    $el.parent().append(
      $('<figure>')
        .append($el)
        .append($(`
          <figcaption>
            <a class="clickable flex-center overflow-hidden" data-popup="${src}" data-bs-dismiss="modal">
                <span class="text-truncate">${src}</span>
                <i class="icon icon-external-link-alt"></i>
            </a>
          </figcaption>`)));
  });
});
