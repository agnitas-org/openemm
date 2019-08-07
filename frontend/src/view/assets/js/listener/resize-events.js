(function(){

  var updateViewport,
      viewportChanged;

  updateViewport = function() {
    if (viewportChanged) {
      clearTimeout(viewportChanged);
    }

    viewportChanged = setTimeout(function() {
      AGN.Initializers.Equalizer();
      AGN.Initializers.Truncate();
      AGN.Initializers.Sizing();
      AGN.Initializers.Scrollable();
      AGN.Initializers.DropdownExpand();

      _.each(AGN.Lib.Editor.all(), function(editor) {
        editor.resize();
      });

      $(window).trigger('viewportChanged');

    }, 500);

  }

  $(window).on('resize', updateViewport);

})();
