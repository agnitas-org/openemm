<%@ page contentType="text/html;charset=UTF-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<mvc:form cssClass="row g-3" servletRelativeAction="/logon/reset-passwordRedesigned.action" method="POST" modelAttribute="form"
          data-form="static" data-form-focus="password" data-validator="logon-new-password/form">

    <mvc:hidden path="username"/>
    <mvc:hidden path="token"/>

    <div class="col-12">
        <label for="password" class="form-label">
            <i class="icon icon-key"></i>
            <mvc:message code="password.new"/>
            <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml"></a>
        </label>

        <mvc:password path="password" id="password" cssClass="form-control" />
    </div>

    <div class="col-12">
        <label for="password-repeat" class="form-label">
            <i class="icon icon-key"></i>
            <mvc:message code="password.repeat"/>
        </label>
        <input type="password" id="password-repeat" class="form-control" />
    </div>

    <div class="col-12">
        <button type="button" class="btn btn-light btn-lg w-100" data-form-submit="">
            <span class="text"><mvc:message code="password.change.now"/></span>
            <i class="icon icon-caret-right"></i>
        </button>
    </div>
</mvc:form>
