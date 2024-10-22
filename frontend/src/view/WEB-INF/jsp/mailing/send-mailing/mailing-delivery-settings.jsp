<%@page import="org.agnitas.util.AgnUtils"%>
<%@ page import="org.agnitas.dao.FollowUpType" %>
<%@ page import="com.agnitas.emm.core.components.service.MailingBlockSizeService" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="followUpType" type="java.lang.String"--%>
<%--@elvariable id="followupFor" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="adminDateFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="targetGroupNames" type="java.util.List<java.lang.String>"--%>
<%--@elvariable id="isMailtrackExtended" type="java.lang.Boolean"--%>
<%--@elvariable id="autoImports" type="java.util.List<org.agnitas.emm.core.autoimport.bean.AutoImportLight>"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, form.workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>
<c:set var="editWithCampaignManagerMessage" scope="page"><mvc:message code="mailing.EditWithCampaignManager" /></c:set>

<c:set var="adminZone" value="${admin.adminTimezone}" />
<c:set var="adminLocale" value="${admin.locale}" />
<c:set var="isFullscreenTileSizingDisabled" value="true" scope="request"/>

<c:set var="TYPE_FOLLOWUP_NON_OPENER" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey() %>"/>
<c:set var="TYPE_FOLLOWUP_NON_CLICKER" value="<%= FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey() %>"/>
<c:set var="TYPE_FOLLOWUP_OPENER" value="<%= FollowUpType.TYPE_FOLLOWUP_OPENER.getKey() %>"/>
<c:set var="TYPE_FOLLOWUP_CLICKER" value="<%= FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey() %>"/>

<c:set var="DEFAULT_STEPPING" value="<%= MailingBlockSizeService.DEFAULT_STEPPING %>"/>

<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate value="${now}" pattern="HH" timeZone="${adminZone}" var="currentHour" />
<fmt:formatDate value="${now}" pattern="mm" timeZone="${adminZone}" var="currentMinutes" />

