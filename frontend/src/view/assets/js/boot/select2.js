jQuery.extend( $.fn.select2.defaults, {

  formatNoMatches: function (term) {
    return t('selects.noMatches', term);
  },

  formatInputTooShort: function (input, min) {
    return t('selects.errors.inputTooShort', min);
  },

  formatInputTooLong: function (input, max) {
    return t('selects.errors.inputTooLong', max);
  },

  formatSelectionTooBig: function (limit) {
    return t('selects.errors.selectionTooBig', limit);
  },

  formatLoadMore: function (pageNumber) {
    return t('selects.loadMore');
  },

  formatSearching: function () {
    return t('selects.loadMore');
  },

  formatAjaxError: function() {
    return t('selects.errors.ajax');
  },

  formatMatches: function (matches) {
    if (matches > 1) {
      return t('selects.matches', matches);
    } else {
      return t('selects.matchesSing', matches);
    }
  }

});

