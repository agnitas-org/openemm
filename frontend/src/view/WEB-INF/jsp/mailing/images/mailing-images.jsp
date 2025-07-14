<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.beans.MailingComponentType" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.UploadMailingImagesForm"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="image" type="com.agnitas.emm.core.components.dto.MailingImageDto"--%>
<%--@elvariable id="images" type="java.util.List<com.agnitas.emm.core.components.dto.MailingImageDto>"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="emmLayoutBase" type="com.agnitas.beans.EmmLayoutBase"--%>

<c:set var="MAILING_COMPONENT_IMAGE_TYPE" value="<%= MailingComponentType.Image %>" scope="request"/>
<c:set var="MAILING_COMPONENT_HOSTED_IMAGE_TYPE" value="<%= MailingComponentType.HostedImage %>" scope="request"/>
<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK %>" scope="page"/>
<c:set var="DARK_MODE_THEME_TYPE_2" value="<%= EmmLayoutBase.ThemeType.DARK_CONTRAST%>" scope="page"/>
<c:set var="isDarkmode" value="${emmLayoutBase.themeType eq DARK_MODE_THEME_TYPE or emmLayoutBase.themeType eq DARK_MODE_THEME_TYPE_2}"/>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-imageUpload">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="mailing.Graphics_Component.imageUpload"/>
        </a>
        <ul class="tile-header-nav">
            <emm:ShowByPermission token="mailing.components.change">
                <li class="active">
                    <a href="#" data-toggle-tab="#tab-imageUploadSingleImage"><mvc:message code="mailing.files"/></a>
                </li>
            </emm:ShowByPermission>
            <%@include file="fragments/mailing-images-sftp-tab.jspf" %>
        </ul>
    </div>

    <div id="tile-imageUpload" class="tile-content">
        <emm:ShowByPermission token="mailing.components.change">
            <%@include file="fragments/mailing-images-upload-files.jspf" %>
        </emm:ShowByPermission>
        <%@include file="fragments/mailing-images-upload-sftp.jspf" %>
    </div>
</div>

