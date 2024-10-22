<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="evaluate-loader-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <mvc:form cssClass="modal-content" servletRelativeAction="/recipient/import/wizard/run.action" data-form="loading">
            <div class="modal-body">
                <img alt="" src='<c:url value="/assets/core/images/facelift/msgs_msg-uploading.svg"/>'>
                <p><mvc:message code="import.process"/></p>
            </div>
            <div class="modal-footer">
                <div class="progress flex-grow-1">
                    <div class="progress-bar-white-bg"></div>
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="${completedPercent}"
                         aria-valuemin="0"
                         aria-valuemax="100"
                         style="width: ${completedPercent}%"></div>
                    <div class="progress-bar-primary-bg"></div>
                    <div class="percentage">${completedPercent}%</div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
