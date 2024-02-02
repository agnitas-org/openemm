<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.action" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.logon.forms.LogonPasswordChangeForm"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="expirationDate" type="java.lang.String"--%>
<%--@elvariable id="isSupervisor" type="java.lang.Boolean"--%>
<%--@elvariable id="isExpiring" type="java.lang.Boolean"--%>
<%--@elvariable id="isExpired" type="java.lang.Boolean"--%>

<%-- Is set to true if user just submitted new password but it was invalid --%>
<%--@elvariable id="isAnotherAttempt" type="java.lang.Boolean"--%>

<div class="system-tile-header">
    <tiles:insertAttribute name="header"/>
</div>

<div class="system-tile-content" data-controller="logon-password-change">
    <%-- If a password is not expired yet just suggest user changing it --%>
    <div id="suggestion-view" class="${isExpired or isAnotherAttempt ? 'hidden' : ''}">
        <div class="align-center">
            <c:if test="${isExpiring}">
                <mvc:message code="password.change.notification.expiration" arguments="${expirationDate}"/><br/>
            </c:if>
            <mvc:message code="password.change.notification.info"/>
        </div>

        <div class="form-group vspace-top-20">
            <div class="col-sm-6 col-sm-push-3">
                <button type="button" class="btn btn-large btn-block btn-primary" data-action="showPasswordChangeForm">
                    <span class="text"><mvc:message code="button.Change"/></span>
                </button>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-6 col-sm-push-3">
                <mvc:form servletRelativeAction="/logon/change-password.action" method="POST">
                    <input type="hidden" name="skip" value="true"/>

                    <button type="submit" class="btn btn-large btn-block">
                        <span class="text"><mvc:message code="Proceed"/></span>
                    </button>
                </mvc:form>
            </div>
        </div>
    </div>

    <%-- If a password is already expired ask user to change it now --%>
    <div id="submission-view" class="${isExpired or isAnotherAttempt ? '' : 'hidden'}">
        <div class="align-center">
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

        <mvc:form servletRelativeAction="/logon/change-password.action" data-form="static" method="POST" modelAttribute="form" data-validator="logon-new-password/form">
            <div class="form-group vspace-top-20">
                <div class="col-sm-4">
                    <label for="password" class="control-label"><i class="icon icon-key"></i>
                        <mvc:message code="password.new"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:password path="password" id="password" cssClass="form-control" />
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="password-repeat" class="control-label"><i class="icon icon-key"></i> <mvc:message code="password.repeat"/></label>
                </div>
                <div class="col-sm-8">
                    <input type="password" id="password-repeat" class="form-control" />
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <button type="button" class="btn btn-large btn-primary btn-block" data-form-submit="">
                        <span class="text"><mvc:message code="password.change.now"/> <i class="icon icon-angle-right"></i></span>
                    </button>
                </div>
            </div>
        </mvc:form>
    </div>
</div>

<div class="system-tile-footer">
    <div class="pull-left">
        <a href="<c:url value="/logon/reset-password.action"/>">
            <mvc:message code="logon.password_reset"/>
        </a>
    </div>

    <div class="pull-right">
        <s:message var="logonHint" code="logon.hint" htmlEscape="true"/>
        <s:message var="logonHintMessage" code="logon.security" arguments="${supportMailAddress}" htmlEscape="true"/>

        <a href="#" data-msg-system="system" data-msg="${logonHint}" data-msg-content="${logonHintMessage}">
            <mvc:message code="logon.hint"/>
        </a>
    </div>
</div>
