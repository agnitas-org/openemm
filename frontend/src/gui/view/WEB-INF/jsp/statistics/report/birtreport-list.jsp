<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="birtReportsForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportOverviewFilter"--%>
<%--@elvariable id="report" type="com.agnitas.emm.core.birtreport.bean.ReportEntry"--%>
<%--@elvariable id="reports" type="com.agnitas.beans.PaginatedList<com.agnitas.emm.core.birtreport.bean.ReportEntry>"--%>
<%--@elvariable id="dateFormat" type="java.text.SimpleDateFormat"--%>

<mvc:message var="deleteMessage" code="Delete" />
<mvc:message var="restoreMsg" code="default.restore" />
<c:set var="allowedDeletion" value="${emm:permissionAllowed('report.birt.delete', pageContext.request)}" />
<c:url var="deleteUrl" value="/statistics/report/delete.action" />
<c:url var="restoreUrl" value="/statistics/report/restore.action"/>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/statistics/reports.action" modelAttribute="birtReportsForm" method="GET" cssClass="tile" data-editable-tile="main">

        <input type="hidden" name="page" value="${reports.pageNumber}"/>
        <input type="hidden" name="sort" value="${reports.sortCriterion}"/>
        <input type="hidden" name="dir" value="${reports.sortDirection}"/>
        <mvc:hidden path="showDeleted" />

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "birt-report-overview": {
                    "rows-count": ${birtReportsForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${allowedDeletion}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:choose>
                                        <c:when test="${birtReportsForm.showDeleted}">
                                            <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}" data-form-method="POST" data-form-submit>
                                                <i class="icon icon-redo"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMessage}" data-form-url="${deleteUrl}" data-form-confirm>
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${reports.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${reports.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="report" modelAttribute="reports" cssClass="table table--borderless js-table ${birtReportsForm.showDeleted ? '' : 'table-hover'}">

                        <c:if test="${allowedDeletion}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${report.id}" autocomplete="off" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="Name" sortable="true" property="shortname" />
                        <emm:column titleKey="Description" sortable="true" property="description" />

                        <emm:column titleKey="default.changeDate" headerClass="fit-content" sortable="true" sortProperty="change_date" property="changeDate" />
                        <emm:column titleKey="mailing.LastDelivery" headerClass="fit-content" sortable="true" sortProperty="delivery_date" property="deliveryDate" />

                        <emm:column headerClass="${allowedDeletion ? '' : 'hidden'}" cssClass="${allowedDeletion ? '' : 'hidden'}">
                            <c:if test="${not birtReportsForm.showDeleted}">
                                <a href='<c:url value="/statistics/report/${report.id}/view.action"/>' class="hidden" data-view-row="page"></a>
                            </c:if>
                            <c:choose>
                                <c:when test="${birtReportsForm.showDeleted}">
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}" data-form-method="POST"
                                       data-form-set="bulkIds: ${report.id}" data-form-submit>
                                        <i class="icon icon-redo"></i>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${allowedDeletion}">
                                        <a href="${deleteUrl}?bulkIds=${report.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMessage}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/statistics/reports/search.action" modelAttribute="birtReportsForm"
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
        <div class="tile-body form-column js-scrollable">
            <div>
                <mvc:message var="nameMsg" code="Name" />
                <label for="filter-name" class="form-label">${nameMsg}</label>
                <mvc:text id="filter-name" path="name" cssClass="form-control" placeholder="${nameMsg}"/>
            </div>

            <div>
                <label class="form-label" for="filter-changeDate-from"><mvc:message code="default.changeDate"/></label>
                <mvc:dateRange id="filter-changeDate" path="changeDate" options="maxDate: 0" />
            </div>

            <div>
                <label class="form-label" for="filter-lastDeliveryDate-from"><mvc:message code="mailing.LastDelivery"/></label>
                <mvc:dateRange id="filter-lastDeliveryDate" path="lastDeliveryDate" options="maxDate: 0" />
            </div>

            <div class="form-check form-switch">
                <mvc:checkbox id="filter-show-deleted" path="showDeleted" cssClass="form-check-input" role="switch"/>
                <label class="form-label form-check-label" for="filter-show-deleted">
                    <mvc:message code="default.list.deleted.show"/>
                </label>
            </div>
        </div>
    </mvc:form>
</div>
