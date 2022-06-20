<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm"--%>
<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="expirationDate" type="java.lang.String"--%>
<%--@elvariable id="isSupervisor" type="java.lang.Boolean"--%>

<div class="system-tile-header">
    <tiles:insert attribute="header"/>
</div>

<div class="system-tile-content">
    <div class="align-center">
        <mvc:message code="password.change.notification.expired" arguments="${expirationDate}"/>
        <c:if test="${not isSupervisor}">
            <br>
            <mvc:message code="password.finally.expired.info"/>
        </c:if>
    </div>
</div>

<div class="system-tile-footer">
    <div class="pull-right">
        <s:message var="logonHint" code="logon.hint" htmlEscape="true"/>
        <s:message var="logonHintMessage" code="logon.security" arguments="${supportMailAddress}" htmlEscape="true"/>

        <a href="#" data-msg-system="system" data-msg="${logonHint}" data-msg-content="${logonHintMessage}">
            <mvc:message code="logon.hint"/>
        </a>
    </div>
</div>
