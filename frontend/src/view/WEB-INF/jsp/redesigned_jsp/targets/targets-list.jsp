<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.target.beans.TargetComplexityGrade" %>
<%@ page import="com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="isSearchEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="targetForm" type="com.agnitas.emm.core.target.form.TargetForm"--%>
<%--@elvariable id="targetComplexities" type="java.util.Map<java.lang.Integer, com.agnitas.emm.core.target.beans.TargetComplexityGrade>"--%>
<%--@elvariable id="target" type="com.agnitas.beans.impl.TargetLightImpl"--%>
<%--@elvariable id="isUserBasedFavorites" type="java.lang.Boolean"--%>

<mvc:message var="restoreMsg" code="default.restore" />
<mvc:message var="deleteMsg"  code="target.Delete" />

<c:url var="restoreUrl" value="/target/restore.action" />

<c:set var="deletionAllowed" value="${emm:permissionAllowed('targets.delete', pageContext.request)}" />

<div class="filter-overview" data-controller="targets-list" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/target/list.action" modelAttribute="targetForm" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "target-overview": {
                    "rows-count": ${targetForm.numberOfRows}
                }
            }
        </script>

        <mvc:hidden path="showDeleted" />

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deletionAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:choose>
                                        <c:when test="${targetForm.showDeleted}">
                                            <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}" data-form-submit>
                                                <i class="icon icon-redo"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="bulkDeleteUrl" value="/target/confirm/bulk/delete.action"/>
                                            <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="bulkAction.delete.target" />" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${targetEntries.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${targetEntries.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="target" modelAttribute="targetEntries" cssClass="table table--borderless js-table ${targetForm.showDeleted ? '' : 'table-hover'}">

                        <c:if test="${deletionAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input type="checkbox" class="form-check-input" data-bulk-checkboxes />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}">
                                <input type="checkbox" class="form-check-input" name="bulkIds" value="${target.id}" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column headerClass="fit-content" property="id" titleKey="MailinglistID" />

                        <c:choose>
                            <c:when test="${targetForm.showDeleted}">
                                <emm:column titleKey="autoImport.status" headerClass="fit-content">
                                    <span class="status-badge mailing.status.deleted mx-auto" data-tooltip="<mvc:message code="target.Deleted" />"></span>
                                </emm:column>
                            </c:when>
                            <c:otherwise>
                                <emm:column headerClass="fit-content" sortable="true" titleKey="default.favourite" sortProperty="${isUserBasedFavorites ? 'admin_favorite' : 'favorite'}">
                                    <label class="icon-checkbox">
                                        <input type="checkbox" ${target.favorite ? 'checked' : ''} autocomplete="off"
                                               data-action="${isUserBasedFavorites ? 'check-admin-favorites' : 'check-favorites'}" data-target-id="${target.id}">
                                        <i class="icon icon-star far" data-icon-off></i>
                                        <i class="icon icon-star" data-icon-on></i>
                                    </label>
                                </emm:column>
                            </c:otherwise>
                        </c:choose>

                        <emm:column titleKey="Name" sortable="true" sortProperty="target_shortname">
                            <div class="hstack gap-1 overflow-wrap-anywhere">
                                <c:if test="${target.accessLimitation}">
                                  <span class="icon-badge badge--dark-red" data-tooltip="<mvc:message code="target.limit.access" />">
                                    <i class="icon icon-user-lock"></i>
                                  </span>
                                </c:if>

                                <%-- Show icon for the target which is unavailable in dropdowns--%>
                                <c:if test="${target.componentHide}">
                                    <emm:HideByPermission token="mailing.content.showExcludedTargetgroups">
                                        <i class="icon icon-exclamation-triangle text-danger"
                                           data-tooltip="<mvc:message code="target.tooltip.not_available_in_components"/>"></i>
                                    </emm:HideByPermission>
                                </c:if>
                                <span class="text-truncate-table">${target.targetName}</span>
                            </div>

                            <c:if test="${not targetForm.showDeleted}">
                                <a href='<c:url value="/target/${target.id}/view.action"/>' class="hidden" data-view-row="page"></a>
                            </c:if>
                        </emm:column>

                        <emm:column titleKey="Description" sortable="true" sortProperty="target_description" property="targetDescription" />

                        <emm:column titleKey="target.group.complexity" headerClass="fit-content">
                            <div class="flex-center">
                                <c:set var="complexityGrade" value="${targetComplexities[target.id]}"/>
                                <c:choose>
                                    <c:when test="${target.valid}">
                                        <c:choose>
                                            <c:when test="${complexityGrade eq TargetComplexityGrade.GREEN}">
                                                <span class="status-badge complexity.status.green" data-tooltip="<mvc:message code="target.group.complexity.low"/>"></span>
                                            </c:when>
                                            <c:when test="${complexityGrade eq TargetComplexityGrade.YELLOW}">
                                                <span class="status-badge complexity.status.yellow" data-tooltip="<mvc:message code="warning.target.group.performance.yellow"/>"></span>
                                            </c:when>
                                            <c:when test="${complexityGrade eq TargetComplexityGrade.RED}">
                                                <span class="status-badge complexity.status.red" data-tooltip="<mvc:message code="warning.target.group.performance.red"/>"></span>
                                            </c:when>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status.error" data-tooltip="<mvc:message code="default.invalid"/>"></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </emm:column>

                        <emm:column headerClass="fit-content" titleKey="default.creationDate" sortable="true" property="creationDate" sortProperty="creation_date"/>

                        <emm:column headerClass="fit-content" titleKey="default.changeDate" sortable="true" property="changeDate" sortProperty="change_date"/>

                        <c:if test="${deletionAllowed}">
                            <emm:column>
                                <c:choose>
                                    <c:when test="${targetForm.showDeleted}">
                                        <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}"
                                           data-form-set="bulkIds: ${target.id}" data-form-submit>
                                            <i class="icon icon-redo"></i>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="deletionLink" value="/target/${target.id}/confirm/delete.action" />
                                        <a href="${deletionLink}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </emm:column>
                        </c:if>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/target/search.action" modelAttribute="targetForm"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="" data-editable-tile="">
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
            <c:if test="${isSearchEnabled}">
                <div>
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="name-filter">${nameMsg}</label>
                    <mvc:text id="name-filter" path="searchName" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>

                <div>
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description-filter">${descriptionMsg}</label>
                    <mvc:text id="description-filter" path="searchDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>

                <div>
                    <label for="filter-complexity" class="form-label"><mvc:message code="target.group.complexity" /></label>

                    <mvc:select id="filter-complexity" path="searchComplexity" cssClass="form-control js-select" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
                        <mvc:option value=""><mvc:message code="default.All"/></mvc:option>

                        <c:forEach var="complexity" items="${TargetComplexityGrade.values()}">
                            <mvc:option value="${complexity}" data-badge-class="complexity.status.${fn:toLowerCase(complexity)}">
                                ${complexity}
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div>
                    <label for="filter-delivery" class="form-label"><mvc:message code="Delivery" /></label>

                    <mvc:select id="filter-delivery" path="searchDeliveryOption" cssClass="form-control js-select">
                        <mvc:option value=""><mvc:message code="default.All"/></mvc:option>

                        <c:forEach var="delivery" items="${TargetGroupDeliveryOption.values()}">
                            <mvc:option value="${delivery}"><mvc:message code="${delivery.messageKey}" /></mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div>
                    <label class="form-label" for="filter-creationDate-from"><mvc:message code="default.creationDate" /></label>
                    <mvc:dateRange id="filter-creationDate" path="searchCreationDate" inline="true" options="maxDate: 0" />
                </div>

                <div>
                    <label class="form-label" for="filter-changeDate-from"><mvc:message code="default.changeDate" /></label>
                    <mvc:dateRange id="filter-changeDate" path="searchChangeDate" inline="true" options="maxDate: 0" />
                </div>

                <div class="form-check form-switch">
                    <mvc:checkbox id="filter-show-deleted" path="showDeleted" cssClass="form-check-input" role="switch"/>
                    <label class="form-label form-check-label" for="filter-show-deleted">
                        <mvc:message code="default.list.deleted.show"/>
                    </label>
                </div>
            </c:if>
        </div>
    </mvc:form>
</div>
