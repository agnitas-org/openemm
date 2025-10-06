<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.mailing.enums.MailingAdditionalColumn" %>
<%@ page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.emm.common.MailingStatus" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingOverviewForm" type="com.agnitas.emm.core.mailing.forms.MailingOverviewForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="mailing" type="java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="mailinglist" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="archives" type="java.util.List<com.agnitas.beans.Campaign>"--%>
<%--@elvariable id="contentSearchEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="searchEnabled" type="java.lang.Boolean"--%>

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
<c:set var="MAILING_ID_FIELD" value="<%= MailingAdditionalColumn.MAILING_ID %>"/>
<c:set var="RECIPIENTS_COUNT_FIELD" value="<%= MailingAdditionalColumn.RECIPIENTS_COUNT %>"/>
<c:set var="TARGET_GROUPS_FIELD" value="<%= MailingAdditionalColumn.TARGET_GROUPS %>"/>

<c:set var="planDateFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, PLAN_DATE_FIELD.sortColumn)}" />
<c:set var="mailingIdFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, MAILING_ID_FIELD.sortColumn)}" />
<c:set var="changeDateFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, CHANGE_DATE_FIELD.sortColumn)}" />
<c:set var="creationDateFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, CREATION_DATE_FIELD.sortColumn)}" />
<c:set var="recipientsCountFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, RECIPIENTS_COUNT_FIELD.sortColumn)}" />
<c:set var="targetGroupsFieldSelected" value="${fn:contains(mailingOverviewForm.selectedFields, TARGET_GROUPS_FIELD.sortColumn)}" />

<c:set var="forTemplates" value="${mailingOverviewForm.forTemplates}"/>
<c:choose>
    <c:when test="${forTemplates}">
        <c:set var="isDeleteAllowed" value="${emm:permissionAllowed('template.delete', pageContext.request)}" />
        <mvc:message var="deleteTooltipMsg" code="template.delete" />
        <mvc:message var="bulkDeleteMsg" code="bulkAction.delete.template" />
        <c:url var="deleteUrl" value="/mailing/deleteTemplates.action"/>
    </c:when>
    <c:otherwise>
        <c:set var="isDeleteAllowed" value="${emm:permissionAllowed('mailing.delete', pageContext.request)}" />
        <mvc:message var="deleteTooltipMsg" code="mailing.MailingDelete" />
        <mvc:message var="bulkDeleteMsg" code="bulkAction.delete.mailing" />
        <c:url var="deleteUrl" value="/mailing/deleteMailings.action"/>
    </c:otherwise>
</c:choose>

