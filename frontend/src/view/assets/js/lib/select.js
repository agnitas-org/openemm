(function(){

  var Select;

  Select = function($select) {
    this.$select = $select;
    this.select  = $select[0];
    this.api =     function() {
      args = Array.prototype.slice.call(arguments, 0);
      $select.select2.apply($select, args);
    }
  };

  Select.get = function($needle) {
    var $select,
        selectObj;

    $select = $needle;
    selectObj = $select.data('_select');

    if (!selectObj) {
      selectObj = new Select($select);
      $select.data('_select', selectObj);
    }

    return selectObj;
  };

  // map of {value: option text}
  Select.prototype.map = function() {
    var self = this;

    return _.reduce(self.values(), function(obj, val) {
      obj[val] = self.$select.find("option[value='" + val +"']").html();

      return obj;
    }, {});
  };

  // array of values
  Select.prototype.values = function() {
    return _.map(this.$select.find("option"), function(el) {
      return $(el).val();
    });
  };

  Select.prototype.clear = function() {
    this.api('val', "");
  };

  Select.prototype.selectValue = function(val) {
    this.api('val', val);
  };

  Select.prototype.getFirstValue = function() {
    return this.$select.find("option:first").val();
  };

  Select.prototype.selectFirstValue = function() {
    this.api('val', this.getFirstValue());
  };

  Select.prototype.selectValueOrSelectFirst = function(values) {
    var allValuesFound  = false,
        valuesAvailable = this.values();

    if (!_.isArray(values)) {
      values = [values];
    }

    allValuesFound = _.all(values, function(val) {
      return valuesAvailable.indexOf(val + "") != -1
    });

    if (allValuesFound && values.length != 0) {
      this.selectValue(values);
    } else {
      this.selectFirstValue();
    }
  };

  Select.prototype.resetOptions = function() {
    this.$select.html("");
  };

  // format of data: [{id: value, text: "option text"} ...]
  Select.prototype.setOptions = function(data) {
    var options;

    options = _.reduce(data, function(html, option) {
      html += '<option value="' + option.id + '">' + option.text + '</option>';

      return html;
    }, "");

    this.$select.html(options);
  };

  Select.prototype.addFormattedOption = function(option) {
    this.$select.append(option);
  };

  Select.prototype.getSelectedValue = function() {
    var value = this.$select.val();
    return value ? value : "";
  };

  AGN.Lib.Select = Select;

})();
