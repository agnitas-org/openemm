<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="com.agnitas.beans.DeliveryStat" %>
<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic" %>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="adminTargetGroupList" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="CAN_SEND_WORLDMAILING" type="java.lang.Boolean"--%>
<%--@elvariable id="CAN_ENABLE_SEND_WORLDMAILING" type="java.lang.Boolean"--%>
<%--@elvariable id="IS_THRESHOlD_CLEARANCE_EXCEEDED" type="java.lang.Boolean"--%>


<c:set var="TYPE_NORMAL" value="<%=MailingType.NORMAL.getCode()%>" scope="request" />
<c:set var="TYPE_FOLLOWUP" value="<%=MailingType.FOLLOW_UP.getCode()%>" scope="request" />
<c:set var="TYPE_ACTIONBASED" value="<%=MailingType.ACTION_BASED.getCode()%>" scope="request" />
<c:set var="TYPE_DATEBASED" value="<%=MailingType.DATE_BASED.getCode()%>" scope="request" />
<c:set var="TYPE_INTERVAL" value="<%=MailingType.INTERVAL.getCode()%>" scope="request" />
<c:set var="ACTION_SAVE_STATUSMAIL_RECIPIENTS" value="<%=ComMailingSendActionBasic.ACTION_SAVE_STATUSMAIL_RECIPIENTS%>" scope="page" />
<c:set var="ACTION_SAVE_STATUSMAIL_ONERRORONLY" value="<%=ComMailingSendActionBasic.ACTION_SAVE_STATUSMAIL_ONERRORONLY%>" scope="page" />
<c:set var="ACTION_VIEW_DELSTATBOX" value="<%=MailingSendAction.ACTION_VIEW_DELSTATBOX%>" scope="page" />
<c:set var="ACTION_DEACTIVATE_MAILING" value="<%=MailingSendAction.ACTION_DEACTIVATE_MAILING%>" scope="page" />
<c:set var="ACTION_ACTIVATE_INTERVALMAILING" value="<%=ComMailingSendActionBasic.ACTION_ACTIVATE_INTERVALMAILING%>" scope="page" />
<c:set var="ACTION_DEACTIVATE_INTERVALMAILING" value="<%=ComMailingSendActionBasic.ACTION_DEACTIVATE_INTERVALMAILING%>" scope="page" />
<c:set var="ACTION_ACTIVATE_CAMPAIGN" value="<%=MailingSendAction.ACTION_ACTIVATE_CAMPAIGN%>" scope="page" />
<c:set var="ACTION_SEND_ADMIN" value="<%=MailingSendAction.ACTION_SEND_ADMIN%>" scope="page" />
<c:set var="ACTION_SEND_TEST" value="<%=MailingSendAction.ACTION_SEND_TEST%>" scope="page" />
<c:set var="ACTION_ACTIVATE_RULEBASED" value="<%=MailingSendAction.ACTION_ACTIVATE_RULEBASED%>" scope="page" />
<c:set var="ACTION_VIEW_SEND" value="<%=MailingSendAction.ACTION_VIEW_SEND%>" scope="page" />
<c:set var="ACTION_VIEW_SEND2" value="<%=MailingSendAction.ACTION_VIEW_SEND2%>" scope="page" />
<c:set var="ACTION_CHECK_LINKS" value="<%=MailingSendAction.ACTION_CHECK_LINKS%>" scope="page" />
<c:set var="ACTION_UNLOCK_SEND" value="<%=ComMailingSendActionBasic.ACTION_UNLOCK_SEND%>" scope="page" />
<c:set var="ACTION_RESUME_SENDING" value="<%=ComMailingSendActionBasic.ACTION_RESUME_SENDING%>" scope="page" />
<c:set var="STATUS_SENT" value="<%=DeliveryStat.STATUS_SENT%>" scope="page" />
<c:set var="ADMIN_TARGET_SINGLE_RECIPIENT" value="${-1}" scope="page" />

<c:set var="ACTION_BASED_MAILING_TYPE" value="<%=MailingType.ACTION_BASED.getCode()%>" scope="page" />
<c:set var="ACTION_PRIORITIZATION_SWITCHING" value="<%=ComMailingSendActionBasic.ACTION_PRIORITIZATION_SWITCHING%>" scope="page" />

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, mailingSendForm.workflowId)}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>

<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid}" />
<c:set var="tmpMailingID" value="${mailingSendForm.mailingID}" />

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<c:if test="${isWorkflowDriven}">
    <c:url var="WORKFLOW_LINK" value="/workflow/${workflowParams.workflowId}/view.action" scope="page">
        <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingSendForm.mailingID}"/>
    </c:url>
</c:if>

