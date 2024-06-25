// TODO check if comment lines are redundant and remove them

// agGrid.BaseFilter.prototype.createConditionTemplate = function (type) {
//   return "<div class=\"ag-filter-condition\">\n            <div class=\"ag-filter-condition-type radio\"><label class=\"\" for=\"andId\"><input id=\"andId\" type=\"radio\" class=\"and\" name=\"booleanLogic\" value=\"AND\" checked=\"checked\" /> AND</label>\n            <label  class=\"\" for=\"orId\"><input id=\"orId\" type=\"radio\" class=\"or\" name=\"booleanLogic\" value=\"OR\" /> OR</label></div>\n            <div>" + this.createConditionBody(type) + "</div>\n        </div>";
// };
//
// agGrid.TextFilter.prototype.bodyTemplate = function (type) {
//   var translate = this.translate.bind(this);
//   var fieldId = type == 0 ? "filterText" : "filterConditionText";
//   return "<div class=\"ag-filter-body\">\n            <input class=\"ag-filter-filter form-control\" id=" + fieldId + " type=\"text\" placeholder=\"" + translate('filterOoo', 'Filter...') + "\"/>\n        </div>";
// };
//
// agGrid.TextFilter.prototype.generateFilterHeader = function (type) {
//   var _this = this;
//   var defaultFilterTypes = this.getApplicableFilterTypes();
//   var restrictedFilterTypes = this.filterParams.filterOptions;
//   var actualFilterTypes = restrictedFilterTypes ? restrictedFilterTypes : defaultFilterTypes;
//   var optionsHtml = actualFilterTypes.map(function (filterType) {
//       var localeFilterName = _this.translate(filterType);
//       return "<option value=\"" + filterType + "\">" + localeFilterName + "</option>";
//   });
//   var readOnly = optionsHtml.length == 1 ? 'disabled' : '';
//   var id = type == 0 ? 'filterType' : 'filterConditionType';
//   return optionsHtml.length <= 0 ?
//       '' :
//       "<div>\n                <select class=\"ag-filter-select form-control\" id=\"" + id + "\" " + readOnly + ">\n                    " + optionsHtml.join('') + "\n                </select>\n            </div>";
// };
//
// agGrid.NumberFilter.prototype.bodyTemplate = function (type) {
//   var translate = this.translate.bind(this);
//   var fieldId = type == 0 ? "filterText" : "filterConditionText";
//   var filterNumberToPanelId = type == 0 ? "filterNumberToPanel" : "filterNumberToPanelCondition";
//   var fieldToId = type == 0 ? "filterToText" : "filterToConditionText";
//   return "<div class=\"ag-filter-body\">\n            <div>\n                <input class=\"ag-filter-filter form-control\" id=\"" + fieldId + "\" type=\"text\" placeholder=\"" + translate('filterOoo') + "\"/>\n            </div>\n             <div class=\"ag-filter-number-to\" id=\"" + filterNumberToPanelId + "\">\n                <input class=\"ag-filter-filter form-control\" id=\"" + fieldToId + "\" type=\"text\" placeholder=\"" + translate('filterOoo') + "\"/>\n            </div>\n        </div>";
// };
//
// agGrid.NumberFilter.prototype.generateFilterHeader = agGrid.TextFilter.prototype.generateFilterHeader;
//
// agGrid.TextFilter.prototype.createConditionTemplate = function (type) {
//   return "<div class=\"ag-filter-condition\">\n            <div class=\"ag-filter-condition-type radio\"><label class=\"\" for=\"andId\"><input id=\"andId\" type=\"radio\" class=\"and\" name=\"booleanLogic\" value=\"AND\" checked=\"checked\" /> AND</label>\n            <label  class=\"\" for=\"orId\"><input id=\"orId\" type=\"radio\" class=\"or\" name=\"booleanLogic\" value=\"OR\" /> OR</label></div>\n            <div>" + this.createConditionBody(type) + "</div>\n        </div>";
// };