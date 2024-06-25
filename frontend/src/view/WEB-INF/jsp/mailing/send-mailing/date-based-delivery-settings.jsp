<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="adminDateFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="targetGroupNames" type="java.util.List<java.lang.String>"--%>
<%--@elvariable id="adminTimezone" type="java.lang.String"--%>
<%--@elvariable id="isMailtrackExtended" type="java.lang.Boolean"--%>
<%--@elvariable id="autoImports" type="java.util.List<org.agnitas.emm.core.autoimport.bean.AutoImportLight>"--%>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, form.workflowId)}" scope="page"/>
<c:set var="workflowId" value="${workflowParams.workflowId}" scope="page"/>
<c:set var="isWorkflowDriven" value="${workflowParams.workflowId gt 0}" scope="page"/>

<c:set var="clearanceThreshold" value="${form.securitySettings.clearanceThreshold gt 0 ? form.securitySettings.clearanceThreshold : ''}"/>
<c:set var="isNotificationEnabled" value="${not empty form.securitySettings.clearanceEmail}"/>

<c:set var="isFullscreenTileSizingDisabled" value="true" scope="request"/>

<c:if test="${isWorkflowDriven}">
    <c:url var="WORKFLOW_LINK" value="/workflow/${workflowParams.workflowId}/view.action" scope="page">
        <c:param name="forwardParams" value="${workflowParams.workflowForwardParams};elementValue=${form.mailingID}"/>
    </c:url>
</c:if>

<jsp:useBean id="now" class="java.util.Date"/>
<fmt:formatDate value="${now}" pattern="HH" timeZone="${adminTimeZone}" var="sendHour"/>
<fmt:formatDate value="${now}" pattern="mm" timeZone="${adminTimeZone}" var="sendMinute"/>

