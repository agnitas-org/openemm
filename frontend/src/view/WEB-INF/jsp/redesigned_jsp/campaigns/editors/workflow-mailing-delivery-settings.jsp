<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowMailing" %>
<%@ page import="com.agnitas.emm.core.mailing.enums.BlocksizeSteppingOption" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="AUTOMATIC_REPORT_NONE" value="<%= WorkflowMailing.AUTOMATIC_REPORT_NONE %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_1DAY" value="<%= WorkflowMailing.AUTOMATIC_REPORT_1DAY %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_2DAYS" value="<%= WorkflowMailing.AUTOMATIC_REPORT_2DAYS %>" scope="page"/>
<c:set var="AUTOMATIC_REPORT_7DAYS" value="<%= WorkflowMailing.AUTOMATIC_REPORT_7DAYS %>" scope="page"/>

<div class="delivery-settings flex-grow-1 mt-3">
    <div id="sendSettingsToggle_${param.editorId}" class="wm-mailing-send-settings-link">
        <a href="#" class="btn btn-primary w-100" data-action="mailing-editor-base-toggle-settings" data-config="editorId:${param.editorId}">
            <mvc:message code="workflow.mailing.DeliverySettings"/>
        </a>
    </div>

    <div id="sendSettings_${param.editorId}" class="wm-mailing-send-settings form-column mt-3" style="display: none">
        <div>
            <label class="form-label" for="delivery-auto-report"><mvc:message code="workflow.mailing.AutomaticReport"/></label>
            <select id="delivery-auto-report" class="form-control js-select" name="autoReport">
                <option value="${AUTOMATIC_REPORT_NONE}"><mvc:message code="default.none"/></option>
                <option value="${AUTOMATIC_REPORT_1DAY}"><mvc:message code="mailing.send.report.24h"/></option>
                <option value="${AUTOMATIC_REPORT_2DAYS}"><mvc:message code="mailing.send.report.48h"/></option>
                <option value="${AUTOMATIC_REPORT_7DAYS}"><mvc:message code="mailing.send.report.1week"/></option>
            </select>
        </div>
        
        <div class="form-check form-switch">
            <input type="checkbox" id=dontDeliverMailing_${param.editorId} name="skipEmptyBlocks" class="form-check-input" role="switch">
            <label class="form-label form-check-label" for="dontDeliverMailing_${param.editorId}"><mvc:message code="skipempty.email"/></label>
        </div>
        
        <div class="form-check form-switch">
            <input type="checkbox" id="duplicateCheck_${param.editorId}" name="doubleCheck" value="true" class="form-check-input" role="switch">
            <label class="form-label form-check-label" for="duplicateCheck_${param.editorId}"><mvc:message code="doublechecking.email"/></label>
        </div>
        
        <div>
            <label class="form-label">
                <mvc:message code="setMaxRecipients"/>
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/MailingMaxsendquantyMsg.xml"></a>
            </label>
            <input type="text" class="form-control" value="0" name="maxRecipients"/>
        </div>
        <div>
            <label class="form-label" for="mailType"><mvc:message code="mailing.mailsperhour"/></label>
            <select class="form-control js-select" name="blocksize" id="mailType">
                <c:forEach var="blocksizeOption" items="${BlocksizeSteppingOption.values()}">
                    <option value="${blocksizeOption.mailsPerHour}">
                        <c:choose>
                            <c:when test="${blocksizeOption eq BlocksizeSteppingOption.UNLIMITED}">
                                <mvc:message code="mailing.unlimited" />
                            </c:when>
                            <c:otherwise>
                                <fmt:formatNumber value="${blocksizeOption.mailsPerHour}" type="number" pattern="#,##0" />
                            </c:otherwise>
                        </c:choose>
                    </option>
                </c:forEach>
            </select>
        </div>
    </div>
</div>
