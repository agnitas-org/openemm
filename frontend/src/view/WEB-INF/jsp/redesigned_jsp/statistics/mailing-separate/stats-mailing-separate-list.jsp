<%@ page import="com.agnitas.emm.core.mailing.enums.MailingAdditionalColumn" %>
<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingStatatisticListForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatatisticListForm"--%>
<%--@elvariable id="mailingStatisticList" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="availableAdditionalFields" type="java.util.List"--%>
<%--@elvariable id="availableMailingLists" type="java.util.List"--%>
<%--@elvariable id="availableArchives" type="java.util.List<com.agnitas.beans.Campaign>"--%>
<%--@elvariable id="mailingStat" type="java.util.Map"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<c:set var="CREATION_DATE_FIELD" value="<%= MailingAdditionalColumn.CREATION_DATE %>"/>
<c:set var="MAILING_ID_FIELD" value="<%= MailingAdditionalColumn.MAILING_ID %>"/>
<c:set var="RECIPIENTS_COUNT_FIELD" value="<%= MailingAdditionalColumn.RECIPIENTS_COUNT %>"/>
<c:set var="TARGET_GROUPS_FIELD" value="<%= MailingAdditionalColumn.TARGET_GROUPS %>"/>

<c:set var="mailingIdFieldSelected" value="${fn:contains(mailingStatatisticListForm.additionalFieldsSet, MAILING_ID_FIELD.sortColumn)}" />
<c:set var="creationDateFieldSelected" value="${fn:contains(mailingStatatisticListForm.additionalFieldsSet, CREATION_DATE_FIELD.sortColumn)}" />
<c:set var="recipientsCountFieldSelected" value="${fn:contains(mailingStatatisticListForm.additionalFieldsSet, RECIPIENTS_COUNT_FIELD.sortColumn)}" />
<c:set var="targetGroupsFieldSelected" value="${fn:contains(mailingStatatisticListForm.additionalFieldsSet, TARGET_GROUPS_FIELD.sortColumn)}" />

