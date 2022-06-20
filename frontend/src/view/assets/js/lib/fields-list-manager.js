(function() {

  var defaultRowTemplate = _.template('<div class="form-group email-list-manager-row" data-action="change-trigger">' +
    '        <div class="col-sm-12">' +
    '            <div class="input-group">' +
    '                <div class="input-group-controls">' +
    '                    <input type="text" value="{{= value}}" class="form-control" size="42" maxlength="99"/>' +
    '                </div>' +
    '                <div class="input-group-btn">' +
    '                    <button type="button" class="btn btn-regular btn-primary email-list-manager-add-btn" data-action="create-email">' +
    '                        <i class="icon icon-plus"></i>' +
    '                    </button>' +
    '                </div>' +
    '            </div>' +
    '        </div>' +
    '    </div>');

  var deleteBtnTemplate =
    _.template('<button type="button" class="btn btn-regular btn-alert email-list-manager-delete-btn" data-action="delete-email">' +
    '        <i class="icon icon-trash-o"></i>' +
    '    </button>')

  function EmailsListManager(scope, data, options) {
    this.scope = scope;
    this.data = data;
    this.options = $.extend({
      rowTemplate: defaultRowTemplate,
      rowSelector: '.email-list-manager-row',
      addBtnSelector: '.email-list-manager-add-btn',
      deleteBtnSelector: '.email-list-manager-delete-btn',
      rowDataMapper: function(data) { return {value: data}; }
		}, options);

    var self = this;
    _.each(self.data, function (rowData) {
        addFieldRow(self.scope, rowData, null, self.options);
    });
  }

  EmailsListManager.initialize = function (scope, data, options) {
    return new EmailsListManager(scope, data, options);
  }

  EmailsListManager.prototype.createRowAfter = function(target) {
    var self = this;
    addFieldRow(self.scope, "", $(target), self.options)
  }

  EmailsListManager.prototype.deleteRow = function(target) {
    $(target).closest(this.options.rowSelector).remove();
    updateRemoveButtons(this.scope, this.options);
  }

  EmailsListManager.prototype.getJsonData = function() {
    var values = [];
    _.each(this.scope.find(this.options.rowSelector + " input"), function(rowInput) {
        var value = rowInput.value;
        if(value.trim() !== '') {
            values.push(value);
        }
    });
    return values;
  }

  function updateRemoveButtons(scope, options) {
    var $rows = scope.find(options.rowSelector);
    if ($rows.length === 1) {
      var $deleteButtons = $rows.find(options.deleteBtnSelector);
      $deleteButtons.remove();
    } else {
      _.each($rows, function(row) {
          var $row = $(row);
          var $deleteBtn = $row.find(options.deleteBtnSelector);

          if (!$deleteBtn.exists()) {
              $row.find('.input-group-btn').append(deleteBtnTemplate({}));
          }
      });
    }
  }

  function addFieldRow(scope, rowData, $target, options) {
    var content = options.rowTemplate(options.rowDataMapper(rowData))

    if ($target && $target.exists()) {
      $target.closest(options.rowSelector).after(content);
    } else {
      scope.append(content);
    }

    updateRemoveButtons(scope, options);
  }

  AGN.Lib.EmailsListManager = EmailsListManager;
})();
