(function(){

  var NoLabelHeader = function() {};

  NoLabelHeader.prototype.init = function (agParams) {
      this.agParams = agParams;
      this.eGui = document.createComment('No label');
  };

  NoLabelHeader.prototype.getGui = function () {
      return this.eGui;
  };

  NoLabelHeader.prototype.destroy = function () {
  };

  AGN.Opt.TableHeaderComponents['NoLabelHeader'] = NoLabelHeader;

})();
