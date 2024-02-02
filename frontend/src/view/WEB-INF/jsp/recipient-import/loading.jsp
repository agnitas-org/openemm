<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>

<mvc:form modelAttribute="form" servletRelativeAction="/recipient/import/errors/edit.action" data-form="polling">
    <mvc:hidden path="numberOfRows"/>
</mvc:form>
