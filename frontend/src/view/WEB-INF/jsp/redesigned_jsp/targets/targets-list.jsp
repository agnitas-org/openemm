<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ page import="com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption" %>
<%@ taglib uri="http://displaytag.sf.net"               prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="isSearchEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="targetForm" type="com.agnitas.emm.core.target.form.TargetForm"--%>
<%--@elvariable id="targetComplexities" type="java.util.Map<java.lang.Integer, com.agnitas.emm.core.target.beans.TargetComplexityGrade>"--%>
<%--@elvariable id="target" type="com.agnitas.beans.impl.TargetLightImpl"--%>
<%--@elvariable id="isUserBasedFavorites" type="java.lang.Boolean"--%>

<c:set var="COMPLEXITIES" value="<%= TargetComplexityGrade.values() %>" scope="page"/>
<c:set var="DELIVERY_OPTIONS" value="<%= TargetGroupDeliveryOption.values() %>" scope="page"/>
<c:set var="COMPLEXITY_RED" value="<%= TargetComplexityGrade.RED %>" scope="page"/>
<c:set var="COMPLEXITY_YELLOW" value="<%= TargetComplexityGrade.YELLOW %>" scope="page"/>
<c:set var="COMPLEXITY_GREEN" value="<%= TargetComplexityGrade.GREEN %>" scope="page"/>

<mvc:message var="targetDeleteMessage" code="target.Delete" />

