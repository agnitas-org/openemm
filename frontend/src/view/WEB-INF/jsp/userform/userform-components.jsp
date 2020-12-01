<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="formId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<%--@elvariable id="imageSrcPattern" type="java.lang.String"--%>
<%--@elvariable id="imageSrcPatternNoCache" type="java.lang.String"--%>
<%--@elvariable id="imageThumbnailPattern" type="java.lang.String"--%>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<div class="row">

    <%-- UPLOADING FORMS--%>
    <%@ include file="userform-components-upload-fragment.jspf" %>

    <c:set var="baseComponentLink" value="${fn:replace(imageSrcPattern, '{form-id}', formId)}"/>

    <%-- LIST FROM --%>
    <mvc:form servletRelativeAction="/webform/${formId}/components/list.action"
              cssClass="form-vertical"
              id="userform-components-from"
              modelAttribute="form">

        <!-- Tile BEGIN -->
        <div class="tile">

            <!-- Tile Header BEGIN -->
            <div class="tile-header">
                <h2 class="headline">
                    <i class="icon icon-image"></i>
                    <mvc:message code="mailing.Graphics_Components"/>
                </h2>

                <ul class="tile-header-actions">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><mvc:message code="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <c:url var="bulkDownload" value="/webform/${formId}/components/bulkDownload.action"/>
                                <a href="${bulkDownload}" data-prevent-load="">
                                    <mvc:message code="mailing.Graphics_Component.bulk.download"/>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>

             <!-- Tile Content BEGIN -->
            <div class="tile-content" data-form-content>
                <div class="table-wrapper">
                    <display:table
                            class="table table-bordered table-striped js-table"
                            id="component"
                            name="components"
                            pagesize="${form.numberOfRows}"
                            excludedParams="*">

                        <c:url var="imageLinkNoCache" value="${fn:replace(imageSrcPatternNoCache, '{name}', component.name)}"/>
                        <c:url var="thumbnailLink" value="${fn:replace(imageThumbnailPattern, '{name}', component.name)}"/>
                        <display:column titleKey="mailing.Graphics_Component" class="align-center" sortable="false">
                            <a href="${imageLinkNoCache}" class="inline-block" data-modal="modal-preview-image"
                                data-modal-set="src: '${imageLinkNoCache}', fileName: '${component.name}', title: '${component.description}'">
                                <img class="l-cp-image" src="${thumbnailLink}" alt="${component.name}" boredr="1" data-display-dimensions="scope: tr">
                            </a>
                        </display:column>

                        <display:column property="name" titleKey="mailing.Graphics_Component" headerClass="js-table-sort"
                                        sortable="true" sortProperty="name" />

                        <display:column property="description" titleKey="Description" headerClass="js-table-sort"
                                        sortable="true" sortProperty="description" />

                        <c:url var="imageLink" value="${fn:replace(imageSrcPattern, '{name}', component.name)}"/>
                        <display:column titleKey="htmled.link" sortable="false">${imageLink}</display:column>

                        <display:column titleKey="mailing.Graphics_Component.AddDate" headerClass="js-table-sort"
                                        sortable="true" sortProperty="creationDate" >
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
                            <c:if test="${not empty component.dataSize}">
                                ${emm:makeUnitSignNumber(component.dataSize, 'B', false, pageContext.request)}
                            </c:if>
                        </display:column>

                        <display:column titleKey="report.data.type" headerClass="js-table-sort"
                                        sortable="true" sortProperty="mimeType" >

                             <c:if test="${not empty component.mimeType and fn:startsWith(component.mimeType, 'image/')}">
                                <span class="badge uppercase">
                                        ${fn:substring(component.mimeType, fn:length('image/'), -1)}
                                </span>
                            </c:if>
                        </display:column>

                        <display:column class="table-actions align-center"
                                        sortable="false">

                            <c:url var="confirmDeleteLink" value="/webform/${formId}/components/${component.name}/confirmDelete.action"/>
                            <a href="${confirmDeleteLink}"
                               class="btn btn-regular btn-alert js-row-delete"
                               data-tooltip="<mvc:message code="mailing.Graphics_Component.delete"/>">
                                <i class="icon icon-trash-o"></i>
                            </a>

                            <a href="${imageLinkNoCache}" class="btn btn-regular btn-info" data-prevent-load="" download="${component.name}"
                               data-tooltip="<mvc:message code='button.Download'/>">
                                <i class="icon icon-cloud-download"></i>
                            </a>
                        </display:column>
                    </display:table>
                </div>
            </div>

        </div>
    </mvc:form>
</div>

<%@ include file="../fragments/modal-preview-image-fragment.jspf" %>

<%@include file="upload-images-template-add-fragment.jspf" %>

<%@include file="../fragments/upload-template-progress-fragment.jspf" %>
