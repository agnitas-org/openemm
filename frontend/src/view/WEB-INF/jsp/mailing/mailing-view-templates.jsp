<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingBaseAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ACTION_NEW" value="<%= ComMailingBaseAction.ACTION_NEW %>"/>

<agn:agnForm action="/mailingbase.do?action=${ACTION_NEW}&mailingID=0&isTemplate=false" method="GET" data-form="resource">
    <html:hidden property="keepForward" value="${not empty workflowId and workflowId gt 0 ? true : false}"/>

    <div class="tile" data-sizing="container">
        <div class="tile-header" data-sizing="top">
            <h2 class="headline"><bean:message key="Templates"/></h2>

            <ul class="tile-header-nav">
                <li>
                    <a href="#" data-toggle-tab="#tab-templates-list"><bean:message key="default.list"/></a>
                </li>
                <li class="active">
                    <a href="#" data-toggle-tab="#tab-templates-preview"><bean:message key="default.Preview"/></a>
                </li>
            </ul>

            <ul class="tile-header-actions">
                <li>
                    <button type="button" class="btn btn-primary btn-large" data-form-submit data-form-set="templateID: 0">
                        <span class="text"><bean:message key="button.template.without"/></span>
                        <i class="icon icon-angle-right"></i>
                    </button>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div id="tab-templates-list" class="hidden" data-sizing="scroll">
                <ul class="link-list">
                    <c:forEach var="template" items="${mailingBaseForm.templateMailingBases}">
                        <li>
                            <a href="#" data-form-submit  class="link-list-item" data-form-set="templateID: ${template.id}" data-layout-id="${template.id}" data-action="select-layout">
                                <p class="headline">${template.shortname}</p>
                                <p class="description">
                                    <span data-tooltip="<bean:message key="default.creationDate"/>">
                                        <i class="icon icon-calendar-o"></i>
                                        <strong>${template.creationDate}</strong>
                                    </span>
                                </p>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <div id="tab-templates-preview" class="card-panel hidden" data-sizing="scroll">
                <div class="row flexbox">
                    <c:forEach var="template" items="${mailingBaseForm.templateMailingBases}">
                        <div class="col-xs-6 col-sm-4 col-md-3 card-content">
                            <a href="#" class="card old-cards" data-form-submit data-action="select-layout" data-form-set="templateID: ${template.id}" data-layout-id="${template.id}" data-action="select-layout">
                                <c:choose>
                                    <c:when test="${template.previewComponentId eq 0}">
                                        <img class="card-image" src="${emmLayoutBase.imagesURL}/facelift/no_preview.png"/>
                                    </c:when>
                                    <c:otherwise>
                                        <img class="card-image" src="<html:rewrite page="/sc?compID=${template.previewComponentId}" />"/>
                                    </c:otherwise>
                                </c:choose>
                                <div class="card-body">
                                    <strong class="headline">
                                            ${template.shortname}
                                    </strong>

                                    <p class="description">
                                    <span data-tooltip="<bean:message key="default.creationDate"/>">
                                        <i class="icon icon-calendar-o"></i>
                                        <strong>${template.creationDate}</strong>
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
            <div class="btn-group pull-right">
                <button type="button" class="btn btn-primary btn-large" data-form-submit data-form-set="templateID: 0">
                    <span class="text"><bean:message key="button.template.without"/></span>
                    <i class="icon icon-angle-right"></i>
                </button>
            </div>
        </div>
    </div>
</agn:agnForm>
