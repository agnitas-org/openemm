<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient.WorkflowTargetOption" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
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

<div id="recipient-editor" data-initializer="recipient-editor-initializer">
    <mvc:form action="" id="recipientForm" name="recipientForm">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mailinglistSelect">
                    <mvc:message code="Mailinglist"/>
                </label>
            </div>

            <div class="col-sm-8">
                <select class="recipient-editor-select form-control js-select" name="mailinglistId" id="mailinglistSelect">
                    <c:forEach var="mlist" items="${allMailinglists}">
                        <option value="${mlist.id}">${mlist.shortname}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="recipientTargetSelect">
                    <mvc:message code="Target"/>
                </label>
            </div>

            <div class="col-sm-8">
                <select class="form-control js-select" multiple="" id="recipientTargetSelect">
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
        </div>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <a href="#" class="btn btn-regular disable-for-active" data-action="recipient-editor-create-new-target">
                    <mvc:message code="target.NewTarget"/>
                </a>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <ul class="list-group">
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <c:choose>
                                <c:when test="${isExtendedAltgEnabled}">
                                    <input name="targetsOption" type="radio" checked="checked" value="${ONE_TARGET_REQUIRED}" id="oneTargetRequired"/>
                                </c:when>
                                <c:otherwise>
                                    <input name="targetsOption" type="radio" checked="checked" value="${ONE_TARGET_REQUIRED}" id="oneTargetRequired"
                                           ${accessLimitTargetId gt 0 ? 'disabled' : ''}/>
                                </c:otherwise>
                            </c:choose>
                            <mvc:message code="workflow.recipient.oneTargetRequired"/> "&cup;"
                        </label>
                    </li>
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <input name="targetsOption" type="radio" value="${ALL_TARGETS_REQUIRED}" id="allTargetsRequired"/>
                            <mvc:message code="mailing.targetmode.and"/> "&cap;"
                        </label>
                    </li>
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <c:choose>
                                <c:when test="${isExtendedAltgEnabled}">
                                    <input name="targetsOption" type="radio" value="${NOT_IN_TARGETS}" id="notInTargets"/>
                                </c:when>
                                <c:otherwise>
                                    <input name="targetsOption" type="radio" value="${NOT_IN_TARGETS}" id="notInTargets"
                                          ${accessLimitTargetId gt 0 ? 'disabled' : ''}/>
                                </c:otherwise>
                            </c:choose>
                            <mvc:message code="workflow.recipient.notInTargets"/> "&ne;"
                        </label>
                    </li>
                </ul>
            </div>
        </div>
        <%@include file="../fragments/workflow-recipient-editor-altg-select.jspf" %>
        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel" >
                        <mvc:message code="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="recipient-editor-save">
                        <mvc:message code="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