<mvc:form servletRelativeAction="/mailing/send/datebased/activation/confirm.action" modelAttribute="form" data-form="resource">

    <mvc:hidden path="mailingID"/>

    <tiles:insertTemplate template="/WEB-INF/jsp/mailing/template.jsp">
        <c:if test="${form.isMailingGrid}">
            <tiles:putAttribute name="header" type="string">
                <ul class="tile-header-nav">
                    <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                </ul>
            </tiles:putAttribute>
        </c:if>

        <tiles:putAttribute name="content" type="string">
            <c:if test="${not form.isMailingGrid}">
                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                    <h2 class="headline">
                        <mvc:message code="MailingSend"/>
                    </h2>
                </div>
            </c:if>
            <div class="tile-content tile-content-forms">
                <div class="row">
                    <div class="col-sm-12">
                        <%-- Schedule mailing block --%>
                        <div class="col-sm-6">
                            <div class="tile">
                                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                    <h2 class="headline">
                                        <mvc:message code="mailing.schedule"/>
                                    </h2>
                                </div>
                                <div class="tile-content tile-content-forms" style="padding: 15px 15px 30px;">
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <div class="col-sm-12">
                                                <div style="margin-bottom: 20px;">
                                                    <div class="panel-body" style="padding: 8px;">
                                                        <h3 class="headline" style="color: #707173; text-transform: capitalize;">
                                                            <mvc:message code="Target-Groups"/>
                                                        </h3>
                                                    </div>
                                                    <ul class="list-group" style="margin: 0;">
                                                        <c:choose>
                                                            <c:when test="${empty targetGroupNames}">
                                                                <li class="list-group-item" style="border: none;">
                                                                    <mvc:message code="statistic.all_subscribers"/>
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
                                        </div>
                                    </div>

                                    <div class="form-group" style="margin: 0;">
                                        <div class="col-sm-12" style="padding-top: 10px;">
                                            <div class="form-group" style="margin-bottom: 0">
                                                <div class="col-sm-12 control-label-left">
                                                    <label class="control-label" for="sendTime">
                                                        <mvc:message code="mailing.SendingTimeDaily"/>
                                                    </label>
                                                </div>

                                                <div class="col-sm-12">
                                                    <c:choose>
                                                        <c:when test="${isWorkflowDriven}">
                                                            <div class="input-group" data-field="split">
                                                                <div class="input-group-controls">
                                                                    <input type="text" id="sendTime" name="sendTime" class="form-control js-timepicker" value="${sendHour}:${sendMinute}"
                                                                           data-field-split="sendHour, sendMinute" data-field-split-rule=":" data-timepicker-options="mask: 'h:00'" disabled='disabled'/>
                                                                </div>
                                                                <div class="input-group-addon" disabled='disabled'>
                                                                    <span class="addon"><i class="icon icon-clock-o"></i></span>
                                                                </div>
                                                                <div class="input-group-addon" disabled='disabled'>
                                                                    <span class="addon">${emm:getTimeZoneId(pageContext.request)}</span>
                                                                </div>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="input-group" data-field="split">
                                                                <div class="input-group-controls">
                                                                    <input type="text" id="sendTime" name="sendTime" class="form-control js-timepicker" value="${sendHour}:${sendMinute}"
                                                                           data-field-split="sendHour, sendMinute" data-field-split-rule=":" data-timepicker-options="mask: 'h:00'"/>
                                                                </div>
                                                                <div class="input-group-addon">
                                                                    <span class="addon"><i class="icon icon-clock-o"></i></span>
                                                                </div>
                                                                <div class="input-group-addon">
                                                                    <span class="addon">${emm:getTimeZoneId(pageContext.request)}</span>
                                                                </div>
                                                            </div>

                                                            <p class="help-block"><mvc:message code="default.interval"/>: <mvc:message code="default.minutes.60"/></p>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>

                                        <%@ include file="fragments/mailing-delivery-settings-optimized-mailing-generation-close.jspf" %>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <%-- Security and notifications + Options blocks --%>
                        <div class="col-sm-6">
                            <%-- Security and notifications --%>
                            <div class="tile">
                                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                    <a href="#" class="headline"
                                       data-toggle-tile="#security-and-notifications-settings">
                                        <i class="tile-toggle icon icon-angle-down"></i>
                                        <mvc:message code="mailing.send.security.notification"/>
                                    </a>
                                </div>
                                <div class="tile-content tile-content-forms hidden" id="security-and-notifications-settings" style="padding: 15px;" data-field="toggle-vis">
                                    <div class="form-group">
                                        <div style="margin: 0 7px;">
                                            <div class="form-group">
                                                <div class="col-sm-4">
                                                    <label class="control-label checkbox-control-label" for="enable-notification">
                                                        <mvc:message code="mailing.notification.enable"/>
                                                    </label>
                                                </div>
                                                <div class="col-sm-8">
                                                    <label class="toggle">
                                                        <input type="checkbox" name="securitySettings.enableNotifications" id="enable-notification"
                                                            ${isNotificationEnabled ? "checked=checked" : ""} data-field-vis="" data-field-vis-show="#notification-related-data">
                                                        <div class="toggle-control"></div>
                                                        <div class="hidden" data-field-vis-default="" data-field-vis-hide="#notification-related-data"></div>
                                                    </label>
                                                </div>
                                            </div>

                                            <div id="notification-related-data">
                                                <div class="form-group">
                                                    <div class="col-sm-4">
                                                        <label class="control-label checkbox-control-label" for="enable-status-on-error">
                                                            <mvc:message code="mailing.SendStatusOnErrorOnly"/>
                                                            <button type="button" class="icon icon-help" tabindex="-1"
                                                                    data-help="help_${helplanguage}/mailing/SendStatusOnErrorOnly.xml"></button>
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-8">
                                                        <label class="toggle">
                                                            <mvc:checkbox path="securitySettings.enableNoSendCheckNotifications" id="enable-status-on-error" value="true"/>
                                                            <div class="toggle-control"></div>
                                                        </label>
                                                    </div>
                                                </div>

                                                <div class="form-group">
                                                    <div class="col-sm-4">
                                                        <label class="control-label" for="clearanceThreshold">
                                                            <mvc:message code="mailing.autooptimization.threshold"/>
                                                            <button type="button" class="icon icon-help" tabindex="-1"
                                                                    data-help="help_${helplanguage}/mailing/Threshold.xml"></button>
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-6">
                                                        <mvc:message var="placeholder" code="mailing.send.threshold"/>
                                                        <input type="text" class="form-control" id="clearanceThreshold" name="securitySettings.clearanceThreshold"
                                                               value="${clearanceThreshold}" placeholder="${placeholder}">
                                                    </div>
                                                </div>

                                                <mvc:hidden path="securitySettings.clearanceEmail"/>

                                                <div class="form-group">
                                                    <div class="col-sm-4">
                                                        <label class="control-label" for="clearanceEmail">
                                                            <mvc:message code="Recipients"/>
                                                            <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/SendStatusEmail.xml"></button>
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-6">
                                                        <div id="clearanceEmail" data-controller="email-list-controller" data-initializer="email-list-initializer" data-target-field="securitySettings.clearanceEmail">
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <%-- Options block --%>
                            <div class="tile">
                                <div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                    <a href="#" class="headline" data-toggle-tile="#mailing-option-settings">
                                        <i class="tile-toggle icon icon-angle-down"></i>
                                        <mvc:message code="mailing.option"/>
                                    </a>
                                </div>
                                <div class="tile-content tile-content-forms" id="mailing-option-settings" style="padding: 15px;">
                                    <div class="form-group">
                                        <div style="margin: 0">
                                            <ul>
                                                <li class="list-group-item" style="border: none;">
                                                    <div class="checkbox">
                                                        <label>
                                                            <mvc:checkbox path="checkForDuplicateRecords"/>
                                                            <mvc:message code="doublechecking.email"/>
                                                        </label>
                                                    </div>
                                                </li>
                                                <c:if test="${isMailtrackExtended}">
                                                    <li class="list-group-item" style="border: none;">
                                                        <div class="checkbox">
                                                            <label>
                                                                <mvc:checkbox path="skipWithEmptyTextContent"/>
                                                                <mvc:message code="skipempty.email"/>
                                                            </label>
                                                        </div>
                                                    </li>
                                                </c:if>
                                                <li class="list-group-item" style="border: none;">
                                                    <div class="checkbox">
                                                        <label>
                                                            <mvc:checkbox path="cleanupTestsBeforeDelivery"/>
                                                            <mvc:message code="mailing.delivery.cleanup"/>
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
                                                        <mvc:message code="mailing.autoimport.required"/>
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
                                                        <mvc:message code="setMaxRecipients"/>
                                                    </label>
                                                    <button type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml">
                                                    </button>
                                                </label>
                                            </div>
                                            <div class="col-sm-12">
                                                <mvc:message var="maxRecipientsPlaceholder" code="mailing.unlimited"/>
                                                <mvc:text path="maxRecipients" id="maxRecipients" cssClass="form-control js-inputmask"
                                                          data-inputmask-options="mask: '9{1,20}'"
                                                          placeholder="${maxRecipientsPlaceholder}"/>
                                            </div>
                                        </div>
                                    </emm:ShowByPermission>

                                    <%@ include file="fragments/mailing-delivery-settings-optimized-mailing-generation-open.jspf" %>
                                    <%@ include file="fragments/blocksize-select.jspf"%>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-12">
                        <div class="row">
                            <div class="tile-footer" data-sizing="bottom" style="padding: 10px 0; margin: 0 7px;">
                                <div class="btn-group">
                                    <c:url var="sendLink" value="/mailing/send/${form.mailingID}/view.action"/>
                                    <a href="${sendLink}" class="btn btn-large pull-left" tabindex="-1">
                                        <i class="icon icon-reply"></i>
                                        <span class="text"><mvc:message code="button.Cancel"/></span>
                                    </a>

                                    <c:choose>
                                        <c:when test="${isWorkflowDriven}">
                                            <div class="input-group-btn">
                                                <a href="${WORKFLOW_LINK}" class="btn btn-info btn-regular btn-large pull-right">
                                                    <i class="icon icon-linkage-campaignmanager"></i>
                                                    <strong><mvc:message code="campaign.manager.icon"/></strong>
                                                </a>
                                                <button class="btn btn-large btn-primary pull-right" disabled='disabled'>
                                                    <i class="icon icon-check-circle-o"></i>
                                                    <span class="text"><mvc:message code="button.Activate" /></span>
                                                </button>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <button id="sendBtn" type="button" class="btn btn-large btn-primary pull-right" data-form-confirm=''>
                                                <i class="icon icon-check-circle-o"></i>
                                                <span class="text"><mvc:message code="button.Activate"/></span>
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </tiles:putAttribute>
    </tiles:insertTemplate>
</mvc:form>
