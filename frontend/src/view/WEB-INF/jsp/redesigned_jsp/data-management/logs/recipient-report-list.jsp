<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.recipientsreport.bean.RecipientsReport" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="IMPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.IMPORT %>" />
<c:set var="EXPORT_REPORT_FILTER" value="<%= RecipientsReport.EntityType.EXPORT %>" />

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm"--%>
<%--@elvariable id="reportsList" type="org.displaytag.pagination.PaginatedList"--%>
<%--@elvariable id="users" type="java.util.List<>org.agnitas.beans.AdminEntry"--%>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" method="GET" servletRelativeAction="/recipientsreport/list.action"
              modelAttribute="recipientsReportForm" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>
        <div class="tile-body" data-form-content="">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "import-export-log-overview": {
                        "rows-count": ${recipientsReportForm.numberOfRows}
                    }
                }
            </script>
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                export="false"
                                id="report"
                                name="reportsList"
                                sort="page"
                                pagesize="${recipientsReportForm.numberOfRows}"
                                requestURI="/recipientsreport/list.action"
                               excludedParams="*">
                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column sortable="true" titleKey="report.mailing.statistics.reportdatum" property="reportDateFormatted" sortProperty="report_date" headerClass="js-table-sort js-filter-date"/>

                        <display:column sortable="true" titleKey="default.Type" headerClass="js-table-sort js-filter-type" sortProperty="type">
                            <mvc:message code="${report.type.messageKey}"/>
                        </display:column>

                        <display:column sortable="true" titleKey="settings.FileName" property="filename" headerClass="js-table-sort"/>

                        <display:column sortable="true" titleKey="recipient.DatasourceId" property="datasourceId" sortProperty="datasource_id" headerClass="js-table-sort"/>

                        <display:column sortable="true" titleKey="recipients.report.username" property="username" headerClass="js-table-sort"/>

                        <display:column headerClass="fit-content">
                            <c:url var="download_link" value="/recipientsreport/${report.id}/download.action"/>
                            <mvc:message var="messageDownload" code="button.Download"/>

                            <a href="${download_link}" class="btn btn-primary btn-icon-sm" data-tooltip="${messageDownload}" data-prevent-load="">
                                <i class="icon icon-download"></i>
                            </a>
                            <a href='<c:url value="/recipientsreport/${report.id}/view.action"/>' class="hidden" data-view-row></a>
                        </display:column>
                    </display:table>
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
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
