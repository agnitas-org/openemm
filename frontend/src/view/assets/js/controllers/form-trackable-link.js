AGN.Lib.Controller.new('form-trackable-link', function() {

  var extensionRow;
  AGN.Opt.DefaultExtensions = {};

  this.addDomInitializer('trackable-link-extensions', function(){
    var config = this.config;
    AGN.Opt.DefaultExtensions = config.defaultExtensions;
    extensionRow = AGN.Lib.Template.prepare('extensions-table-row');
    loadExtensionBody(config.extensions);
  });

  this.addAction({submission: 'bulk-save'}, function() {
    var form = $('#userFormTrackableLinksForm');

    var $form = AGN.Lib.Form.get(form);
    collectExtensions($form, "commonExtensions");
    $form.submit();
  });

  this.addAction({submission: 'save'}, function() {
    var form = $('#userFormTrackableLinkForm');

    var $form = AGN.Lib.Form.get(form);
    collectExtensions($form, "extensions");
    $form.submit();
  });

  this.addAction({click: 'add-default-extensions'}, function() {
    _.each(AGN.Opt.DefaultExtensions, function(property) {
      appendLast(property.name, property.value, true);
    })
  });

  this.addAction({click: 'add-extension'}, function() {
    appendLast('', '');
  });

  this.addAction({click: 'delete-extension'}, function() {
    var rowIndex = $(this.el).data('property-id');
    remove(rowIndex);
  });

  this.addAction({click: 'delete-all-extensions'}, function() {
    clean();
  });

  function getName(row) {
    return row.find('[data-extension-name]').val();
  }

  function getValue(row) {
    return row.find('[data-extension-value]').val();
  }

  function isUniqueExtension(name, value) {
    var isUnique = true;
     _.each($('[data-extension-row]'), function(row) {
       var $row = $(row);
       if (isUnique) {
         if (getName($row) == name && getValue($row) == value) {
            isUnique = false;
          }
       }
      });
     return isUnique;
  }

  function insert(index, name, value) {
    var table = $('#extensions-table tbody');
    if (isUniqueExtension(name, value)) {
      table.append(extensionRow({index: index, name: name, value: value}));
    }
  }

  function remove(index) {
    $('[data-extension-row="' + index + '"]').remove();
  }

  function clean() {
    $('[data-extension-row]').remove();
  }

  function appendLast(name, value) {
    var lastIndex =
      $('#extensions-table tbody [data-extension-row]:last-child')
        .data('extension-row') || 0;
    insert(++lastIndex, name, value);
  }

  function loadExtensionBody(properties) {
    $('#extensions-table tbody').html('');
    _.each(properties, function(property, index){
      insert(index, property.name, property.value);
    });
  }

  function collectExtensions(form, fieldName) {
    _.each($('[data-extension-row]'), function(row, index) {
      var $row = $(row);
      form.setValueOnce(fieldName + '[' + index + '].name', getName($row));
      form.setValueOnce(fieldName + '[' + index + '].value',  getValue($row));
    })
  }

});