<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="formId" type="java.lang.Integer"--%>
<%--@elvariable id="imageSrcPattern" type="java.lang.String"--%>
<%--@elvariable id="imageSrcPatternNoCache" type="java.lang.String"--%>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<mvc:message var="deleteTooltip" code="mailing.Graphics_Component.delete"/>

<div id="userform-images-overview" class="tiles-container" data-controller="userform-images" data-editable-view="${agnEditViewKey}">
    <div class="tiles-block flex-column" style="flex: 3">
        <emm:ShowByPermission token="mailing.components.change">
            <div class="tile" id="upload-tile" data-editable-tile>
                <div class="tile-header">
                    <h1 class="tile-title"><mvc:message code="upload.manual" /></h1>
                </div>
                <div id="upload-file" class="tile-body d-flex flex-column" data-initializer="upload" data-action="image-upload">
                    <div id="dropzone" class="dropzone flex-grow-1" data-form-target="#components-upload-modal" data-upload-dropzone>
                        <div class="dropzone-text">
                            <strong>
                                <i class="icon icon-reply"></i>&nbsp;<mvc:message code="upload_dropzone.title"/>
                            </strong>
                            <span class="btn btn-sm btn-primary btn-upload">
                            <i class="icon icon-file-upload"></i>
                            <h3><mvc:message code="button.multiupload.select"/></h3>
                            <input id="upload-files" type="file" multiple="multiple" data-upload="components[].file"/>
                        </span>
                        </div>
                    </div>

                    <div class="pt-3" data-upload-progress style="display:none;">
                        <%-- Loads by JS --%>
                    </div>
                </div>

                <%@ include file="fragments/upload-templates.jspf" %>
            </div>
        </emm:ShowByPermission>

        <mvc:form id="table-tile" cssClass="tile" method="GET" servletRelativeAction="/webform/${formId}/components/list.action" modelAttribute="form" data-editable-tile="main">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
                <div class="tile-controls">
                    <input type="checkbox" id="switch-table-view" class="icon-switch" data-preview-table="#userform-images-table" checked>
                    <label for="switch-table-view" class="icon-switch__label">
                        <i class="icon icon-image"></i>
                        <i class="icon icon-th-list"></i>
                    </label>
                </div>
            </div>

            <div class="tile-body">
                <div class="table-box">
                    <div class="table-scrollable">
                        <display:table htmlId="userform-images-table" class="table table-hover table-rounded js-table"
                                       id="component" name="components" pagesize="${form.numberOfRows}" excludedParams="*">

                            <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                            <c:url var="imageLinkNoCache" value="${fn:replace(imageSrcPatternNoCache, '{name}', component.name)}"/>

                            <display:column class="table-preview-visible w-100" headerClass="hidden">
                                <div class="table-cell__preview-wrapper">
                                    <img src="${imageLinkNoCache}" alt="" class="table-cell__preview">
                                </div>
                            </display:column>

                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-form-bulk="bulkId"/>
                            </c:set>

                            <display:column title="${checkboxSelectAll}" sortable="false" class="js-checkable mobile-hidden" headerClass="bulk-ids-column fit-content mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${component.id}"/>
                            </display:column>

                            <display:column property="name" titleKey="mailing.Graphics_Component" headerClass="js-table-sort" sortable="true" sortProperty="name" class="table-cell-nowrap order-1"/>

                            <display:column property="description" titleKey="Description" headerClass="js-table-sort" sortable="true" sortProperty="description" class="table-preview-hidden"/>

                            <c:url var="imageLink" value="${fn:replace(imageSrcPattern, '{name}', component.name)}"/>
                            <display:column titleKey="htmled.link" sortable="false" class="table-preview-hidden">${imageLink}</display:column>

                            <display:column titleKey="CreationDate" headerClass="js-table-sort fit-content" sortable="true" sortProperty="creationDate" class="table__cell-sub-info order-3">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-calendar-alt table-preview-visible"></i>
                                    <span><emm:formatDate value="${component.creationDate}" format="${adminDateTimeFormat}" /></span>
                                </div>
                            </display:column>

                            <display:column titleKey="mailing.Graphics_Component.Dimensions" sortable="false" headerClass="fit-content" class="table__cell-sub-info order-3">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-ruler-combined table-preview-visible"></i>
                                    <span>${component.width} x ${component.height} px</span>
                                </div>
                            </display:column>

                            <display:column titleKey="default.Size" sortable="false" headerClass="fit-content" class="table__cell-sub-info order-3">
                                <c:if test="${not empty component.dataSize}">
                                    <div class="table-cell-wrapper">
                                        <i class="icon icon-weight-hanging table-preview-visible"></i>
                                        <span>${emm:makeUnitSignNumber(component.dataSize, 'B', false, pageContext.request)}</span>
                                    </div>
                                </c:if>
                            </display:column>

                            <display:column titleKey="report.data.type" headerClass="js-table-sort fit-content" sortable="true" sortProperty="mimeType" >
                                <c:if test="${not empty component.mimeType and fn:startsWith(component.mimeType, 'image/')}">
                                <span class="text-uppercase pill-badge">
                                        ${fn:substring(component.mimeType, fn:length('image/'), -1)}
                                </span>
                                </c:if>
                            </display:column>

                            <display:column headerClass="fit-content" class="order-2">
                                <script type="text/x-mustache-template" class="js-row-popover">
                                    <img src="${imageLinkNoCache}" style="max-width: 200px" alt="" border="0">
                                </script>

                                <c:url var="deleteUrl" value="/webform/${formId}/components/deleteRedesigned.action?bulkIds=${component.id}"/>
                                <a href="${deleteUrl}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deleteTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </display:column>
                        </display:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/webform/${formId}/components/search.action" modelAttribute="form"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">
        <mvc:message var="minPlaceholder" code="default.min"/>
        <mvc:message var="maxPlaceholder" code="default.max"/>

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
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="filter-description">${descriptionMsg}</label>
                    <mvc:text id="filter-description" path="description" cssClass="form-control" />
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
                    <label class="form-label" for="filter-height-min"><mvc:message code="grid.mediapool.image.sizes.height" /></label>
                    <div class="inline-input-range">
                        <mvc:number id="filter-height-min" path="heightMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+"/>
                        <mvc:number id="filter-height-max" path="heightMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+"/>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="filter-width-min"><mvc:message code="default.image.width" /></label>
                    <div class="inline-input-range">
                        <mvc:number id="filter-width-min" path="widthMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+"/>
                        <mvc:number id="filter-width-max" path="widthMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+"/>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="filter-file-size-min"><mvc:message code="mailing.Graphics_Component.FileSize"/></label>
                    <div class="inline-input-range">
                        <mvc:number id="filter-file-size-min" path="fileSizeMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+"/>
                        <mvc:number id="filter-file-size-max" path="fileSizeMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+"/>
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
            </div>
        </div>
    </mvc:form>
</div>
