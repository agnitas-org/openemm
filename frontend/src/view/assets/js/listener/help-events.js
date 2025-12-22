AGN.Lib.Action.new({click: '[data-help]'}, function () {
  AGN.Lib.Help.show(this.el);
  this.event.preventDefault();
});