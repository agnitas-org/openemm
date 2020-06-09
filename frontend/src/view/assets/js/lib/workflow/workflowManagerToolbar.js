(function($) {
  function bind(thisArg, callback, returnValue) {
    if (typeof callback === "function") {
      var isReturnValueReplaced = arguments.length > 2;
      return function() {
        var value = callback.apply(thisArg, arguments);
        return isReturnValueReplaced ? returnValue : value;
      }
    } else {
      return callback;
    }
  }

  var CampaignManagerToolbar = {
    arrowMode: false,
    deleteSelected: false,
    connectSelected: false,
    undoSelected: false,
    zoomMinSelected: false,
    zoomMiddleSelected: false,
    zoomMaxSelected: false,
    doAutoLayout: false,

    init: function(params) {
      $.extend(this, params);

      $('#arrowButton').on("click", bind(this, this.arrowButtonClick));
      $('#autoLayout, #autoLayoutItem').on("click", bind(this, this.doAutoLayout));
      $('#deleteButton, #deleteItem').on("click", bind(this, this.deleteSelected));
      $('#undoButton, #undoItem').on("click", bind(this, this.undoSelected, false));
      $('#zoomMin, #zoomMinItem').on("click", bind(this, this.zoomMinSelected, false));
      $('#zoomMiddle, #zoomMiddleItem').on("click", bind(this, this.zoomMiddleSelected, false));
      $('#zoomMax, #zoomMaxItem').on("click", bind(this, this.zoomMaxSelected, false));
    },

    arrowButtonClick: function() {
      if (this.arrowMode) {
        this.arrowMode = false;
      } else {
        if (!this.connectSelected()) {
          this.arrowMode = true;
        }
      }

      if (this.arrowMode) {
        $('#arrowButton').addClass('button-selected');
      } else {
        $('#arrowButton').removeClass('button-selected');
      }
    },

    setUndoAvailable: function(isAvailable) {
      if (isAvailable) {
        $('#undoButtonFake').css("visibility", "hidden");
        $('#undoButton').removeClass("backgroundImageNone");
      } else {
        $('#undoButtonFake').css("visibility", "visible");
        $('#undoButton').addClass("backgroundImageNone");
      }
      $('#undoItem').prop('disabled', !isAvailable);
    },

    setDeletionAvailable: function(isAvailable) {
      $('#deleteButton, #deleteItem').prop('disabled', !isAvailable);
    }

  };

  AGN.Lib.WM.CampaignManagerToolbar = CampaignManagerToolbar;
})(jQuery);
