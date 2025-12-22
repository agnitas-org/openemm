AGN.Lib.Action.new({click: '[data-msg]'}, function () {
  AGN.Lib.Messages(
    this.el.data('msg'),
    this.el.data('msg-content'),
    this.el.data('msg-type')
  );
});