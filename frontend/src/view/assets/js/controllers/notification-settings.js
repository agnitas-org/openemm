AGN.Lib.Controller.new('notification-settings', function() {

  this.addAction({
    'editor:create': 'content',
    'editor:change': 'content'
  }, function() {
    var count = this.el.val().length;
    $('#contentSize').text(t('fields.content.charactersEntered', count));
  });

});