<c:set var="tileHeaderActions" scope="page">
	<li class="dropdown">
		<a href="#" class="dropdown-toggle" data-toggle="dropdown">
			<i class="icon icon-eye"></i>
			<span class="text"><bean:message key="default.View" /></span>
			<i class="icon icon-caret-down"></i>
		</a>
		<ul class="dropdown-menu">
			<li>
				<label class="label">
					<input type="radio" value="block" name="view-state" data-view="mailingSend">
					<span class="label-text"><bean:message key="mailing.content.blockview" /></span>
				</label>
				<label class="label">
					<input type="radio" value="split" checked name="view-state" data-view="mailingSend">
					<span class="label-text"><bean:message key="mailing.content.splitview" /></span>
				</label>
			</li>
		</ul>
	</li>
</c:set>

<c:if test="${enableLinkCheck}">
	<c:set var="checkLinksTile">
		<div class="tile">
			<div class="tile-header">
				<h2 class="headline">
					<i class="icon icon-chain"></i>
					<bean:message key="link.check" />
				</h2>
			</div>
			<div class="tile-content tile-content-forms">
				<div class="well block vspace-bottom-10">
					<bean:message key="mailing.link.check.hint" />
				</div>
	
				<a href="#" class="btn btn-regular btn-primary" data-action-value="${ACTION_CHECK_LINKS}" data-action="check-links" data-base-url="<c:url value="/mailingsend.do"/>">
					<i class="icon icon-refresh"></i>
					<span class="text"><bean:message key="button.check" /></span>
				</a>
			</div>
		</div>
	</c:set>
</c:if>

<c:if test="${enableAdminTestDelivery}">
	<c:set var="testRunTile">
		<div class="tile">
			<div class="tile-header">
				<h2 class="headline">
					<i class="icon icon-flask"></i>
					<bean:message key="mailing.testrun" />
				</h2>
			</div>
	
			<div class="tile-content tile-content-forms">
				<c:set var="moveDelivery" value="${false}" />
	
			<c:choose>
				<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
					<div class="well block">
						<c:choose>
							<c:when test="${mailingSendForm.isTemplate}">
								<bean:message key="template.send.test" />
							</c:when>
							<c:otherwise>
								<bean:message key="mailing.send.test" />
							</c:otherwise>
						</c:choose>
					</div>
	
					<div id="test-send-controls-group" class="vspace-top-10"
						 style="${mailingSendForm.transmissionRunning ? 'display: none;' : ''}">
						<emm:ShowByPermission token="mailing.send.admin.target">
							<div class="form-group">
								<div class="col-sm-4">
									<label class="control-label" for="adminTargetGroupSelect">
										<bean:message key="mailing.send.admin.target.to" />
									</label>
								</div>
								<div class="col-sm-8">
									<agn:agnSelect name="mailingSendForm" property="adminTargetGroupID" styleId="adminTargetGroupSelect"
												   styleClass="form-control js-select" data-action="admin-target-group"
												   data-initializer="test-run-recipients-select">
										<html:option value="0"> <bean:message key="mailing.send.adminOrTest" /></html:option>
	
										<html:option value="${ADMIN_TARGET_SINGLE_RECIPIENT}">
											<bean:message key="mailing.test.recipient.single" />
										</html:option>
	
										<c:forEach var="targetGroup" items="${adminTargetGroupList}">
											<html:option value="${targetGroup.id}">${targetGroup.targetName}</html:option>
										</c:forEach>
									</agn:agnSelect>
								</div>
							</div>
						</emm:ShowByPermission>
					</div>
	
					<div class="form-group ${mailingSendForm.adminTargetGroupID eq ADMIN_TARGET_SINGLE_RECIPIENT ? '' : 'hidden'}" id="test-recipients-table">
						<div class="col-sm-push-4 col-sm-8">
							<div class="table-responsive">
								<table class="table table-bordered table-striped">
									<thead>
									<tr>
										<th><bean:message key="settings.Admin.email" /></th>
										<th></th>
									</tr>
									</thead>
									<tbody>
									<c:set var="lastAddress" value="" />
	
									<c:forEach var="address" items="${mailingSendForm.mailingTestRecipients}" varStatus="status">
										<c:choose>
											<c:when test="${status.last}">
												<c:set var="lastAddress" value="${address}" />
											</c:when>
											<c:otherwise>
												<tr>
													<td>
														<input type="text" name="mailingTestRecipients" class="form-control" value="${fn:escapeXml(address)}" data-action="edit-test-recipient" />
													</td>
													<td class="table-actions">
														<button type="button" class="btn btn-regular btn-alert" data-tooltip="<bean:message key='button.Delete'/>" data-action="remove-test-recipient">
															<i class="icon icon-trash-o"></i>
														</button>
													</td>
												</tr>
											</c:otherwise>
										</c:choose>
									</c:forEach>
	
									<tr>
										<td>
											<input type="text" id="new-test-recipient" name="mailingTestRecipients" class="form-control" data-action="new-test-recipient" value="${fn:escapeXml(lastAddress)}" />
										</td>
										<td class="table-actions">
											<button type="button" class="btn btn-regular btn-primary" data-tooltip="<bean:message key='button.Add'/>" data-action="add-test-recipient">
												<i class="icon icon-plus"></i>
											</button>
										</td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
	
					<div class="form-group">
						<div class="col-sm-offset-1 col-sm-11">
							<div class="btn-group">
								<a href="#"
								   id="adminSendButton"
								   class="btn btn-regular btn-primary"
								   data-action-value="${ACTION_SEND_ADMIN}"
								   data-action="send-admin"
								   data-base-url="<c:url value="/mailingsend.do"/>"
								>
									<i class="icon icon-send-o"></i>
									<span class="text">
										<bean:message key="adminMail" />
									</span>
								</a>
	
								<a href="#" class="btn btn-regular btn-primary"
								   data-action-value="${ACTION_SEND_TEST}"
								   data-action="send-test"
								   data-base-url="<c:url value="/mailingsend.do"/>"
								>
									<i class="icon icon-send-o"></i>
									<span class="text">
										<bean:message key="testMail" />
									</span>
								</a>
	
								<c:if test="${not empty externalEditorLink}">
									<a href="${externalEditorLink}" target="_POST_MailingTab" class="btn btn-regular btn-primary">
										<i class="icon icon-send-o"></i>
										<span class="text">
											<bean:message key="openExternalEditor" />
										</span>
									</a>
								</c:if>
							</div>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div class="form-group">
						<div class="notification notification-warning">
							<div class="notification-header">
								<p class="headline">
									<i class="icon icon-state-warning"></i>
									<span class="text"><bean:message key="error.mailing.send" /></span>
								</p>
							</div>
	
							<div class="notification-content">
								<p><bean:message key="MailingTestAdmin.deleted_target_groups" /></p>
							</div>
						</div>
	
					</div>
	
					<div class="form-group">
						<div class="notification notification-warning">
							<div class="notification-header">
								<p class="headline">
									<i class="icon icon-state-warning"></i>
									<span class="text"><bean:message key="error.mailing.send" /></span>
								</p>
							</div>
	
							<div class="notification-content">
								<p><bean:message key="MailingTestDistrib.deleted_target_groups" /></p>
							</div>
						</div>
					</div>
				</c:otherwise>
			</c:choose>
			</div>
		</div>
	</c:set>
