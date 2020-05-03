<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient.WorkflowTargetOption" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm"--%>
<%--@elvariable id="allMailinglists" type="java.util.List"--%>
<%--@elvariable id="allTargets" type="java.util.List"--%>

<c:set var="FORWARD_TARGETGROUP_CREATE" value="<%= WorkflowController.FORWARD_TARGETGROUP_CREATE_QB%>"/>
<c:set var="FORWARD_TARGETGROUP_EDIT" value="<%= WorkflowController.FORWARD_TARGETGROUP_EDIT_QB%>"/>
<c:set var="ALL_TARGETS_REQUIRED" value="<%= WorkflowTargetOption.ALL_TARGETS_REQUIRED %>"/>
<c:set var="NOT_IN_TARGETS" value="<%= WorkflowTargetOption.NOT_IN_TARGETS %>"/>
<c:set var="ONE_TARGET_REQUIRED" value="<%= WorkflowTargetOption.ONE_TARGET_REQUIRED %>"/>

<div id="recipient-editor" data-initializer="recipient-editor-initializer">
    <form action="" id="recipientForm" name="recipientForm">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mailinglistSelect">
                    <bean:message key="Mailinglist"/>
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
                    <bean:message key="Target"/>
                </label>
            </div>

            <div class="col-sm-8">
                <select class="form-control js-select" multiple="" id="recipientTargetSelect">
                    <c:forEach var="target" items="${allTargets}" varStatus="rowCounter">
                        <option value="${target.id}">${target.targetName}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <a href="#" class="btn btn-regular disable-for-active" data-action="recipient-editor-create-new-target">
                    <bean:message key="target.NewTarget"/>
                </a>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <ul class="list-group">
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <input name="targetsOption" type="radio" checked="checked" value="${ONE_TARGET_REQUIRED}" id="oneTargetRequired"/>
                            <bean:message key="workflow.recipient.oneTargetRequired"/> "&cup;"
                        </label>
                    </li>
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <input name="targetsOption" type="radio" value="${ALL_TARGETS_REQUIRED}" id="allTargetsRequired"/>
                            <bean:message key="mailing.targetmode.and"/> "&cap;"
                        </label>
                    </li>
                    <li class="list-group-item">
                        <label class="radio-inline">
                            <input name="targetsOption" type="radio" value="${NOT_IN_TARGETS}" id="notInTargets"/>
                            <bean:message key="workflow.recipient.notInTargets"/> "&ne;"
                        </label>
                    </li>
                </ul>
            </div>
        </div>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel" >
                        <bean:message key="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="recipient-editor-save">
                        <bean:message key="button.Save"/>
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>
