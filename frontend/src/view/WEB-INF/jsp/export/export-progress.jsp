<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>
<%--@elvariable id="progressPercentage" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<mvc:form servletRelativeAction="/export/${id}/evaluate.action" modelAttribute="exportForm" data-form="loading">

    <mvc:hidden path="exportStartTime"/>
    <mvc:hidden path="inProgress" value="true"/>

    <div class="msg-tile msg-tile-primary">
        <div class="msg-tile-header">
            <c:url var="uploadingImageSvg" value="/assets/core/images/facelift/msgs_msg-uploading.svg"/>
            <c:url var="uploadingImagePng" value="assets/core/images/facelift/msgs_msg-uploading.png"/>
            <img alt="" src="${uploadingImageSvg}" onerror="this.onerror=null; this.src='${uploadingImagePng}'">
        </div>
        <div class="msg-tile-content">
            <h3><mvc:message code="export.data"/></h3>
            <div class="progress">
                <div class="progress-bar" role="progressbar" aria-valuenow="${progressPercentage}" aria-valuemin="0" aria-valuemax="100" style="width: ${progressPercentage}%">
                    <mvc:message code="export.csv_exporting_data"/> ${progressPercentage}%
                </div>
            </div>
        </div>
    </div>
</mvc:form>
