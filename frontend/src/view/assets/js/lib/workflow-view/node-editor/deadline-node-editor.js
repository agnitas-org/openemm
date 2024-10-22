(function () {
  const Def = AGN.Lib.WM.Definitions;
  const EditorsHelper = AGN.Lib.WM.EditorsHelper;
  const Messages = AGN.Lib.Messages;

  class DeadlineNodeEditor extends AGN.Lib.WM.NodeEditor {
    constructor() {
      super();
      this.panelIds = ['fixedDeadlinePanel', 'delayDeadlinePanel'];
      this.timeUnitIds = [
        'deadlineTimeUnitMinute',
        'deadlineTimeUnitHour',
        'deadlineTimeUnitDay',
        'deadlineTimeUnitWeek',
        'deadlineTimeUnitMonth'
      ];
    }

    get saveOnOpen () {
      return true;
    }
    
    get formName() {
      return 'deadlineForm';
    }
    
    getDefaultDelayData(deadlineType) {
      if (deadlineType === Def.constants.deadlineTypeDelay && this.isPrecededByImportIcon) {
        return  {
          timeUnit: Def.constants.deadlineTimeUnitHour,
          delayValue: Def.constants.defaultImportDelayLimit
        };
      }
      return {};
    }

    get title() {
      return t('workflow.deadline.title');
    }

    get deadlineType() {
      return this.$find('[name="deadlineType"]').val();
    }

    get timeUnit() {
      return this.$find('select[name="timeUnit"]').val();
    }
    
    fillEditor(node) {
      const data = node.getData();
      this.isPrecededByImportIcon = this.checkPrecededByImportIcon(node);
      this.$form.submit(false);
      this.$find('#deadlineDate').datepicker("setDate", this.dateAsUTC(data.date || new Date()));
      this.fillDelayData(data);
      this.$find('#time').val(('0' + data.hour).slice(-2) + ':' + ('0' + data.minute).slice(-2));
    }

    fillDelayData(data) {
      this.$form.get(0).reset();
      const type = this.deadlineType;
      const defaultValues = this.getDefaultDelayData(type);

      EditorsHelper.fillFormFromObject(this.formName, data, '', defaultValues);
      this.updateVisibility();

      const delayValue = data.delayValue || defaultValues.delayValue;
      EditorsHelper.initSelectWithValueOrChooseFirst(this.$find('[name="delayValue"]'), delayValue);
    }

    saveEditor() {
      const data = EditorsHelper.formToObject(this.formName);
      const time = this.$find('#time').val();

      data.date = this.$find('#deadlineDate').datepicker("getDate");
      data.hour = time.substring(0, 2);
      data.minute = time.substring(3, 5);

      return data;
    }

    // functions handling visibility of different parts of dialog according to selected settings
    updateVisibility() {
      const $deadlineTypeBox = this.$find('#deadline-type-box');
      let type = this.deadlineType;
      
      if (this.checkPresentNodeOfType([Def.NODE_TYPE_ACTION_BASED_MAILING, Def.NODE_TYPE_DATE_BASED_MAILING])) {
        $deadlineTypeBox.hide();
        this.$find('#typeDelay').prop('selected', true);
        type = Def.constants.deadlineTypeDelay;
      } else {
        $deadlineTypeBox.show();
      }

      if (type === Def.constants.deadlineTypeDelay) {
        this.switchPanelsVisibility('delayDeadlinePanel');
        this.switchTimeContainerVisibility(false);

        switch (this.timeUnit) {
          case Def.constants.deadlineTimeUnitMinute:
            this.switchTimeUnitsVisibility('deadlineTimeUnitMinute');
            break;

          case Def.constants.deadlineTimeUnitHour:
            this.switchTimeUnitsVisibility('deadlineTimeUnitHour');
            break;

          case Def.constants.deadlineTimeUnitDay:
            this.switchTimeUnitsVisibility('deadlineTimeUnitDay');
            this.switchTimeContainerVisibility(true);
            $('#deadlineTimeHelp').removeAttr('style');
            break;

          case Def.constants.deadlineTimeUnitWeek:
            this.switchTimeUnitsVisibility('deadlineTimeUnitWeek');
            break;

          case Def.constants.deadlineTimeUnitMonth:
            this.switchTimeUnitsVisibility('deadlineTimeUnitMonth');
            break;
        }
        this.$find('input[name="useTime"]').show();
      } else if (type === Def.constants.deadlineTypeFixedDeadline) {
        this.switchPanelsVisibility('fixedDeadlinePanel');
        this.switchTimeContainerVisibility(true);
        this.hide('#deadlineTimeHelp');
        this.$find('input[name="useTime"]').hide();
      }
    }

    switchPanelsVisibility(selectedPanelId) {
      const self = this;
      this.panelIds.forEach(function(itemId) {
        if (itemId !== selectedPanelId) {
          self.hide(`#${itemId}`);
        }
      });
      this.show(`#${selectedPanelId}`);
    }

    switchTimeUnitsVisibility(selectedTimeUnitId) {
      const self = this;
      this.timeUnitIds.forEach(function(itemId) {
        if (itemId !== selectedTimeUnitId) {
          self.hide(`#${itemId}`);
          $('#' + itemId + ' select').removeAttr('name');
        }
      });
      this.show(`#${selectedTimeUnitId}`);
      $(`#${selectedTimeUnitId} select`).attr('name', 'delayValue');
    }

    switchTimeContainerVisibility(isVisible) {
      this.toggle('#deadlineTimeContainer', isVisible);
    }

    save() {
      let valid = true;

      if (this.isPrecededByImportIcon) {
        if (this.deadlineType === Def.constants.deadlineTypeDelay) {
          if (this.timeUnit === Def.constants.deadlineTimeUnitMinute) {
            valid = false;
          } else if (this.timeUnit === Def.constants.deadlineTimeUnitHour) {
            if ($('#deadlineTimeUnitHour select').val() < Def.constants.defaultImportDelayLimit) {
              valid = false;
            }
          }
        }
      }

      if (valid) {
        super.save();
      } else {
        Messages.warn('error.workflow.deadlineIsTooShortForImport');
      }
    }

    onTimeChanged() {
      if (this.deadlineType == Def.constants.deadlineTypeDelay && this.timeUnit == Def.constants.deadlineTimeUnitDay) {
        this.$find('input[name="useTime"]').prop('checked', true);
      }
    }

    checkPresentNodeOfType(types) {
      return EditorsHelper.getNodesByTypes(types).length > 0;
    }

    checkPrecededByImportIcon(node) {
      return EditorsHelper.getNodesByIncomingConnections(node).some(n => Def.NODE_TYPE_IMPORT == n.getType());
    }
  }
  
  AGN.Lib.WM.DeadlineNodeEditor = DeadlineNodeEditor;
})();
