<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.web.ComWorkflowAction" %>
<%@ page import="com.agnitas.web.ComMailingSendAction" %>
<%@ page import="org.agnitas.beans.Mailing" %>
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

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>"/>

<%--@elvariable id="admin" type="com.agnitas.beans.ComAdmin"--%>
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>

<c:set var="WORKFLOW_FORWARD_PARAMS" value="<%= ComWorkflowAction.WORKFLOW_FORWARD_PARAMS %>" scope="page"/>
<c:set var="ACTION_CONFIRM_SEND_WORLD" value="<%= ComMailingSendAction.ACTION_CONFIRM_SEND_WORLD %>" scope="page"/>
<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>" scope="page"/>

<c:set var="editWithCampaignManagerMessage" scope="page"><bean:message key='mailing.EditWithCampaignManager'/></c:set>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>

<c:set var="aZone" value="${admin.adminTimezone}" />

<c:set var="tmpMailingID" value="${mailingSendForm.mailingID}" />

<%--<fmt:parseDate value="2015-03-31 15:03" type="date" pattern="yyyy-MM-dd HH:mm" var="now"/>--%>
<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate value="${now}" pattern="dd.MM.yyyy" timeZone="${aZone}" var="currentDate" />
<fmt:formatDate value="${now}" pattern="HH" timeZone="${aZone}" var="currentHour" />
<fmt:formatDate value="${now}" pattern="mm" timeZone="${aZone}" var="currentMinutes" />

