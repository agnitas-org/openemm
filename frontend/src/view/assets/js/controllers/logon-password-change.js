AGN.Lib.Controller.new('logon-password-change', function() {
  this.addAction({click: 'showPasswordChangeForm'}, function() {
    $('#suggestion-view').addClass('hidden');
    $('#submission-view').removeClass('hidden');
  });
});
