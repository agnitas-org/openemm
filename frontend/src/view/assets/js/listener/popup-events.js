AGN.Lib.Action.new({click: '[data-popup]'}, function () {
  const loc = window.location.href;
  const baseurl = loc.substring(0, loc.lastIndexOf('/'));

  let url = this.el.data('popup');
  if (!url || url.indexOf('://') <= 0) {
    url = `${baseurl}/${url}`;
  }

  const options = {
    width: 800,
    height: 600,
    url: url
  };

  if (this.el.is('[data-popup-options]')) {
    _.merge(options, AGN.Lib.Helpers.objFromString(this.el.data('popup-options')));
  }

  const screenWidth = window.screen.width;
  const screenHeight = window.screen.height;

  const left = screenWidth > options.width ? ((screenWidth - options.width) / 2) : 0;
  const top = screenHeight > options.height ? ((screenHeight - options.height) / 2) : 0;

  window.open(options.url, 'help', `width=${options.width},height=${options.height},top=${top},left=${left}`);
});