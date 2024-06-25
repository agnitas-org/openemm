(function ($) {

  const Defaults = $.fn.select2.amd.require('select2/defaults');

  $.extend(Defaults.defaults, {
    searchInputPlaceholder: '',
    showSearchIcon: false,
    language: {
      noResults: function (term) {
        return t('selects.noMatches', term || '');
      },
    },
  });

  const SearchDropdown = $.fn.select2.amd.require('select2/dropdown/search');
  const _renderSearchDropdown = SearchDropdown.prototype.render;

  SearchDropdown.prototype.render = function (decorated) {
    // invoke parent method
    const $rendered = _renderSearchDropdown.apply(this, Array.prototype.slice.apply(arguments));

    this.$search.attr('placeholder', this.options.get('searchInputPlaceholder'));

    if (this.options.get('showSearchIcon')) {
      this.$searchContainer.addClass('has-search-icon');
      this.$searchContainer.append(`<i class="icon icon-search select2-search-icon"></i>`);
    }

    return $rendered;
  };

  const Search = $.fn.select2.amd.require('select2/selection/search');
  const _updateSearch = Search.prototype.update;

  Search.prototype.update = function (decorated, data) {
    _updateSearch.apply(this, Array.prototype.slice.apply(arguments));

    if (this.options.get('preventPlaceholderClear') && this.placeholder?.text) {
      this.$search.attr('placeholder', this.placeholder.text); // prevents clear of placeholder
    }
  }

  Search.prototype.resizeSearch = function () {
    this.$search.css('width', '100%');
  }

  const SingleSelection = $.fn.select2.amd.require('select2/selection/single');
  const _update = SingleSelection.prototype.update;

  // Removes 'title' attribute cuz browser displays tooltip with own styles
  SingleSelection.prototype.update = function (decorated, placeholder) {
    _update.apply(this, Array.prototype.slice.apply(arguments));
    this.$selection.find('.select2-selection__rendered').removeAttr('title');
  }

})(window.jQuery);