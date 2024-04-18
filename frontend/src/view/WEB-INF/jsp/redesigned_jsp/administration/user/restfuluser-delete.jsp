<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<mvc:form servletRelativeAction="/restfulUser/${adminForm.adminID}/delete.action" modelAttribute="adminForm" data-form="resource" cssClass="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="settings.admin.delete"/></h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <p><mvc:message code="settings.admin.delete.question" arguments="${adminForm.username}"/></p>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1 gap-1">
                    <i class="icon icon-trash-alt"></i>
                    <span class="text"><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </div>
    </div>
</mvc:form>
