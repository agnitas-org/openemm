<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic"%>
<%@ page import="org.agnitas.beans.Mailing"%>
<%@ page import="org.agnitas.util.AgnUtils"%>
<%@ page import="org.agnitas.web.MailingSendAction"%>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN%>" />

<%--@elvariable id="admin" type="com.agnitas.beans.ComAdmin"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>

<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}" />

<c:set var="WORKFLOW_FORWARD_PARAMS" value="<%= WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS %>" scope="page"/>
<c:set var="ACTION_CONFIRM_SEND_WORLD" value="<%= ComMailingSendActionBasic.ACTION_CONFIRM_SEND_WORLD %>" scope="page" />
<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>" scope="page" />

<c:set var="editWithCampaignManagerMessage" scope="page"><bean:message key="mailing.EditWithCampaignManager" /></c:set>

<c:set var="aZone" value="${admin.adminTimezone}" />
<c:set var="adminLocale" value="${admin.locale}" />
<c:set var="tmpMailingID" value="${mailingSendForm.mailingID}" />

<c:choose>
	<c:when test="${adminLocale eq 'en_US'}">
		<fmt:setLocale value="${adminLocale}"/>
	</c:when>

	<c:otherwise>
		<fmt:setLocale value="de_DE"/>
	</c:otherwise>
</c:choose>


<%--<fmt:parseDate value="2015-03-31 15:03" type="date" pattern="yyyy-MM-dd HH:mm" var="now"/>--%>
<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate value="${now}" pattern="dd.MM.yyyy" timeZone="${aZone}" var="currentDate" />
<fmt:formatDate value="${now}" pattern="HH" timeZone="${aZone}" var="currentHour" />
<fmt:formatDate value="${now}" pattern="mm" timeZone="${aZone}" var="currentMinutes" />