<agn:agnForm action="/mailingsend" data-form="resource">
    <html:hidden property="action"/>
    <html:hidden property="mailingID"/>
    <html:hidden property="sendStatText"/>
    <html:hidden property="sendStatHtml"/>
    <html:hidden property="sendStatOffline"/>
    <html:hidden property="step" value="60"/>

    <c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid}" scope="request"/>

    <tiles:insert page="template.jsp">
        <c:if test="${mailingSendForm.isMailingGrid}">
            <tiles:put name="header" type="string">
                <ul class="tile-header-nav">
                        <%--<div class="headline">
                            <i class="icon icon-th-list"></i>
                        </div>--%>

                    <!-- Tabs BEGIN -->
                    <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    <!-- Tabs END -->
                </ul>
            </tiles:put>
        </c:if>

        <tiles:putList name="footerItems">
            <tiles:add>
                <agn:agnLink class="btn btn-large pull-left" tabindex="-1"
                             page='/mailingsend.do?action=${ACTION_VIEW_SEND}&mailingID=${tmpMailingID}'>
                    <i class="icon icon-reply"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </agn:agnLink>
            </tiles:add>

            <c:if test="${mailingSendForm.workflowId eq 0}">
                <tiles:add>
                        <button type="button" class="btn btn-large btn-primary pull-right" data-form-set="send: send" data-form-confirm="${ACTION_CONFIRM_SEND_WORLD}">
                            <i class="icon icon-send-o"></i>
                            <span class="text"><bean:message key="button.Send"/></span>
                        </button>
                </tiles:add>
            </c:if>
        </tiles:putList>

        <tiles:put name="content" type="string">
            <c:choose>
                <c:when test="${mailingSendForm.isMailingGrid}">
            <div class="tile-content-forms">
                </c:when>
                <c:otherwise>
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline"><bean:message key="MailingSend"/></h2>
                </div>
                <div class="tile-content tile-content-forms">
                </c:otherwise>
            </c:choose>

                    <div class="well block vspace-bottom-20">
                        <bean:message key="mailing.MailingSendXplain"/>
                    </div>

                    <div class="row">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="recipient.RecipientSelection"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="panel panel-default">
                                <c:if test="${empty mailingSendForm.followupFor}">
                                    <div class="panel-body">
                                        <p>
                                            <bean:message key="mailing.RecipientsXplain1"/>
                                            <strong>
                                                <c:if test="${empty mailingSendForm.targetGroups}">
                                                    <bean:message key="statistic.all_subscribers"/>
                                                </c:if>
                                                <c:if test="${not empty mailingSendForm.targetGroups}">
                                                    <c:set var="isFirst" value="${true}"/>
                                                    <c:forEach var="target" items="${targetGroupNames}">
                                                        <c:if test="${isFirst}">
                                                            <c:set var="isFirst" value="${false}"/>
                                                        </c:if>
                                                        <c:if test="${not isFirst}">
                                                            /&nbsp;
                                                        </c:if>
                                                        ${target}
                                                    </c:forEach>
                                                </c:if>
                                            </strong>
                                            <bean:message key="RecipientsXplain2"/>

                                            <bean:write name="mailingSendForm" property="sendTotal" scope="request"/>
                                            <bean:message key="mailing.RecipientsXplain3"/>
                                        </p>
                                    </div>

                                    <ul class="list-group">
                                        <li class="list-group-item">
                                            <span class="badge"><bean:write name="mailingSendForm" property="sendStatText" scope="request"/></span>
                                            <bean:message key="mailing.send.emailsNum.text"/>
                                        </li>
                                        <li class="list-group-item">
                                            <span class="badge"><bean:write name="mailingSendForm" property="sendStatHtml" scope="request"/></span>
                                            <bean:message key="mailing.send.emailsNum.html"/>
                                        </li>
                                        <li class="list-group-item">
                                            <span class="badge"><bean:write name="mailingSendForm" property="sendStatOffline" scope="request"/></span>
                                            <bean:message key="mailing.send.emailsNum.offileHtml"/>
                                        </li>
                                    </ul>
                                </c:if>

                                <c:if test="${not empty mailingSendForm.followupFor}">
                                    <div class="panel-body">
                                        <p><bean:message key="RecipientsFollowupXplain1"/></p>
                                        <p>
                                            <bean:write name="mailingSendForm" property="sendTotal" scope="request"/>&nbsp;
                                            <bean:message key="RecipientsFollowupXplain2"/>
                                        </p>

                                        <div class="well">
                                            <strong><bean:message key="mailing.RecipientsRecieved"/></strong>
                                            <logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_NON_OPENER%>">
                                                <bean:message key="noneOpeners"/>.
                                            </logic:equal>
                                            <logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_NON_CLICKER%>">
                                                <bean:message key="noneClickers"/>
                                            </logic:equal>
                                            <logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_OPENER%>">
                                                <bean:message key="openers"/>.
                                            </logic:equal>
                                            <logic:equal name="mailingSendForm" property="followUpType" value="<%=Mailing.TYPE_FOLLOWUP_CLICKER%>">
                                                <bean:message key="clickers"/>.
                                            </logic:equal>
                                        </div>
                                    </div>
                                </c:if>

                                <ul class="list-group">
                                    <c:forEach var="sendStatKey" items="${mailingSendForm.sendStats.keySet()}">
                                        <c:if test="${sendStatKey gt 0}">
                                            <c:set var="sendStat" value="${mailingSendForm.getSendStat(sendStatKey)}"/>
                                            <c:if test="${sendStat gt 0}">
                                                <li class="list-group-item">
                                                    <span class="badge">${sendStat}</span>
                                                    <bean:message key="mailing.MediaType.${sendStatKey}"/>
                                                </li>
                                            </c:if>
                                        </c:if>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="sendDate">
                                <bean:message key="Date"/>
                            </label>
                        </div>

                        <div class="col-sm-5">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <input name="sendDate" id="fullDate" value="${currentDate}" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: 'dd.mm.yyyy', formatSubmit: 'yyyymmdd', min: true"/>
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
                                        <%--todo: GWUA-4271: change after test sucessfully--%>
                                        <%--<c:url var="workflowManagerUrl" value="/workflow/${mailingSendForm.workflowId}/view.action">--%>
                                            <%--<c:param name="forwardParams" value="${sessionScope[WORKFLOW_FORWARD_PARAMS]};elementValue=${mailingSendForm.mailingID}"/>--%>
                                        <%--</c:url>--%>
                                        <%--<a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">--%>
                                            <%--<i class="icon icon-linkage-campaignmanager"></i>--%>
                                            <%--<strong><bean:message key="campaign.manager.icon"/></strong>--%>
                                        <%--</a>--%>

                                        <agn:agnLink page="/workflow.do?method=view&workflowId=${mailingSendForm.workflowId}&forwardParams=${sessionScope[WORKFLOW_FORWARD_PARAMS]};elementValue=${mailingSendForm.mailingID}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                            <i class="icon icon-linkage-campaignmanager"></i>
                                            <strong><bean:message key="campaign.manager.icon"/></strong>
                                        </agn:agnLink>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <div class="col-sm-3">
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
                                        <agn:agnLink page="/workflow.do?method=view&workflowId=${mailingSendForm.workflowId}&forwardParams=${sessionScope[WORKFLOW_FORWARD_PARAMS]};elementValue=${mailingSendForm.mailingID}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                            <i class="icon icon-linkage-campaignmanager"></i>
                                            <strong><bean:message key="campaign.manager.icon"/></strong>
                                        </agn:agnLink>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-offset-4 col-sm-8">
                        <p class="help-block"><bean:message key="birt.Timezone"/>: ${aZone}</p>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="mailing.send.report"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="list-group">
                                <div class="list-group-item checkbox">
                                    <label>
                                        <html:checkbox property="reportSendAfter24h" styleId="reportSendAfter24h"/>
                                        <bean:message key="mailing.send.report.24h"/>
                                    </label>
                                </div>
                                <div class="list-group-item checkbox">
                                    <label>
                                        <html:checkbox property="reportSendAfter48h" styleId="reportSendAfter48h"/>
                                        <bean:message key="mailing.send.report.48h"/>
                                    </label>
                                </div>
                                <div class="list-group-item checkbox">
                                    <label>
                                        <html:checkbox property="reportSendAfter1Week" styleId="reportSendAfter1Week"/>
                                        <bean:message key="mailing.send.report.1week"/>
                                    </label>
                                </div>
                                <div class="list-group-item">
                                    <div class="form-group vspace-bottom-0">
                                        <div class="col-sm-3 control-label-left col-xs-5 col-md-3">
                                            <label class="control-label"><bean:message key="report.autosend.email"/></label>
                                        </div>
                                        <div class="col-sm-9 col-xs-7 col-md-9">
                                            <html:text styleId="report_email" styleClass="form-control" property="reportSendEmail" maxlength="199"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

					<%@include file="mailing-send2-recipients.jspf" %>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="mailing.option"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="list-group">
                                <div class="list-group-item checkbox">
                                    <label>
                                        <input type="hidden" name="__STRUTS_CHECKBOX_doublechecking" value="0"/>
                                        <html:checkbox property="doublechecking"/>
                                        <bean:message key="doublechecking.email"/>
                                    </label>
                                </div>
                                <div class="list-group-item checkbox">
                                    <label>
                                        <input type="hidden" name="__STRUTS_CHECKBOX_skipempty" value="0"/>
                                        <html:checkbox property="skipempty"/>
                                        <bean:message key="skipempty.email"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <emm:ShowByPermission token="mailing.setmaxrecipients">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <label for="maxRecipients"><bean:message key="setMaxRecipients"/></label>
                                    <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml"></button>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <agn:agnText property="maxRecipients" styleId="maxRecipients" styleClass="form-control js-inputmask" data-inputmask-options="mask: '9{1,20}'" value="0"/>
                            </div>
                        </div>
                    </emm:ShowByPermission>

                    <%@ include file="mailing-send2-optimized-mailing-generation-open.jspf" %>
                    <emm:ShowByPermission token="mailing.send.admin.options">
	                     <div class="form-group" id="blocksizeElement">
	                         <div class="col-sm-4">
	                             <label class="control-label" for="blocksize">
	                                 <bean:message key="mailing.mailsperhour"/>
	                             </label>
	                         </div>
	                         <div class="col-sm-8">
	                             <html:select property="blocksize" styleClass="form-control" styleId="blocksize" >
	                                 <html:option value="0"><bean:message key="mailing.unlimited"/></html:option>
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
                    <%@ include file="mailing-send2-optimized-mailing-generation-close.jspf" %>
            <c:if test="${not mailingSendForm.isMailingGrid}">
                    <div class="form-group">
                        <div class="col-sm-offset-4 col-sm-8">
                            <div class="btn-group">
                                <agn:agnLink class="btn btn-large" tabindex="-1"
                                             page='/mailingsend.do?action=${ACTION_VIEW_SEND}&mailingID=${tmpMailingID}'>
                                    <i class="icon icon-reply"></i>
                                    <span class="text"><bean:message key="button.Cancel"/></span>
                                </agn:agnLink>

                                <c:if test="${mailingSendForm.workflowId eq 0}">
                                    <button type="button" class="btn btn-large btn-primary" data-form-set="send: send" data-form-confirm="${ACTION_CONFIRM_SEND_WORLD}">
                                        <i class="icon icon-send-o"></i>
                                        <span class="text"><bean:message key="button.Send"/></span>
                                    </button>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
            </div>
        </tiles:put>
    </tiles:insert>

</agn:agnForm>
