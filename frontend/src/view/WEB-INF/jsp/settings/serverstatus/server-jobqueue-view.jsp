<%@ page language="java" contentType="text/html; charset=utf-8"
	errorPage="/error.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<div class="tile">
	<div class="tile-header">
		<div class="headline">
			<mvc:message code="settings.jobqueue.show" />
		</div>
	</div>
	<div class="tile-content">
		<div class="table-wrapper">
			<display:table class="table table-bordered table-striped js-table"
				id="job" name="activeJobQueueList" sort="page" partialList="false"
				excludedParams="*">

				<%-- Prevent table controls/headers collapsing when the table is empty --%>
				<display:setProperty name="basic.empty.showtable" value="true" />

				<display:setProperty name="basic.msg.empty_list_row" value=" " />

				<display:column headerClass="sortable" property="id"
					titleKey="MailinglistID" sortable="false" />

				<display:column headerClass="sortable" titleKey="default.status"
					sortable="false">
					<c:if test="${job.running}">
						<span class="status-badge campaign.status.active"></span>
						<mvc:message code="default.status.active" />
					</c:if>

					<c:if test="${not job.running}">
						<span class="status-badge campaign.status.inactive"></span>
						<mvc:message code="workflow.view.status.inActive" />
					</c:if>
				</display:column>

				<display:column headerClass="sortable" property="description"
					titleKey="Description" sortable="false" />

				<display:column headerClass="sortable" titleKey="startDate"
					sortable="false">
					<emm:formatDate value="${job.nextStart}" format="${dateTimeFormat}" />
				</display:column>

				<display:column headerClass="sortable" titleKey="default.Success"
					sortable="false">
					<c:if test="${job.lastResult eq 'OK'}">
						<span class="mailing-badge mailing.status.ready"></span>
					</c:if>
					<c:if test="${job.lastResult ne 'OK'}">
						<i class="icon-fa5 icon-fa5-exclamation icon-jobqueue-failure"></i>
					</c:if>
				</display:column>

				<display:column headerClass="sortable" titleKey="ResultMsg"
					sortable="false" style="word-wrap: break-word;">
					<span class="multiline-auto">${job.lastResult}</span>
				</display:column>

			</display:table>
		</div>
	</div>
</div>
