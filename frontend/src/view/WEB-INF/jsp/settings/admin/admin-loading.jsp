<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="adminForm" type="com.agnitas.web.ComAdminForm"--%>

<c:set var="refreshMillis">${adminForm.refreshMillis}</c:set>
<agn:agnForm action="/admin" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action"/>
    <html:hidden property="error"/>
    <html:hidden property="numberOfRows"/>
</agn:agnForm>