<mvc:form servletRelativeAction="/mailing/${mailingId}/images/list.action" class="form-vertical" data-form="resource" method="GET" modelAttribute="filter">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-images-overview": {
                "rows-count": ${filter.numberOfRows}
            }
        }
    </script>

    <mvc:hidden path="page"/>
    <mvc:hidden path="sort"/>
    <mvc:hidden path="dir"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <i class="icon icon-image"></i>
                <mvc:message code="mailing.Graphics_Components"/>
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text"><mvc:message code="bulkAction"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="#" data-form-url="<c:url value='/mailing/${mailingId}/images/bulkDownload.action'/>" data-prevent-load="" data-form-submit-static>
                                <mvc:message code="bulkAction.download.image.selected"/>
                            </a>
                        </li>
                        <li>
                            <a href="#" data-form-url="<c:url value='/mailing/${mailingId}/images/confirmBulkDelete.action'/>" data-form-confirm>
                                <mvc:message code="bulkAction.delete.image"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped js-table"
                               id="image"
                               name="images"
                               requestURI="/mailing/${mailingId}/images/list.action"
                               excludedParams="*"
                               pagesize="${filter.numberOfRows}">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>
                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <c:set var="checkboxSelectAll"><input type="checkbox" data-form-bulk="bulkIds"/></c:set>
        
                    <display:column title="${checkboxSelectAll}" class="js-checkable" sortable="false" headerClass="squeeze-column">
                        <input type="checkbox" name="bulkIds" value="${image.id}"/>
                    </display:column>

                    <c:url var="sourceSrc" value="/sc?compID=${image.id}"/>
                    <display:column titleKey="mailing.Graphics_Component" sortable="false" class="align-center" style="${isDarkmode ? '' : 'background-color: #e2e3e3'}">
                        <a href="${sourceSrc}" data-modal="modal-preview-image"
                           data-modal-set="src: '${sourceSrc}', fileName: '${image.name}', title: '${image.description}'"
                           class="inline-block">
                            <img data-display-dimensions="scope: tr" src="${sourceSrc}" alt="${image.description}"
                                 style="max-width: 100px; max-height: 150px; width: auto; height: auto; display: block; border: 1px"/>
                        </a>
                    </display:column>

                    <display:column titleKey="settings.FileName" sortable="true" sortProperty="compname" headerClass="js-table-sort">
                        <span class="multiline-auto">${image.name}</span>
                    </display:column>

                    <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                        <span class="multiline-auto">${image.description}</span>
                    </display:column>

                    <display:column titleKey="htmled.link" sortable="false">
                        <c:if test="${not empty image.link}">
                            <a class="btn btn-regular" target="_blank" href="${image.link.fullUrl}" data-tooltip="${image.link.fullUrl}">
                                <i class="icon icon-share-square-o"></i>
                            </a>
                        </c:if>
                    </display:column>

                    <display:column titleKey="mailing.Graphics_Component.AddDate" sortable="true" sortProperty="timestamp" headerClass="js-table-sort">
                        <i class="icon icon-calendar"></i>
                        <fmt:formatDate value="${image.creationDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>
                        &nbsp;<i class="icon icon-clock-o"></i>
                        <fmt:formatDate value="${image.creationDate}" pattern="${adminTimeFormat}" timeZone="${adminTimeZone}"/>
                    </display:column>

                    <display:column titleKey="default.Type" sortable="false">
                        <mvc:message code="${image.mobile ? 'Mobile' : 'predelivery.desktop'}"/>
                    </display:column>

                    <display:column titleKey="mailing.Graphics_Component.Dimensions" sortable="false">
                        <i class="icon icon-expand"></i>
                        <span data-dimensions></span>
                    </display:column>
                    
                    <display:column titleKey="default.Size" sortable="true" sortProperty="comp_size" headerClass="js-table-sort">
                        <c:if test="${not empty image.size}">
                            ${emm:formatBytes(image.size, 1, '', emm:getLocale(pageContext.request))}
                        </c:if>
                    </display:column>
 
                    <display:column titleKey="report.data.type" sortable="true" sortProperty="mtype" headerClass="js-table-sort">
                        <c:if test="${not empty image.mimeType and fn:startsWith(image.mimeType, 'image')}">
                            <span class="badge">${fn:toUpperCase(fn:substring(image.mimeType, 6, -1))}</span>
                        </c:if>
                    </display:column>
                    
                    <display:column class="table-actions" titleKey="action.Action" sortable="false">
                        <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}">
                            <c:url var="sourceSrc" value="/sc?compID=${image.id}"/>
                            <a href="#" data-modal="image-editor-modal" data-tooltip="<mvc:message code="button.Edit"/>"
                               data-modal-set="src: ${sourceSrc}, contentType: ${image.mimeType}, imageId: ${image.id}"
                               class="btn btn-regular btn-secondary	">
                                <i class="icon icon-pencil"></i>
                            </a>
                        </c:if>
                        <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE || image.present == 0}">
                            <a href="<c:url value='/mailing/${mailingId}/images/${image.id}/confirmDelete.action'/>" 
                               data-tooltip="<mvc:message code='mailing.Graphics_Component.delete'/>" class="btn btn-regular btn-alert js-row-delete">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>
                        <c:if test="${image.type == MAILING_COMPONENT_IMAGE_TYPE}">
                            <a href="<c:url value='/mailing/${mailingId}/images/${image.id}/reload.action'/>"
                               data-tooltip="<mvc:message code='mailing.Graphics_Component.Update'/>" class="btn btn-regular btn-info">
                                <i class="icon icon-refresh"></i>
                            </a>
                        </c:if>
                        <c:if test="${image.type == MAILING_COMPONENT_HOSTED_IMAGE_TYPE}">
                            <a href="<c:url value='/dc?compID=${image.id}'/>"
                               data-tooltip="<mvc:message code='button.Download'/>" data-prevent-load class="btn btn-regular btn-info">
                                <i class="icon icon-cloud-download"></i>
                            </a>
                        </c:if>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>

<%@ include file="fragments/image-editor-modal.jspf" %>
<%@ include file="fragments/upload-image-template-add.jspf" %>
<%@ include file="../../fragments/modal-preview-image-fragment.jspf" %>
<%@ include file="../../fragments/upload-template-progress-fragment.jspf" %>
