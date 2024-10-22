;(() => {
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;

  const SCROLLBAR_DATA_KEY = 'agn:select-scrollbar';

  AGN.Lib.CoreInitializer.new('select', ['template'], function($scope = $(document)) {
    _.each($scope.all('select'), el=> {
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
        dropdownParent: getDropdownParent($el),
        preventPlaceholderClear: false // custom option
      };

      if (!$el.hasClass('js-select')) {
        options.minimumResultsForSearch = 0;
      }

      if ($el.is('[data-sort]') ) {
        if ($el.data('sort') === 'alphabetic') {
          options.sorter = function (data) {
            data.sort((a, b) => {
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

      const resultTemplateId = $el.data('result-template');

      options.templateResult = function(data) {
        if (data.loading) {
          return data.text;
        }

        if ($(data.element).hasClass('hidden')) {
          return null;
        }

        if (!resultTemplateId) {
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

      const customSelectionTemplate = $el.data('selection-template');
      options.templateSelection = function(data) {
        return Template.dom(customSelectionTemplate || 'select2-result', {
          element: Array.isArray(data.element) ? data.element[0] : data.element,
          isDisabled: data.disabled,
          text: data.text,
          title: data.title,
          value: data.id
        });
      };

      if ( $el.hasClass('dynamic-tags') ) {
        options.tags = true;
        options.preventPlaceholderClear = true;
      }

      if ( $el.is('[data-select-options]') ) {
        const additionalOptions = AGN.Lib.Helpers.objFromString($el.data('select-options'));
        options = _.extend(options, additionalOptions);
      }
      
      if (options.alignDropdownRight) {
        $(options.dropdownParent).addClass('align-dropdown-right-container');
      }

      if (options.tags && !options.tokenSeparators) {
        options.tokenSeparators = [',', ' ', ';'];
      }

      if ( $el.is('[data-select-tree]') ) {
        adaptOptionsForTree($el);
        $el.select2ToTree(options);
      } else {
        $el.select2(options);
      }

      if ($el.is('[data-show-create-btn]') || options.tags) {
        const $btn = addCreateBtn($el);
        if (options.tags) {
          AGN.Lib.Tooltip.createTip($btn, t('defaults.add'));
          $btn.on('click', function () {
            const value = $el.data('select2').selection.$search.val();
            AGN.Lib.Select.get($el).selectOption(value);
          });
        }
      }

      $el.on('select2:open', e => {
        // fix of page scrolling prevention when select2 is opened
        const evt = "scroll.select2";
        $(e.target).parents().off(evt);
        $(window).off(evt);

        // this is a hack to display scrollbar rails when open dropdown
        window.setTimeout(() => {
          const $options = $el.data('select2').$dropdown.find('.select2-results__options');
          $el.data(SCROLLBAR_DATA_KEY, new AGN.Lib.Scrollbar($options, {wheelSpeed: 0.2}));
        }, 20);
      });

      $el.on('select2:closing', e => $el.data(SCROLLBAR_DATA_KEY)?.destroy());
      // focusing by label is now handled in listener/label-events

      const $exclusiveOption = $el.find('option[data-exclusive]');
      if ($exclusiveOption.length > 0){
        $el.on('select2:select', function (e) {
          var exclusiveIsChosen = $el.find(`option[value='${e.params.data?.element?.value}'][data-exclusive]`).length > 0;

          if (exclusiveIsChosen){
            var needUpdate = false;
            $el.find('option:selected:not([data-exclusive])').each(function (){
              needUpdate = true;
              Select.get($el).unselectValue($(this).val());
            });
            if (needUpdate) {
              $el.trigger('change');
            }
          } else {
            needUpdate = false;
            $el.find('option:selected[data-exclusive]').each(function (){
              needUpdate = true;
              Select.get($el).unselectValue($(this).val());
            });
            if (needUpdate) {
              $el.trigger('change');
            }
          }
        })
      }
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

  function addCreateBtn($el) {
    const $btn = getCreateBtn$($el.data('show-create-btn'));
    $el.next('.select2-container')
      .find('.select2-search')
      .addClass('flex-grow-1')
      .wrap($('<div class="d-flex gap-1"></div>'))
      .parent()
      .append($btn);

    return $btn;
  }

  function getCreateBtn$(attrs) {
    return $(`
        <button type="button" class="btn btn-icon btn-primary" ${attrs}>
          <i class="icon icon-plus"></i>
        </button>
    `);
  }

  function getDropdownParent($el) {
    if ( $el.closest('.modal').exists() ) {
      return $el.closest('.modal');
    }
    if ($el.closest('.dropdown-menu').exists() ) {
      return $el.closest('.dropdown-menu');
    }
    return $('#main-view').exists() ? '#main-view' : 'body';
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
         <button class="btn btn-outline-primary btn-icon btn-select-control">
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
