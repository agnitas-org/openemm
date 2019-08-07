// window.onerror = function() {

//   if (!AGN) return;
//   if (!AGN.Opt) return;
//   if (!AGN.Opt.Templates) return;
//   if (!AGN.Opt.Templates['js-error-message']) return;
//   if (!AGN.Lib) return;
//   if (!AGN.Lib.Loader) return;
//   if (!_) return;
//   if (!$) return;

//   if (window.errorAppeared) return;
//   window.errorAppeared = true;

//   AGN.Lib.Loader.hideAll();
//   $('body').append(AGN.Opt.Templates['js-error-message']);
// }

$(document).on('click', '.js-close-error', function(e) {
  if (!$(e.target).hasClass('js-close-error')) {
    return;
  }
  // window.errorAppearead = false;
  $(this).closest('.backdrop').remove();
})
