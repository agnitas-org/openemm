<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="stepsLength" type="java.lang.Integer"--%>
<%--@elvariable id="completedPercent" type="java.lang.Integer"--%>
<%--@elvariable id="currentProgressStatus" type="com.agnitas.emm.core.imports.beans.ImportItemizedProgress"--%>

<div id="evaluate-loader-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <mvc:form cssClass="modal-content" servletRelativeAction="/recipient/import/execute.action" data-form="loading" modelAttribute="form">
            <mvc:hidden path="profileId" />
            <div class="modal-header">
                <svg><use href="<c:url value="/assets/core/images/facelift/sprite.svg"/>#msgs_msg-uploading"></use></svg>
                <h1>
                    <c:choose>
                        <c:when test="${currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                            <mvc:message code="import.process"/>
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="import.progress.detailedItem.${currentProgressStatus}"/>
                        </c:otherwise>
                    </c:choose>
                </h1>
            </div>
            <div class="modal-body d-flex flex-column gap-3">
                <div class="progress w-100">
                    <div class="progress-bar"
                         role="progressbar"
                         aria-valuenow="${currentProgressStatus.parentStep.ordinal()}"
                         aria-valuemin="0"
                         aria-valuemax="${stepsLength}"
                         style="width: ${(currentProgressStatus.parentStep.ordinal() + 1) / stepsLength * 100}%"></div>
                    <div class="percentage">
                        <mvc:message code="import.steps" arguments="${[currentProgressStatus.parentStep.ordinal() + 1, stepsLength]}"/>
                        : <mvc:message code="import.progress.step.${currentProgressStatus.parentStep}" />
                    </div>
                </div>

                <c:if test="${currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                    <div class="progress w-100">
                        <div class="progress-bar"
                             role="progressbar"
                             aria-valuenow="${completedPercent}"
                             aria-valuemin="0"
                             aria-valuemax="100"
                             style="width: ${completedPercent}%"></div>
                        <div class="percentage">${completedPercent}%</div>
                    </div>
                </c:if>
            </div>
        </mvc:form>
    </div>
</div>
