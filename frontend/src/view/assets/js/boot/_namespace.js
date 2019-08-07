AGN = window.AGN || (window.AGN = {});

AGN.Initializers = window.AGN.Initializers || (window.AGN.Initializers = {});

AGN.Lib = window.AGN.Lib || (window.AGN.Lib = {});
AGN.Lib.WM = window.AGN.Lib.WM || (window.AGN.Lib.WM = {});

AGN.Opt                     = window.AGN.Opt || (window.AGN.Opt = {});
AGN.Opt.Fields              = window.AGN.Opt.Fields || (window.AGN.Opt.Fields = {});
AGN.Opt.Forms               = window.AGN.Opt.Forms || (window.AGN.Opt.Forms = {});
AGN.Opt.Controllers         = window.AGN.Opt.Controllers || (window.AGN.Opt.Controllers = {});
AGN.Opt.Templates           = window.AGN.Opt.Templates || (window.AGN.Opt.Templates = {});
AGN.Opt.TableCellRenderers  = window.AGN.Opt.TableCellRenderers || (window.AGN.Opt.TableCellRenderers = {});
AGN.Opt.Charts              = window.AGN.Opt.Charts || (window.AGN.Opt.Charts = {});
AGN.Opt.Validators          = window.AGN.Opt.Validators || (window.AGN.Opt.Validators = {});


AGN.url = function(address, excludeSessionId) {
  if (window.agnResolveRelativeUrl) {
    if (address && address.charAt(0) === '/') {
      address = address.substring(1);
    }
    return window.agnResolveRelativeUrl(address, excludeSessionId);
  } else {
    console.error('window.agnResolveRelativeUrl is not defined');
    return address;
  }
};

AGN.runAll = function($scope) {
  $.each(AGN.Initializers, function(key, init) {
    init.call(init, $scope);
  });
  AGN.Lib.DomInitializer.autorun($scope);
};