<mvc:form id="mailing-overview-form" cssClass="filter-overview" servletRelativeAction="/mailing/list.action" method="GET" modelAttribute="mailingOverviewForm"
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
                <c:if test="${not mailingOverviewForm.inEditColumnsMode}">
                    <script type="application/json" data-initializer="web-storage-persist">
                        {
                            "mailing-overview": {
                                "page": ${mailingOverviewForm.page},
                                "rows-count": ${mailingOverviewForm.numberOfRows},
                                "fields": ${emm:toJson(mailingOverviewForm.selectedFields)},
                                "mailing-types": ${emm:toJson(mailingOverviewForm.mailingTypes)},
                                "media-types": ${emm:toJson(mailingOverviewForm.mediaTypes)}
                            }
                        }
                    </script>
                </c:if>
            </c:otherwise>
        </c:choose>

        <div class="tile-body">
            <div class="table-wrapper" ${not forTemplate ? 'data-table-column-manager data-action="update-columns"' : ''}>
                <c:if test="${not forTemplates}">
                    <script type="application/json" data-table-column-manager-config>
                        {
                            "columns": [
                                <c:forEach var="field" items="${ADDITIONAL_FIELDS}" varStatus="loop_status">
                                    <mvc:message var="additionalFieldText" code="${field.messageKey}" />
                                    {
                                        "name": ${emm:toJson(field.sortColumn)},
                                        "text": ${emm:toJson(additionalFieldText)},
                                        "selected": ${fn:contains(mailingOverviewForm.selectedFields, field.sortColumn)}
                                    }${loop_status.index + 1 lt fn:length(ADDITIONAL_FIELDS) ? ',' : ''}
                                </c:forEach>
                            ],
                            "editMode": ${mailingOverviewForm.inEditColumnsMode}
                        }
                    </script>
                </c:if>

                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${isDeleteAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:choose>
                                        <c:when test="${mailingOverviewForm.useRecycleBin}">
                                            <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code="bulk.mailing.restore" />"
                                               data-form-url="<c:url value="/mailing/bulkRestore.action" />" data-form-method="POST" data-form-submit>
                                                <i class="icon icon-redo"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${bulkDeleteMsg}" data-form-url="${deleteUrl}" data-form-confirm>
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>

                        <c:if test="${not forTemplates}">
                            <jsp:include page="../../common/table/preview-switch.jsp">
                                <jsp:param name="selector" value="#mailings-table"/>
                            </jsp:include>

                            <%@include file="../../common/table/edit-columns-btn.jspf" %>
                        </c:if>

                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${mailinglist.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${mailinglist.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table id="mailings-table" var="mailing" modelAttribute="mailinglist" cssClass="table table--borderless ${mailingOverviewForm.useRecycleBin ? '' : 'table-hover'} js-table">

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
                            <emm:column cssClass="thumbnail-cell" headerClass="hidden">
                                <img src="${previewImageSrc}" alt="Thumbnail">
                            </emm:column>
                        </c:if>

                        <c:if test="${isDeleteAllowed}">
                            <emm:column title="<input type='checkbox' class='form-check-input' data-bulk-checkboxes />" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input type="checkbox" name="bulkIds" class="form-check-input" value="${mailing.mailingid}" data-bulk-checkbox>
                            </emm:column>
                        </c:if>

                        <c:if test="${not forTemplates or mailingOverviewForm.useRecycleBin}">
                            <emm:column titleKey="Status" sortable="true" sortProperty="work_status" headerClass="mobile-hidden fit-content" data-table-column="">
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
                                        <a href="#" tabindex="-1" type="button" class="flex-center" data-help="mailing/overview/WorkflowEditorMailingOverviewMsg.xml">
                                            <span class="status-badge mailing.status.cm" data-tooltip="<mvc:message code="workflow.single"/>"></span>
                                        </a>
                                    </c:if>

                                    <%--icon for 'Used trigger'--%>
                                    <c:if test="${mailing.hasActions}">
                                        <a href="<c:url value="/mailing/${mailing.mailingid}/actions.action"/>" class="status-badge mailing.status.trigger" data-tooltip="<mvc:message code="action.action_link"/>" data-confirm></a>
                                    </c:if>
                                </div>
                            </emm:column>

                            <emm:column titleKey="Name" sortable="true" property="shortname" cssClass="fluid-cell" data-table-column="" />
                            <emm:column titleKey="Description" sortable="true" property="description" headerClass="mobile-hidden" cssClass="table-preview-hidden mobile-hidden" data-table-column="" />

                            <emm:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="mobile-hidden" cssClass="secondary-cell" data-table-column="">
                                <i class="icon icon-list"></i>
                                <span>${mailing.mailinglist}</span>
                            </emm:column>

                            <emm:column titleKey="mailing.senddate" sortable="true" property="senddate" headerClass="fit-content mobile-hidden" cssClass="table-preview-hidden" data-table-column="" />

                            <emm:column titleKey="${CREATION_DATE_FIELD.messageKey}" sortable="true" sortProperty="creation_date" property="creationdate"
                                               headerClass="fit-content mobile-hidden ${creationDateFieldSelected ? '' : 'hidden'}" cssClass="table-preview-hidden ${creationDateFieldSelected ? '' : 'hidden'}"
                                               data-table-column="${CREATION_DATE_FIELD.sortColumn}" />

                            <emm:column titleKey="Target-Groups" headerClass="mobile-hidden ${targetGroupsFieldSelected ? '' : 'hidden'}"
                                               cssClass="table-preview-hidden ${targetGroupsFieldSelected ? '' : 'hidden'}"
                                               data-table-column="${TARGET_GROUPS_FIELD.sortColumn}">
                                <span>
                                   <c:forEach var="targetgroup" items="${mailing.targetgroups}" varStatus="loop_status">
                                        <a href="<c:url value='/target/${targetgroup.target_id}/view.action'/>">
                                            ${targetgroup.target_name}
                                        </a>
                                       <c:if test="${loop_status.index + 1 lt fn:length(mailing.targetgroups)}">
                                           <br>
                                           <br>
                                       </c:if>
                                   </c:forEach>
                                </span>
                            </emm:column>

                            <c:forEach var="selectedField" items="${mailingOverviewForm.selectedFields}">
                                <c:forEach var="field" items="${ADDITIONAL_FIELDS}">
                                    <c:if test="${selectedField == field.sortColumn}">
                                        <c:choose>
                                            <c:when test="${field == 'ARCHIVE'}">
                                                <emm:column titleKey="${field.messageKey}" sortable="true" sortProperty="campaign_id" property="archive" headerClass="mobile-hidden" cssClass="table-preview-hidden" data-table-column="${field.sortColumn}" />
                                            </c:when>

                                            <c:when test="${field == 'TEMPLATE'}">
                                                <emm:column titleKey="${field.messageKey}" sortable="true" sortProperty="template_name" property="templateName" headerClass="mobile-hidden" cssClass="table-preview-hidden" data-table-column="${field.sortColumn}" />
                                            </c:when>

                                            <c:when test="${field == 'SUBJECT'}">
                                                <emm:column titleKey="${field.messageKey}" sortable="true" property="subject" headerClass="mobile-hidden" cssClass="table-preview-hidden" data-table-column="${field.sortColumn}" />
                                            </c:when>
                                        </c:choose>
                                    </c:if>
                                </c:forEach>
                            </c:forEach>

                            <emm:column titleKey="${MAILING_ID_FIELD.messageKey}" sortable="true" sortProperty="mailing_id" property="mailingid"
                                               headerClass="fit-content mobile-hidden ${mailingIdFieldSelected ? '' : 'hidden'}" cssClass="table-preview-hidden ${mailingIdFieldSelected ? '' : 'hidden'}"
                                               data-table-column="${MAILING_ID_FIELD.sortColumn}" />

                            <c:set var="recipientsTitle">
                                <span><mvc:message code="${RECIPIENTS_COUNT_FIELD.messageKey}"/></span>
                                <button class="icon-btn" data-help="mailing/view_base/NumberOfRecipients.xml">
                                    <i class="icon icon-question-circle"></i>
                                </button>
                            </c:set>
                            <emm:column title="${recipientsTitle}" sortable="true" sortProperty="${RECIPIENTS_COUNT_FIELD.sortColumn}"
                                               headerClass="mobile-hidden ${recipientsCountFieldSelected ? '' : 'hidden'}" cssClass="table-preview-hidden ${recipientsCountFieldSelected ? '' : 'hidden'}"
                                               data-table-column="${RECIPIENTS_COUNT_FIELD.sortColumn}">
                                <span>
                                    <c:choose>
                                        <c:when test="${mailing.mailing_type ne MAILING_TYPE_NORMAL.code}">
                                            <%-- Replace 0 values with "n/a" --%>
                                            <mvc:message code="NotAvailableShort" />
                                        </c:when>
                                        <c:otherwise>
                                            ${mailing.recipientsCount}
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </emm:column>

                            <emm:column titleKey="${CHANGE_DATE_FIELD.messageKey}" sortable="true" sortProperty="change_date" property="changedate" headerClass="mobile-hidden ${changeDateFieldSelected ? '' : 'hidden'}" cssClass="table-preview-hidden ${changeDateFieldSelected ? '' : 'hidden'}" data-table-column="${CHANGE_DATE_FIELD.sortColumn}" />

                            <emm:column titleKey="${PLAN_DATE_FIELD.messageKey}" sortable="true" sortProperty="${PLAN_DATE_FIELD.sortColumn}" property="planDate"
                                               headerClass="mobile-hidden ${planDateFieldSelected ? '' : 'hidden'}" cssClass="table-preview-hidden ${planDateFieldSelected ? '' : 'hidden'}"
                                               data-table-column="${PLAN_DATE_FIELD.sortColumn}" />
                        </c:if>
                        <c:if test="${forTemplates}">
                            <emm:column titleKey="Name" sortable="true" property="shortname" />
                            <emm:column titleKey="Description" sortable="true" property="description" />
                            <emm:column titleKey="Mailinglist" sortable="true" property="mailinglist" />
                            <emm:column titleKey="default.creationDate" sortable="true" sortProperty="creation_date" property="creationdate" />

                            <emm:column titleKey="default.changeDate" sortable="true" sortProperty="change_date" property="changedate" />
                        </c:if>

                        <emm:column cssClass="table-actions mobile-hidden" headerClass="mobile-hidden ${forTemplates ? '' : 'columns-picker'}">
                            <template class="js-row-popover">
                                <img src="${previewImageSrc}" alt="${fn:escapeXml(mailing.shortname)}" class="popover__thumbnail">
                            </template>

                            <c:choose>
                                <c:when test="${mailingOverviewForm.useRecycleBin}">
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="<mvc:message code="default.restore" />" data-form-submit data-form-method="POST"
                                         data-form-url="<c:url value="/mailing/${mailing.mailingid}/restore.action" />">
                                        <i class="icon icon-redo"></i>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${isDeleteAllowed}">
                                        <a href="${deleteUrl}?bulkIds=${mailing.mailingid}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteTooltipMsg}">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>
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
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-toggle-tile data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
                <c:if test="${searchEnabled or contentSearchEnabled}">
                    <a href="#" class="icon icon-question-circle" data-help="mailing/overview/SearchFor.xml"></a>
                </c:if>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"
                   data-form-persist="mailingTypes: '${MAILING_TYPE_NORMAL}', mediaTypes: '${MEDIA_TYPE_EMAIL}', filterName: '', filterDescription: '', filterContent: '', filterStatuses: '', filterBadges: '', filterMailingLists: '', filterSendDateBegin: '', filterSendDateEnd: '', filterCreationDateBegin: '', filterCreationDateEnd: '', filterPlanDateBegin: '', filterPlanDateEnd: '', filterChangeDateBegin: '', filterChangeDateEnd: '', filterArchives: '', useRecycleBin: false">
                    <i class="icon icon-undo-alt"></i>
                </a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body vstack gap-3 js-scrollable">
            <c:if test="${searchEnabled}">
                <div>
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="name-filter">${nameMsg}</label>
                    <mvc:text id="name-filter" path="filterName" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>

                <div>
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description-filter">${descriptionMsg}</label>
                    <mvc:text id="description-filter" path="filterDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </c:if>

            <c:if test="${not forTemplates and contentSearchEnabled}">
                <div>
                    <mvc:message var="contentMsg" code="default.Content"/>
                    <label class="form-label" for="content-filter">${contentMsg}</label>
                    <mvc:text id="content-filter" path="filterContent" cssClass="form-control" placeholder="${contentMsg}"/>
                </div>
            </c:if>

            <div>
                <label class="form-label" for="mailinglists-filter"><mvc:message code="Mailinglist"/></label>
                <mvc:select id="mailinglists-filter" path="filterMailingLists" cssClass="form-control" multiple="true">
                    <mvc:options itemValue="id" itemLabel="shortname" items="${mailinglists}"/>
                </mvc:select>
            </div>

            <c:if test="${not forTemplates}">
                <div>
                    <label class="form-label" for="status-filter"><mvc:message code="Status"/></label>

                    <c:set var="mailingTypes" value="${mailingOverviewForm.mailingTypes}"/>
                    <c:set var="defaultFilterStatuses" value="${[MailingStatus.NEW, MailingStatus.EDIT, MailingStatus.READY, MailingStatus.ADMIN, MailingStatus.TEST]}"/>

                    <mvc:select id="status-filter" path="filterStatuses" cssClass="form-control" multiple="true" data-result-template="select2-badge-option">
                        <c:forEach var="status" items="${defaultFilterStatuses}">
                            <mvc:option value="${status}" data-badge-class="${status.dbKey}"><mvc:message code="${status.messageKey}"/></mvc:option>
                        </c:forEach>

                        <mvc:option value="${MailingStatus.SCHEDULED}" data-badge-class="${MailingStatus.SCHEDULED.dbKey}" data-show-by-select="#mailingType-filter"
                                    data-show-by-select-values="${MAILING_TYPE_NORMAL}, ${MAILING_TYPE_FOLLOW_UP}" data-show-if-no-selection="">
                            <mvc:message code="${MailingStatus.SCHEDULED.messageKey}"/>
                        </mvc:option>

                        <c:forEach var="status" items="${[MailingStatus.IN_GENERATION, MailingStatus.GENERATION_FINISHED]}">
                            <mvc:option value="${status}" data-badge-class="${status.dbKey}" data-show-by-select="#mailingType-filter"
                                        data-show-by-select-values="${MAILING_TYPE_NORMAL}" data-show-if-no-selection="">
                                <mvc:message code="${status.messageKey}"/>
                            </mvc:option>
                        </c:forEach>

                        <c:forEach var="status" items="${[MailingStatus.SENT, MailingStatus.NORECIPIENTS, MailingStatus.CANCELED, MailingStatus.INSUFFICIENT_VOUCHERS, MailingStatus.SENDING]}">
                            <mvc:option value="${status}" data-badge-class="${status.dbKey}" data-show-by-select="#mailingType-filter"
                                        data-show-by-select-values="${MAILING_TYPE_NORMAL}, ${MAILING_TYPE_FOLLOW_UP}" data-show-if-no-selection="">
                                <mvc:message code="${status.messageKey}"/>
                            </mvc:option>
                        </c:forEach>

                        <c:forEach var="status" items="${[MailingStatus.ACTIVE, MailingStatus.DISABLE]}">
                            <mvc:option value="${status}" data-badge-class="${status.dbKey}" data-show-by-select="#mailingType-filter"
                                        data-show-by-select-values="${MAILING_TYPE_ACTION_BASED}, ${MAILING_TYPE_DATE_BASED}, ${MAILING_TYPE_INTERVAL}" data-show-if-no-selection="">
                                <mvc:message code="${status.messageKey}"/>
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <%-- Send date filter --%>
                <div data-date-range>
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
            </c:if>

            <%@include file="fragments/mailing-types-select.jspf" %>

            <%@include file="./fragments/use-recycle-bin-option.jspf" %>

            <c:if test="${not forTemplates}">
                <%@include file="./fragments/mediatypes-filter.jspf" %>

                <%-- Plan date filter --%>
                <c:if test="${planDateFieldSelected}">
                    <div data-date-range>
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
                    <div>
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
            <c:if test="${forTemplates or creationDateFieldSelected}">
                <div data-date-range>
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
            <c:if test="${forTemplates or changeDateFieldSelected}">
                <div data-date-range>
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
</mvc:form>
