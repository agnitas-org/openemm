/*doc
---
title: Autosave
name: autosave
category: Javascripts - Autosave
---

The `data-autosave-scope` attribute defines a root element of the DOM subtree to be watched. The attribute's value is a unique identifier for stored data bundle (empty value is permitted as well).

```html
<div class="tile" data-autosave-scope="valuable-data">
  ...
</div>
```

Add `data-autosave` attribute to an elements (root element or its descendants) which values should be stored. The attribute's value is a unique identifier within the stored bundle.
The simplest way to use an autosave feature is as follows:

```html
<input type="text" name="name" data-autosave-scope="bundle1" data-autosave="user-name">
```

``` html
<div class="tile" data-autosave-scope="bundle2">
  ...
  <input type="text" name="firstName" data-autosave="firstName">
  ...
  <input type="text" name="lastName" data-autosave="lastName">
  ...
</div>
```

There are two triggers for saving: js event `ajax:unauthorized` triggered on `document` element and interval timer (disabled by default).

To enable interval timer add `data-autosave-period` attribute to a root element (the one having `data-autosave-scope` attribute) with positive integer as a value (interval duration in seconds):

```html
<div class="tile" data-autosave-scope="userData" data-autosave-period="60">
  ...
  <input type="text" name="name" data-autosave="name">
  ...
</div>
```

The restoration availability is checked when the page/subtree initialization (when AGN.Initializers.* invoked) goes on.
If the stored values are different that the ones held by watched elements then a user is prompted (modal dialog) to restore data or cancel restoration.

Use `data-autorestore` attribute to enable an automatic (without prompt) restoration for an exact element:

```html
  <div class="tile" data-autosave-scope="userData">
  ...
  <input type="text" name="name" data-autosave="name" data-autorestore="">
  ...
  </div>
```

*/

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

  AGN.Initializers.AutoSave = function($scope) {
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
  };

})();
