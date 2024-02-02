<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>

<jsp:include page="workflow-bulkOperationConfirm.jsp">
    <jsp:param name="headerMessageKey" value="Workflow"/>
    <jsp:param name="bulkOperation" value="bulkDeactivate.action"/>
    <jsp:param name="bulkActionQuestion" value="bulkAction.deactivate.workflow.question"/>
    <jsp:param name="bulkActionButton" value="btndeactivate"/>
</jsp:include>
