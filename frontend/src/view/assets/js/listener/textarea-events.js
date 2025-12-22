$(document).on('input change', '.text-area-grow-wrapper > textarea', function(e) {
  const $el = $(this);
  $el.parent().attr('data-replicated-value', $el.val());
  AGN.Lib.Scrollbar.get($el)?.update();
});