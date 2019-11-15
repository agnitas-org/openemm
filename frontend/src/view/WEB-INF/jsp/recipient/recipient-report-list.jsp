<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.recipientsreport.bean.RecipientsReport" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="DATE_PATTERN" value="yyyy-MM-dd"/>
<c:set var="NO_RESULTS_FOUND_MSG"><bean:message key="noResultsFound"/></c:set>
<c:set var="NO_RESULTS_FOUND" value="<tr class=\"empty\"><td colspan=\"{0}\">${NO_RESULTS_FOUND_MSG}</td></tr>"/>

<c:set var="IMPORT_REPORT_FILTER" value="<%= RecipientsReport.RecipientReportType.IMPORT_REPORT %>" />
<c:set var="EXPORT_REPORT_FILTER" value="<%= RecipientsReport.RecipientReportType.EXPORT_REPORT %>" />

<%--@elvariable id="recipientsReportForm" type="com.agnitas.emm.core.recipientsreport.web.RecipientsReportForm"--%>

<html:form action="/recipientsreport.do">
	<html:hidden property="method" value="list"/>
	<html:hidden property="page"/>

	<div class="tile">
		<div class="tile-header">
			<h2 class="headline"><bean:message key="statistic.protocol"/></h2>
			<ul class="tile-header-nav"></ul>
			<ul class="tile-header-actions">
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<i class="icon icon-eye"></i>
						<span><bean:message key="button.Show"/></span>
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
						<li class="divider"></li>
						<li>
							<p>
								<button class="btn btn-block btn-secondary btn-regular" data-form-submit>
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
			<script type="application/json" data-initializer="web-storage-persist">
				{
					"import-export-log-overview": {
						"rows-count": ${recipientsReportForm.numberOfRows}
					}
				}
			</script>

			<!-- Filters -->
			<div class="hidden">
				<!-- Type filter -->
				<div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-type">
					<button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
						<i class="icon icon-filter"></i>
					</button>

					<c:set var="filterImportReport" value=""/>
					<c:set var="filterExportReport" value=""/>

					<c:forEach var="filter" items="${recipientsReportForm.filterTypes}">
						<c:choose>
							<c:when test="${filter eq IMPORT_REPORT_FILTER}">
								<c:set var="filterImportReport" value="checked"/>
							</c:when>
							<c:when test="${filter eq EXPORT_REPORT_FILTER}">
								<c:set var="filterExportReport" value="checked"/>
							</c:when>
						</c:choose>
					</c:forEach>

					<ul class="dropdown-menu dropdown-menu-left dropdown-menu-top">
						<li>
							<label class="label">
								<input type="checkbox" name="filterTypes" value="${IMPORT_REPORT_FILTER}" data-field-filter="" ${filterImportReport}>
								<bean:message key="${IMPORT_REPORT_FILTER.messageKey}"/>
							</label>
						</li>
						<li>
							<label class="label">
								<input type="checkbox" name="filterTypes" value="${EXPORT_REPORT_FILTER}" data-field-filter="" ${filterExportReport}>
								<bean:message key="${EXPORT_REPORT_FILTER.messageKey}"/>
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
								<button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
									<i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
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
								<label class="label"><bean:message key="From"/></label>
								<input type="text" name="filterDateStart" value="${recipientsReportForm.filterDateStart}" data-filter-date-min="" class="form-control js-datepicker js-datepicker-left" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
							</p>
						</li>
						<li>
							<p>
								<label class="label"><bean:message key="To"/></label>
								<input type="text" name="filterDateFinish" value="${recipientsReportForm.filterDateFinish}" data-filter-date-max="" class="form-control js-datepicker js-datepicker-left" data-datepicker-options="format: '${fn:toLowerCase(DATE_PATTERN)}'">
							</p>
						</li>
						<li class="divider"></li>
						<li>
							<a href="#" class="js-dropdown-open" data-form-persist="filterDateStart: '', filterDateFinish: ''">
								<bean:message key="filter.reset"/>
							</a>
						</li>
						<li class="divider"></li>
						<li>
							<p>
								<button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
									<i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Apply"/></span>
								</button>
							</p>
						</li>
					</ul>
				</div>
			</div>

			<c:set var="messageDownload" scope="page">
				<bean:message key="button.Download"/>
			</c:set>
			<div class="table-wrapper">
				<display:table class="table table-bordered table-striped table-hover js-table"
								export="false"
								id="report"
								name='reportsList'
								sort="page"
								pagesize="${recipientsReportForm.numberOfRows}"
								requestURI="/recipientsreport.do">


					<display:setProperty name="basic.empty.showtable" value="true"/>

					<display:setProperty name="basic.msg.empty_list_row" value="${NO_RESULTS_FOUND}"/>

					<display:column sortable="true" titleKey="report.mailing.statistics.reportdatum" property="reportDate" sortProperty="report_date"
									format="{0,date,${recipientsReportForm.dateFormatPattern}}" headerClass="js-table-sort js-filter-date"/>

					<display:column sortable="true" titleKey="default.Type" headerClass="js-table-sort js-filter-type" sortProperty="type">
						<bean:message key="${report.type.messageKey}"/>
					</display:column>

					<display:column sortable="true" titleKey="settings.FileName" property="filename" headerClass="js-table-sort"/>

					<display:column sortable="true" titleKey="recipient.DatasourceId" property="datasourceId" sortProperty="datasource_id" headerClass="js-table-sort"/>

					<display:column sortable="true" titleKey="recipients.report.username" property="username" headerClass="js-table-sort"/>

					<display:column class="table-actions">
						<agn:agnLink styleClass="btn btn-primary btn-regular" data-tooltip="${messageDownload}" data-prevent-load=""
									page="/recipientsreport.do?method=download&reportId=${report.id}" headerClass="js-table-sort">
							<i class="icon icon-download"></i>
						</agn:agnLink>
						
						<html:link styleClass="hidden js-row-show" page="/recipientsreport.do?method=view&reportId=${report.id}"/>
					</display:column>
				</display:table>
			</div>
		</div>
	</div>
</html:form>
