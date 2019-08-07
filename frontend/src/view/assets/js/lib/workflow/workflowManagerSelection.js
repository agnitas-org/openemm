(function() {
  var CampaignManagerSelection = {

    selected: [],
    prevSelected: null,
    lastSelected: null,
    onSelectionChanged: null,
    nodeSelector: "",

    init: function(params) {
      this.nodeSelector = params.nodeSelector;
      this.onSelectionChanged = params.onSelectionChanged;
    },

    handleClick: function(e, iconNode) {
      e.stopPropagation();

      if (this.selected.length > 0) {
        this.prevSelected = this.selected[this.selected.length - 1];
      }

      this.validateMultiselection(e);

      // if the element is already selected and we clicked
      // it with Shift key down - just unselect it
      if ($.inArray(iconNode.attr("id"), this.selected) != -1) {
        if (this.isMultiselection(e)) {
          this.unselect(iconNode);
        }
      }
      // if the element is not contained in current selection - select it
      else {
        this.select(iconNode);
      }

      this.onSelectionChanged();
    },

    isMultiselection: function(e) {
      return e.shiftKey;
    },

    validateMultiselection: function(e) {
      // if it's not multiselection we need first to unselect
      // all currently selected elements
      if (!this.isMultiselection(e)) {
        this.clear();
      }
    },

    select: function(iconNode) {
      this.selected.push(iconNode.attr("id"));
      iconNode.addClass("selected");
      this.lastSelected = iconNode.attr("id");
      this.onSelectionChanged();
    },

    unselect: function(iconNode) {
      this.selected.splice($.inArray(iconNode.attr("id"), this.selected), 1);
      iconNode.removeClass("selected");
      this.onSelectionChanged();
    },

    isSelected: function(nodeId) {
      return $.inArray(nodeId, this.selected) != -1;
    },

    clear: function() {
      this.selected = [];
      $(this.nodeSelector).removeClass("selected");
      this.onSelectionChanged();
    },

    getSelected: function() {
      return this.selected;
    }
  };

  AGN.Lib.WM.CampaignManagerSelection = CampaignManagerSelection;
})();
