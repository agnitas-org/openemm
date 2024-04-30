<script id="module-IdentifyCustomer" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="IdentifyCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.keyColumn"><mvc:message code="action.identifycust.usercol"/></label>
            </div>
            <div class="col-sm-8">
                {{ var columnNameToSelect = !!keyColumn ? keyColumn.replace('#', '').toLowerCase() : '' }}

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
                <label class="control-label"><mvc:message code="action.identifycust.passcolumn"/></label>
            </div>
            <div class="col-sm-8">
                {{ var passColumnNameToSelect = !!passColumn ? passColumn.replace('#', '').toLowerCase() : ''; }}
                <select class="form-control js-select" name="modules[].passColumn" id="module_{{- index}}.passColumn" size="1">
                    <option value="none"><mvc:message code="action.identifycust.nopass"/></option>
                    <c:forEach items="${columnInfo}" var="columnPair">
                        {{ var selectedSign = ('${columnPair.key}' == passColumnNameToSelect) ? 'selected="selected"' : ''; }}
                        <option value="${columnPair.key}" {{- selectedSign}}>${columnPair.value}</option>
                    </c:forEach>
                </select>
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
