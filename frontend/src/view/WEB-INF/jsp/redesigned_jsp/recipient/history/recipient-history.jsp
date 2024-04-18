<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="statusChangesHistoryJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>

<mvc:message var="dateMsg" code='Date'/>
<mvc:message var="fieldnameMsg" code='recipient.history.fieldname'/>
<mvc:message var="oldvalueMsg" code='recipient.history.oldvalue'/>
<mvc:message var="newvalueMsg" code='recipient.history.newvalue'/>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile js-data-table" data-table="recipient-status-history" data-editable-tile="main">
        <div class="tile-header">
            <h1><mvc:message code="default.search"/></h1>
        </div>

        <div class="tile-body">
            <div class="js-data-table-body" data-web-storage="recipient-status-history-overview"></div>
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
                        "editable": false,
                        "field": "fieldDescription"
                    },
                    {
                        "headerName": "${oldvalueMsg}",
                        "editable": false,
                        "field": "oldValue"
                    },
                    {
                        "headerName": "${newvalueMsg}",
                        "editable": false,
                        "resizable": false,
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

    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" id="apply-filter" data-tooltip="<mvc:message code="button.filter.apply"/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
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
                <div class="col-12">
                    <label class="form-label" for="fieldDescription-filter">${fieldnameMsg}</label>
                    <input type="text" id="fieldDescription-filter" class="form-control" placeholder="${fieldnameMsg}"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="oldValue-filter">${oldvalueMsg}</label>
                    <input type="text" id="oldValue-filter" class="form-control" placeholder="${oldvalueMsg}"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="newValue-filter">${newvalueMsg}</label>
                    <input type="text" id="newValue-filter" class="form-control" placeholder="${newvalueMsg}"/>
                </div>
            </div>
        </div>
    </div>
</div>