<agn:agnForm action="/mailingsend" data-form="resource">
	<html:hidden property="action" />
	<html:hidden property="mailingID" />
	<html:hidden property="sendStatText" />
	<html:hidden property="sendStatHtml" />
	<html:hidden property="sendStatOffline" />
	<html:hidden property="step" value="60" />

	<tiles:insert page="template.jsp">
		<c:if test="${mailingSendForm.isMailingGrid}">
			<tiles:put name="header" type="string">
				<ul class="tile-header-nav">
					<%--<div class="headline">
					<i class="icon icon-th-list"></i>
					</div>--%>

					<!-- Tabs BEGIN -->
					<tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false" />
					<!-- Tabs END -->
				</ul>
			</tiles:put>
		</c:if>

		<tiles:put name="content" type="string">
			<c:choose>
				<c:when test="${mailingSendForm.isMailingGrid}">
					<div class="tile-content-forms col-sm-12">
				</c:when>
				<c:otherwise>
					<div class="tile" data-sizing="container">
						<div class="tile-header">
							<h2 class="headline">
								<bean:message key="MailingSend" />
							</h2>
						</div>
						<div class="tile-content tile-content-forms col-sm-12">
				</c:otherwise>
			</c:choose>
			<div class="col-sm-12">
				<div class="row">
					<div class="well block vspace-bottom-20" style="border: none;">
						<bean:message key="mailing.MailingSendXplain" />
					</div>
				</div>
			<div class="col-sm-6">
				<div class="tile">
					<div class="tile-header">
						<h2 class="headline">
							<bean:message key="mailing.schedule" />
						</h2>
					</div>
					<div class="tile-content tile-content-forms">
							<div class="row">
								<div class="col-sm-12">
										<c:if test="${empty mailingSendForm.followupFor}">
											<div style="border: 1px solid #cccdcd; margin-bottom: 20px;">
												<div class="panel-body" style="border-bottom: 1px solid #cccdcd;">
													<h2 class="headline" style="text-transform: capitalize;">
														<bean:message key="target.Selected" />
													</h2>
												</div>
												<ul class="list-group" style="margin: 0;">
													<c:if test="${empty mailingSendForm.targetGroups}">
														<li class="list-group-item" style="border: none;">
															<bean:message key="statistic.all_subscribers" />
														</li>
													</c:if>
													<c:if test="${not empty mailingSendForm.targetGroups}">
														<c:forEach var="target" items="${targetGroupNames}">
																<li class="list-group-item" style="border: none;">
																	${target}
																</li>
														</c:forEach>
													</c:if>
												</ul>
											</div>
											
											<div style="border: 1px solid #cccdcd; margin-bottom: 20px;">
												<div class="panel-body" style="border-bottom: 1px solid #cccdcd;">
													<h2 class="headline">
														<bean:message key="mailing.recipients" />
													</h2>
												</div>
												<ul class="list-group" style="margin: 0;">
													<li class="list-group-item" style="border: none;">
														<p>
															<span style="font-weight: 700;"><fmt:formatNumber value="${mailingSendForm.sendStatText}" groupingUsed="true"/>															</span>
															<bean:message key="mailing.send.emailsNum.text" />
														</p>
													</li>
													<li class="list-group-item" style="border: none;">
														<p>
															<span style="font-weight: 700;"><fmt:formatNumber value="${mailingSendForm.sendStatHtml}" groupingUsed="true"/>															</span>
															<bean:message key="mailing.send.emailsNum.html" />
														</p>
													</li>
													<li class="list-group-item" style="border: none;">
														<p>
															<span style="font-weight: 700;"><fmt:formatNumber value="${mailingSendForm.sendStatOffline}" groupingUsed="true"/>															</span>
															<bean:message key="mailing.send.emailsNum.offileHtml" />
														</p>
													</li>
													<li class="list-group-item" style="border: none; border-top: 1px solid #cccdcd;">
														<p>
															<span style="font-weight: 700;"><fmt:formatNumber value="${mailingSendForm.sendTotal}" groupingUsed="true"/>															</span>
															<bean:message key="Recipients" />
														</p>
													</li>
												</ul>
											</div>
										</c:if>
	
										<c:if test="${not empty mailingSendForm.followupFor}">
											<div style="border: 1px solid #cccdcd; margin-bottom: 20px;">
												<div class="panel-body" style="border-bottom: 1px solid #cccdcd;">
													<h2 class="headline">
														<bean:message key="mailing.recipients" />
													</h2>
												</div>
												<ul>
													<li class="list-group-item" style="border: none;">
														<p>
															<bean:message key="RecipientsFollowupXplain1" />
															<strong>
																<bean:write name="mailingSendForm" property="sendTotal" scope="request" />
															</strong>
															<bean:message key="RecipientsFollowupXplain2" />
														</p>
													</li>
													<li class="list-group-item" style="border: none;">
														<p>
															<bean:message key="mailing.RecipientsRecieved" />
															<strong>
															<logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_NON_OPENER%>">
																<bean:message key="noneOpeners" />.
															</logic:equal>
															<logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_NON_CLICKER%>">
																<bean:message key="noneClickers" />
															</logic:equal>
															<logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_OPENER%>">
																<bean:message key="openers" />.
															</logic:equal>
															<logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_CLICKER%>">
																<bean:message key="clickers" />.
															</logic:equal>
															</strong>
														</p>
													</li>
												</ul>
											</div>
										</c:if>
	
										<ul class="list-group">
											<c:forEach var="sendStatKey" items="${mailingSendForm.sendStats.keySet()}">
												<c:if test="${sendStatKey gt 0}">
													<c:set var="sendStat" value="${mailingSendForm.getSendStat(sendStatKey)}" />
													<c:if test="${sendStat gt 0}">
														<li class="list-group-item">
															<span class="badge">
																${sendStat}
															</span>
															<bean:message key="mailing.MediaType.${sendStatKey}" />
														</li>
													</c:if>
												</c:if>
											</c:forEach>
										</ul>
								</div>
							</div>
	
							<div class="form-group">
								<div class="col-sm-2" style="padding-top: 10px;">
									<label class="control-label" for="sendDate"> 
										<bean:message key="Date" />
									</label>
								</div>
	
								<div class="col-sm-6" style="padding-top: 10px;">
									<div class="input-group">
										<div class="input-group-controls">
											<input name="sendDate" id="fullDate" value="${currentDate}" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: 'dd.mm.yyyy', formatSubmit: 'yyyymmdd', min: true" />
										</div>
										<div class="input-group-btn">
											<button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
												<i class="icon icon-calendar-o"></i>
											</button>
										</div>
										<c:if test="${mailingSendForm.workflowId ne 0}">
											<div class="input-group-btn">
												<button type="button" class="btn btn-regular" tabindex="-1" data-help="help_${helplanguage}/mailing/view_base/WorkflowEditorMsg.xml">
													<i class="icon icon-help"></i>
												</button>
												<c:url var="workflowManagerUrl" value="/workflow/${mailingSendForm.workflowId}/view.action">
													<c:param name="forwardParams" value="${sessionScope[WORKFLOW_FORWARD_PARAMS]};elementValue=${mailingSendForm.mailingID}" />
												</c:url>
												<a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
													<i class="icon icon-linkage-campaignmanager"></i>
													<strong><bean:message key="campaign.manager.icon" /></strong>
												</a>
											</div>
										</c:if>
									</div>
								</div>
								
								<div class="col-sm-4" style="padding-top: 10px;">
									<div class="input-group" data-field="split">
										<div class="input-group-controls">
											<input type="text" name="sendTime" value="${currentHour}:${currentMinutes}" data-value="${currentHour}:${currentMinutes}" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" data-field-split="sendHour, sendMinute" data-field-split-rule=":">
										</div>
										<div class="input-group-addon">
											<span class="addon"> 
												<i class="icon icon-clock-o"></i>
											</span>
										</div>
										<c:if test="${mailingSendForm.workflowId ne 0}">
											<div class="input-group-btn">
												<c:url var="workflowManagerUrl" value="/workflow/${mailingSendForm.workflowId}/view.action">
													<c:param name="forwardParams" value="${sessionScope[WORKFLOW_FORWARD_PARAMS]};elementValue=${mailingSendForm.mailingID}"/>
												</c:url>
												<a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
													<i class="icon icon-linkage-campaignmanager"></i>
													<strong><bean:message key="campaign.manager.icon"/></strong>
												</a>
											</div>
										</c:if>
									</div>
								</div>
							</div>
							<%@ include file="mailing-send2-optimized-mailing-generation-close.jspf"%>
							<div class="form-group">
								<div class="col-sm-2" style="padding-top: 10px;">
									<label class="control-label" for="sendDate"> 
										<bean:message key="birt.Timezone" />
									</label>
								</div>
								<div class="col-sm-10" style="padding-top: 10px;">
									<div class="input-group">
										<div class="input-group-controls">
											<input value="${aZone}" class="form-control" readonly="readonly"/>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="col-sm-6">
					<div class="tile">
						<div class="tile-header">
							<a href="#" class="headline" data-toggle-tile="#send-report-settings"> 
								<i class="tile-toggle icon icon-angle-down"></i> 
								<bean:message key="mailing.send.report" />
							</a>
						</div>
						<div class="tile-content tile-content-forms hidden" id="send-report-settings">
		
							<div class="form-group">
								<div style="border: 1px solid #cccdcd;">
										
									<div style="width: 100%; border-bottom: 1px solid #cccdcd;">
										<label style="width: 30%; padding: 5px 10px; margin-bottom: 0px; border-right: 1px solid #cccdcd;"> 
											<html:checkbox property="reportSendAfter24h" styleId="reportSendAfter24h" /> 
											<span style="line-height: 24px; vertical-align: top; font-weight: normal">
												<bean:message key="mailing.send.report.24h" />
											</span>
										</label>
										<label style="width: 30%; padding: 5px 10px; margin-bottom: 0px; border-right: 1px solid #cccdcd;"> 
											<html:checkbox property="reportSendAfter48h" styleId="reportSendAfter48h" /> 
											<span style="line-height: 24px; vertical-align: top; font-weight: normal">
												<bean:message key="mailing.send.report.48h" />
											</span>
										</label>
										<label style="width: 33%; padding: 5px 10px; margin-bottom: 0px;"> 
											<html:checkbox property="reportSendAfter1Week" styleId="reportSendAfter1Week" /> 
											<span style="line-height: 24px; vertical-align: top; font-weight: normal">
												<bean:message key="mailing.send.report.1week" />
											</span>
										</label>
									</div>
									
									<div style="width: 100%;">
										<div class="form-group vspace-bottom-0" style="padding: 5px 10px;">
											<div class="col-sm-4 control-label-left col-xs-5 col-md-3">
												<label class="control-label">
													<bean:message key="report.autosend.email" />
												</label>
											</div>
											<div class="col-sm-8 col-xs-7 col-md-9">
												<html:text styleId="report_email" styleClass="form-control" property="reportSendEmail" maxlength="199" />
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				
				<%@include file="mailing-send2-recipients.jspf"%>
						
					<div class="tile">
						<div class="tile-header">
							<a href="#" class="headline" data-toggle-tile="#mailing-option-settings"> 
								<i class="tile-toggle icon icon-angle-down"></i> 
								<bean:message key="mailing.option" />
							</a>
						</div>
						<div class="tile-content tile-content-forms" id="mailing-option-settings">
							<div class="form-group">
								<div style="border: 1px solid #cccdcd;">
		
										<ul>
											<li class="list-group-item" style="border: none;">
												<div class="checkbox">
													<label> 
														<input type="hidden" name="__STRUTS_CHECKBOX_doublechecking" value="0" /> 
														<html:checkbox property="doublechecking" /> 
														<bean:message key="doublechecking.email" />
													</label>
												</div>
											</li>
											<li class="list-group-item" style="border: none;">
												<div class="checkbox">
													<label> 
														<input type="hidden" name="__STRUTS_CHECKBOX_skipempty" value="0" /> 
														<html:checkbox property="skipempty" /> 
														<bean:message key="skipempty.email" />
													</label>
												</div>
											</li>
										</ul>
								</div>
							</div>
		
							<emm:ShowByPermission token="mailing.setmaxrecipients">
								<div class="form-group">
									<div class="col-sm-5">
										<label class="control-label"> 
										<label for="maxRecipients">
											<bean:message key="setMaxRecipients" />
										</label>
											<button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml">
											</button>
										</label>
									</div>
									<div class="col-sm-7">
										<agn:agnText property="maxRecipients" styleId="maxRecipients" styleClass="form-control js-inputmask" data-inputmask-options="mask: '9{1,20}'" value="0" />
									</div>
								</div>
							</emm:ShowByPermission>
		
							<%@ include file="mailing-send2-optimized-mailing-generation-open.jspf"%>
							
							<emm:ShowByPermission token="mailing.send.admin.options">
								<div class="form-group" id="blocksizeElement">
									<div class="col-sm-5">
										<label class="control-label" for="blocksize"> 
										<bean:message key="mailing.mailsperhour" />
										</label>
									</div>
									<div class="col-sm-7">
										<html:select property="blocksize" styleClass="form-control"
											styleId="blocksize">
											<html:option value="0">
												<bean:message key="mailing.unlimited" />
											</html:option>
											<html:option value="500000">500.000</html:option>
											<html:option value="250000">250.000</html:option>
											<html:option value="100000">100.000</html:option>
											<html:option value="50000">50.000</html:option>
											<html:option value="25000">25.000</html:option>
											<html:option value="10000">10.000</html:option>
											<html:option value="5000">5.000</html:option>
											<html:option value="1000">1.000</html:option>
										</html:select>
									</div>
								</div>
							</emm:ShowByPermission>
						</div>
					</div>
				</div>
			</div>
			
			<div class="col-sm-12">
				<div class="row">
					<div class="tile-footer" data-sizing="bottom">
						<div class="btn-group">
							<agn:agnLink class="btn btn-large pull-left" tabindex="-1"
										 page='/mailingsend.do?action=${ACTION_VIEW_SEND}&mailingID=${tmpMailingID}'>
								<i class="icon icon-reply"></i>
								<span class="text"><bean:message key="button.Cancel" /></span>
							</agn:agnLink>
		
							<c:if test="${mailingSendForm.workflowId eq 0}">
								<button type="button" class="btn btn-large btn-primary pull-right" data-form-set="send: send" data-form-confirm="${ACTION_CONFIRM_SEND_WORLD}">
									<i class="icon icon-send-o"></i> <span class="text"><bean:message key="button.Send" /></span>
								</button>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</tiles:put>
	</tiles:insert>
</agn:agnForm>