<div class="filter-overview hidden" data-controller="targets-list" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/target/list.action" modelAttribute="targetForm" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "target-overview": {
                    "rows-count": ${targetForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table" id="target" name="targetEntries" sort="external"
                                   requestURI="/target/list.action" partialList="true" size="${targetForm.numberOfRows}" excludedParams="*">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <emm:ShowByPermission token="targets.delete">
                            <c:set var="checkboxSelectAll">
                                <input type="checkbox" class="form-check-input" data-form-bulk="bulkIds"/>
                            </c:set>

                            <display:column title="${checkboxSelectAll}" class="js-checkable" sortable="false" headerClass="bulk-ids-column fit-content">
                                <input type="checkbox" class="form-check-input" name="bulkIds" value="${target.id}"/>
                            </display:column>
                        </emm:ShowByPermission>

                        <display:column headerClass="js-table-sort fit-content" property="id" titleKey="MailinglistID"/>

                        <display:column class="js-checkable align-center" headerClass="js-table-sort fit-content" sortable="true" titleKey="default.favourite" sortProperty="favorite">
                            <label class="icon-checkbox text-primary">
                                <input type="checkbox" ${target.favorite ? 'checked' : ''} autocomplete="off"
                                       data-action="${isUserBasedFavorites ? 'check-admin-favorites' : 'check-favorites'}" data-target-id="${target.id}">
                                <i class="icon icon-star far" data-icon-off></i>
                                <i class="icon icon-star" data-icon-on></i>
                            </label>
                        </display:column>

                        <display:column headerClass="js-table-sort" titleKey="Name" sortable="true" sortProperty="target_shortname">
                            <%-- Show icon for the target which is unavailable in dropdowns--%>
                            <c:if test="${target.componentHide}">
                                <emm:HideByPermission token="mailing.content.showExcludedTargetgroups">
                                    <i class="icon icon-exclamation-triangle text-danger"
                                       data-tooltip="<mvc:message code="target.tooltip.not_available_in_components"/>"></i>
                                </emm:HideByPermission>
                            </c:if>
                            <span>${target.targetName}</span>
                        </display:column>

                        <display:column headerClass="js-table-sort" titleKey="Description" sortable="true" sortProperty="target_description" property="targetDescription" />

                        <display:column class="align-center bold" titleKey="target.group.complexity" sortable="false" headerClass="fit-content">
                            <div class="flex-center">
                                <c:set var="complexityGrade" value="${targetComplexities[target.id]}"/>
                                <c:choose>
                                    <c:when test="${target.valid}">
                                        <c:choose>
                                            <c:when test="${complexityGrade eq COMPLEXITY_GREEN}">
                                                <span class="status-badge complexity.status.green" data-tooltip="<mvc:message code="target.group.complexity.low"/>"></span>
                                            </c:when>
                                            <c:when test="${complexityGrade eq COMPLEXITY_YELLOW}">
                                                <span class="status-badge complexity.status.yellow" data-tooltip="<mvc:message code="warning.target.group.performance.yellow"/>"></span>
                                            </c:when>
                                            <c:when test="${complexityGrade eq COMPLEXITY_RED}">
                                                <span class="status-badge complexity.status.red" data-tooltip="<mvc:message code="warning.target.group.performance.red"/>"></span>
                                            </c:when>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status.error" data-tooltip="<mvc:message code="default.invalid"/>"></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </display:column>

                        <display:column headerClass="js-table-sort fit-content" titleKey="default.creationDate" sortable="true"
                                        format="{0, date, ${adminDateFormat}}" property="creationDate" sortProperty="creation_date"/>

                        <display:column headerClass="js-table-sort fit-content" titleKey="default.changeDate" sortable="true"
                                        format="{0, date, ${adminDateFormat}}" property="changeDate" sortProperty="change_date"/>

                        <display:column headerClass="fit-content">
                            <a href='<c:url value="/target/${target.id}/view.action"/>' class="hidden" data-view-row="page"></a>

                            <emm:ShowByPermission token="targets.delete">
                                <c:url var="deletionLink" value="/target/${target.id}/confirm/delete.action" />
                                <a href="${deletionLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${targetDeleteMessage}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:ShowByPermission>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/target/search.action" modelAttribute="targetForm"
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
                <c:if test="${isSearchEnabled}">
                    <div class="col-12">
                        <mvc:message var="nameMsg" code="Name"/>
                        <label class="form-label" for="name-filter">${nameMsg}</label>
                        <mvc:text id="name-filter" path="searchName" cssClass="form-control" placeholder="${nameMsg}"/>
                    </div>

                    <div class="col-12">
                        <mvc:message var="descriptionMsg" code="Description"/>
                        <label class="form-label" for="description-filter">${descriptionMsg}</label>
                        <mvc:text id="description-filter" path="searchDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
                    </div>

                    <div class="col-12">
                        <label for="filter-complexity" class="form-label"><mvc:message code="target.group.complexity" /></label>

                        <mvc:select id="filter-complexity" path="searchComplexity" cssClass="form-control js-select" data-result-template="target-complexity-badge-selection">
                            <mvc:option value=""><mvc:message code="default.All"/></mvc:option>

                            <c:forEach var="complexity" items="${COMPLEXITIES}">
                                <mvc:option value="${complexity}">${complexity}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>

                    <div class="col-12">
                        <label for="filter-delivery" class="form-label"><mvc:message code="Delivery" /></label>

                        <mvc:select id="filter-delivery" path="searchDeliveryOption" cssClass="form-control js-select">
                            <mvc:option value=""><mvc:message code="default.All"/></mvc:option>

                            <c:forEach var="delivery" items="${DELIVERY_OPTIONS}">
                                <mvc:option value="${delivery}"><mvc:message code="${delivery.messageKey}" /></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="filter-changeDate-from"><mvc:message code="default.creationDate" /></label>

                        <div class="inline-input-range" data-date-range>
                            <div class="date-picker-container">
                                <mvc:message var="fromMsg" code="From" />
                                <mvc:text id="filter-creationDate-from" path="searchCreationDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                            </div>
                            <div class="date-picker-container">
                                <mvc:message var="toMsg" code="To" />
                                <mvc:text id="filter-creationDate-to" path="searchCreationDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                            </div>
                        </div>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="filter-changeDate-from"><mvc:message code="default.changeDate" /></label>

                        <div class="inline-input-range" data-date-range>
                            <div class="date-picker-container">
                                <mvc:text id="filter-changeDate-from" path="searchChangeDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                            </div>
                            <div class="date-picker-container">
                                <mvc:text id="filter-changeDate-to" path="searchChangeDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </mvc:form>
</div>

<script id="target-complexity-badge-selection" type="text/x-mustache-template">
    <div class="d-flex align-items-center gap-1">
        {{ if (value) { }}
            <span class="status-badge complexity.status.{{- value.toLowerCase() }}"></span>
        {{ } }}
        <span>{{- text }}</span>
    </div>
</script>
