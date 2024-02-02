<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
        <c:url var="uploadingErrorImagePng" value="assets/core/images/facelift/msgs_msg-uploading-error.png"/>

        <mvc:form servletRelativeAction="/recipient/import/execute.action" data-form="resource">
            <div class="msg-tile msg-tile-alert">
                <div class="msg-tile-header">
                    <img alt="" src="${uploadingErrorImageSvg}" onerror="this.onerror=null; this.src='${uploadingErrorImagePng}'">
                </div>
                <div class="msg-tile-content">
                    <h3><mvc:message code="default.loading.stopped"/></h3>
                    <p><mvc:message code="error.importErrorsOccured"/></p>
                    <div class="btn-block">
                        <div class="btn-group">
                            <c:url var="previewLink" value="/recipient/import/preview.action">
                                <c:param name="profileId" value="${profileId}" />
                            </c:url>

                            <a href="${previewLink}" class="btn btn-primary btn-regular" type="button">
                                <i class="icon icon-angle-left"></i>
                                <span class="text"><mvc:message code="button.Back"/></span>
                            </a>
                            <button class="btn btn-regular btn-warning" type="button" data-form-submit="">
                                <i class="icon icon-refresh"></i>
                                <span class="text"><mvc:message code="button.TryAgain"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </mvc:form>
    </c:when>
    <c:otherwise>
        <c:url var="uploadingImageSvg" value="/assets/core/images/facelift/msgs_msg-uploading.svg"/>
        <c:url var="uploadingImagePng" value="assets/core/images/facelift/msgs_msg-uploading.png"/>

        <mvc:form servletRelativeAction="/recipient/import/execute.action" data-form="loading" modelAttribute="form">
            <mvc:hidden path="profileId" />

            <div class="msg-tile msg-tile-primary">
                <div class="msg-tile-header">
                    <img alt="" src="${uploadingImageSvg}" onerror="this.onerror=null; this.src='${uploadingImagePng}'">
                </div>
                <div class="msg-tile-content">
                    <h3><mvc:message code="upload.data"/></h3>
                    <div class="progress thin-progress">
                        <div class="progress-bar" role="progressbar" aria-valuenow="${currentProgressStatus.parentStep.ordinal()}" aria-valuemin="0" aria-valuemax="${stepsLength}" style="width: ${(currentProgressStatus.parentStep.ordinal() + 1) / stepsLength * 100}%">
                        </div>
                    </div>

                    <p class="progress-bottom-desc">
                        <mvc:message code="import.steps" arguments="${[currentProgressStatus.parentStep.ordinal() + 1, stepsLength]}"/>
                        : <mvc:message code="import.progress.step.${currentProgressStatus.parentStep}" />
                    </p>

                    <c:choose>
                        <c:when test="${currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                            <div class="progress">
                                <div class="progress-bar" role="progressbar" aria-valuenow="${completedPercent}" aria-valuemin="0" aria-valuemax="100" style="width: ${completedPercent}%">
                                    <mvc:message code="import.csv_importing_data"/> ${completedPercent}%
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p>Details: <mvc:message code="import.progress.detailedItem.${currentProgressStatus}"/></p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </mvc:form>
    </c:otherwise>
</c:choose>
