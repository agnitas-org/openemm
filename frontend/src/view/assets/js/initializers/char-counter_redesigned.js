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
  
  function initCharCounterForInput($input) {
    const $charCounterVal = $('<span>');
    $input.before($(`<div class="char-counter"></div>`).append($charCounterVal));

    updateCharCounter($charCounterVal, $input);
    $input.on('change input', _.throttle(() => updateCharCounter($charCounterVal, $input), 100));
  }
});
