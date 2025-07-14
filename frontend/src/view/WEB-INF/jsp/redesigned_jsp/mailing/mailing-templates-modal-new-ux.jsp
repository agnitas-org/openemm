<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.util.HttpUtils" %>
<%@ page import="com.agnitas.emm.util.SortDirection" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="templateMailingBases" type="java.util.List<com.agnitas.beans.MailingBase>"--%>
<%--@elvariable id="filter" type="com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter"--%>

<c:set var="IMAGE_PATH_NO_PREVIEW" value="<%= HttpUtils.IMAGE_PATH_NO_PREVIEW_NEW %>" />
<c:set var="templatesExist" value="${fn:length(templateMailingBases) gt 0}" />

<c:set var="isWorkflowDriven" value="${workflowId gt 0 or not empty workflowForwardParams}" />

<div id="template-selection-modal" class="modal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <mvc:form servletRelativeAction="/mailing/templates.action?keepForward=${isWorkflowDriven}" method="GET" modelAttribute="filter"
                  cssClass="modal-content" data-form="resource" data-resource-selector="#template-selection-modal">
            <div class="modal-header">
                <h1 class="modal-title">
                    <span><mvc:message code="mailing.grid.GridMailing.selectLayout" /></span>
                    <c:choose>
                        <c:when test="${filter.mediaType eq 0}">
                            <span class="status-badge mailing.mediatype.email badge--blue" data-tooltip="<mvc:message code="mailing.standard" />"></span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge mailing.mediatype.sms badge--cyan" data-tooltip="<mvc:message code="mailing.MediaType.4" />"></span>
                        </c:otherwise>
                    </c:choose>
                </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-header">
                <div class="input-group">
                    <mvc:message var="searchMsg" code="Search" />
                    <mvc:text path="name" cssClass="form-control" placeholder="${searchMsg}" />
                    <button class="btn btn-primary btn-icon" type="button" data-form-submit>
                        <i class="icon icon-search"></i>
                    </button>
                </div>

                <div class="input-group">
                    <span class="input-group-text"><mvc:message code="report.list.sort" /></span>
                    <mvc:select path="sort" cssClass="form-control js-select" data-form-submit="">
                        <mvc:option value="shortname"><mvc:message code="default.Name" /></mvc:option>
                        <mvc:option value="description"><mvc:message code="Description" /></mvc:option>
                        <mvc:option value="creation_date"><mvc:message code="default.creationDate" /></mvc:option>
                    </mvc:select>
                    <mvc:select path="order" cssClass="form-control js-select" data-form-submit="">
                        <c:forEach var="sortDirection" items="${SortDirection.values()}">
                            <mvc:option value="${sortDirection.id}">
                                <mvc:message code="${sortDirection.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="modal-body js-scrollable">
                <c:if test="${not templatesExist}">
                    <div class="notification-simple">
                        <i class="icon icon-info-circle"></i>
                        <span><mvc:message code="noResultsFound" /></span>
                    </div>
                </c:if>

                <mvc:hidden path="mediaType" />

                <c:forEach var="template" items="${templateMailingBases}">
                    <c:url var="newMailingUrl" value="/mailing/new.action">
                        <c:param name="keepForward">${isWorkflowDriven}</c:param>
                        <c:param name="templateId">${template.id}</c:param>
                    </c:url>

                    <c:choose>
                        <c:when test="${template.onlyPostType}">
                            <c:url var="thumbnailSrc" value="assets/core/images/facelift/post_thumbnail.jpg"/>
                        </c:when>
                        <c:when test="${template.previewComponentId eq 0}">
                            <c:url var="thumbnailSrc" value="${IMAGE_PATH_NO_PREVIEW}"/>
                        </c:when>
                        <c:otherwise>
                            <c:url var="thumbnailSrc" value="/sc?compID=${template.previewComponentId}"/>
                        </c:otherwise>
                    </c:choose>

                    <a href="#" data-form-url="${newMailingUrl}" data-form-submit data-bs-dismiss="modal" class="horizontal-card">
                        <div class="horizontal-card__header">
                            <img class="horizontal-card__thumbnail" src="${thumbnailSrc}"
                                 alt="Thumbnail" data-no-preview-src="${IMAGE_PATH_NO_PREVIEW}">
                        </div>
                        <div class="horizontal-card__body gap-4">
                            <p class="horizontal-card__title">${template.shortname}</p>

                            <c:if test="${not empty template.description}">
                                <p class="horizontal-card__subtitle">
                                    <i class="icon icon-file-alt" data-tooltip="<mvc:message code="Description" />"></i>
                                    <span class="text-truncate">${template.description}</span>
                                </p>
                            </c:if>

                            <p class="horizontal-card__subtitle">
                                <i class="icon icon-clock" data-tooltip="<mvc:message code="default.creationDate" />"></i>
                                <span class="text-truncate"><emm:formatDate value="${template.creationDate}" format="${adminDateTimeFormat}" /></span>
                            </p>
                        </div>
                    </a>
                </c:forEach>
            </div>

            <c:set var="isBackButtonAllowed" value="${not isWorkflowDriven or filter.mediaType eq 0}" />

            <div class="modal-footer modal-footer--nav ${isBackButtonAllowed ? '' : 'justify-content-end'}">
                <c:if test="${isBackButtonAllowed}">
                    <a href="<c:url value="/mailing/create.action?keepForward=${isWorkflowDriven}" />" class="btn btn-secondary" data-bs-dismiss="modal" data-confirm>
                        <i class="icon icon-angle-left"></i>
                        <span class="mobile-hidden"><mvc:message code="mailing.create.back" /></span>
                        <span class="mobile-visible"><mvc:message code="button.Back" /></span>
                    </a>
                </c:if>

                <emm:HideByPermission token="mailing.settings.hide">
                    <c:url var="newMailingUrl" value="/mailing/new.action">
                        <c:param name="keepForward">${isWorkflowDriven}</c:param>
                        <c:param name="templateId" value="0" />
                    </c:url>

                    <button type="button" class="btn btn-primary" data-form-url="${newMailingUrl}" data-form-submit data-bs-dismiss="modal">
                        <i class="icon icon-angle-right"></i>
                        <span><mvc:message code="button.template.without"/></span>
                    </button>
                </emm:HideByPermission>
            </div>
        </mvc:form>
    </div>
</div>
