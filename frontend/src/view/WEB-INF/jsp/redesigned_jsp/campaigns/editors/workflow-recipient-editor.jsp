<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient.WorkflowTargetOption" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm"--%>
<%--@elvariable id="allMailinglists" type="java.util.List"--%>
<%--@elvariable id="allTargets" type="java.util.List"--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>
<%--@elvariable id="regularTargets" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="adminAltgs" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="allAltgs" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="isExtendedAltgEnabled" type="java.lang.Boolean"--%>

<c:set var="FORWARD_TARGETGROUP_CREATE" value="<%= WorkflowController.FORWARD_TARGETGROUP_CREATE_QB%>"/>
<c:set var="FORWARD_TARGETGROUP_EDIT" value="<%= WorkflowController.FORWARD_TARGETGROUP_EDIT_QB%>"/>
<c:set var="ALL_TARGETS_REQUIRED" value="<%= WorkflowTargetOption.ALL_TARGETS_REQUIRED %>"/>
<c:set var="NOT_IN_TARGETS" value="<%= WorkflowTargetOption.NOT_IN_TARGETS %>"/>
<c:set var="ONE_TARGET_REQUIRED" value="<%= WorkflowTargetOption.ONE_TARGET_REQUIRED %>"/>

<%-- todo check radio -> option in js--%>

<div id="recipient-editor" data-initializer="recipient-editor-initializer">
    <mvc:form action="" id="recipientForm" name="recipientForm" cssClass="grid" cssStyle="--bs-columns: 1">
        <div>
            <label class="form-label" for="mailinglistSelect"><mvc:message code="Mailinglist"/></label>
            <select class="recipient-editor-select form-control js-select" name="mailinglistId" id="mailinglistSelect">
                <c:forEach var="mlist" items="${allMailinglists}">
                    <option value="${mlist.id}">${mlist.shortname}</option>
                </c:forEach>
            </select>
        </div>

        <div>
            <label class="form-label" for="recipientTargetSelect"><mvc:message code="Target"/></label>
            <select class="form-control js-select" multiple id="recipientTargetSelect">
                <c:choose>
                    <c:when test="${isExtendedAltgEnabled}">
                        <c:forEach var="target" items="${regularTargets}" varStatus="rowCounter">
                            <option value="${target.id}" data-editable="${not target.accessLimitation}">${target.targetName}</option>
                        </c:forEach>                            
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="target" items="${allTargets}" varStatus="rowCounter">
                            <option value="${target.id}" data-editable="${not target.accessLimitation}"
                                    data-locked="${target.id eq accessLimitTargetId}">${target.targetName}</option>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </select>
        </div>

<%--        <div>--%>
<%--            <a href="#" class="btn btn-regular disable-for-active" data-action="recipient-editor-create-new-target">--%>
<%--                <mvc:message code="target.NewTarget"/>--%>
<%--            </a>--%>
<%--        </div>--%>

        <div>
            <select class="form-control" name="targetsOption">
                <c:set var="oneTargetRequiredOptionDisabled" value="${not isExtendedAltgEnabled and accessLimitTargetId gt 0}"/>
                <option name="targetsOption" value="${ONE_TARGET_REQUIRED}" id="oneTargetRequired" ${oneTargetRequiredOptionDisabled ? 'disabled' : ''}>
                    <mvc:message code="workflow.recipient.oneTargetRequired"/> "&cup;"
                </option>
                <option value="${ALL_TARGETS_REQUIRED}" id="allTargetsRequired"><mvc:message code="mailing.targetmode.and"/> "&cap;"</option>
                
                <c:set var="notInTargetsOptionDisabled" value="${not isExtendedAltgEnabled and accessLimitTargetId gt 0}"/>
                <option name="targetsOption" value="${NOT_IN_TARGETS}" id="notInTargets" ${notInTargetsOptionDisabled ? 'disabled' : ''}>
                    <mvc:message code="workflow.recipient.notInTargets"/> "&ne;"
                </option>
            </select>
        </div>
        
        <%@include file="fragments/workflow-recipient-editor-altg-select.jspf" %>
        
<%--                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="recipient-editor-save">--%>
<%--                        <mvc:message code="button.Apply"/>--%>
<%--                    </a>--%>
    </mvc:form>
</div>