<mvc:form servletRelativeAction="/mailing/send/confirm-send-world.action" modelAttribute="form"
          data-form="resource" data-controller="delivery-settings-view">

    <script type="application/json" data-initializer="delivery-settings-view">
        {
          "adminLocale":"${adminLocale}"
        }
    </script>

    <mvc:hidden path="mailingID" />
    <mvc:hidden path="textEmailsCount" />
    <mvc:hidden path="htmlEmailsCount" />
    <mvc:hidden path="offlineHtmlEmailsCount" />
    <mvc:hidden path="stepping" value="${DEFAULT_STEPPING}" />

    <tiles:insertTemplate template="/WEB-INF/jsp/mailing/template.jsp">
        <c:if test="${form.isMailingGrid}">
            <tiles:putAttribute name="header" type="string">
                <ul class="tile-header-nav">
                    <!-- Tabs BEGIN -->
                    <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false" />
                    <!-- Tabs END -->
                </ul>
            </tiles:putAttribute>
        </c:if>

        <tiles:putAttribute name="content" type="string">
            <c:if test="${not form.isMailingGrid}">
                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                    <h2 class="headline">
                        <mvc:message code="MailingSend" />
                    </h2>
                </div>
            </c:if>
            <div class="tile-content tile-content-forms">
                <div class="row">
                    <div class="col-sm-12">
                        <div class="col-sm-6">
                            <div class="tile">
                                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                    <h2 class="headline">
                                        <mvc:message code="mailing.schedule" />
                                    </h2>
                                </div>
                                <div class="tile-content tile-content-forms" style="padding: 15px 15px 30px;">
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <c:if test="${empty followupFor}">
                                                <div class="col-sm-6" style="padding-right: 0;">
                                                    <div style="margin-bottom: 20px;">
                                                        <div class="panel-body" style="padding: 8px;">
                                                            <h3 class="headline" style="color: #707173;">
                                                                <mvc:message code="Recipients" />
                                                            </h3>
                                                        </div>
                                                        <ul class="list-group" style="margin: 0;">
                                                            <li class="list-group-item" style="border: none;">
                                                                <p class="commaSplitLabel">
                                                                    <span class="commaSplitLabel" style="font-weight: 700;">${form.textEmailsCount}</span>
                                                                    <mvc:message code="mailing.send.emailsNum.text" />
                                                                </p>
                                                            </li>
                                                            <li class="list-group-item" style="border: none;">
                                                                <p class="commaSplitLabel">
                                                                    <span class="commaSplitLabel" style="font-weight: 700;">${form.htmlEmailsCount}</span>
                                                                    <mvc:message code="mailing.send.emailsNum.html" />
                                                                </p>
                                                            </li>
                                                            <li class="list-group-item" style="border: none;">
                                                                <p class="commaSplitLabel">
                                                                    <span class="commaSplitLabel" style="font-weight: 700;">${form.offlineHtmlEmailsCount}</span>
                                                                    <mvc:message code="mailing.send.emailsNum.offileHtml" />
                                                                </p>
                                                            </li>
                                                            <c:forEach var="sendStatKey" items="${form.sentStatistics.keySet()}">
                                                                <c:if test="${sendStatKey gt 0}">
                                                                    <c:set var="sendStat" value="${form.getSendStatisticsItem(sendStatKey)}" />
                                                                    <c:if test="${sendStat gt 0}">
                                                                        <li class="list-group-item" style="border: none;">
                                                                            <p class="commaSplitLabel">
                                                                                <span class="commaSplitLabel" style="font-weight: 700;">${sendStat}</span>
                                                                                <mvc:message code="mailing.MediaType.${sendStatKey}" />
                                                                            </p>
                                                                        </li>
                                                                    </c:if>
                                                                </c:if>
                                                            </c:forEach>
                                                            <li class="list-group-item" style="border: none; border-top: 1px solid #cccdcd;">
                                                                <p class="commaSplitLabel">
                                                                    <span class="commaSplitLabel" style="font-weight: 700;">${form.totalSentCount}</span>
                                                                    <mvc:message code="Recipients" /><span style="text-transform: lowercase;"> <mvc:message code="report.total" /></span>
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
                                                                <mvc:message code="Target-Groups" />
                                                            </h3>
                                                        </div>
                                                        <ul class="list-group" style="margin: 0;">
                                                            <c:choose>
                                                                <c:when test="${empty targetGroupNames}">
                                                                    <li class="list-group-item" style="border: none;">
                                                                        <mvc:message code="statistic.all_subscribers" />
                                                                    </li>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <c:forEach var="target" items="${targetGroupNames}">
                                                                        <li class="list-group-item" style="border: none;">
                                                                                ${target}
                                                                        </li>
                                                                    </c:forEach>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </c:if>

                                            <c:if test="${not empty followupFor}">
                                                <div class="col-sm-12">
                                                    <div style="margin-bottom: 20px;">
                                                        <div class="panel-body" style="padding: 8px;">
                                                            <h3 class="headline" style="color: #707173;">
                                                                <mvc:message code="Recipients" />
                                                            </h3>
                                                        </div>
                                                        <ul>
                                                            <li class="list-group-item" style="border: none;">
                                                                <p>
                                                                    <mvc:message code="RecipientsFollowupXplain1" />
                                                                    <strong>${form.totalSentCount}</strong>
                                                                    <mvc:message code="RecipientsFollowupXplain2" />
                                                                </p>
                                                            </li>
                                                            <li class="list-group-item" style="border: none;">
                                                                <p>
                                                                    <mvc:message code="mailing.RecipientsRecieved" />
                                                                    <strong>
                                                                        <c:if test="${followUpType eq TYPE_FOLLOWUP_NON_OPENER}">
                                                                            <mvc:message code="noneOpeners" />.
                                                                        </c:if>
                                                                        <c:if test="${followUpType eq TYPE_FOLLOWUP_NON_CLICKER}">
                                                                            <mvc:message code="noneClickers" />.
                                                                        </c:if>
                                                                        <c:if test="${followUpType eq TYPE_FOLLOWUP_OPENER}">
                                                                            <mvc:message code="openers" />.
                                                                        </c:if>
                                                                        <c:if test="${followUpType eq TYPE_FOLLOWUP_CLICKER}">
                                                                            <mvc:message code="clickers" />.
                                                                        </c:if>
                                                                    </strong>
                                                                </p>
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>

                                    <div class="form-group" style="margin: 0;">
                                        <div class="col-sm-7 control-label-left" style="margin-bottom: 0; padding-top: 10px;">
                                            <label class="control-label" for="fullDate">
                                                <mvc:message code="Date" />
                                            </label>

                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <input name="sendDate" id="fullDate" value="${sendDateStr}" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: 'yyyymmdd', min: true" />
                                                </div>
                                                <div class="input-group-btn">
                                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                        <i class="icon icon-calendar-o"></i>
                                                    </button>
                                                    <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                                        <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${form.mailingID}" />
                                                    </c:url>
                                                </div>
                                                <c:if test="${workflowId gt 0}">
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-regular" tabindex="-1" data-help="help_${helplanguage}/mailing/view_base/WorkflowEditorMsg.xml">
                                                            <i class="icon icon-help"></i>
                                                        </button>
                                                        <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                                            <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${form.mailingID}" />
                                                        </c:url>
                                                        <a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                                            <i class="icon icon-linkage-campaignmanager"></i>
                                                            <strong><mvc:message code="campaign.manager.icon" /></strong>
                                                        </a>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>

                                        <div class="col-sm-5 control-label-left" style="margin-bottom: 0; padding-top: 10px;">
                                            <label class="control-label" for="sendTime">
                                                <mvc:message code="default.Time" />
                                            </label>
                                            <div class="input-group" data-field="split">
                                                <div class="input-group-controls">
                                                    <input id="sendTime" type="text" name="sendTime" value="${sendTimeStr}" data-value="${sendTimeStr}" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'" data-field-split="sendHour, sendMinute" data-field-split-rule=":">
                                                </div>
                                                <div class="input-group-addon">
												<span class="addon">
													<i class="icon icon-clock-o"></i>
												</span>
                                                </div>
                                                <c:if test="${workflowId gt 0}">
                                                    <div class="input-group-btn">
                                                        <c:url var="workflowManagerUrl" value="/workflow/${workflowId}/view.action">
                                                            <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${form.mailingID}" />
                                                        </c:url>
                                                        <a href="${workflowManagerUrl}" class="btn btn-info btn-regular" data-tooltip="${editWithCampaignManagerMessage}">
                                                            <i class="icon icon-linkage-campaignmanager"></i>
                                                            <strong><mvc:message code="campaign.manager.icon" /></strong>
                                                        </a>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group" style="margin: 15px 0 0 0;">
                                        <%@ include file="fragments/mailing-delivery-settings-optimized-mailing-generation-close.jspf"%>
                                        <div class="col-sm-12" style="padding-top: 10px;">
                                            <div class="form-group" style="margin-bottom: 0;">
                                                <div class="col-sm-12 control-label-left">
                                                    <label class="control-label" for="timeZone">
                                                        <mvc:message code="birt.Timezone" />
                                                    </label>
                                                </div>
                                                <div class="col-sm-12">
                                                    <div class="input-group">
                                                        <div class="input-group-controls">
                                                            <input id="timeZone" value="${adminZone}" class="form-control" readonly="readonly"/>
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
                                        <mvc:message code="mailing.send.report" />
                                    </a>
                                </div>
                                <div class="tile-content tile-content-forms hidden" id="send-report-settings" style="padding: 15px;">

                                    <div class="form-group">
                                        <div style="margin: 0 7px;">

                                            <div style="width: 100%;">
                                                <label style="padding: 5px 0; margin-bottom: 0;">
                                                    <mvc:checkbox path="reportSendAfter24h" id="reportSendAfter24h"/>
                                                    <span style="line-height: 24px; vertical-align: top; font-weight: normal">
													<mvc:message code="mailing.send.report.24h" />
												</span>
                                                </label>
                                            </div>
                                            <div style="width: 100%;">
                                                <label style="padding: 0 0 5px; margin-bottom: 0;">
                                                    <mvc:checkbox path="reportSendAfter48h" id="reportSendAfter48h"/>
                                                    <span style="line-height: 24px; vertical-align: top; font-weight: normal">
													<mvc:message code="mailing.send.report.48h" />
												</span>
                                                </label>
                                            </div>
                                            <div style="width: 100%;">
                                                <label style="padding: 0 0 5px; margin-bottom: 0;">
                                                    <mvc:checkbox path="reportSendAfter1Week" id="reportSendAfter1Week"/>
                                                    <span style="line-height: 24px; vertical-align: top; font-weight: normal">
													    <mvc:message code="mailing.send.report.1week" />
												    </span>
                                                </label>
                                            </div>

                                            <div style="width: 100%;">
                                                <div class="form-group vspace-bottom-0" style="padding: 5px 0;">
                                                    <div class="col-sm-12 control-label-left">
                                                        <label class="control-label">
                                                            <mvc:message code="report.autosend.email" />
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-12">
                                                        <mvc:text path="reportSendEmail" id="report_email" cssClass="form-control" maxlength="199" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="tile">
                                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                    <a href="#" class="headline" data-toggle-tile="#mailing-option-settings">
                                        <i class="tile-toggle icon icon-angle-down"></i>
                                        <mvc:message code="mailing.option" />
                                    </a>
                                </div>
                                <div class="tile-content tile-content-forms" id="mailing-option-settings" style="padding: 15px;">
                                    <div class="form-group">
                                        <div style="margin: 0">
                                            <ul>
                                                <li class="list-group-item" style="border: none;">
                                                    <div class="checkbox">
                                                        <label>
                                                            <mvc:checkbox path="checkForDuplicateRecords" />
                                                            <mvc:message code="doublechecking.email" />
                                                        </label>
                                                    </div>
                                                </li>
                                                <c:if test="${isMailtrackExtended}">
                                                    <li class="list-group-item" style="border: none;">
                                                        <div class="checkbox">
                                                            <label>
                                                                <mvc:checkbox path="skipWithEmptyTextContent" />
                                                                <mvc:message code="skipempty.email" />
                                                            </label>
                                                        </div>
                                                    </li>
                                                </c:if>
                                                <li class="list-group-item" style="border: none;">
                                                    <div class="checkbox">
                                                        <label>
                                                            <mvc:checkbox path="cleanupTestsBeforeDelivery" />
                                                            <mvc:message code="mailing.delivery.cleanup" />
                                                        </label>
                                                    </div>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>

                                    <c:if test="${autoImports ne null}">
                                        <div class="form-group">
                                            <div class="col-sm-12 control-label-left">
                                                <label class="control-label">
                                                    <label for="required-auto-import" style="margin-bottom: 0;">
                                                        <mvc:message code="mailing.autoimport.required" />
                                                    </label>
                                                    <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/RequiredAutoImport.xml"></button>
                                                </label>
                                            </div>
                                            <div class="col-sm-12">
                                                <mvc:select path="autoImportId" id="required-auto-import" cssClass="form-control js-select">
                                                    <mvc:option value="0">---</mvc:option>
                                                    <mvc:options items="${autoImports}" itemValue="autoImportId" itemLabel="shortname"/>
                                                </mvc:select>
                                            </div>
                                        </div>
                                    </c:if>

                                    <emm:ShowByPermission token="mailing.setmaxrecipients">
                                        <div class="form-group">
                                            <div class="col-sm-12 control-label-left">
                                                <label class="control-label">
                                                    <label for="maxRecipients" style="margin-bottom: 0;">
                                                        <mvc:message code="setMaxRecipients" />
                                                    </label>
                                                    <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml">
                                                    </button>
                                                </label>
                                            </div>
                                            <div class="col-sm-12">
                                                <mvc:message var="maxRecipientsPlaceholder" code="mailing.unlimited" />
                                                <mvc:text path="maxRecipients" id="maxRecipients" cssClass="form-control js-inputmask" data-inputmask-options="mask: '9{1,20}'" placeholder="${maxRecipientsPlaceholder}"/>
                                            </div>
                                        </div>
                                    </emm:ShowByPermission>

                                    <%@ include file="fragments/mailing-delivery-settings-optimized-mailing-generation-open.jspf"%>
                                    <%@ include file="fragments/blocksize-select.jspf"%>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-12" onload="decimalSeperator()">
                        <div class="row">
                            <div class="tile-footer" data-sizing="bottom" style="padding: 10px 0; margin: 0 7px;">
                                <div class="btn-group">
                                    <c:url var="sendLink" value="/mailing/send/${form.mailingID}/view.action" />

                                    <a href="${sendLink}" class="btn btn-large pull-left" tabindex="-1">
                                        <i class="icon icon-reply"></i>
                                        <span class="text"><mvc:message code="button.Cancel" /></span>
                                    </a>

                                    <c:if test="${not isWorkflowDriven}">
                                        <button id="sendBtn" type="button" class="btn btn-large btn-primary pull-right" data-form-confirm=''>
                                            <i class="icon icon-send-o"></i> <span class="text"><mvc:message code="button.Send" /></span>
                                        </button>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </tiles:putAttribute>
    </tiles:insertTemplate>
</mvc:form>