<mvc:form cssClass="filter-overview" servletRelativeAction="/statistics/mailing/list.action" method="GET" modelAttribute="mailingStatatisticListForm"
          data-form="search" data-form-content="" data-controller="mailing-separate-statistics" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile" data-editable-tile="main">
        <c:if test="${not mailingStatatisticListForm.inEditColumnsMode}">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "mailing-separate-stats-overview": {
                        "rows-count": ${mailingStatatisticListForm.numberOfRows},
                        "fields": ${emm:toJson(mailingStatatisticListForm.additionalFieldsSet)}
                    }
                }
            </script>
        </c:if>

        <div class="tile-body">
            <div class="table-wrapper" data-table-column-manager data-action="update-columns">
                <script type="application/json" data-table-column-manager-config>
                    {
                        "columns": [
                            <c:forEach var="field" items="${availableAdditionalFields}" varStatus="loop_status">
                                <mvc:message var="additionalFieldText" code="${field.messageKey}" />
                                {
                                "name": ${emm:toJson(field.sortColumn)},
                                "text": ${emm:toJson(additionalFieldText)},
                                "selected": ${fn:contains(mailingStatatisticListForm.additionalFieldsSet, field.sortColumn)}
                                }${loop_status.index + 1 lt fn:length(availableAdditionalFields) ? ',' : ''}
                             </c:forEach>
                        ],
                        "editMode": ${mailingStatatisticListForm.inEditColumnsMode}
                    }
                </script>

                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../common/table/edit-columns-btn.jspf" %>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${mailingStatisticList.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${mailingStatisticList.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="mailingStat" modelAttribute="mailingStatisticList" cssClass="table table-hover table--borderless js-table">

                        <emm:column titleKey="mailing.searchName" sortable="true" property="shortname"   data-table-column="" />
                        <emm:column titleKey="Description"        sortable="true" property="description" data-table-column="" />
                        <emm:column titleKey="Mailinglist"        sortable="true" property="mailinglist" data-table-column="" />
                        <emm:column titleKey="mailing.archive"    sortable="true" property="archive"     data-table-column="" sortProperty="campaign_id" />
                        <emm:column titleKey="mailing.senddate"   sortable="true" property="senddate"    data-table-column="" />

                        <emm:column titleKey="${CREATION_DATE_FIELD.messageKey}" sortable="true" sortProperty="creation_date"
                                           property="creationdate"
                                           headerClass="${creationDateFieldSelected ? '' : 'hidden'}" cssClass="${creationDateFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${CREATION_DATE_FIELD.sortColumn}" />

                        <emm:column titleKey="Target-Groups" sortable="true" sortProperty="target_expression"
                                           headerClass="${targetGroupsFieldSelected ? '' : 'hidden'}" cssClass="${targetGroupsFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${TARGET_GROUPS_FIELD.sortColumn}">
                            <span>
                               <c:forEach var="targetgroup" items="${mailingStat.targetgroups}" varStatus="loop_status">
                                    <a href="<c:url value='/target/${targetgroup.target_id}/view.action'/>">
                                        ${targetgroup.target_name}
                                    </a>
                                   <c:if test="${loop_status.index + 1 lt fn:length(mailingStat.targetgroups)}">
                                       <br>
                                       <br>
                                   </c:if>
                               </c:forEach>
                            </span>
                        </emm:column>

                        <%-- Additional Fiels --%>
                        <c:forEach var="selectedAdditionalField" items="${mailingStatatisticListForm.additionalFields}">
                            <%-- Searching for possible additional field --%>
                            <c:forEach var="availableAdditionalField" items="${availableAdditionalFields}">
                                <c:if test="${availableAdditionalField.sortColumn == selectedAdditionalField}">
                                    <c:choose>
                                        <c:when test="${availableAdditionalField == 'TEMPLATE'}">
                                            <emm:column titleKey="${availableAdditionalField.messageKey}" sortable="true" sortProperty="template_name" property="templateName"
                                                               data-table-column="${availableAdditionalField.sortColumn}" />
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'SUBJECT'}">
                                            <emm:column titleKey="${availableAdditionalField.messageKey}" sortable="true" property="subject"
                                                               data-table-column="${availableAdditionalField.sortColumn}" />
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </c:forEach>

                        <emm:column titleKey="${MAILING_ID_FIELD.messageKey}" sortable="true" sortProperty="mailing_id" property="mailingid"
                                           headerClass="${mailingIdFieldSelected ? '' : 'hidden'}" cssClass="${mailingIdFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${MAILING_ID_FIELD.sortColumn}" />

                        <emm:column titleKey="${RECIPIENTS_COUNT_FIELD.messageKey}" property="recipientsCount" sortable="true" sortProperty="${RECIPIENTS_COUNT_FIELD.sortColumn}"
                                           headerClass="${recipientsCountFieldSelected ? '' : 'hidden'}" cssClass="${recipientsCountFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${RECIPIENTS_COUNT_FIELD.sortColumn}"/>

                        <emm:column headerClass="columns-picker">
                            <a href="<c:url value="/statistics/mailing/${mailingStat.mailingid}/view.action"/>" class="hidden" data-view-row="page"></a>
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
                <a href="#" class="icon icon-question-circle" data-help="mailing/overview/SearchFor.xml"></a>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary"  data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body form-column js-scrollable">
            <div>
                <mvc:message var="nameMsg" code="mailing.searchName"/>
                <label class="form-label" for="name-filter">${nameMsg}</label>
                <mvc:text id="name-filter" path="filterName" cssClass="form-control" placeholder="${nameMsg}"/>
            </div>

            <div>
                <mvc:message var="descriptionMsg" code="Description"/>
                <label class="form-label" for="description-filter">${descriptionMsg}</label>
                <mvc:text id="description-filter" path="filterDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
            </div>

            <div>
                <label for="filter-mailinglists" class="form-label"><mvc:message code="Mailinglist" /></label>
                <select id="filter-mailinglists" class="form-control" name="filteredMailingLists" multiple="multiple">
                    <c:forEach var="availableMailingList" items="${availableMailingLists}">
                        <c:set var="filteredMailingListSelected" value=""/>
                        <c:forEach var="filteredMailingListId"
                                   items="${mailingStatatisticListForm.filteredMailingLists}">
                            <c:if test="${availableMailingList.id eq filteredMailingListId}">
                                <c:set var="filteredMailingListSelected" value="selected"/>
                            </c:if>
                        </c:forEach>

                        <option value="${availableMailingList.id}" ${filteredMailingListSelected}>
                            <c:out value="${availableMailingList.shortname}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div data-date-range>
                <label class="form-label" for="filterSendDateBegin"><mvc:message code="mailing.senddate"/></label>
                <div class="date-picker-container mb-1">
                    <mvc:message var="fromMsg" code="From" />
                    <mvc:text id="filterSendDateBegin" path="filterSendDateBegin" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                </div>
                <div class="date-picker-container">
                    <mvc:message var="toMsg" code="To" />
                    <mvc:text id="filterSendDateEnd" path="filterSendDateEnd" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                </div>
            </div>

            <div>
                <label for="archives-filter" class="form-label"><mvc:message code="mailing.archive" /></label>
                <mvc:select id="archives-filter" cssClass="form-control js-select" path="filterArchives" multiple="multiple">
                    <mvc:options items="${availableArchives}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>
        </div>
    </div>
</mvc:form>
