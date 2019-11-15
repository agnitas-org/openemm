<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ page import="com.agnitas.emm.core.recipientsreport.bean.RecipientsReport" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring" prefix="mvc" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="IMPORT_REPORT_FILTER" value="<%= RecipientsReport.RecipientReportType.IMPORT_REPORT %>" />
<c:set var="EXPORT_REPORT_FILTER" value="<%= RecipientsReport.RecipientReportType.EXPORT_REPORT %>" />

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm"--%>
<%--@elvariable id="reportsList" type="org.displaytag.pagination.PaginatedList"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/recipientsreport/list.action" modelAttribute="recipientsReportForm">
    <input type="hidden" name="page" value="${reportsList.pageNumber}"/>
    <input type="hidden" name="sort" value="${reportsList.sortCriterion}"/>
    <input type="hidden" name="dir" value="${reportsList.sortDirection}"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "import-export-log-overview": {
                "rows-count": ${recipientsReportForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="statistic.protocol"/></h2>
            <ul class="tile-header-nav"></ul>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span><mvc:message code="button.Show"/></span>
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
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" data-form-submit data-form-change>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text">Show</span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content" data-form-content="">
            <!-- Filters -->
            <div class="hidden">
                <!-- Type filter -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-type">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu dropdown-menu-left dropdown-menu-top">
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="${IMPORT_REPORT_FILTER}" data-field-filter=""/>
                                <mvc:message code="${IMPORT_REPORT_FILTER.messageKey}"/>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterTypes" value="${EXPORT_REPORT_FILTER}" data-field-filter=""/>
                                <mvc:message code="${EXPORT_REPORT_FILTER.messageKey}"/>
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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
                <!--Send date filter-->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-date">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>

                    <ul class="dropdown-menu dropdown-menu-left dropdown-menu-top">
                        <li>
                            <p>
                                <label class="label"><mvc:message code="From"/></label>
                                <input type="text" name="filterDateStart.date" value="${recipientsReportForm.filterDateStart.date}" data-filter-date-min="" class="form-control js-datepicker js-datepicker-left" data-datepicker-options="format: '${fn:toLowerCase(dateFormatPattern)}'">
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><mvc:message code="To"/></label>
                                <input type="text" name="filterDateFinish.date" value="${recipientsReportForm.filterDateFinish.date}" data-filter-date-max="" class="form-control js-datepicker js-datepicker-left" data-datepicker-options="format: '${fn:toLowerCase(dateFormatPattern)}'">
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterDateStart.date: '', filterDateFinish.date: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                                export="false"
                                id="report"
                                name="reportsList"
                                sort="page"
                                pagesize="${recipientsReportForm.numberOfRows}"
                                requestURI="/recipientsreport/list.action"
                               excludedParams="*">

                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row">
                        <tr class="empty"><td colspan="{0}"><mvc:message code="noResultsFound"/></td></tr>
                    </display:setProperty>

                    <display:column sortable="true" titleKey="report.mailing.statistics.reportdatum" property="reportDate" sortProperty="report_date"
                                    format="{0,date,${dateFormatPattern}}" headerClass="js-table-sort js-filter-date"/>

                    <display:column sortable="true" titleKey="default.Type" headerClass="js-table-sort js-filter-type" sortProperty="type">
                        <mvc:message code="${report.type.messageKey}"/>
                    </display:column>

                    <display:column sortable="true" titleKey="settings.FileName" property="filename" headerClass="js-table-sort"/>

                    <display:column sortable="true" titleKey="recipient.DatasourceId" property="datasourceId" sortProperty="datasource_id" headerClass="js-table-sort"/>

                    <display:column sortable="true" titleKey="recipients.report.username" property="username" headerClass="js-table-sort"/>

                    <display:column class="table-actions">
                        <c:url var="download_link" value="/recipientsreport/${report.id}/download.action"/>
                        <c:url var="view_link" value="/recipientsreport/${report.id}/view.action"/>
                        <mvc:message var="messageDownload" code="button.Download"/>

                        <a href="${download_link}" class="btn btn-primary btn-regular" data-tooltip="${messageDownload}" data-prevent-load="">
                            <i class="icon icon-download"></i>
                        </a>
                        <a href="${view_link}" class="hidden js-row-show"></a>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
