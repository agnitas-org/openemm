<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring" prefix="mvc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="simpleActionForm" type="com.agnitas.web.forms.SimpleActionForm"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" method="POST" servletRelativeAction="/target/delete.action" modelAttribute="simpleActionForm">
            <mvc:hidden path="id"/>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="target.Delete"/></h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="target.delete.question2" arguments="${simpleActionForm.shortname}"/>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
