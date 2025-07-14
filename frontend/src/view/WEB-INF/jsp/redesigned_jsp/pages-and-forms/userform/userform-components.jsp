<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="formId" type="java.lang.Integer"--%>
<%--@elvariable id="imageSrcPattern" type="java.lang.String"--%>
<%--@elvariable id="imageSrcPatternNoCache" type="java.lang.String"--%>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<mvc:message var="deleteTooltip" code="mailing.Graphics_Component.delete" />

<div id="userform-images-overview" class="tiles-container" data-controller="userform-images" data-editable-view="${agnEditViewKey}">
    <div class="tiles-block flex-column" style="flex: 3">
        <emm:ShowByPermission token="mailing.components.change">
            <div class="tile" id="upload-tile" data-editable-tile>
                <div class="tile-header">
                    <h1 class="tile-title text-truncate"><mvc:message code="upload.manual" /></h1>
                </div>
                <div id="upload-file" class="tile-body d-flex flex-column" data-initializer="upload" data-action="image-upload">
                    <div id="dropzone" class="dropzone flex-grow-1" data-form-target="#components-upload-modal" data-upload-dropzone>
                        <div class="dropzone-text">
                            <b>
                                <i class="icon icon-reply"></i>&nbsp;<mvc:message code="upload_dropzone.title"/>
                            </b>
                            <span class="btn btn-primary btn-upload">
                            <i class="icon icon-file-upload"></i>
                            <span class="text"><mvc:message code="button.multiupload.select"/></span>
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
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "userform-images-overview": {
                        "rows-count": ${form.numberOfRows}
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
                                    <c:url var="bulkDownloadUrl" value="/webform/${formId}/components/bulk/download.action"/>
                                    <c:url var="bulkDeleteUrl" value="/webform/${formId}/components/deleteRedesigned.action"/>

                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code='bulkAction.download.files.selected'/>" data-form-url="${bulkDownloadUrl}" data-prevent-load data-form-submit-static>
                                        <i class="icon icon-file-download"></i>
                                    </a>
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="bulkAction.delete.file" />" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                            <jsp:include page="../../common/table/preview-switch.jsp">
                                <jsp:param name="selector" value="#userform-images-table"/>
                            </jsp:include>
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="filteredEntries" value="${components.fullListSize}"/>
                                <jsp:param name="totalEntries" value="${components.notFilteredFullListSize}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <emm:table id="userform-images-table" var="component" modelAttribute="components" cssClass="table table-hover table--borderless js-table">

                            <c:url var="imageLinkNoCache" value="${fn:replace(imageSrcPatternNoCache, '{name}', component.name)}"/>

                            <emm:column cssClass="thumbnail-cell" headerClass="hidden">
                                <img src="${imageLinkNoCache}" alt="Thumbnail">
                            </emm:column>

                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden order-first" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${component.id}" data-bulk-checkbox />
                            </emm:column>

                            <emm:column titleKey="mailing.Graphics_Component" sortable="true" property="name" cssClass="fluid-cell" />
                            <emm:column titleKey="Description" headerClass="mobile-hidden" sortable="true" property="description" cssClass="table-preview-hidden" />

                            <c:url var="imageLink" value="${fn:replace(imageSrcPattern, '{name}', component.name)}"/>
                            <emm:column titleKey="htmled.link" sortable="false" headerClass="mobile-hidden" cssClass="table-preview-hidden">
                                <span>${imageLink}</span>
                            </emm:column>

                            <emm:column titleKey="CreationDate" headerClass="fit-content mobile-hidden" sortable="true" sortProperty="creation_date" cssClass="secondary-cell">
                                <i class="icon icon-calendar-alt"></i>
                                <span><emm:formatDate value="${component.creationDate}" format="${adminDateTimeFormat}" /></span>
                            </emm:column>

                            <emm:column titleKey="mailing.Graphics_Component.Dimensions" headerClass="fit-content mobile-hidden" cssClass="secondary-cell">
                                <i class="icon icon-ruler-combined"></i>
                                <span>${component.width} x ${component.height} px</span>
                            </emm:column>

                            <emm:column titleKey="default.Size" sortable="true" sortProperty="data_size" headerClass="fit-content mobile-hidden" cssClass="secondary-cell">
                                <c:if test="${not empty component.dataSize}">
                                    <i class="icon icon-weight-hanging"></i>
                                    <span>${emm:makeUnitSignNumber(component.dataSize, 'B', false, pageContext.request)}</span>
                                </c:if>
                            </emm:column>

                            <emm:column titleKey="report.data.type" headerClass="fit-content mobile-hidden" cssClass="order-first" sortable="true" sortProperty="mimetype" >
                                <c:if test="${not empty component.mimeType and fn:startsWith(component.mimeType, 'image/')}">
                                    <span class="text-uppercase table-badge">
                                        ${fn:substring(component.mimeType, fn:length('image/'), -1)}
                                    </span>
                                </c:if>
                            </emm:column>

                            <emm:column headerClass="table-preview-hidden mobile-hidden" cssClass="table-actions mobile-hidden">
                                <script type="text/x-mustache-template" class="js-row-popover">
                                    <img src="${imageLinkNoCache}" alt="" class="popover__thumbnail">
                                </script>

                                <div>
                                    <a href="${imageLinkNoCache}?download=true" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code='button.Download'/>" data-prevent-load download="${component.name}">
                                        <i class="icon icon-file-download"></i>
                                    </a>

                                    <c:url var="deleteUrl" value="/webform/${formId}/components/deleteRedesigned.action?bulkIds=${component.id}"/>
                                    <a href="${deleteUrl}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteTooltip}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </emm:column>
                        </emm:table>
                    </div>
                </div>
            </div>
        </mvc:form>
    </div>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/webform/${formId}/components/search.action" modelAttribute="form"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
        <mvc:message var="minPlaceholder" code="default.min"/>
        <mvc:message var="maxPlaceholder" code="default.max"/>

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
                <mvc:message var="descriptionMsg" code="Description"/>
                <label class="form-label" for="filter-description">${descriptionMsg}</label>
                <mvc:text id="filter-description" path="description" cssClass="form-control" />
            </div>

            <div>
                <label class="form-label" for="filter-upload-date-from"><mvc:message code="CreationDate"/></label>
                <mvc:dateRange id="filter-upload-date" path="uploadDate" inline="true" options="maxDate: 0" />
            </div>

            <div>
                <label class="form-label" for="filter-height-min"><mvc:message code="grid.mediapool.image.sizes.height" /></label>
                <div class="inline-input-range">
                    <mvc:number id="filter-height-min" path="heightMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+" />
                    <mvc:number id="filter-height-max" path="heightMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+" />
                </div>
            </div>

            <div>
                <label class="form-label" for="filter-width-min"><mvc:message code="default.image.width" /></label>
                <div class="inline-input-range">
                    <mvc:number id="filter-width-min" path="widthMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+" />
                    <mvc:number id="filter-width-max" path="widthMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+" />
                </div>
            </div>

            <div>
                <label class="form-label" for="filter-file-size-min"><mvc:message code="mailing.Graphics_Component.FileSize"/></label>
                <div class="inline-input-range">
                    <mvc:number id="filter-file-size-min" path="fileSizeMin" cssClass="form-control" placeholder="${minPlaceholder}" min="1" step="1" pattern="\d+"/>
                    <mvc:number id="filter-file-size-max" path="fileSizeMax" cssClass="form-control" placeholder="${maxPlaceholder}" min="1" step="1" pattern="\d+"/>
                </div>
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
        </div>
    </mvc:form>
</div>
