<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.ecs.EcsModeType" %>
<%@ page import="com.agnitas.emm.core.mobile.bean.DeviceClass" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

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

<c:set var="isMailingGrid" value="${not empty templateId and templateId gt 0}" scope="request"/>

<mvc:form servletRelativeAction="/mailing/${mailing.id}/heatmap/view.action" data-form="resource" modelAttribute="form">
    <c:set var="formHeaderActions">
        <c:if test="${form.recipientId > 0}">
            <c:url var="exportUrl" value="/mailing/${mailing.id}/heatmap/export.action"/>
            <li>
                <a href="#" class="link"
                   data-tooltip="<mvc:message code='export.message.pdf'/>"
                   data-prevent-load
                   data-form-url="${exportUrl}"
                   data-form-submit-static>
                    <i class="icon icon-cloud-download"></i> <mvc:message code="Export"/>
                </a>
            </li>
        </c:if>

        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                <i class="icon icon-eye"></i>
                <span class="text"><mvc:message code="default.View"/></span>
                <i class="icon icon-caret-down"></i>
            </a>
            <ul class="dropdown-menu">
                <li class="dropdown-header"><mvc:message code="ecs.ViewMode"/></li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="viewMode" value="${EcsModeType.GROSS_CLICKS.id}"/>
                        <span class="label-text"><mvc:message code="${EcsModeType.GROSS_CLICKS.msgKey}"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="viewMode" value="${EcsModeType.NET_CLICKS.id}"/>
                        <span class="label-text"><mvc:message code="${EcsModeType.NET_CLICKS.msgKey}"/></span>
                    </label>
                </li>

                <li>
                    <label class="label">
                        <mvc:radiobutton path="viewMode" value="${EcsModeType.PURE_MAILING.id}"/>
                        <span class="label-text"><mvc:message code="${EcsModeType.PURE_MAILING.msgKey}"/></span>
                    </label>
                </li>

                <li class="divider"></li>

                <li class="dropdown-header"><mvc:message code="recipient.deviceType"/></li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="deviceType" value="0"/>
                        <span class="label-text"><mvc:message code="report.total"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="deviceType" value="${DESKTOP_DEVICE}"/>
                        <span class="label-text"><mvc:message code="predelivery.desktop"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="deviceType" value="${MOBILE_DEVICE}"/>
                        <span class="label-text"><mvc:message code="Mobile"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="deviceType" value="${TABLET_DEVICE}"/>
                        <span class="label-text"><mvc:message code="report.device.tablet"/></span>
                    </label>
                </li>
                <li>
                    <label class="label">
                        <mvc:radiobutton path="deviceType" value="${SMARTTV_DEVICE}"/>
                        <span class="label-text"><mvc:message code="report.device.smarttv"/></span>
                    </label>
                </li>

                <li class="divider"></li>
                <li>
                    <p>
                        <button type="button" class="btn btn-block btn-primary btn-regular" data-form-submit>
                            <i class="icon icon-refresh"></i>
                            <mvc:message code="button.Refresh"/>
                        </button>
                    </p>
                </li>
            </ul>
        </li>
    </c:set>
    <tiles:insertTemplate template="/WEB-INF/jsp/mailing/template.jsp">
        <c:if test="${isMailingGrid}">
            <tiles:putAttribute name="header" type="string">
                <ul class="tile-header-nav">
                    <!-- Tabs BEGIN -->
                    <tiles:insertTemplate template="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
                    <!-- Tabs END -->
                </ul>
                <ul class="tile-header-actions">${formHeaderActions}</ul>
            </tiles:putAttribute>
        </c:if>

        <tiles:putAttribute name="content" type="string">
            <c:set var="formContent">
                <div class="mailing-preview-header">
                    <div class="form-group">
                        <div class="col-sm-3 col-xs-12">
                            <label for="selectedRecipient" class="control-label">
                                <mvc:message code="Recipient"/></label>
                        </div>
                        <div class="col-sm-9 col-xs-12">
                            <mvc:select path="recipientId"
                                        cssClass="form-control" id="selectedRecipient"
                                        data-form-submit="">
                                <c:forEach items="${heatmapRecipients}" var="recipient">
                                    <mvc:option value="${recipient.key}">${recipient.value}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-3 col-xs-12">
                            <label for="colorDescription" class="control-label">
                                <mvc:message code="ecs.ColorCoding"/>
                            </label>
                        </div>

                        <div id="colorDescription" class="col-sm-9 col-xs-12">
                            <div class="form-control form-control-unstyled">
                                <ul class="list-floated list-spaced">
                                    <c:forEach var="color" items="${rangeColors}" varStatus="rowCounter">
                                        <li>
                                            <i class="icon icon-circle" style="color:${color.color};"></i>
                                            <mvc:message code="Heatmap.max"/>&nbsp;${color.rangeEnd}%
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <c:if test="${form.recipientId > 0}">
                    <div class="${isMailingGrid ? 'tile-content-padded' : 'mailing-preview-wrapper'}">
                        <div>
                            <c:url var="heatmapURL" value="/mailing/${mailing.id}/heatmap/preview.action">
                                <c:param name="recipientId" value="${form.recipientId}"/>
                                <c:param name="viewMode" value="${form.viewMode}"/>
                                <c:param name="deviceType" value="${form.deviceType}"/>
                            </c:url>
                            <div class="mailing-preview-scroller center-block">
                                <iframe src="${heatmapURL}" id="ecs_frame"
                                        class="mailing-preview-frame js-simple-iframe"
                                        data-height-extra="20"
                                        data-max-width="${previewWidth}"
                                        style="width: ${previewWidth}px"></iframe>
                            </div>
                        </div>
                    </div>
                </c:if>

                <div class="tile-content-padded">
                    <p><mvc:message code="ecs.Heatmap.description"/></p>
                </div>
            </c:set>

            <c:choose>
                <c:when test="${not isMailingGrid}">
                    <div class="tile">
                        <div class="tile-header">
                            <h2 class="headline">${mailing.shortname}</h2>
                            <ul class="tile-header-actions">${formHeaderActions}</ul>
                        </div>
                        <div class="tile-content">
                            ${formContent}
                        </div>
                    </div>
                </c:when>

                <c:otherwise>
                    ${formContent}
                </c:otherwise>
            </c:choose>

        </tiles:putAttribute>
    </tiles:insertTemplate>

</mvc:form>
