/*

Appends 'Pop-out this window' button to the element (initially designed for iframe) 

EXAMPLE
<div id="iframeContainer" class="h-100">
    <iframe src='${url}' width="100%" height="100%" data-popuot-window></iframe>
</div>
*/

AGN.Lib.CoreInitializer.new('popuot-window', function ($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  _.each($scope.find('[data-popuot-window]'), function (el) {
    const $el = $(el);
    const src = $el.attr('src');
    if (!src) {
      return;
    }

    $el.parent().append(
      $('<div class="popout-window-link-container"></div>')
        .append($el)
        .append($(`
          <div class="popout-window-link">
            <span class="text-truncate">${src}</span>
            <a href="" class='link-secondary' data-popup="${src}" data-bs-dismiss="modal">
              ${t('defaults.window.popout')}&nbsp;<i class="icon icon-external-link-alt"></i>
            </a>
          </div>`)));
  });
});
