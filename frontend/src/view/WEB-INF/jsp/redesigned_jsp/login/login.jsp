<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${afterLogout}">
    <c:set var="logoutInfoMsg">
        <b><mvc:message code="logout.successful"/></b>
        </br>
        </br>
        <mvc:message code="logon.security" arguments="${supportMailAddress}"/>
    </c:set>
</c:if>

<mvc:form cssClass="row g-3" servletRelativeAction="/logon.action" modelAttribute="form"
          data-form-focus="username" data-disable-controls="login">

    <script data-initializer="login" type="application/json">
        {
            "SHOW_TAB_HINT": ${SHOW_TAB_HINT},
            "logoutMsg": ${emm:toJson(logoutInfoMsg)}
        }
    </script>

    <div class="col-12">
        <label for="username" class="form-label">
            <i class="icon icon-user"></i>
            <mvc:message code="logon.username"/>
        </label>

        <mvc:text path="username" id="username" cssClass="form-control" maxlength="180" data-controls-group="login" autocomplete="username"/>
    </div>

    <div class="col-12">
        <div class="row g-0">
            <div class="col-12">
                <label for="password" class="form-label">
                    <i class="icon icon-key"></i>
                    <mvc:message code="logon.password"/>
                </label>

                <mvc:password path="password" id="password" cssClass="form-control" showPassword="true" data-controls-group="login" autocomplete="current-password" data-action="password-change" />
            </div>

            <div class="col-12">
                <a class="form-text" href="<c:url value="/logon/reset-passwordRedesigned.action"/>">
                    <mvc:message code="logon.password.forgotten"/>
                </a>
            </div>
        </div>
    </div>

    <div class="col-12">
        <div class="row g-0">
            <div class="col-12">
                <button data-form-submit-static class="btn btn-light btn-lg w-100" data-controls-group="login" type="button">
                    <span class="text"><mvc:message code="logon.login"/></span>
                    <i class="icon icon-caret-right"></i>
                </button>
            </div>

            <div class="col-6">
                <mvc:message var="legalNoticeTitle" code="logon.hint" htmlEscape="true"/>
                <mvc:message var="legalNoticeMessage" code="logon.security" arguments="${supportMailAddress}" htmlEscape="true" />
                <a class="form-text" href="#" data-msg="${legalNoticeTitle}" data-msg-content="${legalNoticeMessage}">
                    <mvc:message code="logon.hint"/>
                </a>
            </div>

            <div class="col-6 d-flex justify-content-end">
                <a class="form-text" href="<c:url value="/serverstatus/externalViewRedesigned.action" />">
                    <mvc:message code="logon.show.status"/>
                    <i class="icon icon-external-link-alt"></i>
                </a>
            </div>
        </div>
    </div>
</mvc:form>
