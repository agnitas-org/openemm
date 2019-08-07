/*doc
 ---
 title: Checkbox Hider Directive
 name: checkbox-hider-directive
 parent: directives
 ---

 By setting `data-show-by-checkbox` the object will be hidden if checkbox is not checked. Checkbox JQuery selector should be set in 'data-hide-by-checkbox'.

 By setting `data-hide-by-checkbox`  the object will be hidden if checkbox is checked.

 ```htmlexample
 <div class="form-group">
 <div class="col-sm-4">
 <label class="control-label">Checkbox or toggle</label>
 </div>
 <div class="col-sm-4">
 <label class="toggle">
 <input type"checkbox" id="checkboxID"/>
 <div class="toggle-control"></div>
 </label>
 </div>
 </div>

 <div class="form-group" data-show-by-checkbox="#checkboxID">
 <div class="col-sm-4">
 <label class="control-label">Some input field</label>
 </div>
 <div class="col-sm-4">
 <input type="text" class="form-control" name="lastname" />
 </div>
 </div>

 ```
 */

;(function(){

  AGN.Initializers.CheckboxHider = function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.find('[data-show-by-checkbox]'), function(el) {
      var selector = $(el);
      var checkboxSelector = $(selector.data('show-by-checkbox'));
      updateVisibleByCheckbox(selector, checkboxSelector, true);
      addCheckboxListener(selector, checkboxSelector, true);
    });

    _.each($scope.find('[data-hide-by-checkbox]'), function(el) {
      var selector = $(el);
      var checkboxSelector = $(selector.data('hide-by-checkbox'));
      updateVisibleByCheckbox(selector, checkboxSelector, false);
      addCheckboxListener(selector, checkboxSelector, false);
    });

    _.each($scope.find('[data-show-by-select]'), function(el) {
      var selector = $(el);
      var selectSelector = $(selector.data('show-by-select'));
      var options = selector.data('show-by-select-values').split(",");
      for (var i = 0; i < options.length; i++) {
        options[i] = options[i].trim();
      }
      updateVisibleBySelect(selector, selectSelector, options);
      addSelectListener(selector, selectSelector, options);
    });
  };

  function addCheckboxListener(selector, checkboxSelector, showIfChecked){
    checkboxSelector.change(function() {
      updateVisibleByCheckbox(selector, checkboxSelector, showIfChecked);
    });
  }

  function updateVisibleByCheckbox(selector, checkboxSelector, showIfChecked) {
    if (showIfChecked){
      if (checkboxSelector.is(":checked")){
        selector.removeClass("hidden");
      } else {
        selector.addClass("hidden");
      }
    } else {
      if (!checkboxSelector.is(":checked")){
        selector.removeClass("hidden");
      } else {
        selector.addClass("hidden");
      }
    }
  }

  function addSelectListener(selector, selectSelector, options){
    selectSelector.change(function() {
      updateVisibleBySelect(selector, selectSelector, options);
    });
  }

  function updateVisibleBySelect(selector, selectSelector, options) {
    var selectValue = selectSelector.val();
    var show = false;
    for (var i = 0; i < options.length; i++) {
      if (options[i] == selectValue){
        show = true;
        break;
      }
    }

    if (show){
      selector.removeClass("hidden");
    } else {
      selector.addClass("hidden");
    }
  }

})();
