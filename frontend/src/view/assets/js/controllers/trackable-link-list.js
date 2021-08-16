AGN.Lib.Controller.new('trackable-link-list', function() {
  var scrollToElemId;
  var extensionRow;
  AGN.Opt.DefaultExtensions = {};

  this.addDomInitializer('trackable-link-extensions', function(){
    var config = this.config;
    AGN.Opt.DefaultExtensions = config.defaultExtensions;
    extensionRow = AGN.Lib.Template.prepare('extensions-table-row');
    var extensionsToLoad = isModalOpened() ? config.bulkExtensions : config.extensionsForAllLinks;
    loadExtensionBody(extensionsToLoad);
  });

  this.addAction({'change': 'elem-edited'}, function() {
    scrollToElemId = this.el.attr('id');
  });

  this.addAction({'scrollTo': 'scroll-to'}, function() {
    var $scrollContainer,
      $target,
      highestOffsetTop,
      targetOffsetTop;

    if (scrollToElemId) {
      $target = $(this.el).find('#' + scrollToElemId);
    } else if ($(this.el).find('[data-sizing="scroll-top-target"]').length > 0) {
      $target = $(this.el).find('[data-sizing="scroll-top-target"]');
      $target.removeAttr('data-sizing');
    } else if (location.href.indexOf('scrollToLinkId') > 0) {
      var parser = document.createElement('a'),
        params,
        targetId;
      parser.href = location.href;
      params = parser.search.substring(1, parser.search.length).split('&');
      for (var i = 0; i < params.length; i++) {
        if (params[i].indexOf('scrollToLinkId') >= 0) {
          targetId = params[i].substring(params[i].indexOf('=') + 1, params[i].length);
          $target = $('#link-'+targetId);
        }
      }
    }

    if ($target && $target.length > 0) {
      $scrollContainer = $target.closest('[data-sizing="scroll"]');
      if ($scrollContainer.length > 0) {
        highestOffsetTop = $scrollContainer.find(':first-child').offset().top;
        targetOffsetTop = $target.offset().top;
        $scrollContainer.scrollTop(0);
        $scrollContainer.scrollTop(targetOffsetTop - highestOffsetTop);
      }
    }
  });

  this.addAction({'click': 'delete-link'}, function() {
    var element = this.el;
    var linkId = element.data('link-id');
    $(element).closest('[data-action="elem-edited"]').trigger('change');
    $('#linkPropertyTable').find('#linkProperty_' + linkId).remove();
  });

  this.addAction({click: 'save-bulk-actions', enterdown: 'description-enterdown, bulkActionExtensionsEnterdown'}, function() {
    var form = AGN.Lib.Form.get($("#trackableLinkForm"));
    var bulkActionsFormData = $('#bulkActionsForm').serializeFormDataObject();
    form._data = {}

    $.each(bulkActionsFormData, function(k, v) {
      form.setValue(k, v);
    });
    form.setValue('action', ACTION_SAVE_ALL);
    form.setValue('modifyExtensionsForAllLinks', false);
    collectExtensions(form);
    form.submit();
    AGN.Lib.Modal.getWrapper(this.el).modal('hide');
  });

  this.addAction({click: 'save', enterdown: 'settingsExtensionsEnterdown'}, function() {
    this.event.preventDefault();
    var form = AGN.Lib.Form.get($("#trackableLinkForm"));
    form._data = {}

    form.setValue('action', ACTION_SAVE_ALL);
    form.setValue('bulkModifyLinkExtensions', false);
    keepBulkSettingsUnchanged(form);
    collectExtensions(form);
    form.submit();
  });

  this.addAction({click: 'add-default-extensions'}, function() {
    var lastRow = getExtensionsTable().find('tr:last');
    if (getName(lastRow) == '' && getValue(lastRow) == '') {
      lastRow.remove();
    } else {
      replaceNewButtonWithDeleteButton();
    }
    _.each(AGN.Opt.DefaultExtensions, function(value, name) {
      appendLast(name, value, false);
    })
    appendLast('', '');
  });

  this.addAction({click: 'add-extension'}, function() {
    if (isUniqueRow('', '')) {
      replaceNewButtonWithDeleteButton();
      appendLast('', '');
    }
  });

  this.addAction({click: 'delete-extension'}, function() {
    var currentRow = this.el.closest('tr');
    currentRow.remove();
  });

  function getName(row) {
    return row.find('[data-extension-name]').val();
  }

  function getValue(row) {
    return row.find('[data-extension-value]').val();
  }

  function isUniqueRow(name, value) {
    var unique = true;
    _.each(getExtensionsTable().find('[data-extension-row]'), function(row) {
      var $row = $(row);
      if (unique) {
        if (getName($row) == name && getValue($row) == value) {
          unique = false;
        }
      }
    });
    return unique;
  }

  function insertExtension(index, name, value) {
    if (isUniqueRow(name, value)) {
      getExtensionsTable().append(extensionRow({index: index, name: name, value: value}));
    }
  }

  function appendLast(name, value) {
    var lastIndex = getExtensionsTable().find('[data-extension-row]:last-child').data('extension-row') || 0;
    insertExtension(++lastIndex, name, value);
  }

  function loadExtensionBody(properties) {
    _.each(properties, function (property, index) {
      insertExtension(index, property.propertyName, property.propertyValue);
    });
    appendLast('', '');
  }

  function collectExtensions(form) {
    _.each(getExtensionsTable().find('[data-extension-row]'), function(row, index) {
      var $row = $(row);
      var name = getName($row);
      var value = getValue($row);
      if (name && value) {
        form.setValue('commonExtension[' + index + '].propertyName', name);
        form.setValue('commonExtension[' + index + '].propertyValue', value);
      }
    })
  }

  function keepBulkSettingsUnchanged(form) {
    form.setValue('globalUsage', KEEP_UNCHANGED);
    form.setValue('linkAction', KEEP_UNCHANGED);
    form.setValue('deepTracking', KEEP_UNCHANGED);
    form.setValue('bulkLinkStatic', KEEP_UNCHANGED);
  }

  function replaceNewButtonWithDeleteButton() {
    var newBtn = getExtensionsTable().find('#newExtensionBtn');
    newBtn.after("<a href='#' class='btn btn-regular btn-alert' data-action='delete-extension'>" +
      "<i class='icon icon-trash-o'></i></a>");
    newBtn.remove();
  }

  function getExtensionsTable() {
    return isModalOpened() ? $('#bulkActionExtensions tbody') : $('#settingsExtensions tbody');
  }

  function isModalOpened() {
    return $('.modal').is(':visible');
  }
});
