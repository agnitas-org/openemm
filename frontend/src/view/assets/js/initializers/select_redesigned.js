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
  const Popover = AGN.Lib.Popover,
    Template = AGN.Lib.Template,
    Page = AGN.Lib.Page;

  const SCROLLBAR_DATA_KEY = 'agn:select-scrollbar';

  AGN.Lib.CoreInitializer.new('select', ['template'], function($scope) {
    if (!$scope) {
      $scope = $(document);
    }

    _.each($scope.all('select'), function(el) {
      const $el = $(el);

      if ($el.data('select2')) {
        $el.select2('destroy');
      }

      if ($el.hasClass('has-arrows')) {
        new SelectWithArrows($el);
      }

      let options = {
        placeholder: $el.attr('placeholder'),
        minimumResultsForSearch: 10,
        showSearchIcon: true,
        width: '100%',
        searchInputPlaceholder: t('tables.searchOoo'),
        dropdownCssClass: ':all:',
        selectionCssClass: ':all:',
        dropdownParent: $('#main-view').exists() ? '#main-view' : 'body',
        preventPlaceholderClear: false // custom option
      };

      if ( $el.closest('.modal').exists() ) {
        options.dropdownParent = $el.closest('.modal');
      }

      if ( $el.closest('.dropdown-menu').exists() ) {
        options.dropdownParent = $el.closest('.dropdown-menu');
      }

      if ( !$el.hasClass('js-select')) {
        options.minimumResultsForSearch = 0;
      }

      if ( $el.hasClass('dropdown-select') ) {
        options.placeholder = t('tables.searchOoo');
        options.preventPlaceholderClear = true;

        // prevents opening of select2 after removing of selected tag
        $el.on('select2:unselecting', function (e) {
          $el.on('select2:opening', function (ev) {
            ev.preventDefault();
            $el.off('select2:opening');
          });
        });
      }

      if ( $el.is('[data-sort]') ) {
        if ($el.data('sort') === 'alphabetic') {
          options.sorter = function (data) {
            data.sort(function(a, b) {
              if ($(a.element).is('[data-no-sort]')) {
                return -1;
              } else if ($(b.element).is('[data-no-sort]')) {
                return 1;
              }

              return a.text.toLowerCase().localeCompare(b.text.toLowerCase());
            });

            return data;
          }
        }
      }

      if ($el.prop('multiple')) {
        options.matcher = multiSelectMatcher;
      }

      if ( $el.is('[data-result-template]') ) {
        const resultTemplateId = $el.data('result-template');

        options.templateResult = function(data) {
          if (data.loading) {
            return data.text;
          }

          return Template.dom(resultTemplateId, {
            element: data.element,
            isDisabled: data.disabled,
            text: data.text,
            title: data.title,
            value: data.id
          });
        };
      }

      // if (resultSelectionTemplateId) {
      const customSelectionTemplate = $el.data('selection-template');
      const createResult = Template.prepare(customSelectionTemplate || 'select2-result');
      options.templateSelection = function(data) {
        const selection = createResult({
          element: Array.isArray(data.element) ? data.element[0] : data.element,
          isDisabled: data.disabled,
          text: data.text,
          title: data.title,
          value: data.id
        });
        return $(selection);
      };
      // } else if ($el.hasClass('js-option-popovers')) {
        // TODO: remove if not necessary
        // options.formatResult = function(data, $label, query, escapeMarkup) {
        //   var text = '<span style="white-space: nowrap;">' + escapeMarkup(data.text) + '</span>';
        //
        //   Popover.new($label, {
        //     trigger: 'hover',
        //     delay: {
        //       show: 300,
        //       hide: 100
        //     },
        //     html: true,
        //     template: '<div class="popover popover-wide" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>',
        //     content: text
        //   });
        //
        //   return text;
        // };
        //
        // $el.on('select2-close', function() {
        //   // Hide and remove abandoned popovers (stuck in shown state when trigger element has been removed)
        //   Popover.validate();
        // });
      // }

      // TODO: remove if not necessary
      // var currentFormatSelection = options.formatSelection;
      // options.formatSelection = function(data) {
      //   var text = data.text;
      //   if (text) {
      //     data.text = text.trim();
      //   }
      //   if(currentFormatSelection instanceof Function) {
      //     return currentFormatSelection.apply(this, arguments);
      //   }
      //   return $.fn.select2.defaults.formatSelection.apply(this, arguments);
      // };

      if ( $el.is('[data-select-options]') ) {
        const additionalOptions = AGN.Lib.Helpers.objFromString($el.data('select-options'));
        options = _.extend(options, additionalOptions);
      }

      if ( $el.is('[data-select-tree]') ) {
        adaptOptionsForTree($el);
        $el.select2ToTree(options);
      } else {
        $el.select2(options);
      }

      $el.on('select2:open', e => {
        // fix of page scrolling prevention when select2 is opened
        const evt = "scroll.select2";
        $(e.target).parents().off(evt);
        $(window).off(evt);

        // this is a hack to display scrollbar rails when open dropdown
        window.setTimeout(() => {
          const $options = $el.data('select2').$dropdown.find('.select2-results__options');
          $el.data(SCROLLBAR_DATA_KEY, new AGN.Lib.Scrollbar($options));
        }, 0);
      });

      $el.on('selec2:closing', e => $el.data(SCROLLBAR_DATA_KEY)?.destroy());
      // focusing by label is now handled in listener/label-events

      // TODO: remove if not necessary
      // var $exclusiveOption = $el.find('option[data-exclusive]');
      // if ($exclusiveOption.length > 0){
      //   $el.on('select2-selecting', function (e) {
      //     var exclusiveIsChosen = $el.find("option[value='"+e.val+"'][data-exclusive]").length > 0;
      //
      //     if (exclusiveIsChosen){
      //       var needUpdate = false;
      //       $el.find('option:selected:not([data-exclusive])').each(function (){
      //         needUpdate = true;
      //         $(this).removeAttr("selected");
      //       });
      //       if (needUpdate) {
      //         $el.trigger('change');
      //       }
      //     } else {
      //       needUpdate = false;
      //       $el.find('option:selected[data-exclusive]').each(function (){
      //         needUpdate = true;
      //         $(this).removeAttr("selected");
      //       });
      //       if (needUpdate) {
      //         $el.trigger('change');
      //       }
      //     }
      //   })
      // }
    });
  });

  // for parent option [data-cup='parent'] will be set. for option that have parents [data-cup='${parentValue}'] will be set
  // this is adapter for select2totree.js
  function adaptOptionsForTree($select) {
    const $options = $select.find('option');
    const parents = $options
      .map(function () {
        return String($(this).data('parent') || '');
      })
      .get();

    $options.each(function () {
      const $el = $(this);

      if ($el.val() && parents.includes($el.val())) {
        $el.attr("data-pup", 'parent');
      } else {
        if ($el.data('parent')) {
          $el.attr("data-pup", $el.data('parent'));
        }
      }
    });
  }

  function multiSelectMatcher(params, data) {
    if (data.selected) {
      return null; // hide already selected options
    }
    if ($.trim(params.term) === '') {
      return data; // if the search term is empty, show all non-selected options
    }
    // perform search
    const searchText = params.term.toLowerCase();
    const optionText = data.text.toLowerCase();
    return optionText.includes(searchText) ? data : null;
  }

  class SelectWithArrows {

    constructor($select) {
      const selectWithArrows = $select.data('selectWithArrows');
      if (selectWithArrows) {
        selectWithArrows.toggleArrows();
        return selectWithArrows; // return the existing object
      }
      this.$select = $select;
      this.init();
    }

    init() {
      this.#createPrevBtn();
      this.#createNextBtn();

      this.$select.wrap($('<div>').addClass('select-container'));
      this.$select.closest('.select-container').prepend(this.$prevBtn).append(this.$nextBtn);
      this.$select.on('change', () => this.toggleArrows());
      this.toggleArrows();
      this.$select.data('selectWithArrows', this);
    }

    toggleArrows() {
      const selectedIndex = this.$select.prop("selectedIndex");
      this.$prevBtn.prop("disabled", selectedIndex === 0);
      this.$nextBtn.prop("disabled", selectedIndex === this.$select.find("option").length - 1);
    }

    #createPrevBtn() {
      this.$prevBtn = this.#createArrowBtn(true);
      this.$prevBtn.on("click", () => this.#stepBack());
    }

    #createNextBtn() {
      this.$nextBtn = this.#createArrowBtn(false);
      this.$nextBtn.on("click", () => this.#stepForward());
    }

    #createArrowBtn(prev) {
      return $(`
         <button class="btn btn-outline-primary btn-icon-sm btn-select-control">
           <i class="icon icon-caret-${prev ? 'left' : 'right'}"></i>
         </button>
       `);
    }

    #stepForward() {
      this.#step(false);
    }

    #stepBack() {
      this.#step(true);
    }

    #step(back) {
      const $arrowBtn = back ? this.$prevBtn : this.$nextBtn;
      if ($arrowBtn.prop("disabled")) {
        return;
      }
      this.$select.prop("selectedIndex", this.$select.prop("selectedIndex") + (back ? -1 : 1)).trigger('change');
      this.toggleArrows();
    }
  }
})();