</c:if>

<tiles:insert page="template.jsp">
	<tiles:put name="header" type="string">
		<ul class="tile-header-nav">
			<%--<div class="headline">
				<i class="icon icon-th-list"></i>
			</div>--%>

			<!-- Tabs BEGIN -->
			<tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false" />
			<!-- Tabs END -->
		</ul>

		<c:if test="${isMailingGrid}">
			<ul class="tile-header-actions">${tileHeaderActions}</ul>
		</c:if>
	</tiles:put>

	<%-- There're footer items as far --%>

	<tiles:put name="content" type="string">
		<div class="${isMailingGrid ? "tile-content-padded" : "row"}">

			<c:set var="contentAttr" value=""/>
			<c:if test="${mailingSendForm.mailingtype eq TYPE_NORMAL or mailingSendForm.mailingtype eq TYPE_FOLLOWUP}">
				<c:set var="contentAttr" value="data-view-block='col-xs-12 row-1-1' data-view-split='col-md-6' data-view-hidden='col-xs-12 row-1-1'"/>
			</c:if>
			<div class="col-xs-12 row-1-1" ${contentAttr} data-controller="mailing-send">
				<agn:agnForm action="/mailingsend.do" id="testDeliveryForm" data-form="static">
					<input type="hidden" name="mailingID" value="${tmpMailingID}" />
					<input type="hidden" name="action" value="${ACTION_VIEW_SEND}" />
					${checkLinksTile}
                    ${testRunTile}
				</agn:agnForm>

				<logic:equal name="mailingSendForm" property="isTemplate" value="false">
					<div class="tile">
						<div class="tile-header">
							<h2 class="headline">
								<i class="icon icon-send-o"></i>
								<bean:message key="mailing.Delivery" />
							</h2>
						</div>
						<div class="tile-content tile-content-forms">
							<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_NORMAL}">
								<%@include file="delivery-normal-settings.jspf"%>
							</logic:equal>
							<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_ACTIONBASED}">
								<%@include file="delivery-actionbased-settings.jspf"%>
							</logic:equal>
							<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_DATEBASED}">
								<%@include file="delivery-datebased-settings.jspf"%>
							</logic:equal>
							<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_FOLLOWUP}">
								<%@include file="delivery-followup-settings.jspf"%>
							</logic:equal>
							<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_INTERVAL}">
								<%@include file="delivery-interval-settings.jspf"%>
							</logic:equal>
						</div>
						<!-- Tile Content END -->
					</div>
					<!-- Tile END -->
				</logic:equal>
				<logic:equal name="mailingSendForm" property="mailingtype" value="${ACTION_BASED_MAILING_TYPE}">
					<%@ include file="mailing-send-dependents-list.jsp"%>
				</logic:equal>
			</div>
			<!-- Col END -->
			<c:if test="${not mailingSendForm.isTemplate}">
				<c:if test="${mailingSendForm.mailingtype eq TYPE_NORMAL or mailingSendForm.mailingtype eq TYPE_FOLLOWUP}">
					<div data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
						<div class="tile">
							<div class="tile-header">
								<h2 class="headline"><bean:message key="Status" /></h2>
							</div>
							<div class="tile-content">
								<div class="mailing-preview-wrapper">
									<c:url var="deliveryStatisticsBoxLink" value="/mailingsend.do">
										<c:param name="action" value="${ACTION_VIEW_DELSTATBOX}" />
										<c:param name="mailingID" value="${tmpMailingID}" />
									</c:url>
									<div data-load="${deliveryStatisticsBoxLink}" data-load-interval="50000"></div>
								</div>
							</div>
						</div>
					</div>
					<!-- Col END -->
				</c:if>
			</c:if>
		</div>
		<!-- Row END -->
	</tiles:put>
