<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.util.HttpUtils" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="templateMailingBases" type="java.util.List<com.agnitas.beans.MailingBase>"--%>
<%--@elvariable id="filter" type="com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter"--%>

<c:set var="IMAGE_PATH_NO_PREVIEW" value="<%= HttpUtils.IMAGE_PATH_NO_PREVIEW_NEW %>" />

<div class="modal modal-adaptive" tabindex="-1">
    <div class="modal-dialog modal-dialog-full-height">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/templates.action?keepForward=${workflowId gt 0}" method="GET" modelAttribute="filter" data-form="">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Templates"/></h1>
                <div class="modal-controls">
                    <emm:HideByPermission token="mailing.settings.hide">
                        <c:url var="createNewUrl" value="/mailing/new.action" />
                        <button type="button" class="btn btn-secondary fw-semibold" data-form-url="${createNewUrl}" data-form-set="templateId: 0, keepForward: ${workflowId gt 0}" data-form-submit-static>
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
                    <div class="table-wrapper__header">
                        <div class="table-wrapper__controls table-wrapper__controls--stretchable">
                            <mvc:hidden path="mediaType" />

                            <div class="input-group">
                                <mvc:message var="searchMsg" code="Search" />
                                <mvc:text path="name" cssClass="form-control" placeholder="${searchMsg}" />
                                <button class="btn btn-primary btn-icon" type="button" data-form-submit>
                                    <i class="icon icon-search"></i>
                                </button>
                            </div>
                        </div>
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
                        <emm:table id="release-grid-templates-table" cssClass="table table-hover table--borderless js-table" var="template" modelAttribute="templateMailingBases">

                            <%--@elvariable id="template" type="com.agnitas.beans.MailingBase"--%>

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
                            <emm:column headerClass="hidden" cssClass="thumbnail-cell">
                                <img src="${thumbnailSrc}" alt="Thumbnail" data-no-preview-src="${IMAGE_PATH_NO_PREVIEW}">
                            </emm:column>

                            <emm:column sortable="true" titleKey="default.Name" property="shortname" />
                            <emm:column class="table-preview-hidden" sortable="true" titleKey="Description" property="description" />

                            <emm:column titleKey="default.creationDate" cssClass="secondary-cell">
                                <i class="icon icon-calendar-alt"></i>
                                <span><emm:formatDate value="${template.creationDate}" format="${adminDateTimeFormat}" /></span>

                                <c:url var="newMailingUrl" value="/mailing/new.action">
                                    <c:param name="keepForward">${workflowId > 0}</c:param>
                                    <c:param name="templateId">${template.id}</c:param>
                                </c:url>
                                <a href="${newMailingUrl}" class="hidden" data-view-row="page"></a>
                            </emm:column>
                        </emm:table>
                    </div>
                    <div class="table-wrapper__footer"></div>
                </div>
            </div>
        </mvc:form>
    </div>
</div>
