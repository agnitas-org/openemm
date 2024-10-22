AGN.Lib.Action.new({click: '[data-popup]'}, function() {
  const loc = window.location.href;
  const baseurl = loc.substring(0, loc.lastIndexOf('/'));

  let url = this.el.data('popup');
  if (!url || url.indexOf('://') <= 0) {
      url = `${baseurl}/${url}`;
  }

  window.open(url,'help','width=800,height=600,left=0,top=0,scrollbars=yes');
});