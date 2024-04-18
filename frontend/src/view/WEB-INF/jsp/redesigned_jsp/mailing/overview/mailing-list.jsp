<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.web.MailingAdditionalColumn" %>
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
<c:set var="CREATION_DATE_FIELD" value="<%= MailingAdditionalColumn.CREATION_DATE %>"/>
<c:set var="PLAN_DATE_FIELD" value="<%= MailingAdditionalColumn.PLAN_DATE %>"/>
<c:set var="CHANGE_DATE_FIELD" value="<%= MailingAdditionalColumn.CHANGE_DATE %>"/>
<c:set var="ARCHIVE_FIELD" value="<%= MailingAdditionalColumn.ARCHIVE %>"/>

<c:set var="forTemplates" value="${mailingOverviewForm.forTemplates}"/>

<mvc:form id="mailing-overview-form" cssClass="filter-overview hidden" servletRelativeAction="/mailing/list.action" method="GET" modelAttribute="mailingOverviewForm"
          data-form="search" data-controller="mailing-overview" data-initializer="mailing-overview" data-form-content="" data-editable-view="${agnEditViewKey}">

    <script id="config:mailing-overview" type="application/json">
      {
        "badgesFilters": ${emm:toJson(mailingOverviewForm.filterBadges)}
      }
    </script>

    <div id="table-tile" class="tile" data-editable-tile="main">
        <mvc:hidden path="forTemplates"/>

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
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
            <c:if test="${not forTemplates}">
                <div class="tile-controls">
                    <input type="checkbox" id="switch-table-view" class="icon-switch" data-preview-table="#mailings-table" checked>
                    <label for="switch-table-view" class="icon-switch__label">
                        <i class="icon icon-image"></i>
                        <i class="icon icon-th-list"></i>
                    </label>
                </div>
            </c:if>
        </div>

        <div class="tile-body">
            <c:if test="${not forTemplates}">
                <c:set var="addAdditionalColumns">
                    <div class="dropdown table-header-dropdown">
                        <i class="icon icon-plus" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false"></i>

                        <ul class="dropdown-menu">
                            <div class="d-flex flex-column gap-2">
                                <mvc:select path="selectedFields" cssClass="form-control dropdown-select" multiple="multiple">
                                    <c:forEach var="field" items="${ADDITIONAL_FIELDS}">
                                        <mvc:option value="${field.sortColumn}">
                                            <mvc:message code="${field.messageKey}"/>
                                        </mvc:option>
                                    </c:forEach>
                                </mvc:select>
                                <button class="btn btn-primary js-dropdown-close" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-sync"></i>
                                    <span class="text"><mvc:message code="button.Refresh"/></span>
                                </button>
                            </div>
                        </ul>
                    </div>
                </c:set>
            </c:if>

            <div class="table-box">
                <div class="table-scrollable">
                    <display:table htmlId="mailings-table" class="table table-rounded ${mailingOverviewForm.useRecycleBin ? '' : 'table-hover'} js-table"
                        id="mailing" name="mailinglist" pagesize="${mailinglist.pageSize}" sort="external"
                        requestURI="/mailing/list.action" excludedParams="*" partialList="true" size="${mailinglist.fullListSize}">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <!-- Displays the invitation if the mailing list is empty. -->
                        <c:choose>
                            <c:when test="${mailing.isOnlyPostType}">
                                <c:url var="previewImageSrc" value="/assets/core/images/facelift/post_thumbnail.jpg"/>
                            </c:when>
                            <c:when test="${not empty mailing.preview_component and mailing.preview_component ne 0}">
                                <c:url var="previewImageSrc" value="/sc">
                                    <c:param name="compID" value="${mailing.preview_component}"/>
                                    <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                                </c:url>
                            </c:when>
                            <c:otherwise>
                                <c:url var="previewImageSrc" value="/assets/core/images/facelift/no_preview.svg"/>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${not forTemplates}">
                            <display:column class="table-preview-visible w-100" headerClass="hidden">
                                <div class="table-cell__preview-wrapper">
                                    <img src="${previewImageSrc}" alt="" class="table-cell__preview">
                                </div>
                            </display:column>
                        </c:if>

                        <emm:ShowByPermission token="mailing.delete">
                            <display:column title="<input type='checkbox' class='form-check-input' data-form-bulk='bulkIds'/>" class="js-checkable mobile-hidden" sortable="false" headerClass="bulk-ids-column mobile-hidden">
                                <input type="checkbox" name="bulkIds" class="form-check-input js-bulk-ids" value="${mailing.mailingid}">
                            </display:column>
                        </emm:ShowByPermission>

                        <c:if test="${not forTemplates}">
                            <display:column titleKey="Status" sortable="true" sortProperty="work_status" headerClass="fit-content js-table-sort">
                                <div class="flex-center gap-1">
                                    <c:choose>
                                        <c:when test="${mailingOverviewForm.useRecycleBin}">
                                            <span class="status-badge mailing.status.deleted" data-tooltip="<mvc:message code="target.Deleted" />"></span>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${not empty mailing.workstatus}">
                                                <mvc:message var="workstatus" code="${mailing.workstatus}"/>
                                                <span class="status-badge ${mailing.workstatus}" data-tooltip="${workstatus}"></span>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>

                                    <%--icon for GRID--%>
                                    <c:if test="${mailing.isgrid}">
                                        <span class="status-badge mailing.status.emc" data-tooltip="<mvc:message code="mailing.grid.GridMailing"/>"></span>
                                    </c:if>

                                    <%--icon for "CM"--%>
                                    <c:if test="${mailing.usedInCM}">
                                        <a href="#" tabindex="-1" type="button" class="flex-center" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml">
                                            <span class="status-badge mailing.status.cm" data-tooltip="<mvc:message code="campaign.manager.icon"/>"></span>
                                        </a>
                                    </c:if>

                                    <%--icon for 'Used trigger'--%>
                                    <c:if test="${mailing.hasActions}">
                                        <a href="<c:url value="/mailing/${mailing.mailingid}/actions.action"/>" class="status-badge mailing.status.trigger" data-tooltip="<mvc:message code="action.action_link"/>"></a>
                                    </c:if>
                                </div>
                            </display:column>

                            <display:column titleKey="Name" sortable="true" sortProperty="shortname" headerClass="js-table-sort" property="shortname" class="table-cell-nowrap"/>
                            <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" property="description" class="table-preview-hidden"/>
                            <display:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort" class="table__cell-sub-info order-2">
                                <div class="table-cell-wrapper">
                                    <i class="icon icon-list mobile-visible table-preview-visible"></i>
                                    <span>${mailing.mailinglist}</span>
                                </div>
                            </display:column>

                            <display:column titleKey="mailing.senddate" format="{0,date,${adminDateTimeFormat}}"
                                property="senddate" sortable="true" headerClass="js-table-sort" class="table-preview-hidden"/>

                            <c:forEach var="selectedField" items="${mailingOverviewForm.selectedFields}">
                                <c:forEach var="field" items="${ADDITIONAL_FIELDS}">
                                    <c:if test="${selectedField == field.sortColumn}">
                                        <c:choose>
                                            <c:when test="${field == 'RECIPIENTS_COUNT'}">
                                                <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="${field.sortColumn}"
                                                                headerClass="js-table-sort" class="table-preview-hidden">
                                                    <c:choose>
                                                        <c:when test="${mailing.mailing_type ne MAILING_TYPE_NORMAL.code}">
                                                            <%-- Replace 0 values with "n/a" --%>
                                                            <mvc:message code="NotAvailableShort"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${mailing.recipientsCount}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </display:column>
                                            </c:when>

                                            <c:when test="${field == 'CREATION_DATE'}">
                                                <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                     property="creationdate" sortable="true" sortProperty="creation_date" headerClass="js-table-sort" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'CHANGE_DATE'}">
                                                <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                     property="changedate" sortable="true" sortProperty="change_date" headerClass="js-table-sort" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'ARCHIVE'}">
                                                <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="campaign_id" headerClass="js-table-sort" property="archive" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'TEMPLATE'}">
                                                <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="template_name" headerClass="js-table-sort" property="templateName" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'SUBJECT'}">
                                                <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="subject" headerClass="js-table-sort" property="subject" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'TARGET_GROUPS'}">
                                                <display:column titleKey="Target-Groups" headerClass="js-table-sort" class="table-preview-hidden">
                                                    <div class="d-flex flex-column">
                                                        <c:forEach var="targetgroup" items="${mailing.targetgroups}">
                                                            <a href="<c:url value='/target/${targetgroup.target_id}/view.action'/>" class="text-truncate">
                                                                ${targetgroup.target_name}
                                                            </a>
                                                        </c:forEach>
                                                    </div>
                                                </display:column>
                                            </c:when>

                                            <c:when test="${field == 'MAILING_ID'}">
                                                <display:column titleKey="${field.messageKey}" sortable="true" sortProperty="mailing_id" headerClass="js-table-sort" property="mailingid" class="table-preview-hidden"/>
                                            </c:when>

                                            <c:when test="${field == 'PLAN_DATE'}">
                                                <display:column titleKey="${field.messageKey}" format="{0,date,${adminDateTimeFormat}}"
                                                                property="planDate" sortable="true" sortProperty="plan_date" headerClass="js-table-sort" class="table-preview-hidden"/>
                                            </c:when>
                                        </c:choose>
                                    </c:if>
                                </c:forEach>
                            </c:forEach>
                        </c:if>
                        <c:if test="${forTemplates}">
                            <display:column titleKey="Name" sortable="true" sortProperty="shortname" headerClass="js-table-sort" property="shortname"/>
                            <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" property="description"/>
                            <display:column titleKey="Mailinglist" property="mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort" />

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

                        <display:column class="table-actions order-1" title="${addAdditionalColumns}" headerClass="additional-columns">
                            <script type="text/x-mustache-template" class="js-row-popover">
                                <img src="${previewImageSrc}" style="max-width: 200px" alt="${fn:escapeXml(mailing.shortname)}" border="0">
                            </script>

                            <c:choose>
                                <c:when test="${mailingOverviewForm.useRecycleBin}">
                                    <a href="<c:url value="/mailing/${mailing.mailingid}/restore.action"/>" class="btn btn-icon-sm btn-primary" data-action="restore" data-tooltip="<mvc:message code="default.restore" />">
                                        <i class="icon icon-redo"></i>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <emm:ShowByPermission token="${forTemplates ? 'template.delete' : 'mailing.delete'}">
                                        <c:set var="deleteTooltipMsgCode" value="${forTemplates ? 'template.delete' : 'mailing.MailingDelete'}"/>
                                        <a href="<c:url value="/mailing/${forTemplates ? 'deleteTemplates' : 'deleteMailings'}.action?bulkIds=${mailing.mailingid}"/>" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="<mvc:message code="${deleteTooltipMsgCode}"/>">
                                            <i class="icon icon-trash-alt"></i>
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
                                    <a href="${mailingViewLink}" class="hidden" data-view-row="page"></a>
                                </c:otherwise>
                            </c:choose>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-toggle-tile="mobile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
                <c:if test="${searchEnabled or contentSearchEnabled}">
                    <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/overview/SearchFor.xml"></a>
                </c:if>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"
                   data-form-persist="mailingTypes: '${MAILING_TYPE_NORMAL}', mediaTypes: '${MEDIA_TYPE_EMAIL}', filterName: '', filterDescription: '', filterContent: '', filterStatuses: '', filterBadges: '', filterMailingLists: '', filterSendDateBegin: '', filterSendDateEnd: '', filterCreationDateBegin: '', filterCreationDateEnd: '', filterPlanDateBegin: '', filterPlanDateEnd: '', filterChangeDateBegin: '', filterChangeDateEnd: '', filterArchives: '', useRecycleBin: false">
                    <i class="icon icon-sync"></i>
                </a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <c:if test="${searchEnabled}">
                    <div class="col-12">
                        <mvc:message var="nameMsg" code="Name"/>
                        <label class="form-label" for="name-filter">${nameMsg}</label>
                        <mvc:text id="name-filter" path="filterName" cssClass="form-control" placeholder="${nameMsg}"/>
                    </div>

                    <div class="col-12">
                        <mvc:message var="descriptionMsg" code="Description"/>
                        <label class="form-label" for="description-filter">${descriptionMsg}</label>
                        <mvc:text id="description-filter" path="filterDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
                    </div>
                </c:if>

                <c:if test="${not forTemplates and contentSearchEnabled}">
                    <div class="col-12">
                        <mvc:message var="contentMsg" code="default.Content"/>
                        <label class="form-label" for="content-filter">${contentMsg}</label>
                        <mvc:text id="content-filter" path="filterContent" cssClass="form-control" placeholder="${contentMsg}"/>
                    </div>
                </c:if>

                <div class="col-12">
                    <label class="form-label" for="mailinglists-filter"><mvc:message code="Mailinglist"/></label>
                    <mvc:select id="mailinglists-filter" path="filterMailingLists" cssClass="form-control" multiple="true">
                        <mvc:options itemValue="id" itemLabel="shortname" items="${mailinglists}"/>
                    </mvc:select>
                </div>

                <c:if test="${not forTemplates}">
                    <div class="col-12">
                        <label class="form-label" for="status-filter"><mvc:message code="Status"/></label>

                        <c:set var="mailingTypes" value="${mailingOverviewForm.mailingTypes}"/>
                        <c:set var="defaultFilterStatuses" value="${[MAILING_STATUS_NEW, MAILING_STATUS_EDIT, MAILING_STATUS_READY, MAILING_STATUS_ADMIN, MAILING_STATUS_TEST]}"/>

                        <mvc:select id="status-filter" path="filterStatuses" cssClass="form-control" multiple="true" data-result-template="mailing-status-selection">
                            <c:forEach var="status" items="${defaultFilterStatuses}">
                                <mvc:option value="${status}" data-badge-class="${status.messageKey}"><mvc:message code="${status.messageKey}"/></mvc:option>
                            </c:forEach>

                            <c:if test="${mailingTypes.contains(MAILING_TYPE_NORMAL) or mailingTypes.contains(MAILING_TYPE_FOLLOW_UP)}">
                                <mvc:option value="${MAILING_STATUS_SCHEDULED}" data-badge-class="${MAILING_STATUS_SCHEDULED.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_SCHEDULED.messageKey}"/>
                                </mvc:option>

                                <c:if test="${mailingTypes.contains(MAILING_TYPE_NORMAL)}">
                                    <mvc:option value="${MAILING_STATUS_IN_GENERATION}" data-badge-class="${MAILING_STATUS_IN_GENERATION.messageKey}">
                                        <mvc:message code="${MAILING_STATUS_IN_GENERATION.messageKey}"/>
                                    </mvc:option>
                                    <mvc:option value="${MAILING_STATUS_GENERATION_FINISHED}" data-badge-class="${MAILING_STATUS_GENERATION_FINISHED.messageKey}">
                                        <mvc:message code="${MAILING_STATUS_GENERATION_FINISHED.messageKey}"/>
                                    </mvc:option>
                                </c:if>

                                <mvc:option value="${MAILING_STATUS_SENT}" data-badge-class="${MAILING_STATUS_SENT.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_SENT.messageKey}"/>
                                </mvc:option>
                                <mvc:option value="${MAILING_STATUS_NORECIPIENTS}" data-badge-class="${MAILING_STATUS_NORECIPIENTS.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_NORECIPIENTS.messageKey}"/>
                                </mvc:option>
                                <mvc:option value="${MAILING_STATUS_CANCELED}" data-badge-class="${MAILING_STATUS_CANCELED.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_CANCELED.messageKey}"/>
                                </mvc:option>
                                <mvc:option value="${MAILING_STATUS_SENDING}" data-badge-class="${MAILING_STATUS_SENDING.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_SENDING.messageKey}"/>
                                </mvc:option>
                            </c:if>

                            <c:if test="${mailingTypes.contains(MAILING_TYPE_ACTION_BASED) or mailingTypes.contains(MAILING_TYPE_DATE_BASED) or mailingTypes.contains(MAILING_TYPE_INTERVAL)}">
                                <mvc:option value="${MAILING_STATUS_ACTIVE}" data-badge-class="${MAILING_STATUS_ACTIVE.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_ACTIVE.messageKey}"/>
                                </mvc:option>
                                <mvc:option value="${MAILING_STATUS_DISABLE}" data-badge-class="${MAILING_STATUS_DISABLE.messageKey}">
                                    <mvc:message code="${MAILING_STATUS_DISABLE.messageKey}"/>
                                </mvc:option>
                            </c:if>
                        </mvc:select>
                    </div>

                    <%-- Send date filter --%>
                    <div class="col-12" data-date-range>
                        <label class="form-label" for="filterSendDateBegin"><mvc:message code="mailing.senddate"/></label>
                        <div class="date-picker-container mb-1">
                            <mvc:message var="fromMsg" code="From" />
                            <mvc:text id="filterSendDateBegin" path="filterSendDateBegin" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                        </div>
                        <div class="date-picker-container mb-1">
                            <mvc:message var="toMsg" code="To" />
                            <mvc:text id="filterSendDateEnd" path="filterSendDateEnd" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                        </div>

                        <div class="row">
                            <div class="col-6">
                                <div class="form-check form-switch">
                                    <input id="sendDate-last-week-filter" type="checkbox" name="sendDatePeriod" value="7" class="form-check-input" role="switch" data-action="send-date-filter-period" />
                                    <label class="form-label form-check-label text-truncate" for="sendDate-last-week-filter">
                                        <mvc:message code="report.recipient.last.week"/>
                                    </label>
                                </div>
                            </div>

                            <div class="col-6">
                                <div class="form-check form-switch">
                                    <input id="sendDate-last-month-filter" type="checkbox" name="sendDatePeriod" value="30" class="form-check-input" role="switch" data-action="send-date-filter-period" />
                                    <label class="form-label form-check-label text-truncate" for="sendDate-last-month-filter">
                                        <mvc:message code="report.recipient.last.month"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="mailingType-filter"><mvc:message code="mailing.types"/></label>
                        <mvc:select id="mailingType-filter" path="mailingTypes" cssClass="form-control mb-1" multiple="true">
                            <mvc:option value="${MAILING_TYPE_NORMAL}"><mvc:message code="Normal"/></mvc:option>
                            <mvc:option value="${MAILING_TYPE_ACTION_BASED}"><mvc:message code="mailing.event"/></mvc:option>
                            <mvc:option value="${MAILING_TYPE_DATE_BASED}"><mvc:message code="mailing.date"/></mvc:option>
                            <%@include file="./fragments/filter-type-followup-option.jspf" %>
                            <%@include file="./fragments/filter-type-interval-option.jspf" %>
                        </mvc:select>

                        <div class="form-check form-switch">
                            <mvc:checkbox id="filter-use-recycle-bin" path="useRecycleBin" cssClass="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="filter-use-recycle-bin">
                                <mvc:message code="mailing.deleted"/>
                            </label>
                        </div>
                    </div>

                    <%@include file="./fragments/mediatypes-filter.jspf" %>

                    <%-- Plan date filter --%>
                    <c:if test="${fn:contains(mailingOverviewForm.selectedFields, PLAN_DATE_FIELD.sortColumn)}">
                        <div class="col-12" data-date-range>
                            <label class="form-label" for="planDate-filter-from">
                                <mvc:message code="${PLAN_DATE_FIELD.messageKey}"/>
                            </label>
                            <div class="date-picker-container mb-1">
                                <mvc:text id="planDate-filter-from" path="filterPlanDateBegin" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                            </div>
                            <div class="date-picker-container mb-1">
                                <mvc:text id="planDate-filter-to" path="filterPlanDateEnd" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                            </div>
                        </div>
                    </c:if>

                    <%-- Archive filter --%>
                    <c:if test="${fn:contains(mailingOverviewForm.selectedFields, ARCHIVE_FIELD.sortColumn)}">
                        <div class="col-12">
                            <label class="form-label" for="archive-filter">
                                <mvc:message code="${ARCHIVE_FIELD.messageKey}"/>
                            </label>

                            <mvc:select id="archive-filter" path="filterArchives" cssClass="form-control" multiple="true">
                                <mvc:options itemValue="id" itemLabel="shortname" items="${archives}"/>
                            </mvc:select>
                        </div>
                    </c:if>
                </c:if>

                <%-- Creation date filter --%>
                <c:if test="${forTemplates or fn:contains(mailingOverviewForm.selectedFields, CREATION_DATE_FIELD.sortColumn)}">
                    <div class="col-12" data-date-range>
                        <label class="form-label" for="creationDate-filter-from">
                            <mvc:message code="${CREATION_DATE_FIELD.messageKey}"/>
                        </label>
                        <div class="date-picker-container mb-1">
                            <mvc:text id="creationDate-filter-from" path="filterCreationDateBegin" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container mb-1">
                            <mvc:text id="creationDate-filter-to" path="filterCreationDateEnd" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                </c:if>

                <%-- Change date filter --%>
                <c:if test="${forTemplates or fn:contains(mailingOverviewForm.selectedFields, CHANGE_DATE_FIELD.sortColumn)}">
                    <div class="col-12" data-date-range>
                        <label class="form-label" for="changeDate-filter-from">
                            <mvc:message code="${CHANGE_DATE_FIELD.messageKey}"/>
                        </label>
                        <div class="date-picker-container mb-1">
                            <mvc:text id="changeDate-filter-from" path="filterChangeDateBegin" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container mb-1">
                            <mvc:text id="changeDate-filter-to" path="filterChangeDateEnd" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</mvc:form>

<script id="mailing-status-selection" type="text/x-mustache-template">
    <div class="d-flex align-items-center gap-1">
        <span class="status-badge {{- element.getAttribute('data-badge-class')}}"></span>
        <span>{{- text }}</span>
    </div>
</script>
