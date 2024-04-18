AGN.Lib.Controller.new('action-view', function () {

  const Form = AGN.Lib.Form;
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;

  let moduleList;
  let operationTypes = {};

  class ModuleList {
    constructor($container) {
      this.$container = $container;
      this.activeModules = [];

      $container.on('remove-module', () => {
        const $list = $container.find('[data-action-module] [data-module-index]');
        $.each($list, (index, el) => $(el).html((index + 1) + '.'));
      });

      $container.on('add-new-module', () => {
        const allModules = $container.find('[data-action-module]');
        allModules.last().find('[data-module-index]').html(allModules.length + '.');
      });
    }

    clean() {
      this.$container.empty();
    }

    addModule(module) {
      if (module && module.type) {
        module.index = this._getNextIndex();

        const $moduleBlock = Template.dom('common-module-data',
          {moduleName: t(`triggerManager.operation.${module.type}`)});

        const moduleContent = Template.text(`module-${module.type}`, module.getParameters());
        $moduleBlock.find('.tile').append(moduleContent);

        this.$container.append($moduleBlock);
        AGN.runAll($moduleBlock);
        this.$container.trigger('add-new-module');
        this.activeModules.push(module);
      }
    }

    deleteModule(index) {
      if (index >= 0) {
        const $content = this.$container.find(`[data-module-content="${index}"]`);
        $content.closest('[data-action-module]').remove();
        this.$container.trigger("remove-module");

        this.activeModules = this.activeModules.filter(module => module.index !== index);
      }
    }

    toJson() {
      const json = [];
      this.$container.find('[data-module-content]').each(function (index, m) {
        json.push(Module.toJson($(m), index));
      });
      return json;
    }

    getSubmissionJson() {
      return JSON.stringify(this.toJson());
    }

    getModulesByType(type) {
      return this.activeModules.filter(module => module.type === type);
    }

    _getNextIndex() {
      const list = this.$container.find('[data-module-content]');
      let index = 0;
      if (list.length > 0) {
        const lastIndex = list.last().data('module-content');
        index = lastIndex + 1;
      }
      return index;
    }
  }

  class Module {
    constructor(type, data, count) {
      this.type = type;
      this.index = count || 0;
      this.id = data.id || 0;
      this.data = $.extend(Module.defaults(type), data);
    }

    static defaults(type) {
      const data = {};
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
          data.userStatusesOption = 1;
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
          data.mailinglistIds = [];
          data.allMailinglistsSelected = false;
          data.additionalMailinglists = false;
          break;
        case 'UpdateCustomer':
          data.useTrack = false;
          data.columnName = '';
          data.updateValue = '';
          data.updateType = 1;
          data.trackingPointId = -1;
          break;
      }
      data.readonly = false;
      return data;
    }

    static create(type) {
      return new Module(type, {});
    }

    static deserialize(object, count) {
      const type = operationTypes[object.operationType];
      return new Module(type, object, count);
    }

    static toJson($module, index) {
      const moduleJson = {};
      moduleJson.index = index;
      $module.find('[name^="modules[]"]').each(function (i, field) {
        const $field = $(field);
        const name = $field.prop('name') || '';

        const fieldName = name.replace('modules[].', '');

        let value = '';

        if ($field.is('select')) {
          value = Select.get($field).getSelectedValue();
        } else if ($field.is('input') && $field.prop('type') === 'checkbox') {
          value = $field.prop('checked');
        } else {
          value = $field.val();
        }

        moduleJson[fieldName] = value;
      });
      return $.extend(Module.defaults(moduleJson.type), moduleJson);
    }

    getParameters() {
      const extraData = {
        type: this.type,
        index: this.index,
        id: this.id
      };
      return _.merge({}, this.data, extraData);
    }
  }

  this.addDomInitializer('action-view', function () {
    const form = Form.get(this.el);

    operationTypes = this.config.operationTypes;

    moduleList = new ModuleList($('#module-list'));
    moduleList.clean();

    let modules = [];

    if (this.config.modules) {
      modules = JSON.parse(this.config.modules).map((object, count) => Module.deserialize(object, count));
    }

    modules.forEach(mod => moduleList.addModule(mod));
    form.initFields();
  });

  this.addAction({click: 'add-new-module'}, function () {
    const moduleType = $('#moduleName').val();
    const module = Module.create(moduleType);

    if (module.type === 'SendMailing') {
      if (moduleList.getModulesByType('ActivateDoubleOptIn').length > 0) {
        module.data.userStatusesOption = 0;
      }
    }

    moduleList.addModule(module);
    Form.get(this.el).initFields();

    if (module.type === 'ActivateDoubleOptIn') {
      const sendMailingModules = moduleList.getModulesByType('SendMailing');

      sendMailingModules.forEach(function (module) {
        const $userStatusesSelect = $(`#module-${module.index}-userStatusesOption`);
        Select.get($userStatusesSelect).selectFirstValue();
      });
    }
  });

  this.addAction({click: 'action-delete-module'}, function () {
    const index = this.el.closest('.tile')
      .find('[data-module-content]')
      .data('module-content');

    moduleList.deleteModule(index);
  });

  this.addAction({submission: 'save-action-data'}, function () {
    const form = Form.get(this.el);
    form.setValueOnce("modulesSchema", moduleList.getSubmissionJson());
    form.submit();
  });

  this.addDomInitializer('update-customer-module', function () {
    const $el = $(this.el);
    const $checkedUseTrack = $el.find('[data-action="toggle-if-first"]:checked');
    toggleTrackingPointData($el, $checkedUseTrack.exists());
  });

  this.addAction({click: 'toggle-if-first'}, function () {
    const $el = $(this.el);
    toggleTrackingPointData($el, $el.prop('checked'));
  });

  function toggleTrackingPointData($el, isEnabled) {
    const $elModule = $el.closest('[data-action-module]');

    const $trackingPointField = $elModule.find('[data-useTrack-trackingPointId]');
    const $updateValueField = $elModule.find('[data-useTrack-updateValue]');

    if (!$trackingPointField.exists()) {
      $updateValueField.removeClass('hidden');
      return;
    }

    if ($elModule.index() === 0) {
      $trackingPointField.toggleClass('hidden', !isEnabled);
      $updateValueField.toggleClass('hidden', isEnabled);
    }
  }
});