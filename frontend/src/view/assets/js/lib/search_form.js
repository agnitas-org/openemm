(function(){

  var SearchForm,
      Form = AGN.Lib.Form;

  // inherit from Form
  SearchForm = function($form) {
    Form.apply(this, [$form]);
  };

  SearchForm.prototype = Object.create(Form.prototype);
  // SearchForm.prototype.constructor = SearchForm;

  SearchForm.prototype.updateHtml = function(resp) {
    var $newSearchResults = $(resp).find('[data-form-content]'),
        $searchResults    =  this.$form.find('[data-form-content]');

    $searchResults.html($newSearchResults.html());

    this.handleMessages(resp);
    this.initFields($searchResults);

    AGN.Lib.Controller.init($searchResults);
    AGN.runAll($searchResults);
    $searchResults.trigger('scrollTo', $searchResults);
  };

  AGN.Lib.SearchForm = SearchForm;
  AGN.Opt.Forms['search'] = SearchForm;
})();
