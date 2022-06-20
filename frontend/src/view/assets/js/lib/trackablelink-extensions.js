(function () {
  var Action = AGN.Lib.Action;
  var TrackableLinkExtensions;
  var extensionRowTemplate;
  var $table;

  TrackableLinkExtensions = function () {
  }

  TrackableLinkExtensions.prototype.load = function (extensions, table) {
    $table = table;
    extensionRowTemplate = _.template(AGN.Opt.Templates['trackablelink-extension-table-row']);
    _.each(extensions, function (extension, index) {
      insertExtension(index, extension.name, extension.value);
    });
    appendLastRow('', '');
  };

  TrackableLinkExtensions.prototype.collect = function () {
    if ($table && $table.length) {
      return _.map($table.find('[data-extension-row]'), function (row) {
        var $row = $(row);
        return {name: getName($row), value: getValue($row)};
      }).filter(function (extension) {
        return extension.name && extension.value;
      })
    }
    return [];
  };

  Action.new({
    click: '[data-extension-add]',
    enterdown: '[data-extension-row]'
  }, function () {
    this.event.preventDefault();
    if (isUniqueRow('', '')) {
      replaceNewButtonWithDeleteButton();
      appendLastRow('', '');
    }
    $table.find('[data-extension-name]:last').focus();
  });

  Action.new({click: '[data-extension-delete]'}, function () {
    var currentRow = this.el.closest('tr');
    currentRow.remove();
  });

  Action.new({click: '[data-add-default-extensions]'}, function () {
    var lastRow = $table.find('tr:last');
    if (getName(lastRow) == '' && getValue(lastRow) == '') {
      lastRow.remove();
    } else {
      replaceNewButtonWithDeleteButton();
    }
    _.each(AGN.Opt.DefaultExtensions, function (extension) {
      appendLastRow(extension.name, extension.value);
    })
    appendLastRow('', '');
  });

  function insertExtension(index, name, value) {
    if (isUniqueRow(name, value)) {
      $table.append(extensionRowTemplate({index: index, name: name, value: value}));
    }
  }

  function appendLastRow(name, value) {
    var lastIndex = $table.find('[data-extension-row]:last-child').data('extension-row') || 0;
    insertExtension(++lastIndex, name, value);
  }

  function isUniqueRow(name, value) {
    var unique = true;
    _.each($table.find('[data-extension-row]'), function (row) {
      var $row = $(row);
      if (unique) {
        if (getName($row) == name && getValue($row) == value) {
          unique = false;
        }
      }
    });
    return unique;
  }

  function replaceNewButtonWithDeleteButton() {
    var newBtn = $table.find('#newExtensionBtn');
    newBtn.after("<a href='#' class='btn btn-regular btn-alert' data-extension-delete>" +
      "<i class='icon icon-trash-o'></i></a>");
    newBtn.remove();
  }

  function getName(row) {
    return row.find('[data-extension-name]').val();
  }

  function getValue(row) {
    return row.find('[data-extension-value]').val();
  }

  AGN.Lib.TrackableLinkExtensions = TrackableLinkExtensions;
})();
