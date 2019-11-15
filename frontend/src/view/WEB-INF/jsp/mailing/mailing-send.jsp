<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="com.agnitas.beans.DeliveryStat" %>
<%@ page import="com.agnitas.web.ComMailingSendAction" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="adminTargetGroupList" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="CAN_SEND_WORLDMAILING" type="java.lang.Boolean"--%>
<%--@elvariable id="CAN_ENABLE_SEND_WORLDMAILING" type="java.lang.Boolean"--%>


<c:set var="TYPE_NORMAL" value="<%= Mailing.TYPE_NORMAL %>" scope="request" />
<c:set var="TYPE_FOLLOWUP" value="<%= Mailing.TYPE_FOLLOWUP %>" scope="request" />
<c:set var="TYPE_ACTIONBASED" value="<%= Mailing.TYPE_ACTIONBASED %>" scope="request" />
<c:set var="TYPE_DATEBASED" value="<%= Mailing.TYPE_DATEBASED %>" scope="request" />
<c:set var="TYPE_INTERVAL" value="<%= Mailing.TYPE_INTERVAL %>" scope="request" />
<c:set var="ACTION_SAVE_STATUSMAIL_RECIPIENTS" value="<%= ComMailingSendAction.ACTION_SAVE_STATUSMAIL_RECIPIENTS %>" scope="page" />
<c:set var="ACTION_SAVE_STATUSMAIL_ONERRORONLY" value="<%= ComMailingSendAction.ACTION_SAVE_STATUSMAIL_ONERRORONLY %>" scope="page" />
<c:set var="ACTION_VIEW_DELSTATBOX" value="<%= MailingSendAction.ACTION_VIEW_DELSTATBOX %>" scope="page" />
<c:set var="ACTION_DEACTIVATE_MAILING" value="<%= MailingSendAction.ACTION_DEACTIVATE_MAILING %>" scope="page" />
<c:set var="ACTION_ACTIVATE_INTERVALMAILING" value="<%= ComMailingSendAction.ACTION_ACTIVATE_INTERVALMAILING%>" scope="page" />
<c:set var="ACTION_DEACTIVATE_INTERVALMAILING" value="<%= ComMailingSendAction.ACTION_DEACTIVATE_INTERVALMAILING %>" scope="page" />
<c:set var="ACTION_ACTIVATE_CAMPAIGN" value="<%= MailingSendAction.ACTION_ACTIVATE_CAMPAIGN %>" scope="page" />
<c:set var="ACTION_SEND_ADMIN" value="<%= MailingSendAction.ACTION_SEND_ADMIN %>" scope="page" />
<c:set var="ACTION_SEND_TEST" value="<%= MailingSendAction.ACTION_SEND_TEST %>" scope="page" />
<c:set var="ACTION_ACTIVATE_RULEBASED" value="<%=MailingSendAction.ACTION_ACTIVATE_RULEBASED%>" scope="page" />
<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>" scope="page" />
<c:set var="ACTION_VIEW_SEND2" value="<%= MailingSendAction.ACTION_VIEW_SEND2 %>" scope="page" />
<c:set var="ACTION_CHECK_LINKS" value="<%= MailingSendAction.ACTION_CHECK_LINKS %>" scope="page" />
<c:set var="ACTION_UNLOCK_SEND" value="<%= ComMailingSendAction.ACTION_UNLOCK_SEND %>" scope="page" />
<c:set var="ACTION_PRIORITIZATION_SWITCHING" value="<%= ComMailingSendAction.ACTION_PRIORITIZATION_SWITCHING %>" scope="page" />
<c:set var="STATUS_SENT" value="<%= DeliveryStat.STATUS_SENT %>" scope="page" />
<c:set var="ADMIN_TARGET_SINGLE_RECIPIENT" value="${-1}" scope="page" />

<c:set var="workflowParameters" value="${emm:getWorkflowParams(pageContext.request)}"/>
<c:set var="isWorkflowDriven" value="${not empty workflowParameters and workflowParameters.workflowId > 0}"/>

<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid}" />
<c:set var="tmpMailingID" value="${mailingSendForm.mailingID}" />

