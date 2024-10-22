// /*doc
// ---
// title: Colorpicker Directive
// name: colorpicker-directive
// parent: directives
// ---
//
// Deprecated: in new version of Bootstrap it's removed, so it has to be replaced with something else
// (perhaps we could keep using .js-colorpicker for new library though).
//
// Adding the `.js-colorpicker` class to an input-group will integrate a colorpicker for that field.
//
// ```html
// <div class="form-group">
//     <label class="form-label">
//       Color
//     </label>
//     <div class="input-group js-colorpicker">
//         <div class="input-group-controls">
//             <input class="form-control" type="text" value="#ff0000">
//         </div>
//         <div class="input-group-addon">
//             <span class="addon">
//                 <i></i>
//             </span>
//         </div>
//     </div>
// </div>
// ```
// */

AGN.Lib.CoreInitializer.new('colorpicker', function($scope) {
  if (!$scope) {
    $scope = $(document);
  }

  $scope.find('.js-colorpicker').each(function() {
    $(this).colorpicker({format: 'hex', container: true});
  });
});
