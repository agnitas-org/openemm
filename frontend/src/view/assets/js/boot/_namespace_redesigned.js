AGN = window.AGN || (window.AGN = {});

AGN.Lib                       = window.AGN.Lib || (window.AGN.Lib = {});
AGN.Lib.WM                    = window.AGN.Lib.WM || (window.AGN.Lib.WM = {});
AGN.Lib.Dashboard             = window.AGN.Lib.Dashboard || (window.AGN.Lib.Dashboard = {});

AGN.Opt                       = window.AGN.Opt || (window.AGN.Opt = {});
AGN.Opt.Fields                = window.AGN.Opt.Fields || (window.AGN.Opt.Fields = {});
AGN.Opt.Forms                 = window.AGN.Opt.Forms || (window.AGN.Opt.Forms = {});
AGN.Opt.Controllers           = window.AGN.Opt.Controllers || (window.AGN.Opt.Controllers = {});
AGN.Opt.Templates             = window.AGN.Opt.Templates || (window.AGN.Opt.Templates = {});
AGN.Opt.TableCellRenderers    = window.AGN.Opt.TableCellRenderers || (window.AGN.Opt.TableCellRenderers = {});
AGN.Opt.TableHeaderComponents = window.AGN.Opt.TableHeaderComponents || (window.AGN.Opt.TableHeaderComponents = {});
AGN.Opt.Charts                = window.AGN.Opt.Charts || (window.AGN.Opt.Charts = {});
AGN.Opt.Validators            = window.AGN.Opt.Validators || (window.AGN.Opt.Validators = {});
AGN.Opt.Components            = window.AGN.Opt.Components || (window.AGN.Opt.Components = {});

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

AGN.isIE = /MSIE|Trident/.test(window.navigator.userAgent);

AGN.runAll = function($scope) {
  AGN.Lib.CoreInitializer.autorun($scope);
  AGN.Lib.DomInitializer.autorun($scope);
};
