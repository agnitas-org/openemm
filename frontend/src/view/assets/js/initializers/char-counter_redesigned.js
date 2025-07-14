/*doc
---
title: Char counter
name: char-counter
category: Components - Char counter
---

Use data `.data-show-char-counter` with
<a href="components_-_input_&_textarea.html">`<input>` or `<textarea>`</a>
to display additional text next to the input, showing how many characters were entered into the field.

```htmlexample
<div class="d-flex gap-3">
  <div class="flex-grow-1">
      <label class="form-label" for="input">Input</label>
      <input type="text" class="form-control" placeholder="Start typing" data-show-char-counter></textarea>
  </div>
  <div class="flex-grow-1">
      <label class="form-label" for="textarea">Text area</label>
      <textarea id="textarea" class="form-control" placeholder="Start typing" data-show-char-counter></textarea>
  </div>
</div>
```

It is also possible to display char counter in the <a href="components_-_tiles.html#tiles_08_footer">`.tile-footer`</a>.
For this use `[data-show-char-counter="tile-footer"]`.

```htmlexample
<div class="tile">
    <div class="tile-header">
        <h1 class="tile-title">Char counter for body input displayed in tile footer</h1>
    </div>
    <div class="tile-body">
        <textarea class="form-control" placeholder="Start typing" data-show-char-counter="tile-footer"></textarea>
    </div>
    <div class="tile-footer"></div>
</div>
```
 */

AGN.Lib.CoreInitializer.new('char-counter', ['growing-textarea'], function ($scope = $(document)) {
  _.each($scope.find('[data-show-char-counter]'), function (el) {
    const $input = $(el);
    if (!$input.is("input") && !$input.is("textarea")) {
      return;
    }
    initCharCounterForInput($input);
  })

  function updateCharCounter($charCounterVal, $input) {
    $charCounterVal.text(t('fields.content.charactersEntered', $input.val()?.length || 0));
  }

  function prependCharCounterForTileFooter($tile, $charCounterVal) {
    let $footer = $tile.find('.tile-footer');
    if (!$footer.exists()) {
      $footer = $('<div class="tile-footer text-secondary fs-3 border-top">');
      $tile.append($footer);
    }
    $footer.prepend($charCounterVal);
  }

  function initCharCounterForInput($input) {
    const $charCounterVal = $('<span class="text-truncate">');
    if ($input.data('show-char-counter') === 'tile-footer') {
      prependCharCounterForTileFooter($input.closest('.tile'), $charCounterVal);
    } else {
      const $container = $input.is('textarea') ? $input.parent() : $input;
      $container.before($(`<div class="char-counter"></div>`).append($charCounterVal));
    }
    updateCharCounter($charCounterVal, $input);
    $input.on('change input editor:create editor:change', _.throttle(() => updateCharCounter($charCounterVal, $input), 100));
  }
});
