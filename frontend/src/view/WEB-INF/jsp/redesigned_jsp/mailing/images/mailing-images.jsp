<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.beans.MailingComponentType" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags/form" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.UploadMailingImagesForm"--%>
<%--@elvariable id="filter" type="com.agnitas.emm.core.components.form.MailingImagesOverviewFilter"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="image" type="com.agnitas.emm.core.components.dto.MailingImageDto"--%>
<%--@elvariable id="images" type="java.util.List<com.agnitas.emm.core.components.dto.MailingImageDto>"--%>

<c:set var="MAILING_COMPONENT_IMAGE_TYPE" value="<%= MailingComponentType.Image %>" scope="request"/>
<c:set var="MAILING_COMPONENT_HOSTED_IMAGE_TYPE" value="<%= MailingComponentType.HostedImage %>" scope="request"/>

<c:set var="noNumberOfRowsSelect" value="true" scope="request" />
<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<div id="mailing-images-overview" class="tiles-container hidden" data-controller="mailing-images" data-editable-view="${agnEditViewKey}">
    <div class="tiles-block flex-column" style="flex: 3">
        <emm:ShowByPermission token="mailing.components.change">
            <div id="mailing-images-upload-block" class="tiles-block mobile-hidden">
                <div class="tile" id="manual-upload-tile" data-editable-tile>
                    <div class="tile-header">
                        <h1 class="tile-title"><mvc:message code="upload.manual" /></h1>
                    </div>
                    <div id="manual-upload-file" class="tile-body d-flex flex-column" data-initializer="upload" data-action="image-upload">
                        <div id="dropzone" class="dropzone flex-grow-1" data-form-target="#components-upload-modal" data-upload-dropzone>
                            <div class="dropzone-text">
                                <strong>
                                    <i class="icon icon-reply"></i>&nbsp;<mvc:message code="upload_dropzone.title"/>
                                </strong>
                                <span class="btn btn-sm btn-primary btn-upload">
                                    <i class="icon icon-file-upload"></i>
                                    <span class="text"><b><mvc:message code="button.multiupload.select"/></b></span>
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

                <%@ include file="fragments/mailing-images-sftp-upload.jspf" %>
            </div>
        </emm:ShowByPermission>
        <mvc:form id="table-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/images/list.action" data-editable-tile="main">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
                <div class="tile-controls">
                    <input type="checkbox" id="switch-table-view" class="icon-switch" data-preview-table="#mailing-images-table" checked>
                    <label for="switch-table-view" class="icon-switch__label">
                        <i class="icon icon-image"></i>
                        <i class="icon icon-th-list"></i>
                    </label>
                </div>
            </div>
            <div class="tile-body">
                <div class="table-box">
                    <div class="table-scrollable">
                        <display:table htmlId="mailing-images-table" id="image" class="table table-hover table-rounded js-table" name="images"
                                       requestURI="/mailing/${mailingId}/images/list.action"
                                       pagesize="${form.numberOfRows}">

                            <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                            <c:url var="sourceSrc" value="/sc?compID=${image.id}"/>

                            <display:column class="table-preview-visible w-100" headerClass="hidden">
                                <div class="table-cell__preview-wrapper">
                                    <img data-display-dimensions="scope: tr" src="${sourceSrc}" alt="${image.description}" class="table-cell__preview">
                                </div>
                            </display:column>

                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-form-bulk="bulkId"/>
                            </c:set>
                            <display:column title="${checkboxSelectAll}" sortable="false" class="js-checkable mobile-hidden" headerClass="bulk-ids-column fit-content mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${image.id}"/>
                            </display:column>

                            <display:column titleKey="report.data.type" sortable="true" sortProperty="mimeType" headerClass="js-table-sort fit-content mobile-hidden">
                                <c:if test="${not empty image.mimeType and fn:startsWith(image.mimeType, 'image')}">
                                    <span class="text-uppercase pill-badge">${fn:toUpperCase(fn:substring(image.mimeType, 6, -1))}</span>
                                </c:if>
                            </display:column>

                            <display:column property="name" titleKey="settings.FileName" headerClass="js-table-sort" sortable="true" sortProperty="name" class="table-cell-nowrap"/>

                            <display:column property="description" titleKey="Description" headerClass="js-table-sort" sortable="true" sortProperty="description" class="table-preview-hidden"/>

                            <display:column titleKey="htmled.link" sortable="false" class="table-preview-hidden" headerClass="fit-content">
                                <c:if test="${not empty image.link}">
                                    <a type="button" class="btn btn-inverse btn-icon-sm" target="_blank" href="${image.link.fullUrl}" data-tooltip="${image.link.fullUrl}">
                                        <i class="icon icon-external-link-alt"></i>
                                    </a>
                                </c:if>
                            </display:column>

                            <display:column titleKey="CreationDate" headerClass="js-table-sort fit-content" sortable="true" sortProperty="creationDate" class="table__cell-sub-info order-3">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-calendar-alt table-preview-visible"></i>
                                    <span><emm:formatDate value="${image.creationDate}" format="${adminDateTimeFormat}" /></span>
                                </div>
                            </display:column>

                            <display:column titleKey="default.Type" sortable="true" class="table-preview-hidden" headerClass="fit-content">
                                <mvc:message code="${image.mobile ? 'Mobile' : 'predelivery.desktop'}"/>
                            </display:column>

                            <display:column titleKey="mailing.Graphics_Component.Dimensions" sortable="false" headerClass="fit-content" class="table__cell-sub-info order-3">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-ruler-combined table-preview-visible"></i>
                                    <span data-dimensions>
                                        <%-- Loads by JS --%>
                                    </span>
                                </div>
                            </display:column>

                            <display:column titleKey="default.Size" sortable="true" class="table__cell-sub-info order-5" sortProperty="size" headerClass="fit-content mobile-hidden">
                                <c:if test="${not empty image.size}">
                                    <div class="table-cell-wrapper">
                                        <i class="icon icon-weight-hanging mobile-visible table-preview-visible"></i>
                                        <span><fmt:formatNumber type="number" value="${image.size / 1024}" maxFractionDigits="1"/>&nbsp;kB</span>
                                    </div>
                                </c:if>
                            </display:column>

                            <display:column headerClass="table-preview-hidden fit-content mobile-hidden" class="table-actions order-2">
                                <script type="text/x-mustache-template" class="js-row-popover">
                                    <img src="${sourceSrc}" style="max-width: 200px" alt="" border="0">
                                </script>

                                <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}">
                                    <a href="#" data-table-modal="image-editor-modal" data-table-modal-options='id: ${image.id}, name: ${image.name}, link: ${image.link}, src: ${sourceSrc}' data-view-row></a>
                                </c:if>

                                <div>
                                    <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE || image.present == 0}">
                                        <a href="<c:url value='/mailing/${mailingId}/images/deleteRedesigned.action?bulkIds=${image.id}'/>"
                                           data-tooltip="<mvc:message code='mailing.Graphics_Component.delete'/>" class="btn btn-icon-sm btn-danger js-row-delete">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${image.type == MAILING_COMPONENT_IMAGE_TYPE}">
                                        <a href="<c:url value='/mailing/${mailingId}/images/${image.id}/reload.action'/>"
                                           data-tooltip="<mvc:message code='mailing.Graphics_Component.Update'/>" class="btn btn-icon-sm btn-primary">
                                            <i class="icon icon-sync-alt"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}">
                                        <a href="<c:url value='/dc?compID=${image.id}'/>"
                                           data-tooltip="<mvc:message code='button.Download'/>" data-prevent-load class="btn btn-icon-sm btn-primary">
                                            <i class="icon icon-file-download"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </display:column>
                        </display:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/mailing/${mailingId}/images/search.action" modelAttribute="filter"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">

        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
