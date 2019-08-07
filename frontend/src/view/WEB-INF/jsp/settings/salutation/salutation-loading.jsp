<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.SalutationAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="salutationForm" type="org.agnitas.web.SalutationForm"--%>

<c:set var="ACTION_LIST" value="<%= SalutationAction.ACTION_LIST %>"/>

<c:set var="refreshMillis">${salutationForm.refreshMillis}</c:set>
<agn:agnForm action="/salutation" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action" value="${ACTION_LIST}"/>
    <html:hidden property="error"/>
    <html:hidden property="numberOfRows"/>
</agn:agnForm>
