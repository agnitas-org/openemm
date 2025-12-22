(function () {
  const Def = AGN.Lib.WM.Definitions;
  
  class FieldRulesTable extends AGN.Lib.InputTable {
    
    constructor($container, data = [], readonly, profileFieldsTypes) {
      $container.empty();
      const tableRows = data.map(row => ({...row,
        chainOperator: row.chainOperator === parseInt(Def.constants.chainOperatorOr)
      }));
      super($container, tableRows, readonly);
      
      this.profileFieldsTypes = profileFieldsTypes;
      this.#hideFirstChainOperatorSwitch();
      this.updateOperatorsAvailability();
    }

    get $form() {
      return this.$container.closest('form');
    }
    
    get $profileFieldSelect() {
      return this.$form.find('[name="profileField"]');
    }
    
    get currentProfileField() {
      return this.$profileFieldSelect.val();
    }
    
    get currentProfileFieldIsGender() {
      return this.currentProfileField?.toLowerCase() === Def.GENDER_PROFILE_FIELD;
    }
    
    addActions() {
      super.addActions();
      this.addAction({click: '[data-add-row]'}, () => this.onRowAddedManually());
      this.addAction({click: '[data-delete-row]'}, () => this.onRowsChangedManually());
    }

    addRow(rowData) {
      const self = this;
      super.addRow(rowData);
      this.$container.find('tr:last [data-name="primaryOperator"]').on('change', function () {
        self.updateValueInput($(this));
      })
    }

    renderTable(data) {
      super.renderTable(data);
      this.$container.find('[data-name="primaryOperator"]').toArray()
        .filter(operator => $(operator).val() === Def.constants.operatorIs)
        .forEach(operator => $(operator).data('previous-value', Def.constants.operatorIs));
      if (this.currentProfileFieldIsGender) {
        this.$profileFieldSelect.data('previous-value', Def.GENDER_PROFILE_FIELD);
      }
      // last filled row acts like last row, so last empty row removed
      this.removeLastRow();
    }

    onRowAddedManually() {
      this.updateOperatorsAvailability();
      this.onRowsChangedManually();
    }
    
    onRowsChangedManually() {
      this.#hideFirstChainOperatorSwitch();
      this.$container.trigger('change');
    }
    
    #hideFirstChainOperatorSwitch() {
      this.$container
        .find('[data-name="chainOperator"]:first')
        .closest('.switch').prop('checked', false)
        .closest('td').hide();
      this.$container
        .find('[data-name="chainOperator"]:not(:first)')
        .closest('.switch')
        .show();
    }
    
    collect() {
      const rules = super.collect();
      rules.forEach(rule => {
        rule.chainOperator = parseInt(rule.chainOperator ? Def.constants.chainOperatorOr : Def.constants.chainOperatorAnd);
        rule.parenthesisOpened = parseInt(rule.parenthesisOpened);
        rule.parenthesisClosed = parseInt(rule.parenthesisClosed);
        rule.primaryOperator = parseInt(rule.primaryOperator);
      });
      return rules;
    }
    
    get rowTemplate() {
      const options = this.$primaryOperatorOptions();
      const valueElement = this.ruleValueInput();
      return `
          <tr>
              <td style="max-width: 70px">
                  <label class="switch">
                      <input type="checkbox" data-name="chainOperator" value="true" {{- chainOperator ? 'checked' : ''}} ${this.readonlyAttr}>
                      <span>AND</span>
                      <span class="flex-center flex-grow-1">OR</span>
                  </label>
              </td>
              <td style="max-width: 45px">
                  <select data-name="parenthesisOpened" class="form-control rule-field" ${this.readonlyAttr}>
                      <option value="0" {{- parenthesisOpened === 0 ? 'selected' : ''}}>&nbsp</option>
                      <option value="1" {{- parenthesisOpened === 1 ? 'selected' : ''}}>(</option>
                  </select>
              </td>
              <td>
                  <select data-name="primaryOperator" class="form-control" data-action="start-rule-operator-change" ${this.readonlyAttr} data-select-options="dropdownAutoWidth: true">
                      ${options}
                  </select>
              </td>
              <td>
                  ${valueElement}
              </td>            
              <td style="max-width: 45px">
                  <select data-name="parenthesisClosed" class="form-control rule-field" ${this.readonlyAttr}>
                      <option value="0" {{- parenthesisClosed === 0 ? 'selected' : ''}}>&nbsp</option>
                      <option value="1" {{- parenthesisClosed === 1 ? 'selected' : ''}}>)</option>
                  </select>
              </td>                        
          </tr>`;
    }

    emptyRuleValueInput() {
      return `<input type="text" data-name="primaryValue" value="" class="form-control rule-field" ${this.readonlyAttr}/>`;
    }
    
    ruleValueInput() {
      return `
        {{ if (${this.currentProfileField === Def.GENDER_PROFILE_FIELD}) { }}
            ${this.genderSelect(true)}
        {{ } else if (primaryOperator == ${Def.constants.operatorIs}) { }}
            ${this.operatorIsSelect(true)}
        {{ } else { }}
            ${this.emptyRuleValueInput().replace('value=""', 'value="{{- primaryValue }}"')}
        {{ } }}`
    }

    $primaryOperatorOptions() {
      let html = '';
      Def.constants.operators.forEach(function(option) {
        let attributes = 'value="' + option.id + '"';
        let extras = option.data;
        if (extras) {
          Object.keys(extras).forEach(function(k) {
            attributes += ' data-' + k + '="' + extras[k].replace('"', '&quot;') + '"';
          });
        }
        html += `<option ${attributes} {{- primaryOperator === ${option.id} ? 'selected' : '' }}>${option.text.replace('<', '&lt;').replace('>', '&gt;')}</option>`;
      });
      return html;
    }

    genderSelect(preSelectedWithMustache) {
      const options = Object.entries(Def.constants.genderOptions)
        .map(([genderVal, genderStr]) => this.genderOption(genderVal, genderStr, preSelectedWithMustache))
        .join('');
      return `<select data-name="primaryValue" class="form-control">${options}</select>`;
    }

    genderOption(genderVal, genderStr, preSelectedWithMustache) {
      const attrs = preSelectedWithMustache
        ? `{{- primaryValue == ${genderVal} ? "selected" : "" }}`
        : '';
      return `<option value="${genderVal}" ${attrs}>${genderStr}</option>`;
    }
    
    operatorIsSelect(preSelectedWithMustache) {
      const select = `
        <select data-name="primaryValue" class="form-control">
            <option value="NULL">NULL</option>
            <option value="NOT_NULL">NOT NULL</option>
        </select>`;
      if (preSelectedWithMustache) {
        return select
          .replace('value="NULL"', 'value="NULL" {{- primaryValue === "NULL" ? "selected" : "" }}')
          .replace('value="NOT_NULL"', 'value="NOT_NULL" {{- primaryValue === "NOT_NULL" ? "selected" : "" }}');
      }
      return select;
    }

    updateOperatorsAvailability() {
      const $primaryOperatorSelect = this.$container.find('select[data-name="primaryOperator"]');
      _.forEach($primaryOperatorSelect.toArray(), (operatorSelect) => this.updateSelectOperatorsAvailability($(operatorSelect)));
      $primaryOperatorSelect.trigger('change.select2');
    }

    updateSelectOperatorsAvailability($operatorSelect) {
      const currentProfileField = this.currentProfileField;
      const fieldType = this.profileFieldsTypes[currentProfileField];
      const optionsToDisable = $operatorSelect.children('option')
        .toArray()
        .filter(option => this.isAllowedOperatorsOption(option, fieldType))
        .map(option => option.value);
      AGN.Lib.Select.get($operatorSelect).disableOptions(optionsToDisable, true);
    }

    isAllowedOperatorsOption(option, fieldType) {
      if (this.currentProfileFieldIsGender) {
        return option.text !== '=' && option.text !== '<>';
      }
      const types = $(option).data('types');
      if (types === '*' || !types) {
        return false;
      }
      return !types.split(/[\s,]+/).includes(fieldType);
    }

    updateValueInput($primaryOperator) {
      const $primaryValue = $primaryOperator.closest('tr').find('[data-name="primaryValue"]');
      if ($primaryOperator.val() === Def.constants.operatorIs) {
        const $operatorIsSelect = $(this.operatorIsSelect());
        $primaryValue.closest('td').html($operatorIsSelect);
        $primaryOperator.data('previous-value', Def.constants.operatorIs);
        this.#initSelect($operatorIsSelect)
      } else if ($primaryOperator.data('previous-value') === Def.constants.operatorIs) {
        $primaryValue.closest('td').html(this.emptyRuleValueInput());
        $primaryOperator.removeData('previous-value');
      }
    }
    
    onProfileFieldChanged() {
      this.updateOperatorsAvailability();
      
      if (this.currentProfileFieldIsGender) {
        this.changeValueInputsToGenderSelect();
        this.$profileFieldSelect.data('previous-value', Def.GENDER_PROFILE_FIELD);
      } else if (this.$profileFieldSelect.data('previous-value') === Def.GENDER_PROFILE_FIELD) {
        this.changeValueInputsToTextInput();
        this.$profileFieldSelect.removeData('previous-value');
      }
    }

    changeValueInputsToGenderSelect() {
      _.each(this.$container.find('[data-name="primaryValue"]'), (primaryValue) => {
        const $genderSelect = $(this.genderSelect());
        $(primaryValue).closest('td').html($genderSelect);
        this.#initSelect($genderSelect);
      });
    }

    changeValueInputsToTextInput() {
      _.each(this.$container.find('[data-name="primaryValue"]'), (primaryValue) => {
        $(primaryValue).closest('td').html(this.emptyRuleValueInput());
      });
    }
    
    #initSelect($select) {
      AGN.Lib.CoreInitializer.run('select', $select);
    }
  }

  AGN.Lib.WM.FieldRulesTable = FieldRulesTable;
})();
