(function($) {

  var CampaignManagerScale = function(properties) {
    var externalScale = 0;
    var self = this;
    var campaignManagerSettings = properties.campaignManagerSettings;

    var getDefaultScalePosition = function() {
      return (campaignManagerSettings.defaultScale - campaignManagerSettings.minScale) / ((campaignManagerSettings.maxScale - campaignManagerSettings.minScale) / 100);
    };

    var currentScaleSliderPosition = getDefaultScalePosition();

    this.getCurrentScale = function() {
      if (externalScale == 0) {
        return campaignManagerSettings.minScale + currentScaleSliderPosition * (campaignManagerSettings.maxScale - campaignManagerSettings.minScale) / 100;
      }
      else {
        return externalScale;
      }
    };

    this.setExternalScale = function(scale) {
      externalScale = scale;
    };

    this.getScaledGridSize = function() {
      return campaignManagerSettings.gridSize * self.getCurrentScale();
    };

    this.getScaledNodeSize = function() {
      return campaignManagerSettings.nodeSize * self.getCurrentScale();
    };

    this.getCurrentScaleSliderPosition = function() {
      return currentScaleSliderPosition;
    };

    this.setCurrentScaleSliderPosition = function(position) {
      currentScaleSliderPosition = position;
    };

    this.coordinateToPx = function(coordinate) {
      return Math.round(coordinate * self.getScaledGridSize());
    };

    this.pxToCoordinate = function(px) {
      return Math.round(px / self.getScaledGridSize());
    };

    this.pxToMaxCoordinate = function(px) {
      return Math.ceil(px / self.getScaledGridSize());
    };

    this.getLineWidth = function() {
      return Math.round(campaignManagerSettings.lineWidth * self.getCurrentScale());
    };

    this.getArrowLength = function() {
      return Math.round(campaignManagerSettings.arrowSize * self.getCurrentScale());
    };

    this.getArrowWidth = function() {
      return Math.round(campaignManagerSettings.arrowSize * self.getCurrentScale());
    };

    this.getLabelFontSize = function() {
      return Math.round(campaignManagerSettings.labelFontSize * self.getCurrentScale());
    };
  };

  AGN.Lib.WM.CampaignManagerScale = CampaignManagerScale;

})(jQuery);