<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>

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
					<emm:table var="job" modelAttribute="activeJobQueueList" cssClass="table table--borderless js-table">

						<emm:column headerClass="fit-content" property="id" titleKey="MailinglistID" />

						<emm:column headerClass="fit-content" titleKey="default.status">
							<div class="flex-center">
								<c:choose>
									<c:when test="${job.running}">
										<span class="status-badge job.status.active" data-tooltip="<mvc:message code="workflow.view.status.active" />"></span>
									</c:when>
									<c:otherwise>
										<span class="status-badge job.status.inactive" data-tooltip="<mvc:message code="workflow.view.status.inActive" />"></span>
									</c:otherwise>
								</c:choose>
							</div>
						</emm:column>

						<emm:column titleKey="Name" property="description" />
						<emm:column headerClass="fit-content" titleKey="target.recipients.delete.scheduled.lastRun" property="lastStart" />

						<emm:column titleKey="ResultMsg">
							<div class="hstack gap-2 overflow-wrap-anywhere">
								<span class="status-badge status.${job.lastResult eq 'OK' ? 'success' : 'error'}"></span>
								<span class="text-truncate-table">${job.lastResult}</span>
							</div>
						</emm:column>

						<emm:column titleKey="predelivery.duration">
							<span>${job.lastDuration} <mvc:message code="seconds" /></span>
						</emm:column>

						<emm:column titleKey="server.status.jobqueue.startAfterError" headerClass="fit-content">
							<div class="flex-center">
								<span class="status-badge status.${job.startAfterError ? 'success' : 'error'}"></span>
							</div>
						</emm:column>

						<emm:column headerClass="fit-content" titleKey="server.status.jobqueue.start.next" property="nextStart" />

						<emm:column titleKey="server.status.jobqueue.error.recipient" property="emailOnError" />
					</emm:table>
				</div>
				<div class="table-wrapper__footer"></div>
			</div>
		</div>
	</div>

	<mvc:form id="filter-tile" cssClass="tile" servletRelativeAction="/serverstatus/jobqueue/search.action" method="GET" modelAttribute="filter"
			  data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
		<div class="tile-header">
			<h1 class="tile-title">
				<i class="icon icon-caret-up mobile-visible"></i>
				<span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
			</h1>
			<div class="tile-controls">
				<a class="btn btn-icon btn-secondary" data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
				<a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
			</div>
		</div>
		<div class="tile-body vstack gap-3 js-scrollable">
			<div>
				<label for="filter-id" class="form-label"><mvc:message code="MailinglistID" /></label>
				<mvc:number id="filter-id" path="id" cssClass="form-control" placeholder="0" step="1" pattern="\d+" />
			</div>

			<div>
				<label for="filter-status" class="form-label"><mvc:message code="default.status" /></label>
				<mvc:select id="filter-status" path="running" cssClass="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
					<mvc:option value=""><mvc:message code="default.All"/></mvc:option>
					<mvc:option value="true"  data-badge-class="job.status.active"><mvc:message code="workflow.view.status.active" /></mvc:option>
					<mvc:option value="false" data-badge-class="job.status.inactive"><mvc:message code="workflow.view.status.inActive" /></mvc:option>
				</mvc:select>
			</div>

			<div>
				<label for="filter-name" class="form-label"><mvc:message code="Name" /></label>
				<mvc:text id="filter-name" path="name" cssClass="form-control" />
			</div>

			<div>
				<label class="form-label" for="filter-next-start-from"><mvc:message code="server.status.jobqueue.start.next" /></label>
				<mvc:dateRange id="filter-next-start" inline="true" path="nextStartDate" />
			</div>

			<div>
				<label for="filter-result" class="form-label"><mvc:message code="ResultMsg" /></label>
				<mvc:select id="filter-result" path="successful" cssClass="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
					<mvc:option value=""><mvc:message code="default.All"/></mvc:option>
					<mvc:option value="true" data-badge-class="status.success"><mvc:message code="OK" /></mvc:option>
					<mvc:option value="false" data-badge-class="status.error"><mvc:message code="default.error" /></mvc:option>
				</mvc:select>
			</div>
		</div>
	</mvc:form>
</div>
