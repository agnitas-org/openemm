<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingOverviewForm" type="com.agnitas.emm.core.mailing.forms.MailingOverviewForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="mailing" type="java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="mailinglist" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="archives" type="java.util.List<com.agnitas.beans.Campaign>"--%>
<%--@elvariable id="contentSearchEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="searchEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="MAILING_STATUS_NEW" value="<%= MailingStatus.NEW %>" />
<c:set var="MAILING_STATUS_EDIT" value="<%= MailingStatus.EDIT %>" />
<c:set var="MAILING_STATUS_READY" value="<%= MailingStatus.READY %>" />
<c:set var="MAILING_STATUS_ADMIN" value="<%= MailingStatus.ADMIN %>" />
<c:set var="MAILING_STATUS_TEST" value="<%= MailingStatus.TEST %>" />
<c:set var="MAILING_STATUS_SCHEDULED" value="<%= MailingStatus.SCHEDULED %>" />
<c:set var="MAILING_STATUS_SENT" value="<%= MailingStatus.SENT %>" />
<c:set var="MAILING_STATUS_NORECIPIENTS" value="<%= MailingStatus.NORECIPIENTS %>" />
<c:set var="MAILING_STATUS_CANCELED" value="<%= MailingStatus.CANCELED %>" />
<c:set var="MAILING_STATUS_SENDING" value="<%= MailingStatus.SENDING %>" />
<c:set var="MAILING_STATUS_ACTIVE" value="<%= MailingStatus.ACTIVE %>" />
<c:set var="MAILING_STATUS_DISABLE" value="<%= MailingStatus.DISABLE %>" />
<c:set var="MAILING_STATUS_IN_GENERATION" value="<%= MailingStatus.IN_GENERATION %>" />
<c:set var="MAILING_STATUS_GENERATION_FINISHED" value="<%= MailingStatus.GENERATION_FINISHED %>" />

<c:set var="MAILING_TYPE_NORMAL" value="<%= MailingType.NORMAL %>"/>
<c:set var="MAILING_TYPE_DATE_BASED" value="<%= MailingType.DATE_BASED %>"/>
<c:set var="MAILING_TYPE_ACTION_BASED" value="<%= MailingType.ACTION_BASED %>"/>
<c:set var="MAILING_TYPE_FOLLOW_UP" value="<%= MailingType.FOLLOW_UP %>"/>
<c:set var="MAILING_TYPE_INTERVAL" value="<%= MailingType.INTERVAL %>"/>

<c:set var="MEDIA_TYPE_EMAIL" value="<%= MediaTypes.EMAIL %>"/>

<c:set var="ADDITIONAL_FIELDS" value="<%= MailingAdditionalColumn.values() %>"/>
<c:set var="forTemplates" value="${mailingOverviewForm.forTemplates}"/>

<%@ page import="org.agnitas.web.MailingAdditionalColumn" %>

<c:choose>
    <c:when test="${forTemplates}">
        <emm:Permission token="template.show"/>
    </c:when>
    <c:otherwise>
        <emm:Permission token="mailing.show"/>
    </c:otherwise>
</c:choose>

