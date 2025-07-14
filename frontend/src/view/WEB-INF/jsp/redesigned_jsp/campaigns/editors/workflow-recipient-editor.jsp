<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient.WorkflowTargetOption" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--@elvariable id="allMailinglists" type="java.util.List"--%>
<%--@elvariable id="allTargets" type="java.util.List"--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>
<%--@elvariable id="regularTargets" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="allAltgs" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="isExtendedAltgEnabled" type="java.lang.Boolean"--%>

<c:set var="ALL_TARGETS_REQUIRED" value="<%= WorkflowTargetOption.ALL_TARGETS_REQUIRED %>"/>
<c:set var="NOT_IN_TARGETS" value="<%= WorkflowTargetOption.NOT_IN_TARGETS %>"/>
<c:set var="ONE_TARGET_REQUIRED" value="<%= WorkflowTargetOption.ONE_TARGET_REQUIRED %>"/>

<div id="recipient-editor" data-initializer="recipient-editor-initializer">
    <mvc:form action="" id="recipientForm" name="recipientForm" cssClass="form-column-1">
        <div>
            <label class="form-label" for="mailinglistSelect"><mvc:message code="Mailinglist"/></label>
            <select class="recipient-editor-select form-control js-select" name="mailinglistId" id="mailinglistSelect" data-field="required">
                <option value=""><mvc:message code="No_Mailinglist"/></option>
                <c:forEach var="mlist" items="${allMailinglists}">
                    <option value="${mlist.id}">${mlist.shortname}</option>
                </c:forEach>
            </select>
        </div>

        <div>
            <label class="form-label" for="recipientTargetSelect"><mvc:message code="Target"/></label>
            <select class="form-control js-select" multiple id="recipientTargetSelect"
                    data-result-template="target-group-select-item"
                    data-show-create-btn="data-action='recipient-editor-create-new-target'"
                    data-selection-template="target-group-select-item">
                <c:choose>
                    <c:when test="${isExtendedAltgEnabled}">
                        <c:forEach var="target" items="${regularTargets}" varStatus="rowCounter">
                            <option value="${target.id}"
                                    data-target-id="${target.id}"
                                    data-editable="${not target.accessLimitation}">${target.targetName}</option>
                        </c:forEach>                            
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="target" items="${allTargets}" varStatus="rowCounter">
                            <option value="${target.id}"
                                    data-editable="${not target.accessLimitation}"
                                    data-target-id="${target.id}"
                                    data-locked="${target.id eq accessLimitTargetId}">${target.targetName}</option>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </select>
        </div>

        <div>
            <select class="form-control js-select" name="targetsOption">
                <option value="${ALL_TARGETS_REQUIRED}" id="allTargetsRequired"><mvc:message code="mailing.targetmode.and"/> "&cap;"</option>
                
                <c:set var="oneTargetRequiredOptionDisabled" value="${not isExtendedAltgEnabled and accessLimitTargetId gt 0}"/>
                <option value="${ONE_TARGET_REQUIRED}" id="oneTargetRequired" ${oneTargetRequiredOptionDisabled ? 'disabled' : ''}>
                    <mvc:message code="workflow.recipient.oneTargetRequired"/> "&cup;"
                </option>
                
                <c:set var="notInTargetsOptionDisabled" value="${not isExtendedAltgEnabled and accessLimitTargetId gt 0}"/>
                <option value="${NOT_IN_TARGETS}" id="notInTargets" ${notInTargetsOptionDisabled ? 'disabled' : ''}>
                    <mvc:message code="workflow.recipient.notInTargets"/> "&ne;"
                </option>
            </select>
        </div>
        
        <%@include file="fragments/workflow-recipient-editor-altg-select.jspf" %>

    </mvc:form>
</div>

<script id="target-group-select-item" type="text/x-mustache-template">
    <div class="d-flex gap-1">
        {{ if (element.dataset.editable && element.dataset.editable === 'true') { }}
            <a href="#" data-action="recipient-editor-target-edit" data-target-id="{{- element.dataset.targetId }}">{{-text }}</a>
        {{ } else { }}
            <span >{{-text }}</span>
        {{ } }}
    </div>
</script>
