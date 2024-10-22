<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="progressPercentage" type="java.lang.Integer"--%>

<div id="evaluate-loader-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <mvc:form cssClass="modal-content" servletRelativeAction="/administration/table/${tableId}/export/${id}/run.action" modelAttribute="exportForm" data-form="loading">
            <div class="modal-body">
                <img alt="Export" src='<c:url value="/assets/core/images/facelift/msgs_msg-uploading.svg"/>'>
                <p><mvc:message code="export.process"/></p>
            </div>
            <div class="modal-footer">
                <div class="progress flex-grow-1">
                    <div class="progress-bar-white-bg"></div>
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="${progressPercentage}"
                         aria-valuemin="0"
                         aria-valuemax="100"
                         style="width: ${progressPercentage}%"></div>
                    <div class="progress-bar-primary-bg"></div>
                    <div class="percentage">${progressPercentage}%</div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
