<%@page import="org.agnitas.dao.FollowUpType"%>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ page import="com.agnitas.web.ComMailingSendActionBasic"%>
<%@ page import="com.agnitas.beans.Mailing"%>
<%@ page import="org.agnitas.util.AgnUtils"%>
<%@ page import="org.agnitas.web.MailingSendAction"%>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN%>" />

<%--@elvariable id="admin" type="com.agnitas.beans.ComAdmin"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="autoImports" type="java.util.List<org.agnitas.emm.core.autoimport.bean.AutoImportLight>"--%>

<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}" />

<c:set var="ACTION_CONFIRM_SEND_WORLD" value="<%= ComMailingSendActionBasic.ACTION_CONFIRM_SEND_WORLD %>" scope="page" />
<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>" scope="page" />

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, mailingSendForm.workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>
<c:set var="editWithCampaignManagerMessage" scope="page"><bean:message key="mailing.EditWithCampaignManager" /></c:set>

<c:set var="aZone" value="${admin.adminTimezone}" />
<c:set var="adminLocale" value="${admin.locale}" />
<c:set var="isFullscreenTileSizingDisabled" value="true" scope="request"/>
<c:set var="tmpMailingID" value="${mailingSendForm.mailingID}" />

<c:set var="aLocale" value="${admin.getLocale()}" />

<%--<fmt:parseDate value="2015-03-31 15:03" type="date" pattern="yyyy-MM-dd HH:mm" var="now"/>--%>

<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate value="${now}" pattern="${adminDateFormat}" timeZone="${aZone}" var="currentDate" />
<fmt:formatDate value="${now}" pattern="HH" timeZone="${aZone}" var="currentHour" />
<fmt:formatDate value="${now}" pattern="mm" timeZone="${aZone}" var="currentMinutes" />

<agn:agnForm action="/mailingsend" data-form="resource" data-controller="mailing-send">
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
					<!-- Tabs BEGIN -->
					<tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false" />
					<!-- Tabs END -->
				</ul>
			</tiles:put>
		</c:if>

	<tiles:put name="content" type="string">
	<c:if test="${not mailingSendForm.isMailingGrid}">
		<div class="tile-header" style="padding-bottom: 15px; height: auto;">
			<h2 class="headline">
				<bean:message key="MailingSend" />
			</h2>
		</div>
	</c:if>
