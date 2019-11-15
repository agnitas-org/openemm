<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<div class="system-tile-header">
    <tiles:insert attribute="header"/>
</div>

<div class="system-tile-content">
    <mvc:form servletRelativeAction="/logon/reset-password.action" method="POST" modelAttribute="form" data-form="static"
              data-form-focus="password"
              data-validator="logon-new-password/form">

        <mvc:hidden path="username"/>
        <mvc:hidden path="token"/>

        <div class="form-group">
            <div class="col-sm-4">
                <label for="password" class="control-label"><i class="icon icon-key"></i>
                    <s:message code="password.new"/>
                    <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                </label>
            </div>
            <div class="col-sm-8">
                <mvc:password path="password" id="password" cssClass="form-control" />
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label for="password-repeat" class="control-label"><i class="icon icon-key"></i> <s:message code="password.repeat"/></label>
            </div>
            <div class="col-sm-8">
                <input type="password" id="password-repeat" class="form-control" />
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <button type="button" class="btn btn-primary btn-large btn-block" data-form-submit="">
                    <s:message code="password.change.now"/> <i class="icon icon-angle-right"></i>
                </button>
            </div>
        </div>
    </mvc:form>
</div>
