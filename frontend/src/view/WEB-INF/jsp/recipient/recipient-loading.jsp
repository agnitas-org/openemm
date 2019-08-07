<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="recipientForm" type="com.agnitas.web.ComRecipientForm"--%>

<c:set var="refreshMillis">${recipientForm.refreshMillis}</c:set>
<agn:agnForm id="recipientForm" action="/recipient" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action"/>
    <html:hidden property="error"/>
    <html:hidden property="numberOfRows"/>

    <c:forEach var="field" items="${recipientForm.selectedFields}">
        <html:hidden property="selectedFields" value="${field}"/>
    </c:forEach>
</agn:agnForm>