<c:if test="${isWorkflowDriven}">
	<%--todo: GWUA-4271: change after test sucessfully--%>
	<%--<c:url var="WORKFLOW_LINK" value="/workflow/${workflowParameters.workflowId}/view.action">--%>
		<%--<c:param name="forwardParams" value="${workflowParameters.workflowForwardParams};elementValue=${mailingSendForm.mailingID}"/>--%>
	<%--</c:url>--%>

	<c:url var="WORKFLOW_LINK" value="/workflow.do">
		<c:param name="method" value="view"/>
		<c:param name="workflowId" value="${workflowParameters.workflowId}"/>
		<c:param name="forwardParams" value="${workflowParameters.workflowForwardParams};elementValue=${mailingSendForm.mailingID}"/>
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

			<c:set var="contentClasses" value=""/>
			<c:if test="${mailingSendForm.mailingtype == TYPE_NORMAL || mailingSendForm.mailingtype == TYPE_FOLLOWUP}">
				<c:set var="contentClasses" value="data-view-block='col-xs-12 row-1-1' data-view-split='col-md-6' data-view-hidden='col-xs-12 row-1-1'"/>
			</c:if>
			<div class="col-xs-12 row-1-1" ${contentClasses} data-controller="mailing-send">
				<agn:agnForm action="/mailingsend" id="testDeliveryForm" data-form="resource">
					<input type="hidden" name="mailingID" value="${tmpMailingID}" />
					<input type="hidden" name="action" value="${ACTION_VIEW_SEND}" />

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

							<a href="#" class="btn btn-regular btn-primary" data-action-value="${ACTION_CHECK_LINKS}" data-action="check-links">
								<i class="icon icon-refresh"></i>
								<span class="text"><bean:message key="button.check" /></span>
							</a>
						</div>
						<!-- Tile Content END -->
					</div>
					<!-- Tile END -->

					<div class="tile">
						<div class="tile-header">
							<h2 class="headline">
								<i class="icon icon-flask"></i>
								<bean:message key="mailing.testrun" />
							</h2>
						</div>

						<div class="tile-content tile-content-forms">
							<c:set var="moveDelivery" value="false" />

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

									<div id="test-send-controls-group" class="vspace-top-10" style="${mailingSendForm.transmissionRunning ? 'display: none;' : ''}">
										<emm:ShowByPermission token="mailing.send.admin.target">
											<div class="form-group">
												<div class="col-sm-4">
													<label class="control-label" for="adminTargetGroupSelect">
														<bean:message key="mailing.send.admin.target.to" />
													</label>
												</div>
												<div class="col-sm-8">
													<agn:agnSelect property="adminTargetGroupID" styleId="adminTargetGroupSelect" styleClass="form-control js-select" data-action="admin-target-group">
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
										<div class="col-sm-offset-4 col-sm-8">
											<div class="btn-group">
												<a href="#"
												   id="adminSendButton"
												   class="btn btn-regular btn-primary"
												   data-action-value="${ACTION_SEND_ADMIN}"
												   data-action="start-delivery">
													<i class="icon icon-send-o"></i>
													<span class="text">
														<bean:message key="adminMail" />
													</span>
												</a>

												<a href="#" class="btn btn-regular btn-primary"
												   data-action-value="${ACTION_SEND_TEST}"
												   data-action="start-delivery">
													<i class="icon icon-send-o"></i>
													<span class="text">
														<bean:message key="testMail" />
													</span>
												</a>
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
						<!-- Tile Content END -->
					</div>
					<!-- Tile END -->
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
							<div class="form-group">
								<div class="well block">
									<logic:equal name="mailingSendForm" property="worldMailingSend" value="true">
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_NORMAL}">
											<c:choose>
												<c:when test="${mailingSendForm.deliveryStat.deliveryStatus eq STATUS_SENT}">
													<bean:message key="mailing.send.finished" />
												</c:when>
												<c:otherwise>
													<bean:message key="mailing.send.scheduled" />
												</c:otherwise>
											</c:choose>

										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_ACTIONBASED}">
											<bean:message key="mailing.send.active.event" /><br>
											<bean:message key="mailing.send.deactivate" />
										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_DATEBASED}">
											<bean:message key="mailing.send.active.date" /><br>
											<bean:message key="mailing.send.deactivate" />
											<logic:equal name="mailingSendForm" property="worldMailingSend" value="true">
												<bean:message key="SendingTimeDaily" />:&nbsp;
												<fmt:formatDate value="${mailingSendForm.deliveryStat.scheduledSendTime}"
																pattern="${adminTimeFormat}" timeZone="${adminTimeZone}" />
											</logic:equal>
										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_FOLLOWUP}">
											<bean:message key="mailing.send.finished" />
										</logic:equal>
									</logic:equal>

									<logic:equal name="mailingSendForm" property="worldMailingSend" value="false">
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_NORMAL}">
											<bean:message key="mailing.send.ready" />
										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_ACTIONBASED}">
											<bean:message key="mailing.send.ready" /><br>
											<bean:message key="mailing.send.activate.event" />
										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_DATEBASED}">
											<bean:message key="mailing.send.ready" /><br>
											<bean:message key="mailing.send.activate.date" />
										</logic:equal>
										<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_FOLLOWUP}">
											<bean:message key="mailing.send.ready" />
										</logic:equal>
									</logic:equal>
								</div>
							</div>

							<logic:equal name="mailingSendForm" property="isTemplate" value="false">

								<%-- Deativate buttons--%>
								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_ACTIONBASED}">
									<logic:equal name="mailingSendForm" property="worldMailingSend" value="true">
										<emm:ShowByPermission token="mailing.send.world">
											<div class="form-group">
												<div class="col-sm-4">
													<label class="control-label">
														<bean:message key="MailingDeactivate" />
													</label>
												</div>

												<div class="col-sm-8">
													<c:if test="${not isWorkflowDriven}">
														<html:link styleClass="btn btn-regular"
																   page='/mailingsend.do?action=${ACTION_DEACTIVATE_MAILING}&mailingID=${tmpMailingID}'>
															<i class="icon icon-ban"></i>
															<span class="text"><bean:message key="btndeactivate" /></span>
														</html:link>
													</c:if>
													<c:if test="${isWorkflowDriven}">
														<div class="input-group">
															<div class="input-group-btn">
																<button class="btn btn-regular" disabled='disabled'>
																	<i class="icon icon-ban"></i>
																	<span class="text"><bean:message key="btndeactivate" /></span>
																</button>
																<a href="${WORKFLOW_LINK}" class="btn btn-info btn-regular">
																	<i class="icon icon-linkage-campaignmanager"></i>
																	<strong><bean:message key="campaign.manager.icon"/></strong>
																</a>
															</div>
														</div>
													</c:if>
												</div>

											</div>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_INTERVAL}">
									<logic:equal name="mailingSendForm" property="workStatus" value="mailing.status.active">
										<emm:ShowByPermission token="mailing.send.world">
											<div class="form-group">
												<div class="col-sm-4">
													<label class="control-label">
														<bean:message key="MailingDeactivate" />
													</label>
												</div>
												<div class="col-sm-8">
													<html:link styleClass="btn btn-regular" page='/mailingsend.do?action=${ACTION_DEACTIVATE_INTERVALMAILING}&mailingID=${tmpMailingID}'>
														<i class="icon icon-ban"></i>
														<span class="text"><bean:message key="btndeactivate" /></span>
													</html:link>
												</div>
											</div>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_DATEBASED}">
									<logic:equal name="mailingSendForm" property="worldMailingSend" value="true">
										<emm:ShowByPermission token="mailing.send.world">
											<div class="form-group">
												<div class="col-sm-4">
													<label class="control-label">
														<bean:message key="MailingDeactivate" />
													</label>
												</div>
												<div class="col-sm-8">
													<c:if test="${not isWorkflowDriven}">
															<html:link styleClass="btn btn-regular"
																	   page='/mailingsend.do?action=${ACTION_DEACTIVATE_MAILING}&mailingID=${tmpMailingID}'>
																<i class="icon icon-ban"></i>
																<span class="text"><bean:message key="btndeactivate" /></span>
															</html:link>
													</c:if>
													<c:if test="${isWorkflowDriven}">
														<div class="input-group">
															<div class="input-group-btn">
																<button class="btn btn-regular" disabled='disabled'>
																	<i class="icon icon-ban"></i>
																	<span class="text"><bean:message key="btndeactivate" /></span>
																</button>
																<a href="${WORKFLOW_LINK}" class="btn btn-info btn-regular">
																	<i class="icon icon-linkage-campaignmanager"></i>
																	<strong><bean:message key="campaign.manager.icon"/></strong>
																</a>
															</div>
														</div>
													</c:if>
												</div>
											</div>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<%-- Activate buttons--%>
								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_ACTIONBASED}">
									<logic:equal name="mailingSendForm" property="worldMailingSend" value="false">
										<emm:ShowByPermission token="mailing.send.world">
											<c:choose>
												<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
													<div class="form-group">
														<div class="col-sm-4">
															<label class="control-label">
																<bean:message key="MailingActivate" />
															</label>
														</div>
														<div class="col-sm-8">
															<c:if test="${not isWorkflowDriven}">
																<html:link styleClass="btn btn-regular"
																		page="/mailingsend.do?action=${ACTION_ACTIVATE_CAMPAIGN}&to=3&mailingID=${tmpMailingID}">
																	<i class="icon icon-check-circle-o"></i>
																	<span class="text"><bean:message key="button.Activate" /></span>
																</html:link>
															</c:if>
															<c:if test="${isWorkflowDriven}">
																<div class="input-group">
																	<div class="input-group-btn">
																		<button class="btn btn-regular" disabled='disabled'>
																			<i class="icon icon-check-circle-o"></i>
																			<span class="text"><bean:message key="button.Activate" /></span>
																		</button>
																		<a href="${WORKFLOW_LINK}" class="btn btn-info btn-regular">
																			<i class="icon icon-linkage-campaignmanager"></i>
																			<strong><bean:message key="campaign.manager.icon"/></strong>
																		</a>
																	</div>
																</div>
															</c:if>
														</div>
													</div>
												</c:when>
												<c:otherwise>
													<div class="form-group">
														<div class="col-sm-8 col-sm-4-push">
															<div class="well">
																<bean:message key="mailing.activate.deleted_target_groups" />
															</div>
														</div>
													</div>
												</c:otherwise>
											</c:choose>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_INTERVAL}">
									<logic:equal name="mailingSendForm" property="workStatus" value="mailing.status.disable">
										<emm:ShowByPermission token="mailing.send.world">
											<c:choose>
												<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
													<div class="form-group">
														<div class="col-sm-4">
															<label class="control-label">
																<bean:message key="MailingActivate" />
															</label>
														</div>
														<div class="col-sm-8">
															<html:link styleClass="btn btn-regular" page="/mailingsend.do?action=${ACTION_ACTIVATE_INTERVALMAILING}&mailingID=${tmpMailingID}">
																<i class="icon icon-check-circle-o"></i>
																<span class="text"><bean:message key="button.Activate" /></span>
															</html:link>
														</div>
													</div>
												</c:when>
												<c:otherwise>
													<div class="form-group">
														<div class="col-sm-8 col-sm-4-push">
															<div class="well">
																<bean:message key="mailing.activate.deleted_target_groups" />
															</div>
														</div>
													</div>
												</c:otherwise>
											</c:choose>
										</emm:ShowByPermission>
									</logic:equal>
									<logic:equal name="mailingSendForm" property="workStatus" value="mailing.status.new">
										<emm:ShowByPermission token="mailing.send.world">
											<c:choose>
												<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
													<div class="form-group">
														<div class="col-sm-4">
															<label class="control-label">
																<bean:message key="MailingActivate" />
															</label>
														</div>
														<div class="col-sm-8">
															<html:link styleClass="btn btn-regular" page="/mailingsend.do?action=${ACTION_ACTIVATE_INTERVALMAILING}&mailingID=${tmpMailingID}">
																<i class="icon icon-ban"></i>
																<span class="text"><bean:message key="button.Activate" /></span>
															</html:link>
														</div>
													</div>
												</c:when>
												<c:otherwise>
													<div class="form-group">
														<div class="col-sm-8 col-sm-4-push">
															<div class="well">
																<bean:message key="mailing.activate.deleted_target_groups" />
															</div>
														</div>
													</div>
												</c:otherwise>
											</c:choose>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<logic:equal name="mailingSendForm" property="mailingtype" value="${TYPE_DATEBASED}">
									<logic:equal name="mailingSendForm" property="worldMailingSend" value="false">
										<emm:ShowByPermission token="mailing.send.world">
											<c:choose>
												<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">

													<agn:agnForm action="/mailingsend" id="activateMailingForm" data-form="resource">
														<input type="hidden" name="action" value="${ACTION_ACTIVATE_RULEBASED}">
														<input type="hidden" name="to" value="4">
														<html:hidden property="mailingID" />

														<div class="form-group">
															<div class="col-sm-4">
																<label class="control-label"><bean:message key="MailingActivate" /></label>
															</div>
															<div class="col-sm-8">
																<button type="button" tabindex="-1" class="btn btn-regular btn-primary" data-form-submit>
																	<span><bean:message key="button.Activate" /></span>
																</button>
															</div>
														</div>

														<div class="form-group">
															<div class="col-sm-4">
																<label class="control-label" for="sendTime">
																	<bean:message key="SendingTimeDaily" />
																</label>
															</div>

															<div class="col-sm-8">
																<c:if test="${not isWorkflowDriven}">
																	<%
																		GregorianCalendar aDate = new GregorianCalendar(AgnUtils.getTimeZone(request));
																		DateFormat internalFormat = new SimpleDateFormat("yyyyMMdd");
																	%>

																	<input type="hidden" name="sendDate" value="<%= internalFormat.format(aDate.getTime()) %>">

																	<div class="input-group" data-field="split">
																		<div class="input-group-controls">
																			<c:set var="sendHour"><fmt:formatNumber minIntegerDigits="2" value="${mailingSendForm.sendHour}" /></c:set>
																			<c:set var="sendMinute"><fmt:formatNumber minIntegerDigits="2" value="${mailingSendForm.sendMinute}" /></c:set>

																			<input type="text" id="sendTime" name="sendTime" class="form-control js-timepicker" value="${sendHour}:${sendMinute}"
																				   data-field-split="sendHour, sendMinute" data-field-split-rule=":" data-timepicker-options="mask: 'h:00'" />
																		</div>
																		<div class="input-group-addon">
																			<span class="addon">
																				<i class="icon icon-clock-o"></i>
																			</span>
																		</div>
																		<div class="input-group-addon">
																			<span class="addon">
																				${emm:getTimeZoneId(pageContext.request)}
																			</span>
																		</div>
																	</div>

																	<p class="help-block"><bean:message key="default.interval" />: <bean:message key="default.minutes.60" /></p>
																</c:if>
																<c:if test="${isWorkflowDriven}">
																	<div class="input-group" data-field="split">
																		<div class="input-group-controls">
																			<c:set var="sendHour"><fmt:formatNumber minIntegerDigits="2" value="${mailingSendForm.sendHour}" /></c:set>
																			<c:set var="sendMinute"><fmt:formatNumber minIntegerDigits="2" value="${mailingSendForm.sendMinute}" /></c:set>

																			<input type="text" id="sendTime" name="sendTime" class="form-control js-timepicker" value="${sendHour}:${sendMinute}"
																				   data-field-split="sendHour, sendMinute" data-field-split-rule=":" data-timepicker-options="mask: 'h:00'" disabled='disabled'/>
																		</div>
																		<div class="input-group-addon" disabled='disabled'>
																			<span class="addon">
																				<i class="icon icon-clock-o"></i>
																			</span>
																		</div>
																		<div class="input-group-addon" disabled='disabled'>
																			<span class="addon">
																				${emm:getTimeZoneId(pageContext.request)}
																			</span>
																		</div>
																		<a href="${WORKFLOW_LINK}" class="btn btn-info btn-regular">
																			<i class="icon icon-linkage-campaignmanager"></i>
																			<strong><bean:message key="campaign.manager.icon"/></strong>
																		</a>
																	</div>
																</c:if>
															</div>
															<div class="col-sm-8">

															</div>
														</div>
													</agn:agnForm>

												</c:when>
												<c:otherwise>
													<div class="form-group">
														<div class="col-sm-8 col-sm-4-push">
															<div class="well">
																<bean:message key="mailing.activate.deleted_target_groups" />
															</div>
														</div>
													</div>
												</c:otherwise>
											</c:choose>
										</emm:ShowByPermission>
									</logic:equal>
								</logic:equal>

								<%@ include file="mailing-send-prioritization.jspf" %>

								<div class="form-group">
									<div class="col-sm-4">
										<label class="control-label">
											<bean:message key="mailing.size" />
											<button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxSize.xml"></button>
										</label>
									</div>

									<div class="col-sm-8">
										<p class="form-control-static">
												<%-- Show the value at least in KB since the byte number is inaccurate anyway --%>
											<c:set var="approximateMaxSizeWithoutExternalImages" value="${emm:formatBytes(mailingSendForm.approximateMaxSizeWithoutExternalImages, 1, 'iec', emm:getLocale(pageContext.request))}" />
											<c:set var="approximateMaxSize" value="${emm:formatBytes(mailingSendForm.approximateMaxSize, 1, 'iec', emm:getLocale(pageContext.request))}" />

											<c:choose>
												<%-- Compare rounded values, not the accurate values in bytes --%>
												<c:when test="${approximateMaxSize eq approximateMaxSizeWithoutExternalImages}">
													<c:set var="sizeMessage">${approximateMaxSize}</c:set>
												</c:when>
												<c:otherwise>
													<c:set var="sizeMessage">${approximateMaxSizeWithoutExternalImages} (${approximateMaxSize})</c:set>
												</c:otherwise>
											</c:choose>

											<c:choose>
												<c:when test="${mailingSendForm.approximateMaxSize < mailingSendForm.sizeWarningThreshold}">
													${sizeMessage}
												</c:when>
												<c:otherwise>
													<span style="color: red; font-weight: bold;">${sizeMessage}</span>
												</c:otherwise>
											</c:choose>
										</p>
									</div>
								</div>

								<emm:ShowByPermission token="mailing.send.world">
									<c:if test="${CAN_SEND_WORLDMAILING}">
										<c:choose>
											<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
												<div class="form-group">
													<div class="col-sm-4">
														<label class="control-label">
															<bean:message key="mailing.Delivery" />
														</label>
													</div>
													<div class="col-sm-8">
														<c:url var="configureLink" value="/mailingsend.do">
															<c:param name="action" value="${ACTION_VIEW_SEND2}" />
															<c:param name="mailingID" value="${tmpMailingID}" />
														</c:url>

														<c:choose>
															<c:when test="${mailingSendForm.approximateMaxSize < mailingSendForm.sizeWarningThreshold}">
																<c:set var="configureButtonAction" value="configure-delivery" />
															</c:when>
															<c:when test="${mailingSendForm.approximateMaxSizeWithoutExternalImages < mailingSendForm.sizeErrorThreshold}">
																<c:set var="configureButtonAction" value="configure-delivery-mailing-size-warning" />
															</c:when>
															<c:otherwise>
																<c:set var="configureButtonAction" value="configure-delivery-mailing-size-error" />
															</c:otherwise>
														</c:choose>

														<button type="button" class="btn btn-regular btn-primary" data-action="${configureButtonAction}" data-url="${configureLink}">
															<i class="icon icon-paper-plane"></i>
															<span class="text"><bean:message key="button.configure" /></span>
														</button>
													</div>
												</div>
											</c:when>
											<c:otherwise>
												<div class="form-group">
													<div class="col-sm-8 col-sm-4-push">
														<div class="well">
															<bean:message key="mailing.activate.deleted_target_groups" />
														</div>
													</div>
												</div>
											</c:otherwise>
										</c:choose>
									</c:if>
								</emm:ShowByPermission>

								<emm:ShowByPermission token="mailing.send.world">
									<c:if test="${CAN_ENABLE_SEND_WORLDMAILING}">
										<c:choose>
											<c:when test="${not mailingSendForm.hasDeletedTargetGroups}">
												<div class="form-group">
													<div class="col-sm-4">
														<label class="control-label">
															<bean:message key="MailingUnlockSend" />
														</label>
													</div>
													<div class="col-sm-8">
														<html:link styleClass="btn btn-regular"
																	page="/mailingsend.do?action=${ACTION_UNLOCK_SEND}&mailingID=${tmpMailingID}">
															<i class="icon icon-check"></i>
															<span class="text"><bean:message key="default.unlock" /></span>
														</html:link>
													</div>
												</div>
											</c:when>
											<c:otherwise>
												<div class="form-group">
													<div class="col-sm-8 col-sm-4-push">
														<div class="well">
															<bean:message key="mailing.activate.deleted_target_groups" />
														</div>
													</div>
												</div>
											</c:otherwise>
										</c:choose>
									</c:if>
								</emm:ShowByPermission>
							</logic:equal>

							<c:if test="${(mailingSendForm.worldMailingSend && not mailingSendForm.isTemplate) || mailingSendForm.mailingtype eq TYPE_ACTIONBASED || mailingSendForm.mailingtype eq TYPE_DATEBASED}">
								<agn:agnForm action="/mailingsend" id="statusMailRecipientsForm" data-form="resource">
									<html:hidden property="action" value="${ACTION_SAVE_STATUSMAIL_RECIPIENTS}" />
									<html:hidden property="mailingID" value="${mailingSendForm.mailingID}" />
									<html:hidden property="statusmailRecipients" styleId="statusmailRecipientsID" />

									<div class="form-group">
										<div class="col-sm-4">
											<label class="control-label">
												<i class="icon icon-help" data-tooltip="<bean:message key='mailing.SendStatusEmail'/>"></i>
											</label>
										</div>
										<div class="col-sm-8">
											<div class="table-responsive">
												<table class="table table-bordered table-striped">
													<thead>
														<tr>
															<th><bean:message key="statusmailRecipients" /></th>
															<th></th>
														</tr>
													</thead>
													<tbody id="statusEmailContainer">
														<tr>
															<td><input type="text" id="newStatusMail" class="form-control" data-action="recipients-row-field" />
															</td>
															<td class="table-actions">
																<a class="btn btn-regular btn-primary" href="#" data-tooltip="<bean:message key='button.Add' />" data-action="recipients-row-add"><i class="icon icon-plus"></i></a>
															</td>
														</tr>
													</tbody>
												</table>
											</div>
										</div>
									</div>
								</agn:agnForm>

								<div class="form-group">
									<div class="col-sm-4">
										<label class="control-label" for="sendStatusOnErrorOnly-toggle">
											<bean:message key="mailing.SendStatusOnErrorOnly" />
											<button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/SendStatusOnErrorOnly.xml"></button>
										</label>
									</div>
									<div class="col-sm-8">
										<c:url var="switchSendStatusOnErrorOnlyUrl" value="/mailingsend.do">
											<c:param name="action" value="${ACTION_SAVE_STATUSMAIL_ONERRORONLY}" />
											<c:param name="mailingId" value="${mailingSendForm.mailingID}" />
										</c:url>

										<label class="toggle">
											<input type="checkbox"
													data-action="sendStatusOnErrorOnly-toggle"
													data-url="${switchSendStatusOnErrorOnlyUrl}"
													${mailingSendForm.statusmailOnErrorOnly ? "checked" : ""}>
											<div class="toggle-control"></div>
										</label>
									</div>
								</div>
							</c:if>
						</div>
						<!-- Tile Content END -->
					</div>
					<!-- Tile END -->
				</logic:equal>
			</div>
			<!-- Col END -->
			<c:if test="${!mailingSendForm.isTemplate}">
				<c:if test="${mailingSendForm.mailingtype == TYPE_NORMAL || mailingSendForm.mailingtype == TYPE_FOLLOWUP}">
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
	</tiles:put>
</tiles:insert>
<!-- Row END -->

<script id="recipients-row" type="text/x-mustache-template">
	<tr class="js-recipients-row">
		<td>{{= email }}</td>
		<td class="table-actions">
			<input type="hidden" name="{{= _.uniqueId('statusmailRecipient_') }}" value="{{= email }}" />
			<a href="#" class="btn btn-regular btn-alert" data-action="recipients-row-remove" data-form-target="#statusMailRecipientsForm" data-from-submit>
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
