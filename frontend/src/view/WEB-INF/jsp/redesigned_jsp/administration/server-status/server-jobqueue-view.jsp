<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="filter" type="com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter"--%>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
	<div id="table-tile" class="tile" data-editable-tile="main">
		<div class="tile-header">
			<h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
		</div>

		<div class="tile-body">
			<div class="table-box">
				<div class="table-scrollable">
					<display:table class="table table-rounded js-table" id="job" name="activeJobQueueList"
								   sort="page" partialList="false" excludedParams="*">

						<c:set var="noNumberOfRowsSelect" value="true" />
						<%@ include file="../../displaytag/displaytag-properties.jspf" %>

						<display:column headerClass="fit-content" property="id" titleKey="MailinglistID" sortable="false" />

						<display:column headerClass="fit-content" titleKey="default.status" sortable="false">
							<div class="d-flex align-items-center gap-2">
								<c:choose>
									<c:when test="${job.running}">
										<span class="status-badge job.status.active"></span>
										<mvc:message code="workflow.view.status.active" />
									</c:when>
									<c:otherwise>
										<span class="status-badge job.status.inactive"></span>
										<mvc:message code="workflow.view.status.inActive" />
									</c:otherwise>
								</c:choose>
							</div>
						</display:column>

						<display:column property="description" titleKey="Name" sortable="false" headerClass="fit-content" />

						<display:column headerClass="fit-content" titleKey="startDate" sortable="false">
							<emm:formatDate value="${job.nextStart}" format="${dateTimeFormat}" />
						</display:column>

						<display:column titleKey="ResultMsg" sortable="false">
							<div class="d-flex align-items-center gap-2">
								<span class="status-badge status.${job.lastResult eq 'OK' ? 'success' : 'error'}"></span>
								<span class="multiline">${job.lastResult}</span>
							</div>
						</display:column>
					</display:table>
				</div>
			</div>
		</div>
	</div>

	<mvc:form id="filter-tile" cssClass="tile" servletRelativeAction="/serverstatus/jobqueue/search.action" method="GET" modelAttribute="filter"
			  data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">
		<div class="tile-header">
			<h1 class="tile-title">
				<i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
			</h1>
			<div class="tile-controls">
				<a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
				<a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
			</div>
		</div>
		<div class="tile-body js-scrollable">
			<div class="row g-3">
				<div class="col-12">
					<label for="filter-id" class="form-label"><mvc:message code="MailinglistID" /></label>
					<mvc:number id="filter-id" path="id" cssClass="form-control" placeholder="0" step="1" pattern="\d+" />
				</div>

				<div class="col-12">
					<label for="filter-status" class="form-label"><mvc:message code="default.status" /></label>
					<mvc:select id="filter-status" path="running" cssClass="form-control js-select" data-result-template="job-badge-selection">
						<mvc:option value=""><mvc:message code="default.All"/></mvc:option>
						<mvc:option value="true"  data-badge-class="job.status.active"><mvc:message code="workflow.view.status.active" /></mvc:option>
						<mvc:option value="false" data-badge-class="job.status.inactive"><mvc:message code="workflow.view.status.inActive" /></mvc:option>
					</mvc:select>
				</div>

				<div class="col-12">
					<label for="filter-name" class="form-label"><mvc:message code="Name" /></label>
					<mvc:text id="filter-name" path="name" cssClass="form-control" />
				</div>

				<div class="col-12">
					<label class="form-label" for="filter-start-from"><mvc:message code="startDate" /></label>
					<div class="inline-input-range" data-date-range>
						<div class="date-picker-container">
							<mvc:message var="fromMsg" code="From" />
							<mvc:text id="filter-start-from" path="startDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
						</div>
						<div class="date-picker-container">
							<mvc:message var="toMsg" code="To" />
							<mvc:text id="filter-start-to" path="startDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
						</div>
					</div>
				</div>

				<div class="col-12">
					<label for="filter-result" class="form-label"><mvc:message code="ResultMsg" /></label>
					<mvc:select id="filter-result" path="successful" cssClass="form-control js-select" data-result-template="job-badge-selection">
						<mvc:option value=""><mvc:message code="default.All"/></mvc:option>
						<mvc:option value="true" data-badge-class="status.success"><mvc:message code="OK" /></mvc:option>
						<mvc:option value="false" data-badge-class="status.error"><mvc:message code="Error" /></mvc:option>
					</mvc:select>
				</div>
			</div>
		</div>
	</mvc:form>
</div>

<script id="job-badge-selection" type="text/x-mustache-template">
	<div class="d-flex align-items-center gap-1">
		{{ if (value) { }}
			<span class="status-badge {{- element.getAttribute('data-badge-class')}}"></span>
		{{ } }}
		<span>{{- text }}</span>
	</div>
</script>
