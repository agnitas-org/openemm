//TODO EMMGUI-714 check usage and remove with old code deletion
AGN.Lib.DomInitializer.new('mailing-content-view', function($elem) {
  AGN.Lib.Storage.set('mailings-content-dynNameID', {dynNameID: $elem.data('dyn-name-id')});
});
