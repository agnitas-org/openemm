<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="newImportWizardForm" type="com.agnitas.web.forms.ComNewImportWizardForm"--%>

<c:set var="ACTION_ERROR_EDIT" value="<%= ProfileImportAction.ACTION_ERROR_EDIT %>"/>

<c:set var="refreshMillis">${newImportWizardForm.refreshMillis}</c:set>
<agn:agnForm action="/newimportwizard" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action" value="${ACTION_ERROR_EDIT}"/>
    <html:hidden property="error"/>
    <html:hidden property="numberOfRows"/>
</agn:agnForm>
