
// TODO: recheck issue EMMGUI-443 with Bootstrap 5
// (function($) {
//
//   // fix for EMMGUI-443
//   $.fn.modal.Constructor.prototype.escape = function() {
//     if (this.isShown && this.options.keyboard) {
//       this.$element.on("keydown.dismiss.bs.modal", $.proxy(function(e) {
//           setTimeout($.proxy(function() {
//               e.which == 27 && this.hide();
//           }, this), 100);
//       }, this));
//     } else if (!this.isShown) {
//       this.$element.off("keydown.dismiss.bs.modal");
//     }
//   };
//
//
// })(jQuery);
