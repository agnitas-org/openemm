<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.recipientsreport.bean.RecipientsReport" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="IMPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.IMPORT %>" />
<c:set var="EXPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.EXPORT %>" />

<c:set var="REFERENCE_TABLE_ENTITY_DATA" value="<%= RecipientsReport.EntityData.REFERENCE_TABLE %>" />
<c:set var="PROFILE_ENTITY_DATA"         value="<%= RecipientsReport.EntityData.PROFILE %>" />

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm"--%>
<%--@elvariable id="reportsList" type="com.agnitas.beans.PaginatedList"--%>
<%--@elvariable id="users" type="java.util.List<>com.agnitas.beans.AdminEntry"--%>

<mvc:message var="downloadMsg" code="button.Download" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" method="GET" servletRelativeAction="/recipientsreport/list.action"
              modelAttribute="recipientsReportForm" data-editable-tile="main">
        <div class="tile-body" data-form-content="">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "import-export-log-overview": {
                        "rows-count": ${recipientsReportForm.numberOfRows}
                    }
                }
            </script>
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <div class="bulk-actions hidden">
                            <p class="bulk-actions__selected">
                                <span><%-- Updates by JS --%></span>
                                <mvc:message code="default.list.entry.select" />
                            </p>
                            <div class="bulk-actions__controls">
                                <c:url var="bulkDownloadUrl" value="/recipientsreport/bulk/download.action"/>
                                <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${downloadMsg}" data-form-url="${bulkDownloadUrl}" data-prevent-load data-form-submit-static>
                                    <i class="icon icon-file-download"></i>
                                </a>
                            </div>
                        </div>

                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${reportsList.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${reportsList.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="report" modelAttribute="reportsList" cssClass="table table-hover table--borderless js-table">

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                        </c:set>

                        <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${report.id}" autocomplete="off" data-bulk-checkbox />
                        </emm:column>

                        <emm:column sortable="true" titleKey="report.mailing.statistics.reportdatum" sortProperty="report_date" property="reportDateFormatted" />

                        <emm:column sortable="true" titleKey="default.Type" sortProperty="entity_type">
                            <span><mvc:message code="${report.entityType.messageKey}"/></span>
                        </emm:column>

                        <emm:column sortable="true" titleKey="import.datatype" sortProperty="entity_data">
                            <span>
                                <c:choose>
                                    <c:when test="${report.entityData eq REFERENCE_TABLE_ENTITY_DATA}">
                                        <mvc:message code="ReferenceTable" />
                                    </c:when>
                                    <c:when test="${report.entityData eq PROFILE_ENTITY_DATA}">
                                        <mvc:message code="Recipients" />
                                    </c:when>
                                    <c:otherwise>
                                        <mvc:message code="MailType.unknown" />
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </emm:column>

                        <emm:column sortable="true" titleKey="settings.FileName" property="filename" />
                        <emm:column sortable="true" titleKey="recipient.DatasourceId" sortProperty="datasource_id" property="datasourceId" />
                        <emm:column sortable="true" titleKey="recipients.report.username" property="username" />

                        <emm:column>
                            <c:url var="download_link" value="/recipientsreport/${report.id}/download.action"/>

                            <a href="${download_link}" class="icon-btn icon-btn--primary" data-tooltip="${downloadMsg}" data-prevent-load="">
                                <i class="icon icon-download"></i>
                            </a>
                            <a href='<c:url value="/recipientsreport/${report.id}/view.action"/>' class="hidden" data-view-row></a>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/recipientsreport/search.action" modelAttribute="recipientsReportForm"
              data-toggle-tile=""
              data-form="resource"
              data-resource-selector="#table-tile" data-editable-tile="">
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
            <div>
                <label class="form-label" for="dateStart-filter-from"><mvc:message code="report.mailing.statistics.reportdatum"/></label>
                <mvc:dateRange id="dateStart-filter" inline="true" path="reportDate" options="maxDate: 0" />
            </div>
            <div>
                <label class="form-label" for="types-filter"><mvc:message code="default.Type"/></label>
                <mvc:select id="types-filter" path="types" cssClass="form-control">
                    <mvc:option value="${IMPORT_REPORT_FILTER}"><mvc:message code="recipient.reports.type.import.report"/></mvc:option>
                    <mvc:option value="${EXPORT_REPORT_FILTER}"><mvc:message code="recipient.reports.type.export.report"/></mvc:option>
                </mvc:select>
            </div>
            <div>
                <label class="form-label" for="file-name-filter"><mvc:message code="settings.FileName"/></label>
                <mvc:text id="file-name-filter" path="fileName" cssClass="form-control"/>
            </div>
            <div>
                <label class="form-label" for="datasource-id-filter"><mvc:message code="recipient.DatasourceId"/></label>
                <mvc:number id="datasource-id-filter" path="datasourceId" cssClass="form-control" placeholder="1234" min="1" step="1" pattern="\d+"/>
            </div>
            <div>
                <label class="form-label" for="user-filter"><mvc:message code="recipients.report.username"/></label>
                <mvc:select id="user-filter" path="adminId" cssClass="form-control" data-sort="alphabetic">
                    <mvc:option value="0" data-no-sort=""><mvc:message code="upload.for.none"/></mvc:option>
                    <mvc:options items="${users}" itemValue="id" itemLabel="username"/>
                </mvc:select>
            </div>
        </div>
    </mvc:form>
</div>
