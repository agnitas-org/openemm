AGN.Lib.Controller.new('profile-field-view', function() {
  this.addDomInitializer('profile-field-view', function($elem) {
    var data = $elem.json();

    $.i18n.load(data.translations);
  });
});
