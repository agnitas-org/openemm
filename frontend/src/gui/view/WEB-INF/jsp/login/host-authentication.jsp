<%@ page pageEncoding="UTF-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="backToLogin" value="true" scope="request" />

<mvc:form servletRelativeAction="/logon/authenticate-host.action" data-form-focus="authenticationCode" modelAttribute="form" cssClass="row g-3">
    <div class="col-12">
        <label class="form-label">
            <i class="icon icon-unlock"></i>
            <mvc:message code="logon.hostauth.code"/>
        </label>

        <mvc:text path="authenticationCode" cssClass="form-control" maxlength="20"/>
    </div>

    <div class="col-12">
        <div class="form-check form-switch">
            <mvc:checkbox path="trustedDevice" id="trustedDevice" value="true" cssClass="form-check-input" role="switch"/>
            <label class="form-label form-check-label" for="trustedDevice">
                <mvc:message code="logon.hostauth.trustDevice"/>
            </label>
        </div>
    </div>

    <div class="col-12">
        <button data-form-submit-static class="btn btn-light btn-lg w-100" type="button">
            <span class="text"><mvc:message code="logon.hostauth.authenticate"/></span>
            <i class="icon icon-caret-right"></i>
        </button>
    </div>
</mvc:form>
