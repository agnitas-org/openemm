(function(){

  var Table = AGN.Lib.Table,
      SelectHeaderComponent = function() {}

  SelectHeaderComponent.prototype.init = function (agParams) {
      this.agParams = agParams;
      this.eGui = document.createElement('div');
      this.eGui.innerHTML = '' +
          '<input type="checkbox" data-form-bulk="bulkID">'
  };

  SelectHeaderComponent.prototype.getGui = function () {
      return this.eGui;
  };

  SelectHeaderComponent.prototype.destroy = function () {
  };

  AGN.Lib.SelectHeaderComponent = SelectHeaderComponent;


})();