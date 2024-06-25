<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>
<%--@elvariable id="progressPercentage" type="java.lang.Integer"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>

<mvc:form servletRelativeAction="/export/${id}/evaluate.action" modelAttribute="exportForm" data-form="loading">
    <mvc:hidden path="exportStartTime"/>

    <mvc:hidden path="shortname"/>
    <mvc:hidden path="description"/>
    <mvc:hidden path="targetId"/>
    <mvc:hidden path="mailinglistId"/>
    <mvc:hidden path="userType"/>
    <mvc:hidden path="userStatus"/>
    <mvc:hidden path="separator"/>
    <mvc:hidden path="delimiter"/>
    <mvc:hidden path="charset"/>
    <mvc:hidden path="alwaysQuote"/>
    <mvc:hidden path="exportStartTime"/>
    <mvc:hidden path="dateFormat"/>
    <mvc:hidden path="dateTimeFormat"/>
    <mvc:hidden path="timezone"/>
    <mvc:hidden path="decimalSeparator"/>
    <mvc:hidden path="locale"/>
    <mvc:hidden path="timestampStart"/>
    <mvc:hidden path="timestampEnd"/>
    <mvc:hidden path="timestampLastDays"/>
    <mvc:hidden path="timestampIncludeCurrentDay"/>
    <mvc:hidden path="creationDateStart"/>
    <mvc:hidden path="creationDateEnd"/>
    <mvc:hidden path="creationDateLastDays"/>
    <mvc:hidden path="creationDateIncludeCurrentDay"/>
    <mvc:hidden path="timeLimitsLinkedByAnd"/>
    <mvc:hidden path="mailinglistBindStart"/>
    <mvc:hidden path="mailinglistBindEnd"/>
    <mvc:hidden path="mailinglistBindLastDays"/>
    <mvc:hidden path="mailinglistBindIncludeCurrentDay"/>
    <mvc:hidden path="mailinglists"/>
    <mvc:hidden path="userColumns"/>
    <c:forEach var="col" items="${exportForm.customColumns}" varStatus="status">
        <mvc:hidden path="customColumns[${status.index}].dbColumn"/>
        <mvc:hidden path="customColumns[${status.index}].defaultValue"/>
    </c:forEach>

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