</tiles:insert>

<script id="recipients-row" type="text/x-mustache-template">
	<tr class="js-recipients-row">
		<td>{{= email }}</td>
		<td class="table-actions">
			<input type="hidden" name="{{= _.uniqueId('statusmailRecipient_') }}" value="{{= email }}" />
			<a href="#" class="btn btn-regular btn-alert" data-action="recipients-row-remove" data-form-target="#statusMailRecipientsForm" data-from-submit data-tooltip="<bean:message key='button.Delete'/>">
				<i class="icon icon-trash-o"></i>
			</a>
		</td>
	</tr>
</script>

<script id="test-recipient-row" type="text/x-mustache-template">
	<tr>
		<td>
			<input type="text" name="mailingTestRecipients" class="form-control" value="{{- value }}" data-action="edit-test-recipient" />
		</td>
		<td class="table-actions">
			<button type="button" class="btn btn-regular btn-alert" data-tooltip="<bean:message key='button.Delete'/>" data-action="remove-test-recipient">
				<i class="icon icon-trash-o"></i>
			</button>
		</td>
	</tr>
</script>

<script id="warning-mailing-size-modal" type="text/x-mustache-template">
	<div class="modal modal-wide">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
					<h4 class="modal-title text-state-warning">
						<i class="icon icon-state-warning"></i>
						<bean:message key="warning" />
					</h4>
				</div>

				<div class="modal-body">
					<c:set var="mailingSizeWarningThreshold" value="${emm:formatBytes(mailingSendForm.sizeWarningThreshold, 1, 'iec', emm:getLocale(pageContext.request))}" />
					<p><bean:message key="warning.mailing.size.large" arg0="${mailingSizeWarningThreshold}" /></p>
				</div>

				<div class="modal-footer">
					<div class="btn-group">
						<button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
							<i class="icon icon-times"></i>
							<span class="text">
								<bean:message key="button.Cancel" />
							</span>
						</button>

						<button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
							<i class="icon icon-check"></i>
							<span class="text">
								<bean:message key="button.Proceed" />
							</span>
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</script>

<script id="error-mailing-size-modal" type="text/x-mustache-template">
	<div class="modal">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
					<h4 class="modal-title text-state-alert">
						<i class="icon icon-state-alert"></i>
						<bean:message key="Error" />
					</h4>
				</div>

				<div class="modal-body">
					<c:set var="mailingSizeErrorThreshold" value="${emm:formatBytes(mailingSendForm.sizeErrorThreshold, 1, 'iec', emm:getLocale(pageContext.request))}" />
					<p><bean:message key="error.mailing.size.large" arg0="${mailingSizeErrorThreshold}" /></p>
				</div>

				<div class="modal-footer">
					<div class="btn-group">
						<button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
							<i class="icon icon-times"></i>
							<span class="text">
								<bean:message key="button.OK" />
							</span>
						</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</script>
