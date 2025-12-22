<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm"--%>
<%--@elvariable id="passwordPolicy" type="java.lang.String"--%>
<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="expirationDate" type="java.lang.String"--%>
<%--@elvariable id="isSupervisor" type="java.lang.Boolean"--%>
<%--@elvariable id="isExpiring" type="java.lang.Boolean"--%>
<%--@elvariable id="isExpired" type="java.lang.Boolean"--%>

<%-- Is set to true if user just submitted new password but it was invalid --%>
<%--@elvariable id="isAnotherAttempt" type="java.lang.Boolean"--%>

<c:set var="backToLogin" value="true" scope="request" />

<div>
    <div id="suggestion-view" class="row g-3 ${isExpired or isAnotherAttempt ? 'hidden' : ''}">
        <div class="col-12">
            <c:if test="${isExpiring}">
                <mvc:message code="password.change.notification.expiration" arguments="${expirationDate}"/><br/>
            </c:if>
            <mvc:message code="password.change.notification.info"/>
        </div>

        <div class="col-12">
            <button type="button" class="btn btn-lg btn-info w-100" data-action="showPasswordChangeForm">
                <mvc:message code="button.Change"/>
            </button>
        </div>

        <mvc:form cssClass="col-12" servletRelativeAction="/logon/change-password.action" method="POST">
            <input type="hidden" name="skip" value="true"/>

            <button data-form-submit-static class="btn btn-lg btn-light w-100" type="button">
                <mvc:message code="Proceed"/>
            </button>
        </mvc:form>
    </div>

    <%-- If a password is already expired ask user to change it now --%>
    <mvc:form id="submission-view" servletRelativeAction="/logon/change-password.action" method="POST" modelAttribute="form"
              cssClass="row g-3 ${isExpired or isAnotherAttempt ? '' : 'hidden'}" data-form="static">
        <div class="col-12">
            <c:if test="${isExpired and not empty expirationDate}">
                <c:choose>
                    <c:when test="${isSupervisor}">
                        <mvc:message code="password.supervisor.change.notification.expired" arguments="${expirationDate}"/><br/>
                    </c:when>
                    <c:otherwise>
                        <mvc:message code="password.change.notification.expired" arguments="${expirationDate}"/><br/>
                    </c:otherwise>
                </c:choose>
            </c:if>
            <mvc:message code="password.change.text"/>
        </div>

        <div class="col-12" data-field="password">
            <label for="password" class="form-label">
                <i class="icon icon-key"></i>
                <mvc:message code="password.new"/>
                <a href="#" class="icon icon-question-circle" data-help="settings/AdminPasswordRules.xml"></a>
            </label>
            <mvc:password path="password" id="password" cssClass="form-control js-password-strength" data-rule="${passwordPolicy}" />
        </div>

        <div class="col-12">
            <label for="password-repeat" class="form-label">
                <i class="icon icon-key"></i>
                <mvc:message code="password.repeat"/>
            </label>
            <input type="password" id="password-repeat" class="form-control js-password-match" />
        </div>

        <div class="col-12">
            <button type="button" class="btn btn-lg btn-light w-100" data-form-submit="">
                <span><mvc:message code="password.change.now"/></span>
                <i class="icon icon-caret-right"></i>
            </button>
        </div>
    </mvc:form>
</div>
