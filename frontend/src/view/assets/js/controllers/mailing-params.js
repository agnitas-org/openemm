AGN.Lib.Controller.new('mailing-params', function() {
  var rowTemplate = _.template(AGN.Opt.Templates['mailing-param-row']);
  var $table;
  var isChangeable = false;

  this.addDomInitializer('mailing-params', function() {
    $table = $('#mailingParamsTable tbody');
    isChangeable = this.config.isChangeable;
    loadTableRows(this.config.params);
  });

  function loadTableRows(rows) {
    _.each(rows, function(row, index) {
      insertRow(index, row.name, row.value, row.description);
    });
    if (isChangeable) {
      appendLastRow('', '', '');
    }
  }

  function addParam() {
    if (isUniqueRow('', '')) {
      replaceNewButtonWithDeleteButton();
      appendLastRow('', '', '');
    }
  }
  
  function getNextInputToFocus($current) {
    if (!$current.is("[data-param-description]")) {
      return $current.closest('td').next().find('input:text');
    }
    var $nameInput = $current.closest('tr').find('[data-param-name]');
    if (!$nameInput.val()) {
      return $nameInput;
    }
    if (!$current.is('[data-action="param-enterdown"]:last')) {
      return $current.closest('tr').next().find('input:text:first');
    }
    addParam();
    return $table.find('[data-param-name]:last');
  }
  
  this.addAction({enterdown: 'param-enterdown'}, function() {
    this.event.preventDefault();
    getNextInputToFocus(this.el).focus();
  });

  this.addAction({click: 'add-param-row'}, function() {
    this.event.preventDefault();
    addParam();
    $table.find('[data-param-name]:last').focus();
  });

  this.addAction({click: 'delete-param-row'}, function() {
    this.el.closest('tr').remove();
  });

  function insertRow(index, name, value, description) {
    if (isUniqueRow(name, value)) {
      $table.append(rowTemplate({
        index: index,
        name: name,
        value: value,
        description: description,
        isChangeable: isChangeable
      }));
    }
  }

  function getLastParamRow() {
    return $table.find('[data-param-row]:last-child');
  }

  function appendLastRow(name, value, description) {
    var lastIndex = getLastParamRow().data('param-row') || 0;
    insertRow(++lastIndex, name, value, description);
  }

  function isUniqueRow(name, value) {
    var unique = true;
    _.each($table.find('[data-param-row]'), function(row) {
      var $row = $(row);
      if (unique) {
        if (getCellValByCol($row, 'name') == name && getCellValByCol($row, 'value') == value) {
          unique = false;
        }
      }
    });
    return unique;
  }

  function replaceNewButtonWithDeleteButton() {
    var newBtn = $table.find('#newParamBtn');
    newBtn.after("<a href='#' class='btn btn-regular btn-alert' data-action='delete-param-row'>" +
      "<i class='icon icon-trash-o'></i></a>");
    newBtn.remove();
  }

  function getCellValByCol($row, col) {
    return $row.find('[data-param-' + col + ']').val();
  }
});
