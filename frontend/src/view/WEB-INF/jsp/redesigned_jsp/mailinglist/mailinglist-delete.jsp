<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="mailinglistShortname" type="java.lang.String"--%>
<%--@elvariable id="sentMailingsCount" type="java.lang.Integer"--%>
<%--@elvariable id="affectedReportsCount" type="java.lang.Integer"--%>


<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailinglist/${id}/delete.action" method="DELETE">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="settings.mailinglist.delete"/>&nbsp;‘${mailinglistShortname}’</h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <ul>
                    <p><mvc:message code="mailinglist.delete.question"/></p>
                    <c:if test="${sentMailingsCount > 0}">
                        <p class="mt-3"><mvc:message code="mailinglist.count.sentMailings" arguments="${sentMailingsCount}"/></p>
                    </c:if>
                    <c:if test="${affectedReportsCount > 0}">
                        <p class="mt-3"><mvc:message code="mailinglist.count.reports" arguments="${affectedReportsCount}"/></p>
                    </c:if>
                </ul>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1" data-dismiss="modal">
                    <i class="icon icon-trash-alt"></i>
                    <b><mvc:message code="${sentMailingsCount > 0 or affectedReportsCount > 0 ? 'button.DeleteAnyway' : 'button.Delete'}"/></b>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
