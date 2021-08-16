$.fn.queryBuilder.defaults({templates: { group: '\
<dl id="{{= it.group_id }}" class="rules-group-container"> \
  <dt class="rules-group-header"> \
    <div class="btn-group pull-right group-actions"> \
      <button type="button" class="btn btn-xs btn-primary" data-add="rule"> \
        <i class="{{= it.icons.add_rule }}"></i> {{= it.translate("add_rule") }} \
      </button> \
      {{? it.settings.allow_groups===-1 || it.settings.allow_groups>=it.level }} \
        <button type="button" class="btn btn-xs btn-primary" data-add="group"> \
          <i class="{{= it.icons.add_group }}"></i> {{= it.translate("add_group") }} \
        </button> \
      {{?}} \
      {{? it.level>1 }} \
        <button type="button" class="btn btn-xs btn-alert" data-delete="group"> \
          <i class="{{= it.icons.remove_group }}"></i> {{= it.translate("delete_group") }} \
        </button> \
      {{?}} \
    </div> \
    <div class="btn-group group-conditions"> \
      {{~ it.conditions: condition }} \
        <label class="btn btn-xs btn-primary"> \
          <input type="radio" name="{{= it.group_id }}_cond" value="{{= condition }}"> {{= it.translate("conditions", condition) }} \
        </label> \
      {{~}} \
    </div> \
    {{? it.settings.display_errors }} \
      <div class="error-container"><i class="{{= it.icons.error }}"></i></div> \
    {{?}} \
  </dt> \
  <dd class=rules-group-body> \
    <ul class=rules-list></ul> \
  </dd> \
</dl>' }});

$.fn.queryBuilder.defaults({templates: { rule: '\
<li id="{{= it.rule_id }}" class="rule-container"> \
  <div class="rule-header clearfix"> \
    <div class="btn-group pull-right rule-actions"> \
      <button type="button" class="btn btn-regular btn-alert" data-delete="rule"> \
        <i class="{{= it.icons.remove_rule }}"></i> \
      </button> \
    </div> \
    <div class="rule-operator-conditions-container pull-right"></div> \
  </div> \
  {{? it.settings.display_errors }} \
    <div class="error-container"><i class="{{= it.icons.error }}"></i></div> \
  {{?}} \
  <div class="rule-filter-container"></div> \
  <div class="rule-operator-container"></div> \
  <div class="rule-value-container"></div> \
</li>' }});

$.fn.queryBuilder.defaults({templates: { filterSelect: '\
{{ var optgroup = null; }} \
<select class="form-control js-select" name="{{= it.rule.id }}_filter"> \
  {{? it.settings.display_empty_filter }} \
    <option value="-1">{{= it.settings.select_placeholder }}</option> \
  {{?}} \
  {{~ it.filters: filter }} \
    {{? optgroup !== filter.optgroup }} \
      {{? optgroup !== null }}</optgroup>{{?}} \
      {{? (optgroup = filter.optgroup) !== null }} \
        <optgroup label="{{= it.translate(it.settings.optgroups[optgroup]) }}"> \
      {{?}} \
    {{?}} \
    <option value="{{= filter.id }}">{{= it.translate(filter.label) }}</option> \
  {{~}} \
  {{? optgroup !== null }}</optgroup>{{?}} \
</select>' }});


$.fn.queryBuilder.defaults({templates: { ruleValueSelect: '\
{{ var optgroup = null; }} \
<select class="form-control js-select" name="{{= it.name }}" {{? it.rule.filter.multiple }}multiple{{?}}> \
  {{? it.rule.filter.placeholder }} \
    <option value="{{= it.rule.filter.placeholder_value }}" disabled selected>{{= it.rule.filter.placeholder }}</option> \
  {{?}} \
  {{~ it.rule.filter.values: entry }} \
    {{? optgroup !== entry.optgroup }} \
      {{? optgroup !== null }}</optgroup>{{?}} \
      {{? (optgroup = entry.optgroup) !== null }} \
        <optgroup label="{{= it.translate(it.settings.optgroups[optgroup]) }}"> \
      {{?}} \
    {{?}} \
    <option value="{{= entry.value }}">{{= entry.label }}</option> \
  {{~}} \
  {{? optgroup !== null }}</optgroup>{{?}} \
</select>' }});


$.fn.queryBuilder.defaults({ icons: {
  add_group: 'icon icon-plus-circle',
  add_rule: 'icon icon-plus',
  remove_group: 'icon icon-times',
  remove_rule: 'icon icon-trash',
  error: 'icon icon-exclamation-triangle'
}});
