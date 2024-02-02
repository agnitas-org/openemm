(function($) {

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

    SearchDropdown.prototype.render = function(decorated) {
        // invoke parent method
        const $rendered = _renderSearchDropdown.apply(this, Array.prototype.slice.apply(arguments));

        this.$search.attr('placeholder', this.options.get('searchInputPlaceholder'));

        if (this.options.get('showSearchIcon')) {
            this.$searchContainer.addClass('has-search-icon');
            this.$searchContainer.append(`<i class="icon icon-search select2-search-icon"></i>`);
        }

        return $rendered;
    };

})(window.jQuery);