<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="statusChangesHistoryJson" type="org.json.JSONArray"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>

<mvc:message var="dateMsg" code='Date'/>
<mvc:message var="fieldnameMsg" code='recipient.history.fieldname'/>
<mvc:message var="oldvalueMsg" code='recipient.history.oldvalue'/>
<mvc:message var="newvalueMsg" code='recipient.history.newvalue'/>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-body">
            <div class="table-wrapper" data-web-storage="recipient-status-history-overview" data-js-table="recipient-status-history">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp" />
                    </div>
                </div>
            </div>
        </div>

        <script id="recipient-status-history" type="application/json">
            {
                "columns": [
                     {
                        "headerName": "${dateMsg}",
                        "editable": false,
                        "field": "changeDate",
                        "type": "dateTimeColumn"
                    },
                    {
                        "headerName": "${fieldnameMsg}",
                        "type": "textCaseInsensitiveColumn",
                        "field": "fieldDescription",
                        "cellRenderer": "NotEscapedStringCellRenderer"
                    },
                    {
                        "headerName": "${oldvalueMsg}",
                        "type": "textCaseInsensitiveColumn",
                        "field": "oldValue",
                        "cellRenderer": "NotEscapedStringCellRenderer"
                    },
                    {
                        "headerName": "${newvalueMsg}",
                        "type": "textCaseInsensitiveColumn",
                        "resizable": false,
                        "field": "newValue",
                        "cellRenderer": "NotEscapedStringCellRenderer"
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

    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code="button.filter.apply"/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="changeDate-from-filter">${dateMsg}</label>
                <div class="inline-input-range" data-date-range>
                    <div class="date-picker-container">
                        <input type="text" id="changeDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker" />
                    </div>
                    <div class="date-picker-container">
                        <input type="text" id="changeDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker" />
                    </div>
                </div>
            </div>
            <div>
                <label class="form-label" for="fieldDescription-filter">${fieldnameMsg}</label>
                <input type="text" id="fieldDescription-filter" class="form-control" placeholder="${fieldnameMsg}"/>
            </div>
            <div>
                <label class="form-label" for="oldValue-filter">${oldvalueMsg}</label>
                <input type="text" id="oldValue-filter" class="form-control" placeholder="${oldvalueMsg}"/>
            </div>
            <div>
                <label class="form-label" for="newValue-filter">${newvalueMsg}</label>
                <input type="text" id="newValue-filter" class="form-control" placeholder="${newvalueMsg}"/>
            </div>
        </div>
    </div>
</div>
