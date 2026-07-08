<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>

<%--@elvariable id="activeJobQueueList" type="java.util.List<com.agnitas.service.JobDto>"--%>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
	<div id="table-tile" class="tile" data-editable-tile="main">
		<div class="tile-body">
			<div class="table-wrapper" data-web-storage="job-queue-overview" data-js-table="jobs-list">
				<div class="table-wrapper__header">
					<h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
					<div class="table-wrapper__controls">
						<%@include file="../../common/table/toggle-truncation-btn.jspf" %>
						<jsp:include page="../../common/table/entries-label.jsp" />
					</div>
				</div>
			</div>

			<script id="jobs-list" type="application/json">
				{
                    "columns": [
                        {
                            "headerName": "<mvc:message code='MailinglistID'/>",
							"cellRenderer": "StringCellRenderer",
							"field": "id",
							"type": "numberColumn",
							"suppressSizeToFit": true
            			},
						{
							"headerName": "<mvc:message code='default.status'/>",
							"cellRenderer": "MustacheTemplateCellRender",
							"cellRendererParams": {"templateName": "running-cell"},
							"cellStyle": {"display": "flex", "align-items": "center"},
							"field": "running",
							"resizable": true,
							"type": "setColumn",
							"suppressSizeToFit": true
						},
						{
							"headerName": "<mvc:message code='server.status.jobqueue.description'/>",
							"cellRenderer": "NotEscapedStringCellRenderer",
							"field": "description",
							"type": "textCaseInsensitiveColumn"
						},
						{
							"headerName": "<mvc:message code='target.recipients.delete.scheduled.lastRun'/>",
							"field": "lastStart",
							"suppressSizeToFit": true,
							"type": "dateTimeColumn"
						},
						{
							"headerName": "<mvc:message code='ResultMsg'/>",
							"cellRenderer": "MustacheTemplateCellRender",
							"cellRendererParams": {"templateName": "last-result-cell"},
							"field": "successful",
							"type": "setColumn"
						},
						{
                            "headerName": "<mvc:message code='predelivery.duration'/>",
							"cellRenderer": "MustacheTemplateCellRender",
							"cellRendererParams": {"templateName": "duration-cell"},
							"field": "lastDuration",
							"type": "numberColumn"
            			},
						{
							"headerName": "<mvc:message code='server.status.jobqueue.startAfterError'/>",
							"cellRenderer": "MustacheTemplateCellRender",
							"cellRendererParams": {"templateName": "start-after-error-cell"},
							"cellStyle": {"display": "flex", "align-items": "center"},
							"field": "startAfterError",
							"resizable": true,
							"type": "setColumn",
							"suppressSizeToFit": true
						},
						{
							"headerName": "<mvc:message code='server.status.jobqueue.start.next'/>",
							"field": "nextStart",
							"suppressSizeToFit": true,
							"type": "dateTimeColumn"
						},
						{
							"headerName": "<mvc:message code='server.status.jobqueue.error.recipient'/>",
							"cellRenderer": "NotEscapedStringCellRenderer",
							"field": "emailOnError",
							"type": "textCaseInsensitiveColumn"
						}
        			],
        			"data": ${emm:toJson(activeJobQueueList)}
				}
			</script>
		</div>
	</div>

	<div id="filter-tile" class="tile" data-toggle-tile data-editable-tile>
		<div class="tile-header">
			<h1 class="tile-title">
				<i class="icon icon-caret-up mobile-visible"></i>
				<span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
			</h1>
			<div class="tile-controls">
				<a class="btn btn-icon btn-secondary" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>">
					<i class="icon icon-undo-alt"></i>
				</a>
				<a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>">
					<i class="icon icon-search"></i>
				</a>
			</div>
		</div>
		<div class="tile-body vstack gap-3 js-scrollable">
			<div>
				<label class="form-label" for="id-filter"><mvc:message code="MailinglistID"/></label>
				<input type="text" id="id-filter" class="form-control"/>
			</div>

			<div>
				<label class="form-label" for="running-filter"><mvc:message code="default.status" /></label>
				<select id="running-filter" class="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
					<option value=""><mvc:message code="default.All" /></option>
					<option value="true" data-badge-class="job.status.active"><mvc:message code="workflow.view.status.active" /></option>
					<option value="false" data-badge-class="job.status.inactive"><mvc:message code="workflow.view.status.inActive" /></option>
				</select>
			</div>

			<div>
				<label class="form-label" for="description-filter"><mvc:message code="Name"/></label>
				<input type="text" id="description-filter" class="form-control"/>
			</div>

			<div data-date-range>
				<label class="form-label" for="nextStart-from-filter"><mvc:message code="server.status.jobqueue.start.next"/></label>

				<div class="inline-input-range">
					<div class="date-picker-container">
						<input type="text" id="nextStart-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
					</div>
					<div class="date-picker-container">
						<input type="text" id="nextStart-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
					</div>
				</div>
			</div>

			<div>
				<label class="form-label" for="successful-filter"><mvc:message code="ResultMsg" /></label>
				<select id="successful-filter" class="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
					<option value=""><mvc:message code="default.All" /></option>
					<option value="true" data-badge-class="status.success"><mvc:message code="OK" /></option>
					<option value="false" data-badge-class="status.error"><mvc:message code="error.generic" /></option>
				</select>
			</div>
		</div>
	</div>
</div>

<template id="running-cell">
	{{ if (value) { }}
		<span class="status-badge job.status.active" data-tooltip="<mvc:message code="workflow.view.status.active" />"></span>
	{{ } else { }}
		<span class="status-badge job.status.inactive" data-tooltip="<mvc:message code="workflow.view.status.inActive" />"></span>
	{{ } }}
</template>

<template id="last-result-cell">
	<div class="hstack gap-2 overflow-wrap-anywhere">
		<span class="status-badge status.{{- value ? 'success' : 'error' }}"></span>
		<span class="text-truncate-table">{{- entry.lastResult }}</span>
	</div>
</template>

<template id="duration-cell">
	<span>{{= value }} <mvc:message code="seconds" /></span>
</template>

<template id="start-after-error-cell">
	<span class="status-badge status.{{- value ? 'success' : 'error' }}"></span>
</template>
