<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.action" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="system-tile-header">
    <tiles:insertAttribute name="header"/>
</div>

<div class="system-tile-content">
    <mvc:form servletRelativeAction="/logon/reset-password.action" method="POST" modelAttribute="form" data-form="static" data-form-focus="username">
        <div class="form-group" data-field="required">
            <div class="col-sm-4">
                <label for="username" class="control-label">
                    <i class="icon icon-user"></i>
                    <mvc:message code="logon.username"/>
                </label>
            </div>
            <div class="col-sm-8">
                <mvc:text path="username" id="username" cssClass="form-control" maxlength="180" data-field-required=""/>
            </div>
        </div>

        <div class="form-group" data-field="required">
            <div class="col-sm-4">
                <label for="email" class="control-label">
                    <i class="icon icon-envelope"></i>
                    <mvc:message code="mailing.MediaType.0"/>
                </label>
            </div>
            <div class="col-sm-8">
                <mvc:text path="email" id="email" cssClass="form-control" maxlength="200" data-field-required=""/>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="button" class="btn btn-primary btn-large btn-block" data-form-submit="">
                    <mvc:message code="logon.password_reset"/> <i class="icon icon-angle-right"></i>
                </button>
            </div>
        </div>
    </mvc:form>
</div>
