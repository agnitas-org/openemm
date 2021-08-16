AGN.Lib.Controller.new('action-view', function() {
  var moduleList;
  var operationTypes = {};

  function ModuleList() {
    var self = this;
    self.$container = $('#module-list');

    self.$container.on('remove-module', function() {
      var list = self.$container.find('[data-action-module] .headline span.module-count');
      $.each(list, function(index, el) {
        $(el).html((index + 1) + '.');
      });
    });

    self.$container.on('add-new-module', function() {
      var allModules = self.$container.find('[data-action-module]');
      var lastModule = allModules.last();
      lastModule.find('.headline span.module-count').html(allModules.length + '.');
    });
  }

  ModuleList.prototype.clean = function () {
    this.$container.empty();
  };

  ModuleList.prototype.getNextIndex = function() {
    var list = this.$container.find('[data-module-content');
    var index = 0;
    if (list.length > 0) {
      var lastIndex = list.last().data('module-content');
      index = lastIndex + 1;
    }
    return index;
  };

  ModuleList.prototype.addModule = function (module) {
    if (module && module.type) {
      var self = this;
      module.index = self.getNextIndex();

      var moduleTile = AGN.Lib.Template.dom('common-module-data',
        {moduleName: t('triggerManager.operation.' + module.type)});

      var moduleContent = AGN.Lib.Template.text('module-' + module.type, module.getParameters());
      moduleTile.filter('.inline-tile').append(moduleContent);

      self.$container.append(moduleTile);
      AGN.runAll(moduleTile);
      self.$container.trigger('add-new-module');
    }
  };

  ModuleList.prototype.deleteModule = function (index) {
    if (index >= 0) {
      var content = this.$container.find("[data-module-content=" + index + "]");
      var module = content.parent('[data-action-module]');
      var separator = module.next('.tile-separator');
      module.remove();
      separator.remove();
      this.$container.trigger("remove-module");
    }
  };

  ModuleList.prototype.toJson = function() {
    var json = [];
    this.$container.find('[data-module-content]').each(function(index, m){
      json.push(Module.toJson($(m), index));
    });
    return json;
  };

  ModuleList.prototype.getSubmissionJson = function() {
    var self = this;
    return JSON.stringify(self.toJson());
  };

  function Module(type, data, count) {
    this.type = type;
    this.index = count || 0;
    this.id = data.id || 0;
    this.data = $.extend(Module.defaults(type), data);
  }

  Module.defaults = function(type) {
    var data = {};
    switch (type) {
      case 'ActivateDoubleOptIn':
        data.forAllLists = false;
        data.mediaTypeCode = 0;
        break;
      case 'ContentView':
        data.tagName = '';
        break;
      case 'ExecuteScript':
        data.script = '';
        break;
      case 'GetArchiveList':
        data.campaignID = 0;
        break;
      case 'GetArchiveMailing':
        data.expireDate = null;
        break;
      case 'GetCustomer':
        data.loadAlways = false;
        break;
      case 'IdentifyCustomer':
        data.keyColumn = '';
        data.passColumn = '';
        break;
      case 'SendMailing':
        data.mailingID = 0;
        data.delayMinutes = 0;
        data.bcc = '';
        break;
      case 'ServiceMail':
        data.toAddress = '';
        data.fromAddress = '';
        data.replyAddress = '';
        data.subjectLine = '';
        data.mailtype = 0;
        data.textMail = '';
        data.htmlMail = '';
        break;
      case 'SubscribeCustomer':
        data.doubleCheck = false;
        data.keyColumn = '';
        data.doubleOptIn = false;
        break;
      case 'UnsubscribeCustomer':
        //no data
        break;
      case 'UpdateCustomer':
        data.useTrack = false;
        data.columnName = '';
        data.updateValue = '';
        data.updateType = 1;
        data.trackingPointId = -1;
        break;
    }
    return data;
  };

  Module.toJson = function($module, index) {
    var moduleJson = {};
    moduleJson.index = index;
    $module.find('[name^="modules[]"]').each(function (i, field) {
      var $field = $(field);
      var name = $field.prop('name') || '';

      var fieldName = name.replace('modules[].', '');

      var value = '';

      if ($field.is('select')) {
        value = AGN.Lib.Select.get($field).getSelectedValue();
      } else if ($field.is('input') && $field.prop('type') === 'checkbox') {
        value = $field.prop('checked');
      } else {
        value = $field.val();
      }

      moduleJson[fieldName] = value;
    });
    return $.extend(Module.defaults(moduleJson.type), moduleJson);
  };

  Module.prototype.getParameters = function() {
      var extraData = {
          type: this.type,
          index: this.index,
          id: this.id
      };
      return _.merge({}, this.data, extraData);
  };

    Module.create = function(type) {
    return new Module(type, {});
  };

  Module.deserialize = function(object, count) {
    var type = operationTypes[object.operationType];
    return new Module(type, object, count);
  };

  this.addDomInitializer('action-view', function () {
    var $form = $(this.el);
    var form = AGN.Lib.Form.get($form);
    form.loader().show();

    var config = this.config;
    operationTypes = this.config.operationTypes;

    moduleList = new ModuleList();
    moduleList.clean();

    var modules = [];

    if (config.modules) {
      modules = JSON.parse(config.modules).map(function (object, count) {
        return Module.deserialize(object, count);
      });
    }

    modules.forEach(function (elem) {
      moduleList.addModule(elem);
    });

    form.initFields();

    $form.removeClass('hidden');
    form.loader().hide();
  });

  this.addAction({click: 'add-new-module'}, function() {
    var $elem = $(this.el);
    var config = _.merge({}, {}, AGN.Lib.Helpers.objFromString($elem.data('config')));
    var typeSelector = $(config.moduleTypeSelector);

    moduleList.addModule(Module.create(typeSelector.val()));

    AGN.Lib.Form.get($elem).initFields();
  });

  this.addAction({click: 'action-delete-module'}, function() {
    var $elem = $(this.el);
    moduleList.deleteModule($elem.data('property-id'));
  });

  this.addAction({submission: 'save-action-data'}, function() {
    var form = AGN.Lib.Form.get($(this.el));
    form.setValueOnce("modulesSchema", moduleList.getSubmissionJson());
    form.submit();
  });

  this.addDomInitializer('update-customer-module', function() {
    var $el = $(this.el);
    var checkedUseTrack = $el.find('[data-action="toggle-if-first"]:checked');
    toggleTrackingPointData($el, checkedUseTrack.exists());
  });

  this.addAction({click: 'toggle-if-first'}, function() {
    var $el = $(this.el);
    toggleTrackingPointData($el, $el.prop('checked'));
  });

  function toggleTrackingPointData($el, isEnabled) {
    var $elModule = $($el.closest('[data-action-module]'));

    var trackingPointField = $elModule.find('[data-useTrack-trackingPointId]');
    var updateValueField = $elModule.find('[data-useTrack-updateValue]');

    if (!trackingPointField.exists()) {
      updateValueField.show();
      return;
    }

    if ($elModule.index() === 0) {
      if (isEnabled) {
        trackingPointField.show();
        updateValueField.hide();
      } else {
        trackingPointField.hide();
        updateValueField.show();
      }
    }
  }
});