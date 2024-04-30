<script id="module-SubscribeCustomer" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="SubscribeCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label checkbox-control-label" for="module_{{- index}}_doubleCheck"><mvc:message code="import.doublechecking"/></label>
            </div>
            <div class="col-sm-8">
                <label class="toggle">
                    {{ if (doubleCheck) { }}
                        <input type="checkbox" name="modules[].doubleCheck" id="module_{{- index}}_doubleCheck" checked="ckecked"/>
                    {{ } else { }}
                        <input type="checkbox" name="modules[].doubleCheck" id="module_{{- index}}_doubleCheck" />
                    {{ } }}

                    <div class="toggle-control"></div>
                </label>
            </div>
        </div>

        <div class="form-group hidden" data-show-by-checkbox="#module_{{- index}}_doubleCheck">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.keyColumn"><mvc:message code="import.keycolumn"/></label>
            </div>
            <div class="col-sm-8">
                {{ var columnNameToSelect = !!keyColumn ? keyColumn.replace('#', '').toLowerCase() : ''; }}

                <select class="form-control js-select" name="modules[].keyColumn" id="module_{{- index}}.keyColumn" size="1">
                    <c:forEach items="${columnInfo}" var="columnPair">
                        {{ var selectedSign = ('${columnPair.key}' == columnNameToSelect) ? 'selected="selected"' : ''; }}
                        <option value="${columnPair.key}" {{- selectedSign}}>${columnPair.value}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label checkbox-control-label" for="module_{{- index}}.doubleOptIn"><mvc:message code="action.UseDblOptIn"/></label>
            </div>
            <div class="col-sm-8">
                <label class="toggle">
                    {{ if (doubleOptIn) { }}
                        <input type="checkbox" name="modules[].doubleOptIn" id="module_{{- index}}.doubleOptIn" checked="ckecked"/>
                    {{ } else { }}
                        <input type="checkbox" name="modules[].doubleOptIn" id="module_{{- index}}.doubleOptIn" />
                    {{ } }}

                    <div class="toggle-control"></div>
                </label>
            </div>
        </div>
    </div>
    <div class="inline-tile-footer">
    <emm:ShowByPermission token="actions.change">
        <a class="btn btn-regular" href="#" data-action="action-delete-module" data-property-id="{{- index}}">
            <i class="icon icon-trash-o"></i>
            <span class="text"><mvc:message code="button.Delete"/></span>
        </a>
    </emm:ShowByPermission>
    </div>
</script>
