<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>
<%--@elvariable id="stepsLength" type="java.lang.Integer"--%>
<%--@elvariable id="completedPercent" type="java.lang.Integer"--%>
<%--@elvariable id="currentProgressStatus" type="com.agnitas.emm.core.imports.beans.ImportItemizedProgress"--%>
<%--@elvariable id="errorOccurred" type="java.lang.Boolean"--%>
<%--@elvariable id="profileId" type="java.lang.Integer"--%>

<c:choose>
    <c:when test="${errorOccurred}">
        <c:url var="uploadingErrorImageSvg" value="/assets/core/images/facelift/msgs_msg-uploading-error.svg"/>

        <div class="modal modal-alert" tabindex="-1">
            <div class="modal-dialog modal-fullscreen-lg-down">
                <mvc:form cssClass="modal-content" servletRelativeAction="/recipient/import/execute.action" data-form="resource">
                    <div class="modal-header justify-content-center">
                        <img alt="" src="${uploadingErrorImageSvg}">
                    </div>

                    <div class="modal-body d-flex flex-column gap-3">
                        <h3><mvc:message code="default.loading.stopped"/></h3>
                        <p><mvc:message code="error.importErrorsOccured"/></p>
                    </div>

                    <div class="modal-footer">
                        <c:url var="previewLink" value="/recipient/import/preview.action">
                            <c:param name="profileId" value="${profileId}" />
                        </c:url>

                        <a href="${previewLink}" class="btn btn-inverse flex-none" type="button">
                            <i class="icon icon-angle-left"></i>
                            <span class="text"><mvc:message code="button.Back"/></span>
                        </a>
                        <button class="btn btn-primary" type="button" data-form-submit data-bs-dismiss="modal">
                            <i class="icon icon-sync"></i>
                            <span class="text"><mvc:message code="button.TryAgain"/></span>
                        </button>
                    </div>
                </mvc:form>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div id="evaluate-loader-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
            <div class="modal-dialog">
                <mvc:form cssClass="modal-content" servletRelativeAction="/recipient/import/execute.action" data-form="loading" modelAttribute="form">
                    <mvc:hidden path="profileId" />
                    <div class="modal-body">
                        <img alt="" src='<c:url value="/assets/core/images/facelift/msgs_msg-uploading.svg"/>'>
                        <c:choose>
                            <c:when test="${currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                                <p><mvc:message code="import.process"/></p>
                            </c:when>
                            <c:otherwise>
                                <p><mvc:message code="import.progress.detailedItem.${currentProgressStatus}"/></p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="modal-footer flex-column">
                        <div class="progress w-100">
                            <div class="progress-bar-white-bg"></div>
                            <div class="progress-bar"
                                 role="progressbar"
                                 aria-valuenow="${currentProgressStatus.parentStep.ordinal()}"
                                 aria-valuemin="0"
                                 aria-valuemax="${stepsLength}"
                                 style="width: ${(currentProgressStatus.parentStep.ordinal() + 1) / stepsLength * 100}%"></div>
                            <div class="progress-bar-primary-bg"></div>
                            <div class="percentage">
                                <mvc:message code="import.steps" arguments="${[currentProgressStatus.parentStep.ordinal() + 1, stepsLength]}"/>
                                : <mvc:message code="import.progress.step.${currentProgressStatus.parentStep}" />
                            </div>
                        </div>

                        <c:if test="${currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                            <div class="progress w-100">
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
                        </c:if>
                    </div>
                </mvc:form>
            </div>
        </div>
    </c:otherwise>
</c:choose>
