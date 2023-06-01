AGN.Lib.Controller.new('reference-content-settings-new', function() {
  var Template = AGN.Lib.Template;
  var isAvailable = false;
  var createItemHtml;
  var items = [];
  var initialItems = [];
  var $form;

  function setEnabled(enabled) {
    $('#tile-mailingReferenceContent').toggleClass('hidden', !enabled);
  }

  function selectTable() {
    var $select = $('#referenceTableSelect');
    var $option = $select.children('option:selected');

    $('#keyColumnText').val($option.exists() ? $option.data('key-column') : '');
  }

  function createItem(index, value) {
    return $(createItemHtml({name: 'Item' + index, value: value}));
  }

  function addItem(value) {
    var $item = createItem(items.length + 1, value);
    $('#reference-content-items').append($item);
    items.push($item);
  }

  this.addDomInitializer('reference-content-settings-new', function() {
    $form = $('#mailingSettingsForm');
    selectTable();

    createItemHtml = Template.prepare('mailing-reference-content-item');
    isAvailable = this.config.isAvailable;
    items = [];

    if (isAvailable) {
      if ($.isArray(this.config.items)) {
        this.config.items.forEach(function(value) {
          addItem(value);
        });

        initialItems = this.config.items.slice();
      }

      setEnabled(this.config.isEnabled);
    } else {
      setEnabled(false);
    }

    $form.dirty('setAsClean');
    $form.dirty('refreshEvents');
  });

  this.addAction({change: 'enable-reference-content'}, function() {
    if (isAvailable) {
      setEnabled(this.el.prop('checked'));
    } else {
      AGN.Lib.Messages(t('defaults.warning'), t('mailing.default.item_referencetable_warning'), 'warning');
      $(this.el).prop('checked', false).trigger('change.dirty');
    }
  });

  this.addAction({change: 'select-reference-table'}, function() {
    selectTable();
  });

  this.addAction({click: 'add-reference-content-item'}, function() {
    addItem('');
    $form.dirty('setAsDirty');
  });

  this.addAction({click: 'delete-reference-content-item'}, function() {
    var row = this.el.closest('.form-group')[0];
    var position = items.findIndex(function($row) { return $row[0] == row; });

    while (position + 1 < items.length) {
      var $input1 = items[position].find('input');
      var $input2 = items[position + 1].find('input');

      $input1.val($input2.val());
      position++;
    }

    items[position].remove();
    items.pop();

    var isTheSameItems = (initialItems.length === items.length) && (initialItems.every(function (value) {
      return items.includes(value);
    }));

    if (!isTheSameItems) {
      $form.dirty('setAsDirty');
    }
  });
});
