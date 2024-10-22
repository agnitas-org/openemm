<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.beans.MailingComponentType" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="fmt"        uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"          uri="http://www.springframework.org/tags/form" %>

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
                                    <a href="#" class="icon-btn text-primary" data-tooltip="<mvc:message code='bulkAction.download.image.selected'/>" data-form-url="${bulkDownloadUrl}" data-prevent-load data-form-submit-static data-bulk-action="download">
                                        <i class="icon icon-file-download"></i>
                                    </a>

                                    <c:url var="bulkDeleteUrl" value="/mailing/${mailingId}/images/deleteRedesigned.action"/>
                                    <a href="#" class="icon-btn text-danger" data-tooltip="<mvc:message code="bulkAction.delete.image" />" data-form-url='${bulkDeleteUrl}' data-form-confirm data-bulk-action="delete">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                            <jsp:include page="../../common/table/preview-switch.jsp">
                                <jsp:param name="selector" value="#mailing-images-table"/>
                            </jsp:include>
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="filteredEntries" value="${images.fullListSize}"/>
                                <jsp:param name="totalEntries" value="${images.notFilteredFullListSize}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <agnDisplay:table htmlId="mailing-images-table" id="image" class="table table-hover table--borderless js-table" name="images"
                                       requestURI="/mailing/${mailingId}/images/list.action"
                                       excludedParams="*" pagesize="${filter.numberOfRows}">

                            <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                            <c:url var="sourceSrc" value="/sc?compID=${image.id}"/>
                            <c:set var="imageDownloadPossible" value="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}" />
                            <c:set var="imageDeletePossible" value="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE || image.present == 0}" />

                            <agnDisplay:column class="thumbnail-cell" headerClass="hidden">
                                <img data-display-dimensions="scope: tr" src="${sourceSrc}" alt="${image.description}">
                            </agnDisplay:column>

                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>
                            <agnDisplay:column title="${checkboxSelectAll}" class="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${image.id}"
                                    ${not imageDownloadPossible and not imageDeletePossible ? 'disabled' : 'data-bulk-checkbox'} />
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="report.data.type" sortable="true" sortProperty="mtype" headerClass="js-table-sort fit-content mobile-hidden">
                                <c:if test="${not empty image.mimeType and fn:startsWith(image.mimeType, 'image')}">
                                    <span class="text-uppercase table-badge">${fn:toUpperCase(fn:substring(image.mimeType, 6, -1))}</span>
                                </c:if>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="settings.FileName" headerClass="js-table-sort" sortable="true" sortProperty="compname" class="fluid-cell">
                                <span>${image.name}</span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="Description" headerClass="js-table-sort" sortable="true" sortProperty="description" class="table-preview-hidden">
                                <span>${image.description}</span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="htmled.link" sortable="false" class="table-preview-hidden" headerClass="fit-content">
                                <c:if test="${not empty image.link}">
                                    <a type="button" class="btn btn-inverse btn-icon" target="_blank" href="${image.link.fullUrl}" data-tooltip="${image.link.fullUrl}">
                                        <i class="icon icon-external-link-alt"></i>
                                    </a>
                                </c:if>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="CreationDate" headerClass="js-table-sort fit-content" sortable="true" sortProperty="timestamp" class="secondary-cell">
                                <i class="icon icon-calendar-alt"></i>
                                <span><emm:formatDate value="${image.creationDate}" format="${adminDateTimeFormat}" /></span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="default.Type" sortable="false" class="table-preview-hidden mobile-hidden" headerClass="fit-content">
                                <span><mvc:message code="${image.mobile ? 'Mobile' : 'predelivery.desktop'}"/></span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="mailing.Graphics_Component.Dimensions" sortable="false" headerClass="fit-content" class="secondary-cell">
                                <i class="icon icon-ruler-combined"></i>
                                <span data-dimensions>
                                    <%-- Loads by JS --%>
                                </span>
                            </agnDisplay:column>

                            <agnDisplay:column titleKey="default.Size" sortable="true" class="secondary-cell" sortProperty="comp_size" headerClass="js-table-sort fit-content mobile-hidden">
                                <c:if test="${not empty image.size}">
                                    <i class="icon icon-weight-hanging"></i>
                                    <span><fmt:formatNumber type="number" value="${image.size / 1024}" maxFractionDigits="1"/>&nbsp;kB</span>
                                </c:if>
                            </agnDisplay:column>

                            <agnDisplay:column headerClass="table-preview-hidden fit-content mobile-hidden" class="table-actions mobile-hidden">
                                <script type="text/x-mustache-template" class="js-row-popover">
                                    <img src="${sourceSrc}" style="max-width: 200px" alt="" border="0">
                                </script>

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
                                        <a class="icon-btn text-danger js-row-delete" ${deleteBtnAttrs}>
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
                                        <a class="icon-btn text-primary" ${updateBtnAttrs}>
                                            <i class="icon icon-sync-alt"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${imageDownloadPossible}">
                                        <a href="<c:url value='/dc?compID=${image.id}'/>"
                                           data-tooltip="<mvc:message code='button.Download'/>" data-prevent-load class="icon-btn text-primary" data-bulk-action="download">
                                            <i class="icon icon-file-download"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </agnDisplay:column>
                        </agnDisplay:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/images/search.action" modelAttribute="filter"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">

        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="fileNameMsg" code="settings.FileName"/>
                    <mvc:message var="nameMsg" code="Name" />

                    <label class="form-label" for="filter-file-name">${fileNameMsg}</label>
                    <mvc:text id="filter-file-name" path="fileName" cssClass="form-control" placeholder="${nameMsg}.png"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-upload-date-from"><mvc:message code="CreationDate"/></label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <mvc:message var="fromMsg" code="From" />
                            <mvc:text id="filter-upload-date-from" path="uploadDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container">
                            <mvc:message var="toMsg" code="To" />
                            <mvc:text id="filter-upload-date-to" path="uploadDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-file-type"><mvc:message code="report.data.type"/></label>

                    <mvc:select id="filter-file-type" path="mimetypes" cssClass="form-control">
                        <c:forEach var="mimetype" items="${imagesMimetypes}">
                            <c:set var="fileType" value="${fn:substring(mimetype, fn:length('image/'), -1)}"/>
                            <mvc:option value="${mimetype}">${fn:toUpperCase(fileType)}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-type"><mvc:message code="default.Type"/></label>
                    <mvc:select id="filter-type" path="mobile" cssClass="form-control js-select">
                        <mvc:option value=""><mvc:message code="default.All"/></mvc:option>
                        <mvc:option value="true"><mvc:message code="Mobile"/></mvc:option>
                        <mvc:option value="false"><mvc:message code="predelivery.desktop"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </mvc:form>
</div>

<script id="image-editor-modal" type="text/x-mustache-template">
    <%@ include file="fragments/image-editor-modal.jspf" %>
</script>
