AGN.Lib.DomInitializer.new('account-data', function () {
  var $accountInitials = $('#account-data .account-initials');
  var $accountDataInfobox = $accountInitials.next('.account-data-infobox');
  $accountInitials.popover({
    trigger: 'hover',
    container: 'body',
    html: true,
    content: $accountDataInfobox.html()
  });
});