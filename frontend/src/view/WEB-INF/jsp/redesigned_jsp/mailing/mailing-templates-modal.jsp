<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.HttpUtils" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="fmt"        uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="templateMailingBases" type="java.util.List<org.agnitas.beans.MailingBase>"--%>
<%--@elvariable id="template" type="org.agnitas.beans.MailingBase"--%>
<%--@elvariable id="mediaType" type="java.lang.Integer"--%>

<c:set var="IMAGE_PATH_NO_PREVIEW" value="<%= HttpUtils.IMAGE_PATH_NO_PREVIEW %>" />

<c:set var="templatesUrl" value="/mailing/templates.action?keepForward=${workflowId gt 0} "/>
<c:if test="${mediaType ne null}">
    <c:set var="templatesUrl" value="${templatesUrl}&mediaType=${mediaType}" />
</c:if>

<div class="modal modal-adaptive" tabindex="-1">
    <div class="modal-dialog modal-dialog-full-height">
        <mvc:form cssClass="modal-content" servletRelativeAction="${templatesUrl}" method="GET">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Templates"/></h1>
                <div class="modal-controls">
                    <emm:HideByPermission token="mailing.settings.hide">
                        <c:url var="createNewUrl" value="/mailing/new.action" />
                        <button type="button" class="btn btn-inverse fw-semibold" data-form-url="${createNewUrl}" data-form-set="templateId: 0, keepForward: ${workflowId gt 0}, mediaType: ${mediaType ne null ? mediaType : ''}" data-form-submit-static>
                            <span class="text"><mvc:message code="button.template.without"/></span>
                            <i class="icon icon-angle-right"></i>
                        </button>
                    </emm:HideByPermission>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
            </div>
            <div class="modal-body">
                <div class="table-wrapper" style="width: 90vw">
                    <div class="table-wrapper__header justify-content-end">
                        <div class="table-wrapper__controls">
                            <jsp:include page="../common/table/preview-switch.jsp">
                                <jsp:param name="selector" value="#release-grid-templates-table"/>
                            </jsp:include>
                            <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../common/table/entries-label.jsp">
                                <jsp:param name="totalEntries" value="${fn:length(templateMailingBases)}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <agnDisplay:table htmlId="release-grid-templates-table" class="table table-hover table--borderless js-table" id="template" list="${templateMailingBases}"
                                       requestURI="${templatesUrl}" excludedParams="*" length="${fn:length(templateMailingBases)}" sort="list">

                            <c:set var="noNumberOfRowsSelect" value="true" />
                            <%@ include file="../common/displaytag/displaytag-properties.jspf" %>

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
                            <agnDisplay:column headerClass="hidden" class="thumbnail-cell">
                                <img src="${thumbnailSrc}" alt="Thumbnail" data-no-preview-src="${IMAGE_PATH_NO_PREVIEW}">
                            </agnDisplay:column>

                            <agnDisplay:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" sortProperty="shortname">
                                <span>${template.shortname}</span>
                            </agnDisplay:column>

                            <c:set value="${template.creationDate}" var="creationDateTimeStamp" />
                            <fmt:parseDate value="${creationDateTimeStamp}" var="creationDateParsed" pattern="yyyy-MM-dd HH:mm:ss" />
                            <fmt:formatDate value="${creationDateParsed}" var="creationDateFormatted" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
                            <agnDisplay:column titleKey="default.creationDate" sortable="false" class="secondary-cell">
                                <i class="icon icon-calendar-alt"></i>
                                <span>${creationDateFormatted}</span>

                                <c:url var="newMailingUrl" value="/mailing/new.action">
                                    <c:param name="keepForward">${workflowId > 0}</c:param>
                                    <c:param name="templateId">${template.id}</c:param>
                                </c:url>
                                <a href="${newMailingUrl}" class="hidden" data-view-row="page"></a>
                            </agnDisplay:column>
                        </agnDisplay:table>
                    </div>
                    <div class="table-wrapper__footer"></div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
