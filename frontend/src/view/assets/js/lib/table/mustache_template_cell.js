var cachedTemplates = [];
(function(){
  var Template = AGN.Lib.Template;

  var MustacheTemplateCellRender = function () {};
  // gets called once before the renderer is used
  MustacheTemplateCellRender.prototype.init = function(params) {
    this.eGui = document.createElement('div');
    var preparedTemplate;
    var templateName = params.templateName;
    if(cachedTemplates[templateName]) {
      preparedTemplate = cachedTemplates[templateName];
    } else {
      preparedTemplate = Template.prepare(templateName);
      cachedTemplates[templateName] = preparedTemplate;
    }
      this.eGui.innerHTML = preparedTemplate({value: params.value});
  };

  // gets called once when grid ready to insert the element
  MustacheTemplateCellRender.prototype.getGui = function() {
    return this.eGui;
  };

  // gets called whenever the user gets the cell to refresh
  MustacheTemplateCellRender.prototype.refresh = function() {
    return false;
  };

  // gets called when the cell is removed from the grid
  MustacheTemplateCellRender.prototype.destroy = function() {
    // do cleanup, remove event listener from button
  };

  AGN.Opt.TableCellRenderers['MustacheTemplateCellRender'] = MustacheTemplateCellRender;

})();
