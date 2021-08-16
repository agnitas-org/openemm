<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<emm:instantiate var="trackingPoints" type="java.util.LinkedHashMap">
    <c:set target="${trackingPoints}" property="-1"><mvc:message code="statistic.revenue"/></c:set>
    <%@include file="load-tracking-points.jsp"%>
</emm:instantiate>

<emm:instantiate var="updateCustomerColumnInfo" type="java.util.LinkedHashMap">
    <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                        hide="change_date, timestamp, creation_date, datasource_id, bounceload, email, customer_id,
                        gender, mailtype, firstname, lastname, title, cleaned_date, facebook_status, foursquare_status,
                        google_status, twitter_status, xing_status">

            <%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
            <%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>

        <c:set target="${updateCustomerColumnInfo}" property="${fn:toLowerCase(_agnTbl_column_name)}" value="${_agnTbl_shortname}"/>
    </emm:ShowColumnInfo>
</emm:instantiate>

<script id="module-UpdateCustomer" type="text/x-mustache-template">
    <div class="inline-tile-content" data-module-content="{{- index}}" data-initializer="update-customer-module">
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
                            {{ var columnNameToSelect = !!columnName ? columnName.replace('#', '').toLowerCase() : ''; }}
                            <select class="form-control js-select" name="modules[].columnName" id="module_{{- index}}.columnName" size="1">
                                <c:forEach items="${updateCustomerColumnInfo}" var="columnPair">
                                    {{ var selectedSign = ('${columnPair.key}' == columnNameToSelect) ? 'selected="selected"' : ''; }}
                                    <option value="${columnPair.key}" {{- selectedSign}}>${columnPair.value}</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td>
                            <select class="form-control js-select" name="modules[].updateType" id="module_{{- index}}.updateType" size="1">
                                <option value="1" {{ updateType == 1 ? print('selected="selected"') : print('') }}>+</option>
                                <option value="2" {{ updateType == 2 ? print('selected="selected"') : print('') }}>-</option>
                                <option value="3" {{ updateType == 3 ? print('selected="selected"') : print('') }}>=</option>
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
