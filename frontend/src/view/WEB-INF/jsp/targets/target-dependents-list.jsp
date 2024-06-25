<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="dependentsForm" type="com.agnitas.emm.core.target.form.TargetDependentsListForm"--%>
<%--@elvariable id="dependents" type="java.util.List<com.agnitas.emm.core.beans.Dependent<com.agnitas.emm.core.target.beans.TargetGroupDependentType>>"--%>
<%--@elvariable id="mailingGridTemplateMap" type="java.util.Map<java.lang.Integer, java.lang.Integer>"--%>

<mvc:form servletRelativeAction="/target/${dependentsForm.targetId}/dependents.action" modelAttribute="dependentsForm">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "target-dependents-overview": {
                "rows-count": ${dependentsForm.numberOfRows},
                "types": ${emm:toJson(dependentsForm.filterTypes)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.usedIn"/>
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
                        </li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-change data-form-submit type="button">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
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
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="MAILING" data-field-filter=""/>
                                <mvc:message code="Mailings"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="REPORT" data-field-filter=""/>
                                <mvc:message code="Reports"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="EXPORT_PROFILE" data-field-filter=""/>
                                <mvc:message code="export.ExportProfile"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="MAILING_CONTENT" data-field-filter=""/>
                                <mvc:message code="mailing.searchContent"/>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterTypes: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change="" data-form-submit="">
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Apply"/></span>
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
                        list="${dependents}"
                        pagesize="${dependentsForm.numberOfRows}"
                        sort="external"
                        excludedParams="*"
                        requestURI="/target/${dependentsForm.targetId}/dependents.action"
                        partialList="true"
                        size="${dependents.fullListSize}">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:column headerClass="js-table-sort js-filter-type" sortProperty="type" sortable="true" titleKey="default.Type">
                        <c:choose>
                            <c:when test="${item.type == 'MAILING'}">
                                <emm:ShowByPermission token="mailing.show">
                                    <a href="<c:url value="/mailing/${item.id}/settings.action"/>" class="hidden js-row-show"></a>
                                </emm:ShowByPermission>
                                <mvc:message code="Mailings"/>
                            </c:when>
                            <c:when test="${item.type == 'REPORT'}">
                                <emm:ShowByPermission token="report.birt.show">
                                    <a href="<c:url value='/statistics/report/${item.id}/view.action'/>" class="hidden js-row-show" ></a>
                                </emm:ShowByPermission>
                                <mvc:message code="Reports"/>
                            </c:when>
                            <c:when test="${item.type == 'EXPORT_PROFILE'}">
                                <emm:ShowByPermission token="wizard.export">
                                    <a href="<c:url value='/export/${item.id}/view.action'/>" class="hidden js-row-show"></a>
                                </emm:ShowByPermission>
                                <mvc:message code="export.ExportProfile"/>
                            </c:when>
                            <c:when test="${item.type == 'MAILING_CONTENT'}">
                                <emm:ShowByPermission token="mailing.content.show">
                                    <c:set var="templateId" value="${mailingGridTemplateMap[item.id]}"/>
                                    <c:choose>
                                        <c:when test="${templateId > 0}">
                                            <a href="<c:url value='/layoutbuilder/template/${templateId}/view.action'/>" class="hidden js-row-show" ></a>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="contentViewLink" value='/mailing/content/${item.id}/view.action'/>
                                            <a href="${contentViewLink}" class="hidden js-row-show" ></a>
                                        </c:otherwise>
                                    </c:choose>
                                </emm:ShowByPermission>
                                <mvc:message code="mailing.searchContent"/>
                            </c:when>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="js-table-sort" property="shortname" sortProperty="name" sortable="true" titleKey="Name" escapeXml="true"/>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
