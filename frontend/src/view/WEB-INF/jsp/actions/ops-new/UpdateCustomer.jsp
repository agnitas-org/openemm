<script id="module-UpdateCustomer" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}">
        <input type="hidden" name="modules[].type" id="module_{{- index}}.type" value="UpdateCustomer"/>
        <input type="hidden" name="modules[].id" id="module_{{- index}}.id" value="{{- id}}"/>

        <%@ include file="UpdateCustomer-trackpoint.jspf" %>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="Column_Name"/></label>
            </div>
            <div class="col-sm-8">
                <table class="table table-bordered table-form">
                    <tr>
                        <td>
                            {{ var columnNameToSelect = !columnName ? '' : columnName; }}
                            {{ columnNameToSelect = columnNameToSelect.includes('#') ? columnNameToSelect.replace('#', '') : columnNameToSelect;}}

                            <select class="form-control js-select" name="modules[].columnName" id="module_{{- index}}.columnName" size="1">
                                <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                                                    hide="change_date, timestamp, creation_date, datasource_id, bounceload, email, customer_id, gender, mailtype, firstname, lastname, title, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status">
                                    <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
                                    <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>

                                    {{ var selectedSign = ${(not empty _agnTbl_column_name) and _agnTbl_column_name.equalsIgnoreCase(columnNameToSelect)} ? 'selected="selected"' : ''; }}

                                    <option value="${_agnTbl_column_name}" {{- selectedSign}}>${_agnTbl_shortname}</option>
                                </emm:ShowColumnInfo>

                            </select>
                        </td>
                        <td>
                            <select class="form-control js-select" name="modules[].updateType" id="module_{{- index}}.updateType" size="1" value="{{- updateType}}">
                                <option value="1">+</option>
                                <option value="2">-</option>
                                <option value="3">=</option>
                            </select>
                        </td>
                        <td>
                            <input type="text" name="modules[].updateValue" id="module_{{- index}}_updateValue" class="form-control" data-useTrack-updateValue
                                   value="{{- updateValue}}">
                            <%@ include file="UpdateCustomer-trackpoint-table.jspf" %>

                        </td>
                    </tr>
                </table>
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
