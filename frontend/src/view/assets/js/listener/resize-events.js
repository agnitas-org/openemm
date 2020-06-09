(function(){

  var updateViewport,
      viewportChanged;

  updateViewport = function() {
    if (viewportChanged) {
      clearTimeout(viewportChanged);
    }

    viewportChanged = setTimeout(function() {
      AGN.Lib.CoreInitializer.run(['equalizer', 'truncate', 'sizing', 'scrollable', 'dropdown-expand']);

      _.each(AGN.Lib.Editor.all(), function(editor) {
        editor.resize();
      });

      $(window).trigger('viewportChanged');

    }, 500);

  };

  $(window).on('resize', updateViewport);

})();
