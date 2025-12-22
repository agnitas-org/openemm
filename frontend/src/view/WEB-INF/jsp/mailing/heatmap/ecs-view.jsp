<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.ecs.EcsModeType" %>
<%@ page import="com.agnitas.emm.core.mobile.bean.DeviceClass" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="form" type="com.agnitas.emm.ecs.form.EcsHeatmapForm"--%>
<%--@elvariable id="heatmapRecipients" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>
<%--@elvariable id="rangeColors" type="java.util.List<com.agnitas.ecs.backend.beans.ClickStatColor>"--%>
<%--@elvariable id="previewWidth" type="com.agnitas.ecs.EcsPreviewSize"--%>

<c:set var="DESKTOP_DEVICE" value="<%= DeviceClass.DESKTOP.getId() %>"/>
<c:set var="MOBILE_DEVICE" value="<%= DeviceClass.MOBILE.getId() %>"/>
<c:set var="TABLET_DEVICE" value="<%= DeviceClass.TABLET.getId() %>"/>
<c:set var="SMARTTV_DEVICE" value="<%= DeviceClass.SMARTTV.getId() %>"/>

<c:set var="MODE_TYPES" value="<%= EcsModeType.values() %>"/>

<mvc:form cssClass="tiles-container" servletRelativeAction="/mailing/${mailing.id}/heatmap/view.action" data-form="resource" modelAttribute="form">
    <div class="tile">
        <div class="tile-header flex-wrap border-bottom">
            <h1 class="tile-title text-truncate"><mvc:message code="ecs.Heatmap" /></h1>

            <div class="tile-controls">
                <c:if test="${form.recipientId > 0}">
                    <c:url var="exportUrl" value="/mailing/${mailing.id}/heatmap/export.action"/>
                    <a href="#" class="btn btn-icon btn-secondary" data-tooltip="<mvc:message code='export.message.pdf'/>"
                       data-prevent-load data-form-url="${exportUrl}" data-form-submit-static>
                        <i class="icon icon-file-pdf"></i>
                    </a>
                </c:if>

                <div class="dropdown">
                    <button class="btn btn-sm-horizontal btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                        <i class="icon icon-eye"></i>
                        <mvc:message code="default.View"/>
                    </button>

                    <div class="dropdown-menu">
                        <div class="row g-2">
                            <div class="col-12">
                                <label for="view-mode" class="form-label"><mvc:message code="ecs.ViewMode"/></label>

                                <mvc:select id="view-mode" path="viewMode" cssClass="form-control" data-form-submit="">
                                    <c:forEach var="modeType" items="${MODE_TYPES}">
                                        <mvc:option value="${modeType.id}"><mvc:message code="${modeType.msgKey}"/></mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </div>

                            <div class="col-12">
                                <label for="device-type" class="form-label"><mvc:message code="recipient.deviceType"/></label>

                                <mvc:select id="device-type" path="deviceType" cssClass="form-control" data-form-submit="">
                                    <mvc:option value="0"><mvc:message code="report.total"/></mvc:option>
                                    <mvc:option value="${DESKTOP_DEVICE}"><mvc:message code="predelivery.desktop"/></mvc:option>
                                    <mvc:option value="${MOBILE_DEVICE}"><mvc:message code="Mobile"/></mvc:option>
                                    <mvc:option value="${TABLET_DEVICE}"><mvc:message code="report.device.tablet"/></mvc:option>
                                    <mvc:option value="${SMARTTV_DEVICE}"><mvc:message code="report.device.smarttv"/></mvc:option>
                                </mvc:select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-3 w-100 mb-2">
                <div class="col">
                    <label for="selectedRecipient" class="form-label"><mvc:message code="Recipient"/></label>
                    <mvc:select id="selectedRecipient" path="recipientId" cssClass="form-control" data-form-submit="">
                        <c:forEach items="${heatmapRecipients}" var="recipient">
                            <mvc:option value="${recipient.key}">${recipient.value}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div class="col-auto d-flex flex-column">
                    <label class="form-label">
                        <mvc:message code="ecs.ColorCoding"/>
                    </label>

                    <div class="d-flex gap-2 flex-grow-1">
                        <c:forEach var="color" items="${rangeColors}" varStatus="rowCounter">
                            <div class="hstack gap-1">
                                <span class="square-badge" style="background:${color.color};"></span>
                                <span><mvc:message code="Heatmap.max"/>&nbsp;${color.rangeEnd}%</span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <div class="tile-body js-scrollable" data-controller="iframe-progress" data-initializer="iframe-progress">
            <c:if test="${form.recipientId > 0}">
                <div id="preview-progress" class="progress loop w-100" style="display:none;"></div>

                <div class="flex-center">
                    <div class="flex-center flex-grow-1">
                        <c:url var="heatmapURL" value="/mailing/${mailing.id}/heatmap/preview.action">
                            <c:param name="recipientId" value="${form.recipientId}"/>
                            <c:param name="viewMode" value="${form.viewMode}"/>
                            <c:param name="deviceType" value="${form.deviceType}"/>
                        </c:url>

                        <iframe src="${heatmapURL}" class="default-iframe js-simple-iframe" data-max-width="${previewWidth}"
                                style="width: ${previewWidth}px; height: 0">
                        </iframe>
                    </div>
                </div>
            </c:if>
        </div>

        <div class="tile-footer flex-center">
            <div class="flex-center">
                <p><mvc:message code="ecs.Heatmap.description"/></p>
            </div>
        </div>
    </div>
</mvc:form>
