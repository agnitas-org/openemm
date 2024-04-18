/*
EXAMPLE
<div class="form-group">
    <label class="form-label" for="message"><mvc:message code="default.message"/></label>
    <textarea id="message" name="message" class="form-control v-resizable" data-show-char-counter></textarea>
</div>
*/

AGN.Lib.CoreInitializer.new('char-counter', function ($scope) {
  if (!$scope) {
    $scope = $(document);
  }

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
      $footer = $('<div class="tile-footer text-dark fs-3 bg-transparent border-top">');
      $tile.append($footer);
    }
    $footer.prepend($charCounterVal);
  }

  function initCharCounterForInput($input) {
    const $charCounterVal = $('<span class="text-truncate">');
    if ($input.data('show-char-counter') === 'tile-footer') {
      prependCharCounterForTileFooter($input.closest('.tile'), $charCounterVal);
    } else {
      $input.before($(`<div class="char-counter"></div>`).append($charCounterVal));
    }
    updateCharCounter($charCounterVal, $input);
    $input.on('change input editor:create editor:change', _.throttle(() => updateCharCounter($charCounterVal, $input), 100));
  }
});
