<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>
<%--@elvariable id="progressPercentage" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<div id="evaluate-loader-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <mvc:form cssClass="modal-content" servletRelativeAction="/export/${id}/evaluate.action" modelAttribute="exportForm" data-form="loading">
            <div class="modal-header">
                <svg><use href="<c:url value="/assets/core/images/facelift/sprite.svg"/>#msgs_msg-uploading"></use></svg>
                <h1><mvc:message code="export.process"/></h1>
            </div>

            <mvc:hidden path="exportStartTime"/>
            <mvc:hidden path="inProgress" value="true"/>

            <div class="modal-body">
                <div class="progress">
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="${progressPercentage}"
                         aria-valuemin="0"
                         aria-valuemax="100"
                         style="width: ${progressPercentage}%"></div>
                    <div class="percentage">${progressPercentage}%</div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
