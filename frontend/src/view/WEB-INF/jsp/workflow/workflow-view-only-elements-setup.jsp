<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>

<emm:CheckLogon/>
<emm:Permission token="workflow.show"/>

<c:set var="agnNavigationKey" 	value="Workflow" 								scope="request" />
<c:set var="agnNavHrefAppend" 	value="&workflowId=${workflowForm.workflowId}" 	scope="request" />
<c:set var="agnTitleKey" 		value="workflow.single" 						scope="request" />
<c:set var="agnHighlightKey" 	value="workflow.single" 						scope="request" />

<c:if test="${workflowForm.workflowId eq 0}">
    <c:set var="agnNavigationKey" 	value="Workflow" 		scope="request" />
    <c:set var="agnSubtitleKey" 	value="workflow.new" 	scope="request" />
    <c:set var="agnHighlightKey" 	value="workflow.new" 	scope="request" />
</c:if>

<c:set var="agnHelpKey" value="workflow" scope="request" />
