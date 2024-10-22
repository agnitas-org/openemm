<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.recipientsreport.bean.RecipientsReport" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="IMPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.IMPORT %>" />
<c:set var="EXPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.EXPORT %>" />

<c:set var="REFERENCE_TABLE_ENTITY_DATA" value="<%= RecipientsReport.EntityData.REFERENCE_TABLE %>" />
<c:set var="PROFILE_ENTITY_DATA"         value="<%= RecipientsReport.EntityData.PROFILE %>" />

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm"--%>
<%--@elvariable id="reportsList" type="org.displaytag.pagination.PaginatedList"--%>
<%--@elvariable id="users" type="java.util.List<>org.agnitas.beans.AdminEntry"--%>

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
                                <a href="#" class="icon-btn text-primary" data-tooltip="${downloadMsg}" data-form-url="${bulkDownloadUrl}" data-prevent-load data-form-submit-static>
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
                    <agnDisplay:table class="table table-hover table--borderless js-table" id="report" name="reportsList" sort="page"
                                      pagesize="${recipientsReportForm.numberOfRows}" requestURI="/recipientsreport/list.action" excludedParams="*">

                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                        </c:set>

                        <agnDisplay:column title="${checkboxSelectAll}" class="mobile-hidden" headerClass="mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${report.id}" data-bulk-checkbox />
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="report.mailing.statistics.reportdatum" sortProperty="report_date" headerClass="js-table-sort">
                            <span>${report.reportDateFormatted}</span>
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="default.Type" headerClass="js-table-sort" sortProperty="type">
                            <span><mvc:message code="${report.type.messageKey}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="import.datatype" headerClass="js-table-sort" sortProperty="entity_data">
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
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="settings.FileName" sortProperty="filename" headerClass="js-table-sort">
                            <span>${report.filename}</span>
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="recipient.DatasourceId" sortProperty="datasource_id" headerClass="js-table-sort">
                            <span>${report.datasourceId}</span>
                        </agnDisplay:column>

                        <agnDisplay:column sortable="true" titleKey="recipients.report.username" sortProperty="username" headerClass="js-table-sort">
                            <span>${report.username}</span>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="fit-content">
                            <c:url var="download_link" value="/recipientsreport/${report.id}/download.action"/>

                            <a href="${download_link}" class="icon-btn text-primary" data-tooltip="${downloadMsg}" data-prevent-load="">
                                <i class="icon icon-download"></i>
                            </a>
                            <a href='<c:url value="/recipientsreport/${report.id}/view.action"/>' class="hidden" data-view-row></a>
                        </agnDisplay:column>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/recipientsreport/search.action" modelAttribute="recipientsReportForm"
              data-toggle-tile="mobile"
              data-form="resource"
              data-resource-selector="#table-tile" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body grid gap-3 js-scrollable" style="--bs-columns: 1">
            <div>
                <label class="form-label" for="dateStart-filter"><mvc:message code="report.mailing.statistics.reportdatum"/></label>
                <div class="inline-input-range" data-date-range>
                    <div class="date-picker-container">
                        <mvc:message var="fromMsg" code="From" />
                        <mvc:text id="dateStart-filter" path="reportDate.from" cssClass="form-control js-datepicker" placeholder="${fromMsg}"/>
                    </div>
                    <div class="date-picker-container">
                        <mvc:message var="toMsg" code="To" />
                        <mvc:text id="dateEnd-filter" path="reportDate.to" cssClass="form-control js-datepicker" placeholder="${toMsg}"/>
                    </div>
                </div>
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