<mvc:form servletRelativeAction="/mailing/list.action" method="GET" id="mailingOverviewForm" modelAttribute="mailingOverviewForm"
          data-form="search"
          data-controller="mailing-overview"
          data-initializer="mailing-overview">
    <mvc:hidden path="forTemplates"/>
    <mvc:hidden path="numberOfRowsChanged"/>

    <script id="config:mailing-overview" type="application/json">
      { "adminDateFormat": "${adminDateFormat}" }
    </script>
    
    <c:if test="${not forTemplates and searchEnabled}">
        <div class="tile">
            <div class="tile-header">
                <a class="headline" href="#" data-toggle-tile="#tile-mailingSearch">
                    <i class="icon tile-toggle icon-angle-up"></i>
                    <mvc:message code="Search"/>
                </a>
                <ul class="tile-header-actions">
                    <li>
                        <button class="btn btn-primary btn-regular" type="button" data-form-submit="">
                            <i class="icon icon-search"></i>
                            <span class="text"><mvc:message code="Search"/></span>
                        </button>
                    </li>
                </ul>
            </div>

            <div class="tile-content tile-content-forms form-vertical" id="tile-mailingSearch">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <div class="col-md-12">
                                <label class="control-label">
                                    <label for="searchQueryText"><mvc:message code="mailing.searchFor"/></label>
                                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/overview/SearchFor.xml" tabindex="-1" type="button"></button>
                                </label>
                            </div>
                            <div class="col-md-12">
                                <mvc:text path="searchQueryText" id="searchQueryText" cssClass="form-control"/>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="form-group">
                            <div class="col-md-12">
                                <label for="placesToSearch" class="control-label"><mvc:message code="mailing.searchIn"/></label>
                            </div>
                            <div class="col-md-12">
                                <ul class="list-group" style="margin-bottom: 0;" id="placesToSearch">
                                    <li class="list-group-item checkbox">
                                        <label>
                                            <mvc:checkbox path="searchInName" value="true"/>
                                            <mvc:message code="mailing.searchName"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item checkbox">
                                        <label>
                                            <mvc:checkbox path="searchInDescription" value="true"/>
                                            <mvc:message code="mailing.searchDescription"/>
                                        </label>
                                    </li>
                                    <c:if test="${contentSearchEnabled}">
                                        <li class="list-group-item checkbox">
                                            <label>
                                                <mvc:checkbox path="searchInContent" value="true"/>
                                                <mvc:message code="mailing.searchContent"/>
                                            </label>
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
    
    <div class="tile" data-form-content="">
        <c:choose>
            <c:when test="${forTemplates}">
                <script type="application/json" data-initializer="web-storage-persist">
                    {
                        "mailing-overview": {
                            "rows-count": ${mailingOverviewForm.numberOfRows}
                        }
                    }
                </script>
            </c:when>
            <c:otherwise>
                <script type="application/json" data-initializer="web-storage-persist">
                {
                    "mailing-overview": {
                        "page": ${mailingOverviewForm.page},
                        "rows-count": ${mailingOverviewForm.numberOfRows},
                        "fields": ${emm:toJson(mailingOverviewForm.selectedFields)},
                        "mailing-types": ${emm:toJson(mailingOverviewForm.mailingTypes)},
                        "media-types": ${emm:toJson(mailingOverviewForm.mediaTypes)},
                        "use-recycle-bin": ${emm:toJson(mailingOverviewForm.useRecycleBin)}
                    }
                }
            </script>
            </c:otherwise>
        </c:choose>

        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <c:if test="${not forTemplates}">
                <ul class="tile-header-nav">
                    <li class="active">
                        <a href="#" data-toggle-tab="#mailing"><mvc:message code="default.list"/></a>
                    </li>
                    <li>
                        <a href="#" data-toggle-tab="#mailings-overviewPreview"><mvc:message code="default.Preview"/></a>
                    </li>
                </ul>
            </c:if>
            <ul class="tile-header-actions">
                <emm:ShowByPermission token="mailing.delete|template.delete">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <i class="icon icon-pencil"></i>
                            <span class="text"><mvc:message code="bulkAction"/></span>
                            <i class="icon icon-caret-down"></i>
                        </a>

                        <ul class="dropdown-menu">
                            <li>
                                <c:choose>
                                    <c:when test="${mailingOverviewForm.useRecycleBin}">
                                        <a href="#" data-action="bulk-restore">
                                            <mvc:message code="bulk.mailing.restore"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="bulkDeleteUrl" value="/mailing/confirmBulkDelete.action">
                                            <c:param name="forTemplates" value="${forTemplates}"/>
                                        </c:url>
                                        <a href="#" data-form-url="${bulkDeleteUrl}" data-form-confirm>
                                            <c:choose>
                                                <c:when test="${forTemplates}">
                                                    <emm:ShowByPermission token="template.delete">
                                                        <mvc:message code="bulkAction.delete.template"/>
                                                    </emm:ShowByPermission>
                                                </c:when>
                                                <c:otherwise>
                                                    <emm:ShowByPermission token="mailing.delete">
                                                        <mvc:message code="bulkAction.delete.mailing"/>
                                                    </emm:ShowByPermission>
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </li>
                        </ul>
                    </li>
                </emm:ShowByPermission>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                      <i class="icon icon-eye"></i>
                      <span class="text"><mvc:message code="button.Show"/></span>
                      <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <c:if test="${not forTemplates}">
                            <li class="dropdown-header"><mvc:message code="mailing.types"/></li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="mailingTypes" value="${MAILING_TYPE_NORMAL}" cssClass="js-form-change" />
                                    <mvc:message code="Normal"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="mailingTypes" value="${MAILING_TYPE_ACTION_BASED}" cssClass="js-form-change" />
                                    <mvc:message code="mailing.event"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="mailingTypes" value="${MAILING_TYPE_DATE_BASED}" cssClass="js-form-change" />
                                    <mvc:message code="mailing.date" />
                                </label>
                            </li>
                            <%@include file="./fragments/filter-type-followup-checkbox.jspf" %>
                            <%@include file="./fragments/filter-type-interval-checkbox.jspf" %>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="useRecycleBin" cssClass="js-form-change"/>
                                    <mvc:message code="mailing.deleted" />
                                </label>
                            </li>
                            <li class="divider"></li>

                            <%@include file="./fragments/mediatypes-filters.jspf" %>
                        </c:if>

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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
				<c:if test="${not forTemplates}"> 
                    <li class="dropdown">
                         <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                             <i class="icon icon-columns"></i>
                             <span class="text"><mvc:message code="settings.fields"/></span>
                             <i class="icon icon-caret-down"></i>
                         </a>
                         <ul class="dropdown-menu">
                             <li class="dropdown-header"><mvc:message code="settings.fields"/></li>
                             <li><p>
                                 <mvc:select path="selectedFields" cssClass="form-control js-select" tabindex="-1" multiple="multiple">
                                    <c:forEach var="field" items="${ADDITIONAL_FIELDS}">
                                        <mvc:option title="${field.messageKey}" value="${field.sortColumn}">
                                            <mvc:message code="${field.messageKey}"/>
                                        </mvc:option>
                                    </c:forEach>
                                 </mvc:select>
                            </p></li>
        	                <li>
    	                        <p>
	                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    	<i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Refresh"/></span>
                                	</button>
                            	</p>
                        	</li>
                    	</ul>
                	</li>
                </c:if>  
            </ul>
        </div>

        <div class="tile-content hidden" data-initializer="mailing-overview-table">
            <!-- Filters -->
            <div class="hidden">
                <c:set var="filterBtn">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                </c:set>
                <c:set var="applyBtn">
                    <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                        <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Apply"/></span>
                    </button>
                </c:set>
                
                <!-- dropdown for status -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-status">
                    ${filterBtn}
                    <c:set var="mailingTypes" value="${mailingOverviewForm.mailingTypes}"/>
                    <c:set var="filterStatuses" value="${mailingOverviewForm.filterStatuses}"/>
                    <ul class="dropdown-menu dropdown-menu-left">
                        <c:set var="defaultFilterStatuses" value="${[MAILING_STATUS_NEW, MAILING_STATUS_EDIT, MAILING_STATUS_READY, MAILING_STATUS_ADMIN, MAILING_STATUS_TEST]}"/>
                        <c:forEach var="status" items="${defaultFilterStatuses}">
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${status}" data-field-filter=""/>
                                    <mvc:message code="${status.messageKey}"/>
                                </label>
                            </li>                            
                        </c:forEach>

                        <c:if test="${mailingTypes.contains(MAILING_TYPE_NORMAL) or mailingTypes.contains(MAILING_TYPE_FOLLOW_UP)}">
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_SCHEDULED}" data-field-filter=""/>
                                    <mvc:message code="<%= MailingStatus.SCHEDULED.getMessageKey() %>"/>
                                </label>
                            </li>

							<c:if test="${mailingTypes.contains(MAILING_TYPE_NORMAL)}">
	                            <li>
	                                <label class="label">
	                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_IN_GENERATION}" data-field-filter=""/>
	                                    <mvc:message code="${MAILING_STATUS_IN_GENERATION.messageKey}"/>
	                                </label>
	                            </li>
	                            <li>
	                                <label class="label">
                                        <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_GENERATION_FINISHED}" data-field-filter=""/>
   	                                    <mvc:message code="${MAILING_STATUS_GENERATION_FINISHED.messageKey}"/>
	                                </label>
	                            </li>
                            </c:if>

                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_SENT}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_SENT.messageKey}"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_NORECIPIENTS}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_NORECIPIENTS.messageKey}"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_CANCELED}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_CANCELED.messageKey}"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_SENDING}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_SENDING.messageKey}"/>
                                </label>
                            </li>
                        </c:if>
                        <c:if test="${mailingTypes.contains(MAILING_TYPE_ACTION_BASED) or mailingTypes.contains(MAILING_TYPE_DATE_BASED) or mailingTypes.contains(MAILING_TYPE_INTERVAL)}">
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_ACTIVE}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_ACTIVE.messageKey}"/>
                                </label>
                            </li>
                            <li>
                                <label class="label">
                                    <mvc:checkbox path="filterStatuses" value="${MAILING_STATUS_DISABLE}" data-field-filter=""/>
                                    <mvc:message code="${MAILING_STATUS_DISABLE.messageKey}"/>
                                </label>
                            </li>
                        </c:if>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterStatuses: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>

                <!-- dropdown for mailinglists -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-mailing-list">
                    ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <mvc:select path="filterMailingLists" class="form-control js-select" multiple="true" data-field-filter="">
                                    <mvc:options itemValue="id" itemLabel="shortname" items="${mailinglists}"/>
                                </mvc:select>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterMailingLists: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>

                <!-- dropdown for archives -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-archive">
                    ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <mvc:select path="filterArchives" class="form-control js-select" multiple="true" data-field-filter="">
                                    <mvc:options itemValue="id" itemLabel="shortname" items="${archives}"/>
                                </mvc:select>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterArchives: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>
                
                <!-- dropdown for send date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-send-date">
                    ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><mvc:message code="operator.between"/></label>
                                <mvc:text path="filterSendDateBegin" data-filter-date-min="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><mvc:message code="default.and"/></label>
                                <mvc:text path="filterSendDateEnd" data-filter-date-max="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li>
                            <label class="label">
                                <input type="checkbox" name="sendDatePeriod" value="7" data-field-filter="" data-action="send-date-filter-period"/>
                                <mvc:message code="report.recipient.last.week"/>
                            </label>
                            <label class="label">
                                <input type="checkbox" name="sendDatePeriod" value="30" data-field-filter="" data-action="send-date-filter-period"/>
                                <mvc:message code="report.recipient.last.month"/>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-action="reset-send-date-filter">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>

                <!-- dropdown for creation date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-creation-date">
                    ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><mvc:message code="operator.between"/></label>
                                <mvc:text path="filterCreationDateBegin" data-filter-date-min="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><mvc:message code="default.and"/></label>
                                <mvc:text path="filterCreationDateEnd" data-filter-date-max="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterCreationDateBegin: '', filterCreationDateEnd: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>

                <!-- dropdown for plan date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-plan-date">
                        ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><mvc:message code="operator.between"/></label>
                                <mvc:text path="filterPlanDateBegin" data-filter-date-min="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><mvc:message code="default.and"/></label>
                                <mvc:text path="filterPlanDateEnd" data-filter-date-max="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterPlanDateBegin: '', filterPlanDateEnd: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>
                
                <!-- dropdown for change date -->
                <div class="dropdown filter" data-field="date-filter" data-filter-target=".js-filter-change-date">
                    ${filterBtn}
                    <ul class="dropdown-menu">
                        <li>
                            <p>
                                <label class="label"><mvc:message code="operator.between"/></label>
                                <mvc:text path="filterChangeDateBegin" data-filter-date-min="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li>
                            <p>
                                <label class="label"><mvc:message code="default.and"/></label>
                                <mvc:text path="filterChangeDateEnd" data-filter-date-max="" cssClass="form-control js-datepicker js-datepicker-right" data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'"/>
                            </p>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterChangeDateBegin: '', filterChangeDateEnd: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>

                <!-- dropdown for badge -->
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-badge">
                    ${filterBtn}
                    <ul class="dropdown-menu dropdown-menu-left">
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterBadges" value="isgrid" data-field-filter=""/>
                                <span class="badge badge-info">
                                    <i class="icon-emc"></i>
                                    <b><mvc:message code="mailing.grid.GridMailing"/></b>
                                </span>
                            </label>
                        </li>
                        <li>
                            <label class="label">
                                <mvc:checkbox path="filterBadges" value="isCampaignManager" data-field-filter=""/>
                                <span class="badge badge-linkage-campaignmanager">
                                    <i class="icon icon-linkage-campaignmanager"></i>
                                    <strong><mvc:message code="campaign.manager.icon"/></strong>
                                </span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filterBadges: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li><p>${applyBtn}</p></li>
                    </ul>
                </div>
            </div>

            <div class="table-wrapper table-overflow-visible">
                <display:table
                    class="table table-bordered table-striped ${mailingOverviewForm.useRecycleBin ? '' : 'table-hover'} js-table"
                    id="mailing"
                    name="mailinglist"
                    pagesize="${mailinglist.pageSize}"
                    sort="external"
                    requestURI="/mailing/list.action"
                    excludedParams="*"
                    partialList="true"
                    size="${mailinglist.fullListSize}">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <!-- Displays the invitation text and a button to create a new mailing, if the mailing list is empty. -->
                    <emm:ShowByPermission token="mailing.change">
                    <c:if test="${not forTemplates}">
                        <display:setProperty name="basic.msg.empty_list_row">
                            <tr class="empty">
                                <td colspan="{0}">
                                    <mvc:message code="mailing.create.first"/>
                                        <c:url var="mailingCreateLink" value="/mailing/create.action"/>
                                    <a href="${mailingCreateLink}" class="btn btn-inverse btn-regular">
                                        <i class="icon icon-plus"></i>
                                        <span class="text"><mvc:message code="mailing.New_Mailing"/></span>
                                    </a>
                                </td>
                            </tr>
                        </display:setProperty>
                    </c:if>
                    </emm:ShowByPermission>
                    
                    <emm:ShowByPermission token="mailing.delete">
                        <display:column title="<input type='checkbox' data-form-bulk='bulkIds'/>" class="js-checkable" sortable="false" headerClass="squeeze-column">
                            <input type="checkbox" name="bulkIds" class="js-bulk-ids" value="${mailing.mailingid}">
                        </display:column>
                    </emm:ShowByPermission>

                    <c:if test="${not forTemplates}">
                        <display:column titleKey="Status" sortable="true" sortProperty="work_status" headerClass="js-table-sort js-filter-status" class="align-center">
                            <c:choose>
                                <c:when test="${mailingOverviewForm.useRecycleBin}">
                                    <i class="icon icon-trash-o" data-tooltip="<mvc:message code="target.Deleted" />"></i>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${not empty mailing.workstatus}">
                                        <c:set var="workstatus">
                                            <mvc:message code="${mailing.workstatus}"/>
                                        </c:set>
                                        <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${workstatus}"></span>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </display:column>
                        <display:column class="table-actions" headerClass="js-table-sort js-filter-badge">
                            <%--icon for GRID--%>
                            <c:if test="${mailing.isgrid}">
                                <span class="badge badge-info">
                                    <i class="icon-emc"></i>
                                    <b><mvc:message code="mailing.grid.GridMailing"/></b>
                                </span>
                            </c:if>
                            <%--icon for "CM"--%>
                            <c:if test="${mailing.usedInCM}">
                                <button class="badge badge-linkage-campaignmanager" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml" tabindex="-1" type="button">
                                <i class="icon icon-linkage-campaignmanager"></i> <strong><mvc:message code="campaign.manager.icon"/></strong>
                                </button>
                            </c:if>
                        </display:column>

                        <display:column titleKey="Mailing" sortable="true" sortProperty="shortname" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.shortname}</span>
                        </display:column>

                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.description}</span>
                        </display:column>

                        <display:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort js-filter-mailing-list">
                            <span class="multiline-auto">${mailing.mailinglist}</span>
                        </display:column>

                        <display:column titleKey="mailing.senddate" format="{0,date,${adminDateTimeFormat}}"
                            property="senddate" sortable="true" headerClass="js-table-sort js-filter-send-date" />

                        <c:forEach var="selectedField" items="${mailingOverviewForm.selectedFields}">
                            <c:forEach var="field" items="${ADDITIONAL_FIELDS}">
                                <c:if test="${selectedField == field.sortColumn}">
                                    <c:choose>
                                        <c:when test="${field == 'RECIPIENTS_COUNT'}">
                                            <%-- Replace 0 values with "n/a" for all the mailings but normal --%>
                                            <c:if test="${mailing.mailing_type ne MAILING_TYPE_NORMAL.code}">
                                                <c:set target="${mailing}" property="recipientsCount"><mvc:message code="NotAvailableShort"/></c:set>
                                            </c:if>
                                            <display:column property="recipientsCount"
                                                            titleKey="${field.messageKey}"
                                                            sortable="true"
                                                            sortProperty="${field.sortColumn}"
                                                            headerClass="js-table-sort"/>
                                        </c:when>
                                        
                                        <c:when test="${field == 'CREATION_DATE'}">
                                            <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                 property="creationdate" sortable="true" sortProperty="creation_date" headerClass="js-table-sort js-filter-creation-date" />
                                        </c:when>
                                        
                                        <c:when test="${field == 'CHANGE_DATE'}">
                                            <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                 property="changedate" sortable="true" sortProperty="change_date" headerClass="js-table-sort js-filter-change-date" />
                                        </c:when>

                                        <c:when test="${field == 'ARCHIVE'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="campaign_id" headerClass="js-table-sort js-filter-archive">
                                                <span class="multiline-auto">${mailing.archive}</span>
                                            </display:column>
                                        </c:when>
                                        
                                        <c:when test="${field == 'TEMPLATE'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="template_name" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.templateName}</span>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${field == 'SUBJECT'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="subject" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.subject}</span>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${field == 'TARGET_GROUPS'}">
                                            <display:column titleKey="Target-Groups" headerClass="js-table-sort">
                                                <c:forEach var="targetgroup" items="${mailing.targetgroups}">
                                                    <a href="<c:url value='/target/${targetgroup.target_id}/view.action'/>"><span class="multiline-auto">${targetgroup.target_name}</span></a>
                                                    <br/>
                                                </c:forEach>
                                            </display:column>
                                        </c:when>
                                        <c:when test="${field == 'MAILING_ID'}">
                                            <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="mailing_id" headerClass="js-table-sort">
                                                <span class="multiline-auto">${mailing.mailingid}</span>
                                            </display:column>
                                        </c:when>
                                        <c:when test="${field == 'PLAN_DATE'}">
                                            <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                            property="planDate" sortable="true" sortProperty="plan_date" headerClass="js-table-sort js-filter-plan-date" />
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                    </c:if>
                    <c:if test="${forTemplates}">
                        <display:column titleKey="Template" sortable="true" sortProperty="shortname" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.shortname}</span>
                        </display:column>
                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                            <span class="multiline-auto">${mailing.description}</span>
                        </display:column>
                        <display:column titleKey="Mailinglist" property="mailinglist"
                            sortable="true" sortProperty="mailinglist" headerClass="js-table-sort" />
                        <display:column titleKey="default.creationDate" sortable="true"
                            format="{0, date, ${adminDateFormat}}" property="creationdate"
                            sortProperty="creation_date" headerClass="js-table-sort">
                            ${mailing.creationdate}
                        </display:column>
                        <display:column titleKey="default.changeDate" sortable="true"
                            format="{0, date, ${adminDateFormat}}" property="changedate"
                            sortProperty="change_date" headerClass="js-table-sort">
                            ${mailing.changedate}
                        </display:column>
                    </c:if>
                    
                    <display:column class="table-actions">
                        <script type="text/x-mustache-template" class="js-row-popover">
                            <%@include file="fragments/mailing-preview-src.jspf"%>
                            <img src="${previewImageSrc}" style="max-width: 200px" alt="${fn:escapeXml(mailing.shortname)}" border="0">
                        </script>

                        <c:if test="${mailing.hasActions}">
                            <a href="<c:url value="/mailing/${mailing.mailingid}/actions.action"/>" class="badge badge-linkage-action" data-tooltip="<mvc:message code="action.action_link"/>">
                                <i class="icon icon-linkage-action"></i> <strong>A</strong>
                            </a>
                        </c:if>

                        <c:choose>
                            <c:when test="${mailingOverviewForm.useRecycleBin}">
                                <a href="<c:url value="/mailing/${mailing.mailingid}/restore.action"/>" class="btn btn-regular btn-info" data-action="restore" data-tooltip="<mvc:message code="default.restore" />">
                                    <i class="icon icon-repeat"></i>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <emm:ShowByPermission token="${forTemplates ? 'template.delete' : 'mailing.delete'}">
                                    <c:set var="deleteTooltipMsgCode" value="${forTemplates ? 'template.delete' : 'mailing.MailingDelete'}"/>
                                    <a href="<c:url value="/mailing/${mailing.mailingid}/confirmDelete.action"/>" class="btn btn-regular btn-alert js-row-delete" data-tooltip="<mvc:message code="${deleteTooltipMsgCode}"/>">
                                        <i class="icon icon-trash-o"></i>
                                    </a>
                                </emm:ShowByPermission>
                                <c:url var="mailingViewLink" value="/mailing/${mailing.mailingid}/settings.action"/>
                                <c:if test="${mailing.workstatus eq 'mailing.status.sent' or mailing.workstatus eq 'mailing.status.norecipients'}">
                                    <emm:ShowByPermission token="stats.mailing">
                                        <c:url var="mailingViewLink" value="/statistics/mailing/${mailing.mailingid}/view.action">
                                            <c:param name="init" value="true"/>
                                        </c:url>
                                    </emm:ShowByPermission>
                                </c:if>
                                <a href="${mailingViewLink}" class="hidden js-row-show"></a>
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                </display:table>
            </div>
            
            <div class="card-panel hidden" id="mailings-overviewPreview">
                <div class="row">
                    <c:forEach var="mailing" items="${mailinglist.list}">
                        <c:choose>
                            <c:when test="${mailing.workstatus eq 'mailing.status.sent' or mailing.workstatus eq 'mailing.status.norecipients'}">
                                <emm:ShowByPermission token="stats.mailing">
                                    <c:url var="mailingViewLink" value="/statistics/mailing/${mailing.mailingid}/view.action">
                                        <c:param name="init" value="true"/>
                                    </c:url>
                                </emm:ShowByPermission>
                                <emm:HideByPermission token="stats.mailing">
                                    <c:url var="mailingViewLink" value="/mailing/${mailing.mailingid}/settings.action"/>
                                </emm:HideByPermission>
                            </c:when>
                            <c:otherwise>
                                <c:url var="mailingViewLink" value="/mailing/${mailing.mailingid}/settings.action"/>
                            </c:otherwise>
                        </c:choose>
    
                        <div class="col-xs-6 col-sm-4 col-md-3">
                            <a href="${mailingViewLink}" class="card">
                                <%@include file="fragments/mailing-preview-src.jspf"%>
                                <img src="${previewImageSrc}" class="card-image" alt="${fn:escapeXml(mailing.shortname)}"/>
                                <div class="card-body" style="overflow: hidden">
                                    <div class="col-sm-12">
                                        <div class="input-group">
                                            <div class="input-group-controls">
                                                <div class="block" style="width: 100%; position: absolute;">
                                                    <c:set var="workstatus" value=""/>
                                                    <c:if test="${not empty mailing.workstatus}">
                                                        <c:set var="workstatus">
                                                            <mvc:message code="${mailing.workstatus}"/>
                                                        </c:set>
                                                    </c:if>
    
                                                    <strong class="headline">
                                                        <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${workstatus}"></span>
                                                        <span>${fn:escapeXml(mailing.shortname)}</span>
                                                    </strong>
                                                </div>
                                            </div>
                                            <div class="input-group-btn">
                                                <c:url var="deleteMailingLink" value="/mailing/${mailing.mailingid}/confirmDelete.action"/>
    
                                                <c:set var="mailingDeleteMessage" scope="page">
                                                    <mvc:message code="mailing.MailingDelete"/>
                                                </c:set>
    
                                                <c:choose>
                                                    <c:when test="${forTemplates}">
                                                        <emm:ShowByPermission token="template.delete">
                                                            <c:set var="mailingDeleteMessage" scope="page">
                                                                <mvc:message code="template.delete"/>
                                                            </c:set>
    
                                                            <button type="button" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${mailingDeleteMessage}" data-url="${deleteMailingLink}">
                                                                <i class="icon icon-trash-o"></i>
                                                            </button>
                                                        </emm:ShowByPermission>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <emm:ShowByPermission token="mailing.delete">
                                                            <c:set var="mailingDeleteMessage" scope="page">
                                                                <mvc:message code="mailing.MailingDelete"/>
                                                            </c:set>
    
                                                            <button type="button" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${mailingDeleteMessage}" data-url="${deleteMailingLink}">
                                                                <i class="icon icon-trash-o"></i>
                                                            </button>
                                                        </emm:ShowByPermission>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
    
                                    <div class="col-sm-12">
                                        <div class="block">
                                            <p class="description">
                                                <c:if test="${mailing.usedInCM}">
                                                    <span class="badge badge-linkage-campaignmanager">
                                                        <i class="icon icon-linkage-campaignmanager"></i> <strong><mvc:message code="campaign.manager.icon"/></strong>
                                                    </span>
                                                </c:if>
                                                <c:if test="${mailing.isgrid}">
                                                    <span class="badge badge-info">
                                                        <i class="icon-emc"></i>
                                                        <b><mvc:message code="mailing.grid.GridMailing"/></b>
                                                    </span>
                                                </c:if>
                                                <span data-tooltip="<mvc:message code='birt.mailinglist'/>">
                                                    <i class="icon icon-list-ul"></i>
                                                    <span class="text">${mailing.mailinglist}</span>
                                                </span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </div>
            
            <c:if test="${not forTemplates}">
                <emm:instantiate var="appliedFilters" type="java.util.LinkedHashMap">
                    <c:forEach var="mailingType" items="${mailingOverviewForm.mailingTypes}">
                        <c:set target="${appliedFilters}" property="${fn:length(appliedFilters)}"><mvc:message code="${mailingType.messagekey}"/></c:set>
                    </c:forEach>
                </emm:instantiate>

                <script data-initializer="mailing-overview-filters" type="application/json">
                    {
                        "filters": ${emm:toJson(appliedFilters.values())}
                    }
                </script>

                <script id="mailing-overview-filters" type="text/x-mustache-template">
                    <div class='well'>
                        <strong><mvc:message code="mailing.showing"/></strong>
                        {{- filters.join(', ') }}
                    </div>
                    <button class="btn btn-regular" data-form-persist="mailingTypes: '${MAILING_TYPE_NORMAL}', mediaTypes: '${MEDIA_TYPE_EMAIL}', filterStatuses: '', filterBadges: '', filterMailingLists: '', filterSendDateBegin: '', filterSendDateEnd: '', filterCreationDateBegin: '', filterCreationDateEnd: '', filterPlanDateBegin: '', filterPlanDateEnd: '', filterChangeDateBegin: '', filterChangeDateEnd: '', filterArchives: '', useRecycleBin: false">
                        <mvc:message code="filter.reset"/>
                    </button>
                </script>
            </c:if>
        </div>
    </div>
</mvc:form>
