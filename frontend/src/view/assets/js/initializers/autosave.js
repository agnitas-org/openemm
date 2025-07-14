(function(){

  var AutoSave = AGN.Lib.AutoSave;

  function restoreValue($target, value) {
    var id = $target.attr('id'), editor = $target.data('_editor');
    if (window.CKEDITOR && CKEDITOR.instances[id]) {
      CKEDITOR.instances[id].setData(value);
    } else if (editor) {
      editor.val(value);
    } else {
      $target.val(value);
    }
  }

  AGN.Lib.CoreInitializer.new('auto-save', ['ace'], function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-autosave-scope]'), function(e) {
      var $this = $(e);
      var scopeId = $this.data('autosave-scope');
      var period = $this.data('autosave-period');

      var $targets = $this.all('[data-autosave]');

      if ($targets.length > 0) {
        function save() {
          // Exclude detached elements
          $targets = $targets.filter(function(index, e) {
            return document.body.contains(e);
          });

          if ($targets.length > 0) {
            var values = {};

            _.each($targets, function(target) {
              var $target = $(target);
              values['value#' + $target.data('autosave')] = $target.val();
            });

            return values;
          }

          return false;
        }

        function check(values) {
          var available = $.map($targets, function(target) {
            var $target = $(target);
            var key = 'value#' + $target.data('autosave');

            if (key in values) {
              var currentValue = $target.val();
              var restoredValue = values[key];

              if (currentValue != restoredValue) {
                if ($target.is('[data-autorestore]')) {
                  restoreValue($target, restoredValue);
                } else {
                  return {
                    target: $target,
                    value: restoredValue
                  };
                }
              }

              return null;
            }
          });

          if (available.length > 0) {
            return available;
          }

          return false;
        }

        function restore(values) {
          _.each(values, function(a) {
            restoreValue(a.target, a.value);
          });
        }

        AutoSave.initialize(scopeId, save, check, restore, period);
      }
    });
  });

})();
