<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientSimpleActionForm"--%>

<c:choose>
    <c:when test="${fn:trim(form.shortname) eq ''}">
        <c:set var="recipientIdentifier" value="${fn:escapeXml(form.email)}"/>
    </c:when>
    <c:otherwise>
        <c:set var="recipientIdentifier" value="${fn:escapeXml(form.shortname)}"/>
    </c:otherwise>
</c:choose>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" method="DELETE" servletRelativeAction="/recipient/delete.action" modelAttribute="form">
            <mvc:hidden path="id"/>
            <mvc:hidden path="email"/>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="recipient.RecipientDelete"/></h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="recipient.delete.question" arguments="${recipientIdentifier}"/>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
