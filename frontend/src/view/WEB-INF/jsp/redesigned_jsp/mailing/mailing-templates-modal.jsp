<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.HttpUtils" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="templateMailingBases" type="java.util.List<org.agnitas.beans.MailingBase>"--%>
<%--@elvariable id="template" type="org.agnitas.beans.MailingBase"--%>

<c:set var="IMAGE_PATH_NO_PREVIEW" value="<%= HttpUtils.IMAGE_PATH_NO_PREVIEW %>"/>

<div class="modal modal-adaptive" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-dialog-full-height">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/templates.action?keepForward=${workflowId > 0}" method="GET">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Templates"/></h1>
                <div class="modal-controls">
                    <emm:HideByPermission token="mailing.settings.hide">
                        <c:url var="createNewUtl" value="/mailing/new.action"/>
                        <button type="button" class="btn btn-inverse fw-semibold" data-form-url="${createNewUtl}" data-form-set="templateId: 0, keepForward: ${workflowId > 0}" data-form-submit-static>
                            <span class="text"><mvc:message code="button.template.without"/></span>
                            <i class="icon icon-angle-right"></i>
                        </button>
                    </emm:HideByPermission>
                    <input type="checkbox" id="switch-templates-view" class="icon-switch" data-preview-table="#release-grid-templates-table" checked>
                    <label for="switch-templates-view" class="icon-switch__label">
                        <i class="icon icon-image"></i>
                        <i class="icon icon-th-list"></i>
                    </label>
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
            </div>
            <div class="modal-body">
                <div class="table-box" style="width: 90vw">
                    <div class="table-scrollable">
                        <display:table htmlId="release-grid-templates-table" class="table table-hover table-rounded js-table" id="template" list="${templateMailingBases}"
                                       requestURI="/mailing/templates.action?keepForward=${workflowId > 0}" excludedParams="*" length="${fn:length(templateMailingBases)}" sort="list">

                            <c:set var="noNumberOfRowsSelect" value="true" />
                            <%@ include file="../displaytag/displaytag-properties.jspf" %>

                            <c:choose>
                                <c:when test="${template.onlyPostType}">
                                    <c:url var="thumbnailSrc" value="assets/core/images/facelift/post_thumbnail.jpg"/>
                                </c:when>
                                <c:when test="${template.previewComponentId eq 0}">
                                    <c:url var="thumbnailSrc" value="/assets/core/images/facelift/no_preview.png"/>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="thumbnailSrc" value="/sc?compID=${template.previewComponentId}"/>
                                </c:otherwise>
                            </c:choose>
                            <display:column headerClass="hidden" class="table-preview-visible w-100">
                                <div class="table-cell__preview-wrapper">
                                    <img src="${thumbnailSrc}" alt="" class="table-cell__preview" data-no-preview-src="${IMAGE_PATH_NO_PREVIEW}">
                                </div>
                            </display:column>

                            <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" property="shortname" />

                            <c:set value="${template.creationDate}" var="creationDateTimeStamp" />
                            <fmt:parseDate value="${creationDateTimeStamp}" var="creationDateParsed" pattern="yyyy-MM-dd HH:mm:ss" />
                            <fmt:formatDate value="${creationDateParsed}" var="creationDateFormatted" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
                            <display:column titleKey="default.creationDate" sortable="false" class="table__cell-sub-info">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-calendar-alt mobile-visible table-preview-visible"></i>
                                    <span>${creationDateFormatted}</span>
                                </div>

                                <c:url var="newMailingUrl" value="/mailing/new.action">
                                    <c:param name="keepForward">${workflowId > 0}</c:param>
                                    <c:param name="templateId">${template.id}</c:param>
                                </c:url>
                                <a href="${newMailingUrl}" class="hidden" data-view-row="page"></a>
                            </display:column>
                        </display:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
