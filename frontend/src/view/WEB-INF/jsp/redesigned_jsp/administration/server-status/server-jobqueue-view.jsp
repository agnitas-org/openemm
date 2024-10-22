<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag"%>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core"%>

<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="filter" type="com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter"--%>
<%--@elvariable id="notFilteredFullListSize" type="java.lang.Integer"--%>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
	<div id="table-tile" class="tile" data-editable-tile="main">
		<div class="tile-body">
			<div class="table-wrapper">
				<div class="table-wrapper__header">
					<h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
					<div class="table-wrapper__controls">
						<%@include file="../../common/table/toggle-truncation-btn.jspf" %>
						<jsp:include page="../../common/table/entries-label.jsp">
							<jsp:param name="filteredEntries" value="${fn:length(activeJobQueueList)}"/>
							<jsp:param name="totalEntries" value="${notFilteredFullListSize}"/>
						</jsp:include>
					</div>
				</div>

				<div class="table-wrapper__body">
					<agnDisplay:table class="table table--borderless js-table" id="job" name="activeJobQueueList"
								   sort="page" partialList="false" excludedParams="*">

						<c:set var="noNumberOfRowsSelect" value="true" />
						<%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

						<agnDisplay:column headerClass="fit-content" property="id" titleKey="MailinglistID" sortable="false" />

						<agnDisplay:column headerClass="fit-content" titleKey="default.status" sortable="false">
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
						</agnDisplay:column>

						<agnDisplay:column titleKey="Name" sortable="false" headerClass="fit-content">
							<span>${job.description}</span>
						</agnDisplay:column>

						<agnDisplay:column headerClass="fit-content" titleKey="startDate" sortable="false">
							<span><emm:formatDate value="${job.nextStart}" format="${dateTimeFormat}" /></span>
						</agnDisplay:column>

						<agnDisplay:column titleKey="ResultMsg" sortable="false">
							<div class="d-flex align-items-center gap-2 overflow-wrap-anywhere">
								<span class="status-badge status.${job.lastResult eq 'OK' ? 'success' : 'error'}"></span>
								<span class="text-truncate-table">${job.lastResult}</span>
							</div>
						</agnDisplay:column>
					</agnDisplay:table>
				</div>
				<div class="table-wrapper__footer"></div>
			</div>
		</div>
	</div>

	<mvc:form id="filter-tile" cssClass="tile" servletRelativeAction="/serverstatus/jobqueue/search.action" method="GET" modelAttribute="filter"
			  data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">
		<div class="tile-header">
			<h1 class="tile-title">
				<i class="icon icon-caret-up mobile-visible"></i>
				<span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
			</h1>
			<div class="tile-controls">
				<a class="btn btn-icon btn-inverse" data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
				<a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
					<mvc:select id="filter-status" path="running" cssClass="form-control js-select" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
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
					<mvc:select id="filter-result" path="successful" cssClass="form-control js-select" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
						<mvc:option value=""><mvc:message code="default.All"/></mvc:option>
						<mvc:option value="true" data-badge-class="status.success"><mvc:message code="OK" /></mvc:option>
						<mvc:option value="false" data-badge-class="status.error"><mvc:message code="Error" /></mvc:option>
					</mvc:select>
				</div>
			</div>
		</div>
	</mvc:form>
</div>
