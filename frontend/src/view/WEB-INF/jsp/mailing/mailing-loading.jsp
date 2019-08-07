<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>

<c:set var="refreshMillis">${mailingBaseForm.refreshMillis}</c:set>
<agn:agnForm action="/mailingbase" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action"/>
    <html:hidden property="error"/>
    <html:hidden property="numberOfRows"/>

    <c:forEach var="field" items="${mailingBaseForm.selectedFields}">
        <html:hidden property="selectedFields" value="${field}"/>
    </c:forEach>

    <html:hidden property="mailingTypeNormal"/>
    <html:hidden property="mailingTypeDate"/>
    <html:hidden property="mailingTypeEvent"/>
    <html:hidden property="mailingTypeFollowup"/>
    <html:hidden property="mailingTypeInterval"/>
</agn:agnForm>
