AGN.Lib.Controller.new('trackable-link', function () {
  var self = this;
  var TrackableLinkExtensions = new AGN.Lib.TrackableLinkExtensions();

  this.addDomInitializer('trackable-link-extensions', function () {
    TrackableLinkExtensions.load(this.config.extensions, $('#extensions tbody'));
  });

  this.addAction({click: 'save'}, function () {
    this.event.preventDefault();
    var form = AGN.Lib.Form.get($('#trackableLinkForm'));

    setExtensionsToForm(form);
    form.submit("static");
  });

  function setExtensionsToForm(form) {
    _.each(TrackableLinkExtensions.collect(), function (extension, index) {
      form.setValue('extensions[' + index + '].name', extension.name);
      form.setValue('extensions[' + index + '].value', extension.value);
    })
  }

  this.addAction({'change': 'link-details-trackable'}, function () {
    self.runInitializer('trackableAction');
  });

  this.addInitializer('trackableAction', function ($scope) {
    var $trigger = $('[data-action="link-details-trackable"] :selected');
    var $linkAction = $('#linkAction');
    $linkAction.prop('disabled', $linkAction.prop('disabled') || $trigger.val() == 0);
  })
});
