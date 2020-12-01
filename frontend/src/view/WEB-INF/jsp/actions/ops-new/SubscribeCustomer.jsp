<script id="module-SubscribeCustomer" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="SubscribeCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}_doubleCheck"><mvc:message code="import.doublechecking"/></label>
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
                {{ var columnNameToSelect = !keyColumn ? '' : keyColumn; }}
                {{ columnNameToSelect = columnNameToSelect.includes('#') ? columnNameToSelect.replace('#', '') : columnNameToSelect;}}

                <select class="form-control js-select" name="modules[].keyColumn" id="module_{{- index}}.keyColumn" size="1">
                    <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>" hide="change_date, timestamp, creation_date, datasource_id, bounceload, sys_tracking_veto, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                        <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
                        <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>

                        {{ var selectedSign = ${(not empty _agnTbl_column_name) and _agnTbl_column_name.equalsIgnoreCase(columnNameToSelect)} ? 'selected="selected"' : ''; }}

                        <option value="${_agnTbl_column_name}" {{- selectedSign}}>${_agnTbl_shortname}</option>
                    </emm:ShowColumnInfo>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="module_{{- index}}.doubleOptIn"><mvc:message code="action.UseDblOptIn"/></label>
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
