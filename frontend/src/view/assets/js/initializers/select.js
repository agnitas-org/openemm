/*doc
---
title: Select Directive
name: select-directive
parent: directives
---

All selects are automatically decorated with select2. When setting the `js-select` class on a select tag a search field will be enabled if there are at least 10 options present.

```htmlexample
<div class="form-group">
  <div class="col-sm-4">
    <label class="form-label">
      Select
    </label>
  </div>
  <div class="col-sm-8">
    <select class="form-control">
      <option>An Option</option>
    </select>
  </div>
</div>
<div class="form-group">
  <div class="col-sm-4">
    <label class="form-label">
      Select with Search
    </label>
  </div>
  <div class="col-sm-8">
    <select class="form-control js-select">
      <option value="1">Option 1</option>
      <option value="2">Option 2</option>
      <option value="3">Option 3</option>
      <option value="4">Option 4</option>
      <option value="5">Option 5</option>
      <option value="6">Option 6</option>
      <option value="7">Option 7</option>
      <option value="8">Option 8</option>
      <option value="9">Option 9</option>
      <option value="10">Option 10</option>
    </select>
  </div>
</div>
```

For multi-selects you can also use `data-url` attributes on `<option>` elements in order to turn selected items into links:

```htmlexample
<div class="form-group">
  <div class="col-sm-4">
    <label class="form-label">
      Select with Links
    </label>
  </div>
  <div class="col-sm-8">
    <select class="form-control js-select" multiple="multiple">
      <option value="1" data-url="https://www.google.com?q=alaska">Alaska</option>
      <option value="2" data-url="https://www.google.com?q=hawaii">Hawaii</option>
      <option value="3" data-url="https://www.google.com?q=california">California</option>
      <option value="4" data-url="https://www.google.com?q=nevada">Nevada</option>
      <option value="5" data-url="https://www.google.com?q=oregon">Oregon</option>
    </select>
  </div>
</div>
```

*/

;(function(){
  var Popover = AGN.Lib.Popover,
      Template = AGN.Lib.Template,
      Page = AGN.Lib.Page;

  AGN.Lib.CoreInitializer.new('select', ['template'], function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.all('select'), function(el) {
      var $el = $(el);
      var resultTemplateId = $el.data('result-template');

      if ($el.data('select2')) {
        $el.select2('destroy');
      }

      var options = {
        placeholder: $el.attr('placeholder'),
        minimumResultsForSearch: 10
      };

      if ( !$el.hasClass('js-select') ) {
        options.minimumResultsForSearch = -1;
      }

      if ( $el.prop('multiple') ) {
        var selectionTemplateId = $el.data('selection-template');

        options.minimumResultsForSearch = 1;

        if (selectionTemplateId) {
          var createSelection = Template.prepare(selectionTemplateId);

          options.formatSelection = function(data) {
            return createSelection({
              element: data.element[0],
              isLocked: data.locked,
              isDisabled: data.disabled,
              text: data.text,
              value: data.id
            });
          };
        } else {
          options.formatSelection = function(state) {
            if (state.element && state.element.length === 1) {
              var url = $(state.element).data('url');
              if (url) {
                return $('<a></a>', {
                  href: url,
                  text: state.text,
                  click: function() { Page.reload(url); }
                });
              }
            }
            return state.text;
          };
        }
      }

      if (resultTemplateId) {
        var createResult = Template.prepare(resultTemplateId);

        options.formatResult = function(data) {
          return createResult({
            element: data.element[0],
            isLocked: data.locked,
            isDisabled: data.disabled,
            text: data.text,
            value: data.id
          });
        };
      } else if ($el.hasClass('js-option-popovers')) {
        options.formatResult = function(data, $label, query, escapeMarkup) {
          var text = '<span style="white-space: nowrap;">' + escapeMarkup(data.text) + '</span>';

          Popover.new($label, {
            trigger: 'hover',
            delay: {
              show: 300,
              hide: 100
            },
            html: true,
            template: '<div class="popover popover-wide" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>',
            content: text
          });

          return text;
        };

        $el.on('select2-close', function() {
          // Hide and remove abandoned popovers (stuck in shown state when trigger element has been removed)
          Popover.validate();
        });
      }

      $el.select2(options);
      // focusing by label is now handled in listener/label-events

      var $exclusiveOption = $el.find('option[data-exclusive]');
      if ($exclusiveOption.length > 0){
        $el.on('select2-selecting', function (e) {
          var exclusiveIsChosen = $el.find("option[value='"+e.val+"'][data-exclusive]").length > 0;

          if (exclusiveIsChosen){
            var needUpdate = false;
            $el.find('option:selected:not([data-exclusive])').each(function (){
              needUpdate = true;
              $(this).removeAttr("selected");
            });
            if (needUpdate) {
              $el.trigger('change');
            }
          } else {
            needUpdate = false;
            $el.find('option:selected[data-exclusive]').each(function (){
              needUpdate = true;
              $(this).removeAttr("selected");
            });
            if (needUpdate) {
              $el.trigger('change');
            }
          }
        })
      }
    });
  });

})();
