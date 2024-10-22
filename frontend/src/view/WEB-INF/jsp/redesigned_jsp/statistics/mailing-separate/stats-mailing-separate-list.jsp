<%@ page import="org.agnitas.web.MailingAdditionalColumn" %>
<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingStatatisticListForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatatisticListForm"--%>
<%--@elvariable id="mailingStatisticList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="availableAdditionalFields" type="java.util.List"--%>
<%--@elvariable id="availableMailingLists" type="java.util.List"--%>
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
                    <agnDisplay:table class="table table-hover table--borderless js-table"
                                   id="mailingStat" name="mailingStatisticList" partialList="true"
                                   pagesize="${mailingStatatisticListForm.numberOfRows}"
                                   size="${mailingStatisticList.fullListSize}" excludedParams="*"
                                   requestURI="/statistics/mailing/list.action">

                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <agnDisplay:column titleKey="mailing.searchName" sortable="true" sortProperty="shortname" headerClass="js-table-sort" data-table-column="">
                            <span>${mailingStat.shortname}</span>
                        </agnDisplay:column>
                        <agnDisplay:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" data-table-column="">
                            <span>${mailingStat.description}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort" data-table-column="">
                            <span>${mailingStat.mailinglist}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="mailing.senddate" sortable="true" sortProperty="senddate" headerClass="js-table-sort" data-table-column="">
                            <span><emm:formatDate value="${mailingStat.senddate}" format="${dateTimeFormat}"/></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="${CREATION_DATE_FIELD.messageKey}" sortable="true" sortProperty="creation_date"
                                           headerClass="js-table-sort ${creationDateFieldSelected ? '' : 'hidden'}" class="${creationDateFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${CREATION_DATE_FIELD.sortColumn}">
                            <span><emm:formatDate value="${mailingStat.creationdate}" format="${dateTimeFormat}" /></span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="Target-Groups" sortable="true" sortProperty="target_expression"
                                           headerClass="js-table-sort ${targetGroupsFieldSelected ? '' : 'hidden'}" class="${targetGroupsFieldSelected ? '' : 'hidden'}"
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
                        </agnDisplay:column>

                        <%-- Additional Fiels --%>
                        <c:forEach var="selectedAdditionalField" items="${mailingStatatisticListForm.additionalFields}">
                            <%-- Searching for possible additional field --%>
                            <c:forEach var="availableAdditionalField" items="${availableAdditionalFields}">
                                <c:if test="${availableAdditionalField.sortColumn == selectedAdditionalField}">
                                    <c:choose>
                                        <c:when test="${availableAdditionalField == 'TEMPLATE'}">
                                            <agnDisplay:column titleKey="${availableAdditionalField.messageKey}" sortable="true" sortProperty="template_name" headerClass="js-table-sort"
                                                               data-table-column="${availableAdditionalField.sortColumn}">
                                                <span>${mailingStat.templateName}</span>
                                            </agnDisplay:column>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'SUBJECT'}">
                                            <agnDisplay:column titleKey="${availableAdditionalField.messageKey}" sortable="true" sortProperty="subject" headerClass="js-table-sort"
                                                               data-table-column="${availableAdditionalField.sortColumn}">
                                                <span>${mailingStat.subject}</span>
                                            </agnDisplay:column>
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </c:forEach>

                        <agnDisplay:column titleKey="${MAILING_ID_FIELD.messageKey}" sortable="true" sortProperty="mailing_id" property="mailingid"
                                           headerClass="js-table-sort ${mailingIdFieldSelected ? '' : 'hidden'}" class="${mailingIdFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${MAILING_ID_FIELD.sortColumn}" />

                        <agnDisplay:column titleKey="${RECIPIENTS_COUNT_FIELD.messageKey}" property="recipientsCount" sortable="true" sortProperty="${RECIPIENTS_COUNT_FIELD.sortColumn}"
                                           headerClass="js-table-sort ${recipientsCountFieldSelected ? '' : 'hidden'}" class="${recipientsCountFieldSelected ? '' : 'hidden'}"
                                           data-table-column="${RECIPIENTS_COUNT_FIELD.sortColumn}"/>

                        <agnDisplay:column title="${addAdditionalColumns}" headerClass="fit-content columns-picker">
                            <a href="<c:url value="/statistics/mailing/${mailingStat.mailingid}/view.action"/>" class="hidden" data-view-row="page"></a>
                        </agnDisplay:column>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-toggle-tile="mobile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/overview/SearchFor.xml"></a>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse"  data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="nameMsg" code="mailing.searchName"/>
                    <label class="form-label" for="name-filter">${nameMsg}</label>
                    <mvc:text id="name-filter" path="filterName" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>

                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description-filter">${descriptionMsg}</label>
                    <mvc:text id="description-filter" path="filterDescription" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>

                <div class="col-12">
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

                <div class="col-12" data-date-range>
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
            </div>
        </div>
    </div>
</mvc:form>
