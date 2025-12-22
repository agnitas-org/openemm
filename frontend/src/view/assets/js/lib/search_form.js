(function () {

  const Form = AGN.Lib.Form;

  // inherit from Form
  class SearchForm extends Form {
    constructor($form) {
      super($form);
    }

    updateHtml(resp) {
      const $newSearchResults = $(resp).find('[data-form-content]');
      const $searchResults = this.$form.is('[data-form-content]')
          ? this.$form
          : this.$form.find('[data-form-content]');

      $searchResults.html($newSearchResults.html());

      this.handleMessages(resp);
      this.initFields($searchResults);

      AGN.Lib.Controller.init($searchResults);
      AGN.runAll($searchResults);
      $searchResults.trigger('scrollTo', $searchResults);
    }
  }

  AGN.Lib.SearchForm = SearchForm;
  AGN.Opt.Forms['search'] = SearchForm;
})();
