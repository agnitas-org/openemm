<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="statusChangesHistoryJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>

<div class="tile js-data-table" data-sizing="container" data-table="recipient-status-history">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <mvc:message code="default.search"/>
        </h2>

        <ul class="tile-header-actions">
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <i class="icon icon-eye"></i>
                    <span class="text"><mvc:message code="button.Show"/></span>
                    <i class="icon icon-caret-down"></i>
                </a>
                <ul class="dropdown-menu">
                    <li class="dropdown-header"><mvc:message code="listSize"/></li>
                    <li>
                        <label class="label js-data-table-paginate" data-page-size="20" data-table-body=".js-data-table-body" data-web-storage="recipient-status-history-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50" data-table-body=".js-data-table-body" data-web-storage="recipient-status-history-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100" data-table-body=".js-data-table-body" data-web-storage="recipient-status-history-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="recipient-status-history-overview" style="height: 100%;"></div>
    </div>

    <script id="recipient-status-history" type="application/json">
        {
            "columns": [
                 {
                    "headerName": "<mvc:message code='Date'/>",
                    "editable": false,
                    "field": "changeDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(adminDateTimeFormat, "d", "D"), "y", "Y")}" }
                },
                {
                    "headerName": "<mvc:message code='recipient.history.fieldname'/>",
                    "editable": false,
                    "field": "fieldDescription"
                },
                {
                    "headerName": "<mvc:message code='recipient.history.oldvalue'/>",
                    "editable": false,
                    "field": "oldValue"
                },
                {
                    "headerName": "<mvc:message code='recipient.history.newvalue'/>",
                    "editable": false,
                    "field": "newValue"
                }
            ],
            "data": ${statusChangesHistoryJson},
            "options": {
                "rowClassRules": {
                    "recipient-history-row-odd": "data.groupIndex % 2 == 0",
                    "recipient-history-row-even": "data.groupIndex % 2 != 0"
                }
            }
        }
    </script>
</div>
