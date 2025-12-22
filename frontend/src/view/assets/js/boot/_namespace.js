AGN = window.AGN || (window.AGN = {});

AGN.Lib                        = window.AGN.Lib || (window.AGN.Lib = {});
AGN.Lib.WM                     = window.AGN.Lib.WM || (window.AGN.Lib.WM = {});
AGN.Lib.Dashboard              = window.AGN.Lib.Dashboard || (window.AGN.Lib.Dashboard = {});
AGN.Lib.MailingContent         = window.AGN.Lib.MailingContent || (window.AGN.Lib.MailingContent = {});
AGN.Lib.Schedule               = window.AGN.Lib.Schedule || (window.AGN.Lib.Schedule = {});
AGN.Lib.LB                     = window.AGN.Lib.LB || (window.AGN.Lib.LB = {});

AGN.Opt                        = window.AGN.Opt || (window.AGN.Opt = {});
AGN.Opt.Fields                 = window.AGN.Opt.Fields || (window.AGN.Opt.Fields = {});
AGN.Opt.Forms                  = window.AGN.Opt.Forms || (window.AGN.Opt.Forms = {});
AGN.Opt.Controllers            = window.AGN.Opt.Controllers || (window.AGN.Opt.Controllers = {});
AGN.Opt.Templates              = window.AGN.Opt.Templates || (window.AGN.Opt.Templates = {});
AGN.Opt.TableCellRenderers     = window.AGN.Opt.TableCellRenderers || (window.AGN.Opt.TableCellRenderers = {});
AGN.Opt.TableActionsConditions = window.AGN.Opt.TableActionsConditions || (window.AGN.Opt.TableActionsConditions = {});
AGN.Opt.Table                  = window.AGN.Opt.Table || (window.AGN.Opt.Table = {'filters': {}, 'comparators': {}});
AGN.Opt.Validators             = window.AGN.Opt.Validators || (window.AGN.Opt.Validators = {});
AGN.Opt.Components             = window.AGN.Opt.Components || (window.AGN.Opt.Components = {});

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
  AGN.Lib.CoreInitializer.autorun($scope);
  AGN.Lib.DomInitializer.autorun($scope);
};
