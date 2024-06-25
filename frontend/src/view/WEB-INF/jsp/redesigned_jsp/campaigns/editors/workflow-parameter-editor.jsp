<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="parameter-editor" data-initializer="parameter-editor-initializer">
    <mvc:form action="" id="parameterForm" name="parameterForm">
        <input name="id" type="hidden">
        
        <label class="form-label"><mvc:message code="Value"/></label>
        <select name="value" class="form-control">
            <%-- Filled by JS --%>
        </select>
<%--        <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="parameter-editor-save">--%>
<%--            <mvc:message code="button.Apply"/>--%>
<%--        </a>--%>
    </mvc:form>
</div>
