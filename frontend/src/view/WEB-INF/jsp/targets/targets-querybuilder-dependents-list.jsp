<%@ page language="java" import="com.agnitas.web.ComMailingBaseAction" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="editTargetForm" type="com.agnitas.emm.core.target.web.QueryBuilderTargetGroupForm"--%>
<%--@elvariable id="mailingGridTemplateMap" type="java.util.Map<java.lang.Integer, java.lang.Integer>"--%>

<c:set var="ACTION_MAILING_VIEW" value="<%= ComMailingBaseAction.ACTION_VIEW %>" scope="page"/>
<c:set var="ACTION_MAILING_VIEW_CONTENT" value="<%= ComMailingContentAction.ACTION_VIEW_CONTENT %>" scope="page"/>

<agn:agnForm action="/targetQB" id="targetForm">
    <html:hidden property="targetID"/>
    <html:hidden property="method"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "target-dependents-overview": {
                "rows-count": ${editTargetForm.numberOfRows},
                "types": ${emm:toJson(editTargetForm.filterTypes)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="default.usedIn"/>
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>

                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <!-- Filters -->
            <div class="hidden">
                <!-- dropdown for type -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-type">
                    <html:hidden property="__STRUTS_MULTIPLE_filterTypes" value=""/>

                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="MAILING" data-field-filter=""/>
                                <bean:message key="Mailings"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="REPORT" data-field-filter=""/>
                                <bean:message key="Reports"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="EXPORT_PROFILE" data-field-filter=""/>
                                <bean:message key="export.ExportProfile"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <agn:agnCheckbox property="filterTypes" value="MAILING_CONTENT" data-field-filter=""/>
                                <bean:message key="mailing.searchContent"/>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterTypes: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="table-wrapper table-overflow-visible">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="item"
                        list="${editTargetForm.dependents}"
                        pagesize="${editTargetForm.numberOfRows}"
                        sort="external"
                        excludedParams="*"
                        requestURI="/targetQB.do?method=listDependents&targetID=${editTargetForm.targetID}&__fromdisplaytag=true"
                        partialList="true"
                        size="${editTargetForm.dependents.fullListSize}">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:column headerClass="js-table-sort js-filter-type" sortProperty="type" sortable="true" titleKey="default.Type">
                        <c:choose>
                            <c:when test="${item.type == 'MAILING'}">
                                <emm:ShowByPermission token="mailing.show">
                                    <html:link styleClass="hidden js-row-show"
                                               page="/mailingbase.do?action=${ACTION_MAILING_VIEW}&mailingID=${item.id}"/>
                                </emm:ShowByPermission>
                                <bean:message key="Mailings"/>
                            </c:when>
                            <c:when test="${item.type == 'REPORT'}">
                                <emm:ShowByPermission token="report.birt.show">
                                    <html:link styleClass="hidden js-row-show" page="/statistics/report/${item.id}/view.action"/>
                                </emm:ShowByPermission>
                                <bean:message key="Reports"/>
                            </c:when>
                            <c:when test="${item.type == 'EXPORT_PROFILE'}">
                                <emm:ShowByPermission token="wizard.export">
                                    <html:link styleClass="hidden js-row-show"
                                               page="/exportwizard.do?action=2&exportPredefID=${item.id}"/>
                                </emm:ShowByPermission>
                                <bean:message key="export.ExportProfile"/>
                            </c:when>
                            <c:when test="${item.type == 'MAILING_CONTENT'}">
                                <emm:ShowByPermission token="mailing.content.show">
                                    <c:set var="templateId" value="${mailingGridTemplateMap[item.id]}"/>
                                    <c:choose>
                                        <c:when test="${templateId > 0}">
                                            <html:link styleClass="hidden js-row-show" page="/layoutbuilder/template/${templateId}/view.action"/>
                                        </c:when>
                                        <c:otherwise>
                                            <html:link styleClass="hidden js-row-show" page="/mailingcontent.do?action=${ACTION_MAILING_VIEW_CONTENT}&mailingID=${item.id}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </emm:ShowByPermission>
                                <bean:message key="mailing.searchContent"/>
                            </c:when>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="js-table-sort" property="shortname" sortProperty="name" sortable="true" titleKey="Name" escapeXml="true"/>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