<div id="hidden" style="display: none;">${aLocale}</div>
	<div class="tile-content tile-content-forms">
		<div class="row">
			<div class="col-sm-12">
				<div class="col-sm-6">
					<div class="tile">
						<div class="tile-header" style="padding-bottom: 15px; height: auto;">
							<h2 class="headline">
								<bean:message key="mailing.schedule" />
							</h2>
						</div>
						<div class="tile-content tile-content-forms" style="padding: 15px 15px 30px;">
							<div class="row">
								<div class="col-sm-12">
										<c:if test="${empty mailingSendForm.followupFor}">
											<div class="col-sm-6" style="padding-right: 0;">
												<div style="margin-bottom: 20px;">
													<div class="panel-body" style="padding: 8px;">
														<h3 class="headline" style="color: #707173;">
															<bean:message key="Recipients" />
														</h3>
													</div>
													<ul class="list-group" style="margin: 0;">
														<li class="list-group-item" style="border: none;">
															<p class="commaNumber">
																<span class="commaNumber" style="font-weight: 700;"><bean:write name="mailingSendForm" property="sendStatText" scope="request" /></span>
																<bean:message key="mailing.send.emailsNum.text" />
															</p>
														</li>
														<li class="list-group-item" style="border: none;">
															<p class="commaNumber">
																<span class="commaNumber" style="font-weight: 700;"><bean:write name="mailingSendForm" property="sendStatHtml" scope="request" /></span>
																<bean:message key="mailing.send.emailsNum.html" />
															</p>
														</li>
														<li class="list-group-item" style="border: none;">
															<p class="commaNumber">
																<span class="commaNumber" style="font-weight: 700;"><bean:write name="mailingSendForm" property="sendStatOffline" scope="request" /></span>
																<bean:message key="mailing.send.emailsNum.offileHtml" />
															</p>
														</li>
														<c:forEach var="sendStatKey" items="${mailingSendForm.sendStats.keySet()}">
															<c:if test="${sendStatKey gt 0}">
																<c:set var="sendStat" value="${mailingSendForm.getSendStat(sendStatKey)}" />
																<c:if test="${sendStat gt 0}">
																	<li class="list-group-item" style="border: none;">
																		<p class="commaNumber">	
																			<span class="commaNumber" style="font-weight: 700;">${sendStat}</span>
																			<bean:message key="mailing.MediaType.${sendStatKey}" />
																		</p>
																	</li>
																</c:if>
															</c:if>
														</c:forEach>
														<li class="list-group-item" style="border: none; border-top: 1px solid #cccdcd;">
															<p class="commaNumber">	
																<span class="commaNumber" style="font-weight: 700;"><bean:write name="mailingSendForm" property="sendTotal" scope="request" /></span>
																<bean:message key="Recipients" /><span style="text-transform: lowercase;"> <bean:message key="report.total" /></span>
															</p>
														</li>
													</ul>
												</div>
											</div>
											<div class="col-sm-1">
												&nbsp;
											</div>
											<div class="col-sm-5" style="padding-left: 0;">
												<div style="margin-bottom: 20px;">
													<div class="panel-body" style="padding: 8px;">
														<h3 class="headline" style="color: #707173; text-transform: capitalize;">
															<bean:message key="Target-Groups" />
														</h3>
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
											</div>
										</c:if>
	
										<c:if test="${not empty mailingSendForm.followupFor}">
											<div class="col-sm-12">
												<div style="margin-bottom: 20px;">
													<div class="panel-body" style="padding: 8px;">
														<h3 class="headline" style="color: #707173;">
															<bean:message key="Recipients" />
														</h3>
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
																<logic:equal name="mailingSendForm" property="followUpType" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey() %>">
																	<bean:message key="noneOpeners" />.
																</logic:equal>
																<logic:equal name="mailingSendForm" property="followUpType" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey() %>">
																	<bean:message key="noneClickers" />
																</logic:equal>
																<logic:equal name="mailingSendForm" property="followUpType" value="<%= FollowUpType.TYPE_FOLLOWUP_OPENER.getKey() %>">
																	<bean:message key="openers" />.
																</logic:equal>
																<logic:equal name="mailingSendForm" property="followUpType" value="<%= FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey() %>">
																	<bean:message key="clickers" />.
																</logic:equal>
																</strong>
															</p>
														</li>
													</ul>
												</div>
											</div>
										</c:if>
									</div>
								</div>

								<div class="form-group" style="margin: 0px;">
									<div class="col-sm-7 control-label-left" style="margin-bottom: 0; padding-top: 10px;">
										<label class="control-label" for="sendDate"> 
											<bean:message key="Date" />
										</label>
										<div class="input-group">
											<div class="input-group-controls">
												<input name="sendDate" id="fullDate" value="${currentDate}" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: 'yyyymmdd', min: true" />
											</div>
											<div class="input-group-btn">
												<button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
													<i class="icon icon-calendar-o"></i>
												</button>
												<c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
													<c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingSendForm.mailingID}" />
												</c:url>
											</div>
											<c:if test="${workflowId gt 0}">
												<div class="input-group-btn">
													<button type="button" class="btn btn-regular" tabindex="-1" data-help="help_${helplanguage}/mailing/view_base/WorkflowEditorMsg.xml">
														<i class="icon icon-help"></i>
													</button>
													<c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
														<c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingSendForm.mailingID}" />
													</c:url>
													<a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
														<i class="icon icon-linkage-campaignmanager"></i>
														<strong><bean:message key="campaign.manager.icon" /></strong>
													</a>
												</div>
											</c:if>
										</div>
									</div>
									
									<div class="col-sm-5 control-label-left" style="margin-bottom: 0; padding-top: 10px;">
										<label class="control-label" for="sendTime"> 
											<bean:message key="default.Time" />
										</label>
										<div class="input-group" data-field="split">
											<div class="input-group-controls">
												<input type="text" name="sendTime" value="${currentHour}:${currentMinutes}" data-value="${currentHour}:${currentMinutes}" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" data-field-split="sendHour, sendMinute" data-field-split-rule=":">
											</div>
											<div class="input-group-addon">
												<span class="addon"> 
													<i class="icon icon-clock-o"></i>
												</span>
											</div>
											<c:if test="${workflowId gt 0}">
												<div class="input-group-btn">
													<c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
														<c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${mailingSendForm.mailingID}" />
													</c:url>
													<a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
														<i class="icon icon-linkage-campaignmanager"></i>
														<strong><bean:message key="campaign.manager.icon" /></strong>
													</a>
												</div>
											</c:if>
										</div>
									</div>
								</div>
								<div class="form-group" style="margin: 15px 0 0 0;">
									<%@ include file="mailing-send2-optimized-mailing-generation-close.jspf"%>
									<div class="col-sm-12" style="padding-top: 10px;">
										<div class="form-group" style="margin-bottom: 0;">
											<div class="col-sm-12 control-label-left">
												<label class="control-label" for="timeZone"> 
													<bean:message key="birt.Timezone" />
												</label>
											</div>
											<div class="col-sm-12">
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
						</div>
					</div>
					<div class="col-sm-6">
						<div class="tile">
							<div class="tile-header" style="padding-bottom: 15px; height: auto;">
								<a href="#" class="headline" data-toggle-tile="#send-report-settings"> 
									<i class="tile-toggle icon icon-angle-down"></i> 
									<bean:message key="mailing.send.report" />
								</a>
							</div>
							<div class="tile-content tile-content-forms hidden" id="send-report-settings" style="padding: 15px;">
			
								<div class="form-group">
									<div style="margin: 0 7px;">
											
										<div style="width: 100%;">
											<label style="padding: 5px 0; margin-bottom: 0px;"> 
												<html:checkbox property="reportSendAfter24h" styleId="reportSendAfter24h" /> 
												<span style="line-height: 24px; vertical-align: top; font-weight: normal">
													<bean:message key="mailing.send.report.24h" />
												</span>
											</label>
										</div>
										<div style="width: 100%;">
											<label style="padding: 0 0 5px; margin-bottom: 0px;"> 
												<html:checkbox property="reportSendAfter48h" styleId="reportSendAfter48h" /> 
												<span style="line-height: 24px; vertical-align: top; font-weight: normal">
													<bean:message key="mailing.send.report.48h" />
												</span>
											</label>
										</div>
										<div style="width: 100%;">
											<label style="padding: 0 0 5px; margin-bottom: 0px;">
												<html:checkbox property="reportSendAfter1Week" styleId="reportSendAfter1Week" /> 
												<span style="line-height: 24px; vertical-align: top; font-weight: normal">
													<bean:message key="mailing.send.report.1week" />
												</span>
											</label>
										</div>
										
										<div style="width: 100%;">
											<div class="form-group vspace-bottom-0" style="padding: 5px 0;">
												<div class="col-sm-12 control-label-left">
													<label class="control-label">
														<bean:message key="report.autosend.email" />
													</label>
												</div>
												<div class="col-sm-12">
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
							<div class="tile-header" style="padding-bottom: 15px; height: auto;">
								<a href="#" class="headline" data-toggle-tile="#mailing-option-settings">
									<i class="tile-toggle icon icon-angle-down"></i>
									<bean:message key="mailing.option" />
								</a>
							</div>
							<div class="tile-content tile-content-forms" id="mailing-option-settings" style="padding: 15px;">
								<div class="form-group">
									<div style="margin: 0">
			
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
                                                <c:if test="${isMailtrackExtended}">
                                                    <li class="list-group-item" style="border: none;">
                                                        <div class="checkbox">
                                                            <label>
                                                                <input type="hidden" name="__STRUTS_CHECKBOX_skipempty" value="0" />
                                                                <html:checkbox property="skipempty" />
                                                                <bean:message key="skipempty.email" />
                                                            </label>
                                                        </div>
                                                    </li>
                                                </c:if>
											</ul>
									</div>
								</div>

								<emm:ShowByPermission token="recipient.import.auto.mailing">
									<div class="form-group">
										<div class="col-sm-12 control-label-left">
											<label class="control-label">
												<label for="required-auto-import" style="margin-bottom: 0;">
													<bean:message key="autoImport.autoImport" />
												</label>
											</label>
										</div>
										<div class="col-sm-12">
											<html:select property="autoImportId" styleClass="form-control js-select" styleId="required-auto-impor">
												<html:option value="0">---</html:option>
												<c:forEach items="${autoImports}" var="autoImport">
													<html:option value="${autoImport.autoImportId}">${autoImport.shortname}</html:option>
												</c:forEach>
											</html:select>
										</div>
									</div>
								</emm:ShowByPermission>

								<emm:ShowByPermission token="mailing.setmaxrecipients">
									<div class="form-group">
										<div class="col-sm-12 control-label-left">
											<label class="control-label"> 
												<label for="maxRecipients" style="margin-bottom: 0px;">
													<bean:message key="setMaxRecipients" />
												</label>
												<button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml">
												</button>
											</label>
										</div>
										<div class="col-sm-12">
											<agn:agnText property="maxRecipients" styleId="maxRecipients" styleClass="form-control js-inputmask" data-inputmask-options="mask: '9{1,20}'" placeholder="" />
										</div>
									</div>
								</emm:ShowByPermission>
			
								<%@ include file="mailing-send2-optimized-mailing-generation-open.jspf"%>
								
								<emm:ShowByPermission token="mailing.send.admin.options">
									<div class="form-group" id="blocksizeElement">
										<div class="col-sm-12 control-label-left">
											<label class="control-label" for="blocksize"> 
											<bean:message key="mailing.mailsperhour" />
											</label>
										</div>
										<div class="col-sm-12">
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
				
				<div class="col-sm-12" onload="decimalSeperator()">
					<div class="row">
						<div class="tile-footer" data-sizing="bottom" style="padding: 10px 0; margin: 0 7px;">
							<div class="btn-group">
								<agn:agnLink class="btn btn-large pull-left" tabindex="-1" page='/mailingsend.do?action=${ACTION_VIEW_SEND}&mailingID=${tmpMailingID}'>
									<i class="icon icon-reply"></i>
									<span class="text"><bean:message key="button.Cancel" /></span>
								</agn:agnLink>

								<c:if test="${not isWorkflowDriven}">
									<button id="sendForm" type="button" class="btn btn-large btn-primary pull-right" data-form-set="send: send" data-form-confirm="${ACTION_CONFIRM_SEND_WORLD}">

										<i class="icon icon-send-o"></i> <span class="text"><bean:message key="button.Send" /></span>
									</button>
								</c:if>
							</div>
						</div>
					</div>
				</div>
				
			</div>
		</div>
		<script>
		$(document).ready(function(){
			var maxRecipientVal = $("#maxRecipients").val();
			$("#maxRecipients").val('');
			$("#maxRecipients").attr('placeholder', '<bean:message key="mailing.unlimited" />');
			
	    $("#maxRecipients").focusout(function(){
	    	if($(this).val() === '0') {
	        $("#maxRecipients").val('');
	    	}
	    });
	    
	    $("#sendForm").mousedown(function(){
	    	if($("#maxRecipients").val() === "") {
	    		$("#maxRecipients").val('0');
	    	}
	    });
		});
		</script>
		<script>
		var commaNumber = document.getElementsByClassName("commaNumber");
		var decimalSeperator = ".";
		
		for (var i = 0; i < commaNumber.length; i++) {
			
			if(document.getElementById("hidden").innerHTML == "en_US") {
				decimalSeperator = ",";
			} else {
				decimalSeperator = ".";
			}
			commaNumber[i].innerHTML = commaNumber[i].innerHTML.replace(/\B(?=(\d{3})+(?!\d))/g, decimalSeperator);
		}
		</script>
		</tiles:put>
	</tiles:insert>
</agn:agnForm>
