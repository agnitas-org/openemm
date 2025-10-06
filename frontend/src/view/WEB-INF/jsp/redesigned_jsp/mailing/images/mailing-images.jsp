<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.beans.MailingComponentType" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"   uri="http://www.springframework.org/tags/form" %>

<%--@elvariable id="filter" type="com.agnitas.emm.core.components.form.MailingImagesOverviewFilter"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="image" type="com.agnitas.emm.core.components.dto.MailingImageDto"--%>
<%--@elvariable id="images" type="java.util.List<com.agnitas.emm.core.components.dto.MailingImageDto>"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>

<c:set var="MAILING_COMPONENT_IMAGE_TYPE" value="<%= MailingComponentType.Image %>" scope="request"/>
<c:set var="MAILING_COMPONENT_HOSTED_IMAGE_TYPE" value="<%= MailingComponentType.HostedImage %>" scope="request"/>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<c:set var="isChangeAllowed" value="${emm:permissionAllowed(isTemplate ? 'template.change' : 'mailing.change', pageContext.request)}" />
<c:set var="isDeleteAllowed" value="${emm:permissionAllowed(isTemplate ? 'template.change' : 'mailing.change', pageContext.request)}" />
<c:set var="notAllowedAttrs">
    disabled data-tooltip='<mvc:message code="permission.denied.message"/>'
</c:set>

