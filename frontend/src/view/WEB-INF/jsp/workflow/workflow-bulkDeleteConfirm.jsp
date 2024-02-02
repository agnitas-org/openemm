<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>

<jsp:include page="workflow-bulkOperationConfirm.jsp">
    <jsp:param name="headerMessageKey" value="Workflow"/>
    <jsp:param name="bulkOperation" value="bulkDelete.action"/>
    <jsp:param name="bulkActionQuestion" value="bulkAction.delete.workflow.question"/>
    <jsp:param name="bulkActionButton" value="button.Delete"/>
</jsp:include>
