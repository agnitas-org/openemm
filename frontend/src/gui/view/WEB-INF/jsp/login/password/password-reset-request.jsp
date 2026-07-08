<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="backToLogin" value="true" scope="request" />

<mvc:form cssClass="row g-3" servletRelativeAction="/logon/reset-password.action" method="POST" modelAttribute="form"
          data-form="static" data-form-focus="username">

    <div class="col-12">
        <label for="username" class="form-label">
            <i class="icon icon-user"></i>
            <mvc:message code="logon.username"/>
        </label>

        <mvc:text path="username" id="username" cssClass="form-control" maxlength="180" data-field="required"/>
    </div>

    <div class="col-12">
        <label for="email" class="form-label">
            <i class="icon icon-envelope"></i>
            <mvc:message code="mailing.MediaType.0"/>
        </label>

        <mvc:text path="email" id="email" cssClass="form-control" maxlength="200" data-field="required"/>
    </div>

    <div class="col-12">
        <button type="button" class="btn btn-light btn-lg w-100" data-form-submit="">
            <span class="text"><mvc:message code="logon.password_reset"/></span>
            <i class="icon icon-caret-right"></i>
        </button>
    </div>
</mvc:form>
