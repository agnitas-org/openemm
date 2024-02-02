<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="templateMailingBases" type="java.util.List<org.agnitas.beans.MailingBase>"--%>

<mvc:form servletRelativeAction="/mailing/new.action?keepForward=${workflowId > 0}" method="GET" data-form="resource">
    <div class="tile" data-sizing="container">
        <div class="tile-header" data-sizing="top">
            <h2 class="headline"><mvc:message code="Templates"/></h2>

            <ul class="tile-header-nav">
                <li>
                    <a href="#" data-toggle-tab="#tab-templates-list"><mvc:message code="default.list"/></a>
                </li>
                <li class="active">
                    <a href="#" data-toggle-tab="#tab-templates-preview"><mvc:message code="default.Preview"/></a>
                </li>
            </ul>

            <ul class="tile-header-actions">
                <emm:HideByPermission token="mailing.settings.hide">
                <li>
                    <button type="button" class="btn btn-primary btn-large" data-form-submit data-form-set="templateID: 0">
                        <span class="text"><mvc:message code="button.template.without"/></span>
                        <i class="icon icon-angle-right"></i>
                    </button>
                </li>
                </emm:HideByPermission>
            </ul>
        </div>

        <div class="tile-content">
            <div id="tab-templates-list" class="hidden" data-sizing="scroll">
                <ul class="link-list">
                    <c:forEach var="template" items="${templateMailingBases}">
                        <c:set var="creationDateTimeStamp" value="${template.creationDate}" />
                        <fmt:parseDate value="${creationDateTimeStamp}" var="creationDateParsed" pattern="yyyy-MM-dd HH:mm:ss" />
                        <fmt:formatDate value="${creationDateParsed}" var="creationDateFormatted" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
                        
                        <li>
                            <a href="#" data-form-submit  class="link-list-item" data-form-set="templateId: ${template.id}" data-layout-id="${template.id}">
                                <p class="headline">${template.shortname}</p>
                                <p class="description">
                                    <span data-tooltip="<mvc:message code="default.creationDate"/>">
                                        <i class="icon icon-calendar-o"></i>
                                        <strong>${creationDateFormatted}</strong>
                                    </span>
                                </p>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <div id="tab-templates-preview" class="card-panel" data-sizing="scroll">
                <div class="row flexbox">
                    <c:forEach var="template" items="${templateMailingBases}">
                        <c:set value="${template.creationDate}" var="creationDateTimeStamp" />
                        <fmt:parseDate value="${creationDateTimeStamp}" var="creationDateParsed" pattern="yyyy-MM-dd HH:mm:ss" />
                        <fmt:formatDate value="${creationDateParsed}" var="creationDateFormatted" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" />
                        
                        <div class="col-xs-6 col-sm-4 col-md-3 card-content">
                            <a href="#" class="card old-cards" data-form-submit data-form-set="templateId: ${template.id}" data-layout-id="${template.id}">
                                <c:choose>
                                    <c:when test="${template.onlyPostType}">
                                        <c:url var="previewImageSrc" value="assets/core/images/facelift/post_thumbnail.jpg"/>
                                    </c:when>
                                    <c:when test="${template.previewComponentId eq 0}">
                                        <c:url var="previewImageSrc" value="/assets/core/images/facelift/no_preview.png"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="previewImageSrc" value="/sc?compID=${template.previewComponentId}"/>
                                    </c:otherwise>
                                </c:choose>
                                <img class="card-image" src="${previewImageSrc}"/>
                                <div class="card-body">
                                    <strong class="headline">
                                            ${template.shortname}
                                    </strong>

                                    <p class="description">
                                    <span data-tooltip="<mvc:message code="default.creationDate"/>">
                                        <i class="icon icon-calendar-o"></i>
                                        <strong>${creationDateFormatted}</strong>
                                    </span>
                                    </p>
                                </div>
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>

        <div class="tile-footer" data-sizing="bottom">
            <emm:HideByPermission token="mailing.settings.hide">
                <div class="btn-group pull-right">
                    <button type="button" class="btn btn-primary btn-large" data-form-submit data-form-set="templateID: 0">
                        <span class="text"><mvc:message code="button.template.without"/></span>
                        <i class="icon icon-angle-right"></i>
                    </button>
                </div>
            </emm:HideByPermission>
        </div>
    </div>
</mvc:form>
