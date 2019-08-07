<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="mailingBaseForm" type="org.agnitas.web.MailingSendForm"--%>
<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>

<c:set var="refreshMillis">${mailingBaseForm.refreshMillis}</c:set>
<agn:agnForm action="/mailingsend" data-form="polling" data-polling-interval="${refreshMillis}">
    <html:hidden property="action"/>
    <html:hidden property="error"/>
    <html:hidden property="mailingID" value="${mailingSendForm.mailingID}"/>
    <html:hidden property="sendDate" value="${mailingSendForm.sendDate}"/>
    <html:hidden property="sendHour" value="${mailingSendForm.sendHour}"/>
    <html:hidden property="sendMinute" value="${mailingSendForm.sendMinute}"/>
    <html:hidden property="blocksize" value="${mailingSendForm.blocksize}"/>
    <html:hidden property="refreshMillis" value="${mailingSendForm.refreshMillis}"/>
    <html:hidden property="reportSendAfter24h" value="${mailingSendForm.reportSendAfter24h}" />
    <html:hidden property="reportSendAfter48h" value="${mailingSendForm.reportSendAfter48h}"/>
    <html:hidden property="reportSendAfter1Week" value="${mailingSendForm.reportSendAfter1Week}" />
    <html:hidden property="reportSendEmail" value="${mailingSendForm.reportSendEmail}" />
</agn:agnForm>
