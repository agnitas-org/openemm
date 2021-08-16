<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.commons.ActivenessStatus" %>
<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="userFormURLPattern" type="java.lang.String"--%>
<%--@elvariable id="webformListJson" type="net.sf.json.JSONArray"--%>

<c:set var="active" value="<%= ActivenessStatus.ACTIVE %>"/>
<c:set var="inactive" value="<%= ActivenessStatus.INACTIVE %>"/>

<div class="tile js-data-table" data-sizing="container" data-table="webform-lists" data-controller="emm-activeness">

    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <mvc:message code="default.Overview"/>
        </h2>
        <ul class="tile-header-nav"></ul>

        <ul class="tile-header-actions">
            <emm:ShowByPermission token="forms.delete">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text"><mvc:message code="bulkAction"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <c:url var="confirmBulkDelete" value="/webform/confirmBulkDelete.action"/>

                            <a href="#" class="js-data-table-bulk-delete"
                               data-table-body=".js-data-table-body"
                               data-bulk-url="${confirmBulkDelete}">
                                    <mvc:message code="bulkAction.delete.userform"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </emm:ShowByPermission>

            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <i class="icon icon-eye"></i>
                    <span class="text"><mvc:message code="button.Show"/></span>
                    <i class="icon icon-caret-down"></i>
                </a>

                <ul class="dropdown-menu">
                    <li class="dropdown-header"><mvc:message code="listSize"/></li>
                    <li>
                        <label class="label js-data-table-paginate" data-page-size="20" data-table-body=".js-data-table-body" data-web-storage="userform-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50" data-table-body=".js-data-table-body" data-web-storage="userform-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100" data-table-body=".js-data-table-body" data-web-storage="userform-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="userform-overview" style="height: 100%;"></div>
    </div>

    <div class="tile-footer" data-sizing="bottom">
        <c:url var="saveActivenessUrl" value="/webform/saveActiveness.action"/>
        <button type="button" class="btn btn-large btn-primary pull-right disabled" data-table-body=".js-data-table-body"
                data-form-url="${saveActivenessUrl}" data-action="save">
            <i class="icon icon-save"></i>
            <span class="text"><mvc:message code="button.Save"/></span>
        </button>
    </div>

    <c:forEach var="entry" items="${webformListJson}">
        <c:url var="viewLink" value="/webform/${entry['id']}/view.action"/>
        <c:set target="${entry}" property="show" value="${viewLink}"/>

        <c:url var="deleteLink" value="/webform/${entry['id']}/confirmDelete.action"/>
        <c:set target="${entry}" property="delete" value="${deleteLink}"/>

        <c:set target="${entry}" property="webformUrl"
               value="${fn:replace(userFormURLPattern, '{user-form-name}', entry['name'])}"/>
    </c:forEach>

    <c:set var="isDeletionAllowed" value="false"/>
    <emm:ShowByPermission token="forms.delete">
        <c:set var="isDeletionAllowed" value="true"/>
    </emm:ShowByPermission>

    <script id="webform-lists" type="application/json">
        {
            "columns": [
                {
                    "field": "select",
                    "type": "bulkSelectColumn",
                    "headerCheckboxSelectionFilteredOnly": true,
                    "hide": ${not isDeletionAllowed}
                },
                {
                    "headerName": "<mvc:message code='Form'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "name",
                    "type": "textCaseInsensitiveColumn"
                },
                {
                    "headerName": "<mvc:message code='Description'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "description",
                    "type": "textCaseInsensitiveColumn"
                },
                {
                    "headerName": "<mvc:message code='userform.usesActions'/>",
                    "editable": false,
                    "suppressMenu": true,
                    "cellAction": "goTo",
                    "field": "actionNames",
                    "cellStyle": {"textAlign": "center"},
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "webform-usage-badge"},
                    "width": 85
                },
                {
                    "headerName": "<mvc:message code='default.url'/>",
                    "editable": false,
                    "suppressMenu": true,
                    "cellAction": "goTo",
                    "field": "webformUrl"
                },
                {
                    "headerName": "<mvc:message code='default.creationDate'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "creationDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:toUpperCase(adminDateFormat)}" },
                    "width": 150
                },
                {
                    "headerName": "<mvc:message code='default.changeDate'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "changeDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:toUpperCase(adminDateFormat)}" },
                    "width": 150
                },
                {
                    "headerName": "<mvc:message code='mailing.status.active'/>",
                    "editable": true,
                    "field": "activeStatus",
                    "type": "setColumn",
                    "filterParams": {
                        "labels": {
                            "${active}": "<mvc:message code="workflow.view.status.active"/>",
                            "${inactive}": "<mvc:message code="workflow.view.status.inActive"/>"
                        }
                    },
                    "cellStyle": {"textAlign": "center"},
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "webform-activeness"},
                    "width": 85
                },
                {
                    "field": "delete",
                    "type": "deleteColumn",
                    "hide": ${not isDeletionAllowed}
                }
            ],
            "data": ${webformListJson},
            "options": {
                "filtersDescription": {
                    "enabled": true,
                    "templateName": "webform-filters-description"
                }
            }
        }
    </script>

</div>

<script id="webform-filters-description" type="text/x-mustache-template">
    <div class='well'>
        <strong><mvc:message code="yourCompanyID"/></strong>
        ${AgnUtils.getCompanyID(pageContext.request)}
    </div>
</script>

<script id="webform-usage-badge" type="text/x-mustache-template">
    {{ if (value.length > 0) { }}
        <span class="badge badge-highlighted" data-tooltip="<mvc:message code="default.Name"/>: {{- value.join(', ') }}">
            <mvc:message code="default.Yes"/>
        </span>
    {{ } else { }}
        <span class="badge"><mvc:message code="No"/></span>
    {{ } }}
</script>

<script id="webform-activeness" type="text/x-mustache-template">
  {{ var isActive = value == '${active}';}}
  {{ var checked = isActive ? 'checked' : '';}}
    <label class="toggle">
        <input type="checkbox" {{- checked }} data-initial-state="{{- isActive }}" data-action="toggle-active" data-item-id="{{- entry.id}}"/>
        <div class="toggle-control"></div>
    </label>
</script>