<div id="mailing-images-overview" class="tiles-container" data-controller="mailing-images" data-editable-view="${agnEditViewKey}">
    <div class="tiles-block flex-column" style="flex: 3">
        <emm:ShowByPermission token="mailing.components.change">
            <div id="mailing-images-upload-block" class="tiles-block mobile-hidden">
                <c:if test="${isChangeAllowed}">
                    <div class="tile" id="manual-upload-tile" data-editable-tile>
                        <div class="tile-header">
                            <h1 class="tile-title text-truncate"><mvc:message code="upload.manual" /></h1>
                        </div>

                        <div id="manual-upload-file" class="tile-body d-flex flex-column" data-initializer="upload" data-action="image-upload">
                            <div id="dropzone" class="dropzone flex-grow-1" data-form-target="#components-upload-modal" data-upload-dropzone>
                                <div class="dropzone-text">
                                    <b>
                                        <i class="icon icon-reply"></i>&nbsp;<mvc:message code="upload_dropzone.title"/>
                                    </b>
                                    <span class="btn btn-primary btn-upload">
                                        <i class="icon icon-file-upload"></i>
                                        <span class="text"><mvc:message code="button.multiupload.select"/></span>
                                        <input id="upload-files" data-upload="images[].file" type="file" multiple="multiple"/>
                                    </span>
                                </div>
                            </div>

                            <div class="pt-3" data-upload-progress style="display:none;">
                                <%-- Loads by JS --%>
                            </div>

                            <%@ include file="fragments/upload-templates.jspf" %>
                        </div>
                    </div>
                </c:if>

                <%@ include file="fragments/mailing-images-sftp-upload.jspf" %>
            </div>
        </emm:ShowByPermission>
        <mvc:form id="table-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/images/list.action" data-editable-tile="main" modelAttribute="filter">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "mailing-images-overview": {
                        "rows-count": ${filter.numberOfRows}
                    }
                }
            </script>

            <div class="tile-body">
                <div class="table-wrapper">
                    <div class="table-wrapper__header">
                        <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                        <div class="table-wrapper__controls">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:url var="bulkDownloadUrl" value="/mailing/${mailingId}/images/bulkDownload.action"/>
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code='bulkAction.download.image.selected'/>" data-form-url="${bulkDownloadUrl}" data-prevent-load data-form-submit-static data-bulk-action="download">
                                        <i class="icon icon-file-download"></i>
                                    </a>

                                    <c:url var="bulkDeleteUrl" value="/mailing/${mailingId}/images/deleteRedesigned.action"/>
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="bulkAction.delete.image" />" data-form-url='${bulkDeleteUrl}' data-form-confirm data-bulk-action="delete">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                            <jsp:include page="../../common/table/preview-switch.jsp">
                                <jsp:param name="selector" value="#mailing-images-table" />
                                <jsp:param name="visualList" value="true" />
                            </jsp:include>
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="filteredEntries" value="${images.fullListSize}"/>
                                <jsp:param name="totalEntries" value="${images.notFilteredFullListSize}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <emm:table id="mailing-images-table" var="image" modelAttribute="images" cssClass="table table-hover table--borderless js-table">

                            <c:url var="sourceSrc" value="/sc?compID=${image.id}"/>
                            <c:set var="imageDownloadPossible" value="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}" />
                            <c:set var="imageDeletePossible" value="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE || image.present == 0}" />

                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>
                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${image.id}"
                                    ${not imageDownloadPossible and not imageDeletePossible ? 'disabled' : 'data-bulk-checkbox'} />
                            </emm:column>

                            <emm:column titleKey="mailing.Graphics_Component" cssClass="thumbnail-cell" headerClass="thumbnail-header-cell fit-content">
                                <img data-display-dimensions="scope: tr" src="${sourceSrc}" alt="${image.description}">
                            </emm:column>

                            <emm:column titleKey="report.data.type" sortable="true" sortProperty="mtype" headerClass="fit-content mobile-hidden">
                                <c:if test="${not empty image.mimeType and fn:startsWith(image.mimeType, 'image')}">
                                    <span class="text-uppercase table-badge">${fn:toUpperCase(fn:substring(image.mimeType, 6, -1))}</span>
                                </c:if>
                            </emm:column>

                            <emm:column titleKey="settings.FileName" sortable="true" sortProperty="compname" property="name" cssClass="fluid-cell" />
                            <emm:column titleKey="Description" sortable="true" property="description" class="table-preview-hidden" />

                            <emm:column titleKey="htmled.link" cssClass="table-preview-hidden" headerClass="fit-content">
                                <c:if test="${not empty image.link}">
                                    <a type="button" class="btn btn-secondary btn-icon" target="_blank" href="${image.link.fullUrl}" data-tooltip="${image.link.fullUrl}">
                                        <i class="icon icon-external-link-alt"></i>
                                    </a>
                                </c:if>
                            </emm:column>

                            <emm:column titleKey="CreationDate" headerClass="fit-content" sortable="true" sortProperty="timestamp" cssClass="secondary-cell">
                                <i class="icon icon-calendar-alt"></i>
                                <span><emm:formatDate value="${image.creationDate}" format="${adminDateTimeFormat}" /></span>
                            </emm:column>

                            <emm:column titleKey="default.Type" sortable="false" cssClass="table-preview-hidden mobile-hidden" headerClass="fit-content">
                                <span><mvc:message code="${image.mobile ? 'Mobile' : 'predelivery.desktop'}"/></span>
                            </emm:column>

                            <emm:column titleKey="mailing.Graphics_Component.Dimensions" headerClass="fit-content" cssClass="secondary-cell">
                                <i class="icon icon-ruler-combined"></i>
                                <span data-dimensions>
                                    <%-- Loads by JS --%>
                                </span>
                            </emm:column>

                            <emm:column titleKey="default.Size" sortable="true" cssClass="secondary-cell" sortProperty="comp_size" headerClass="fit-content mobile-hidden">
                                <c:if test="${not empty image.size}">
                                    <i class="icon icon-weight-hanging"></i>
                                    <span><fmt:formatNumber type="number" value="${image.size / 1024}" maxFractionDigits="1"/>&nbsp;kB</span>
                                </c:if>
                            </emm:column>

                            <emm:column headerClass="table-preview-hidden mobile-hidden" cssClass="table-actions mobile-hidden">
                                <template class="js-row-popover">
                                    <img src="${sourceSrc}" alt="" class="popover__thumbnail">
                                </template>

                                <c:if test="${isChangeAllowed and image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}">
                                    <a href="#" data-modal="image-editor-modal" data-modal-set='id: ${image.id}, name: ${image.name}, link: ${image.link}, src: ${sourceSrc}' data-view-row></a>
                                </c:if>

                                <div>
                                    <c:set var="deleteBtnAttrs">
                                        <c:choose>
                                            <c:when test="${isDeleteAllowed}">
                                                href="<c:url value='/mailing/${mailingId}/images/deleteRedesigned.action?bulkIds=${image.id}'/>"
                                                data-tooltip="<mvc:message code='mailing.Graphics_Component.delete'/>" data-bulk-action="delete"
                                            </c:when>
                                            <c:otherwise>
                                                ${notAllowedAttrs}
                                            </c:otherwise>
                                        </c:choose>
                                    </c:set>

                                    <c:if test="${imageDeletePossible}">
                                        <a class="icon-btn icon-btn--danger js-row-delete" ${deleteBtnAttrs}>
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>

                                    <c:set var="updateBtnAttrs">
                                        <c:choose>
                                            <c:when test="${isChangeAllowed}">
                                                href="<c:url value='/mailing/${mailingId}/images/${image.id}/reload.action'/>"
                                                data-tooltip="<mvc:message code='mailing.Graphics_Component.Update'/>"
                                            </c:when>
                                            <c:otherwise>
                                                ${notAllowedAttrs}
                                            </c:otherwise>
                                        </c:choose>
                                    </c:set>
                                    <c:if test="${image.type == MAILING_COMPONENT_IMAGE_TYPE}">
                                        <a class="icon-btn icon-btn--primary" ${updateBtnAttrs}>
                                            <i class="icon icon-sync-alt"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${imageDownloadPossible}">
                                        <a href="<c:url value='/dc?compID=${image.id}'/>"
                                           data-tooltip="<mvc:message code='button.Download'/>" data-prevent-load class="icon-btn icon-btn--primary" data-bulk-action="download">
                                            <i class="icon icon-file-download"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </emm:column>
                        </emm:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/images/search.action" modelAttribute="filter"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <mvc:message var="fileNameMsg" code="settings.FileName"/>
                <mvc:message var="nameMsg" code="Name" />

                <label class="form-label" for="filter-file-name">${fileNameMsg}</label>
                <mvc:text id="filter-file-name" path="fileName" cssClass="form-control" placeholder="${nameMsg}.png"/>
            </div>

            <div>
                <label class="form-label" for="filter-upload-date-from"><mvc:message code="CreationDate"/></label>
                <mvc:dateRange id="filter-upload-date" path="uploadDate" inline="true" options="maxDate: 0" />
            </div>

            <div>
                <label class="form-label" for="filter-file-type"><mvc:message code="report.data.type"/></label>

                <mvc:select id="filter-file-type" path="mimetypes" cssClass="form-control">
                    <c:forEach var="mimetype" items="${imagesMimetypes}">
                        <c:set var="fileType" value="${fn:substring(mimetype, fn:length('image/'), -1)}"/>
                        <mvc:option value="${mimetype}">${fn:toUpperCase(fileType)}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-type"><mvc:message code="default.Type"/></label>
                <mvc:select id="filter-type" path="mobile" cssClass="form-control js-select">
                    <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                    <mvc:option value="true"><mvc:message code="Mobile"/></mvc:option>
                    <mvc:option value="false"><mvc:message code="predelivery.desktop"/></mvc:option>
                </mvc:select>
            </div>
        </div>
    </mvc:form>
</div>

<script id="image-editor-modal" type="text/x-mustache-template">
    <%@ include file="fragments/image-editor-modal.jspf" %>
</script>
