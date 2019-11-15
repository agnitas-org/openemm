<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<div class="row">
    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-imageUpload">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="mailing.Graphics_Component.imageUpload"/>
            </a>
            <ul class="tile-header-nav">
                <emm:ShowByPermission token="mailing.components.change">
                    <li class="active">
                        <a href="#" data-toggle-tab="#tab-dragAndDropImageUpload">
                            <bean:message key="mailing.Graphics_Components"/>
                        </a>
                    </li>
                    <li>
                        <a href="#" data-toggle-tab="#tab-imageUploadZipFolder">
                            <bean:message key="mailing.Graphics_Component.zipFolder"/>
                        </a>
                    </li>
                </emm:ShowByPermission>
            </ul>
        </div>

        <div id="tile-imageUpload" class="tile-content">
            <emm:ShowByPermission token="mailing.components.change">
                <div data-initializer="upload">
                    <agn:agnForm action="/formcomponents" enctype="multipart/form-data" class="form-vertical" data-form="resource" data-custom-loader="">
                        <agn:agnHidden property="formID"/>
                        <agn:agnHidden property="method" value="upload"/>

                        <div id="tab-dragAndDropImageUpload">
                            <div class="tile-content-forms">
                                <div class="dropzone" data-upload-dropzone="">
                                    <div class="dropzone-text">
                                        <strong>
                                            <i class="icon icon-reply"></i>&nbsp;<bean:message key="upload_dropzone.title"/>
                                        </strong>
                                        <span class="btn btn-regular btn-primary btn-upload">
                                            <i class="icon icon-cloud-upload"></i>
                                            <span class="text"><bean:message key="button.multiupload.select"/></span>
                                            <input type="file" name="newFiles[]" multiple="multiple" data-upload="">
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div class="hidden" data-upload-add="">
                                <div class="actions actions-top">
                                    <div class="action-left">
                                        <button type="button" class="btn btn-regular" data-upload-reset="">
                                            <i class="icon icon-times"></i>
                                        <span class="text">
                                            <bean:message key="button.Cancel"/>
                                        </span>
                                        </button>
                                    </div>
                                    <div class="action-right">
                                        <button type="button" class="btn btn-regular btn-primary" data-form-submit="">
                                            <i class="icon icon-cloud-upload"></i>
                                            <span class="text">
                                                <bean:message key="button.Upload"/>
                                            </span>
                                        </button>
                                    </div>
                                </div>
                                <table class="table table-bordered table-striped">
                                    <thead>
                                    <tr>
                                        <th class="squeeze-column"><bean:message key="mailing.Preview"/></th>
                                        <th><bean:message key="Description"/></th>
                                    </tr>
                                    </thead>
                                    <tbody data-upload-add-template="upload-template-add"></tbody>
                                </table>
                                <div class="actions">
                                    <div class="action-left">
                                        <button type="button" class="btn btn-regular" data-upload-reset="">
                                            <i class="icon icon-times"></i>
                                        <span class="text">
                                            <bean:message key="button.Cancel"/>
                                        </span>
                                        </button>
                                    </div>
                                    <div class="action-right">
                                        <button type="button" class="btn btn-regular btn-primary" data-form-submit="">
                                            <i class="icon icon-cloud-upload"></i>
                                            <span class="text">
                                                <bean:message key="button.Upload"/>
                                            </span>
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div class="hidden" data-upload-progress="">
                                <div class="actions actions-top actions-bottom">
                                    <div class="action-right">
                                        <button type="button" class="btn btn-regular" data-form-abort="">
                                            <i class="icon icon-times"></i>
                                            <span class="text">
                                                <bean:message key="button.Cancel"/>
                                            </span>
                                        </button>
                                    </div>
                                </div>
                                <div class="progress-wrapper" data-upload-progress-template="upload-template-progress"></div>
                                <div class="actions actions-top">
                                    <div class="action-right">
                                        <button type="button" class="btn btn-regular" data-form-abort="">
                                            <i class="icon icon-times"></i>
                                            <span class="text">
                                                <bean:message key="button.Cancel"/>
                                            </span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </agn:agnForm>
                </div>

                <agn:agnForm action="/formcomponents" enctype="multipart/form-data" class="form-vertical" data-form="resource">
                    <agn:agnHidden property="formID"/>
                    <agn:agnHidden property="method" value="uploadArchive"/>

                    <div id="tab-imageUploadZipFolder" class="hidden tile-content-forms">
                        <div id="componentsHolder" class="row">
                            <div class="col-sm-12">
                                <div class="form-group">
                                    <label for="new_component_upload" class="control-label"><bean:message key="mailing.Graphics_Component.archive.upload"/>:</label>
                                    <html:file property="archiveFile" styleClass="form-control"/>
                                </div>
                            </div>

                            <div class="col-sm-12">
                                <div class="form-group">
                                    <label for="overwriteExisting"><bean:message key="OverwriteExistingData"/></label>
                                    <html:checkbox property="overwriteExisting" value="true"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <button type="button" class="btn btn-regular btn-primary" data-form-submit="">
                                <i class="icon icon-cloud-upload"></i>
                                <span class="text">
                                    <bean:message key="button.Upload"/>
                                </span>
                            </button>
                        </div>
                    </div>
                </agn:agnForm>
            </emm:ShowByPermission>
        </div>
    </div>

    <agn:agnForm action="/formcomponents" enctype="multipart/form-data" class="form-vertical" data-form="search">
        <html:hidden property="formID"/>
        <html:hidden property="method" value="downloadArchive"/>
        <html:hidden property="filename"/>

        <!-- Tile BEGIN -->
        <div class="tile">

            <!-- Tile Header BEGIN -->
            <div class="tile-header">
                <h2 class="headline">
                    <i class="icon icon-image"></i>
                    <bean:message key="mailing.Graphics_Components"/>
                </h2>

                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><bean:message key="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <a href="#" data-prevent-load="" data-form-submit-static="">
                                    <bean:message key="mailing.Graphics_Component.bulk.download"/>
                                </a>
                            </li>
                        </ul>
                    </li>

                </ul>

                <!-- Tile Header Actions END -->
            </div>
            <!-- Tile Header END -->

            <!-- Tile Content BEGIN -->
            <div class="tile-content" data-form-content>
                <!-- Table BEGIN -->
                <div class="table-wrapper">

                    <display:table
                            class="table table-bordered table-striped js-table"
                            id="component"
                            name="components"
                            pagesize="${mailingComponentsForm.numberOfRows}"
                            excludedParams="*">

                        <display:column titleKey="mailing.Graphics_Component" sortable="false">
                            <a href="<html:rewrite page="${imageData[component.id].nocacheUrl}"/>" data-modal="modal-preview-image"
                               data-modal-set="src: <html:rewrite page="${imageData[component.id].nocacheUrl}"/>, title: ${component.description}">
                                <img src="<html:rewrite page="${imageData[component.id].thumbnailUrl}"/>" alt="${component.description}" border="1" style="width: auto; height: auto; max-height: 135px; max-width: 360px;" data-display-dimensions="scope: tr"/>
                            </a>
                        </display:column>

                        <display:column titleKey="mailing.Graphics_Component" sortable="true" sortProperty="name" headerClass="js-table-sort" property="name"/>

                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" property="description"/>

                        <display:column titleKey="htmled.link" sortable="false">
                            ${imageData[component.id].standardRdirUrl}
                        </display:column>

                        <display:column titleKey="mailing.Graphics_Component.AddDate" sortable="true" sortProperty="creationDate" headerClass="js-table-sort">
                            <i class="icon icon-calendar"></i>
                            <fmt:formatDate value="${component.creationDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>
                            &nbsp;
                            <i class="icon icon-clock-o"></i>
                            <fmt:formatDate value="${component.creationDate}" pattern="${adminTimeFormat}" timeZone="${adminTimeZone}"/>
                        </display:column>

                        <display:column titleKey="mailing.Graphics_Component.Dimensions" sortable="false">
                            <i class="icon icon-expand"></i>
                            ${component.width} x ${component.height} px
                        </display:column>

                        <display:column titleKey="default.Size" sortable="false">
                            <c:if test="${imageData[component.id].fileSize ne null}">
                                ${imageData[component.id].fileSize}
                            </c:if>
                        </display:column>

                        <display:column titleKey="report.data.type" sortable="true" sortProperty="mimeType" headerClass="js-table-sort">
                            <c:if test="${component.mimeType ne null and fn:startsWith(component.mimeType, 'image')}">
                                <span class="badge">
                                    ${fn:toUpperCase(fn:substring(component.mimeType, 6, -1))}
                                </span>
                            </c:if>
                        </display:column>

                        <display:column class="table-actions" titleKey="Actions" sortable="false">
                            <c:set var="pictureDeleteMessage" scope="page">
                                <bean:message key="mailing.Graphics_Component.delete"/>
                            </c:set>
                            <agn:agnLink styleClass="btn btn-regular btn-alert js-row-delete" data-tooltip="${pictureDeleteMessage}"
                                         page="/formcomponents.do?method=delete&formID=${formComponentsForm.formID}&filename=${component.name}">
                                <i class="icon icon-trash-o"></i>
                            </agn:agnLink>

                            <a href="<html:rewrite page="${imageData[component.id].nocacheUrl}"/>"
                               data-tooltip="<bean:message key='button.Download'/>" data-prevent-load download="${component.name}"
                               class="btn btn-regular btn-info">
                                <i class="icon icon-cloud-download"></i>
                            </a>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </agn:agnForm>

</div>

<script id="modal-preview-image" type="text/x-mustache-template">
    <div class="modal modal-adaptive">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                </div>
                <div class="modal-body">
                    <img src="{{= src }}">
                </div>
            </div>
        </div>
    </div>
</script>

<script id="upload-template-add" type="text/x-mustache-template">
    <tr>
        <td>
            {{ if (preview) { }}
            <img src="{{= preview }}" style="max-width: 250px; max-height: 250px; width: auto; height: auto; margin: 20px;" border="0"/>
            {{ } else { }}
            <img src="<c:url value='/assets/core/images/facelift/no_preview.svg'/>"
                 style="max-width: 250px; max-height: 250px; width: auto; height: auto; margin: 20px;"
                 border="0"/>
            {{ } }}
        </td>
        <td>
            <input type="text" id="descriptionByIndex{{= count }}" name="descriptionByIndex[{{= count }}]" value="" class="form-control">
        </td>
    </tr>
</script>

<%@include file="../fragments/upload-template-progress-fragment.jspf" %>
