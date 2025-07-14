AGN.Lib.Controller.new('widget-creation', function () {

  let rdirDomain;

  this.addDomInitializer('widget-creation', function() {
    rdirDomain = this.config.rdirDomain;
  });

  this.addAction({click: 'generate-widget'}, function () {
    const form = AGN.Lib.Form.get($('#settings-tile form:visible'));
    if (!form.validate()) {
      return;
    }

    form.jqxhr().done(resp => {
      if (resp.success) {
        showPreview(resp.data);
      }

      AGN.Lib.JsonMessages(resp.popups);
    });
  });

  function showPreview(token) {
    const widgetCode = `
      <div class="emm-widget" data-emm-widget="${_.escape(token)}"></div> <script async src="${rdirDomain}/assets/widgets.js" type="application/javascript"></script>
    `.trim();

    const $previewTileBody = $('#preview-tile .tile-body');
    $previewTileBody.html(AGN.Lib.Template.text('widget-preview-block', {widgetCode}));

    AGN.Lib.CoreInitializer.run('tooltip', $previewTileBody);
  }
});
