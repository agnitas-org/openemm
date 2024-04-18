<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingStatatisticListForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatatisticListForm"--%>
<%--@elvariable id="mailingStatisticList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="availableAdditionalFields" type="java.util.List"--%>
<%--@elvariable id="availableMailingLists" type="java.util.List"--%>
<%--@elvariable id="mailingStat" type="java.util.Map"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<mvc:form cssClass="filter-overview hidden" servletRelativeAction="/statistics/mailing/list.action" method="GET" modelAttribute="mailingStatatisticListForm"
          data-form="search" data-form-content="" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile" data-editable-tile="main">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mailing-separate-stats-overview": {
                    "rows-count": ${mailingStatatisticListForm.numberOfRows},
                    "fields": ${emm:toJson(mailingStatatisticListForm.additionalFieldsSet)}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <c:set var="addAdditionalColumns">
            <div class="dropdown table-header-dropdown">
                <i class="icon icon-plus" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false"></i>

                <ul class="dropdown-menu">
                    <div class="d-flex flex-column gap-2">
                        <select class="form-control dropdown-select" multiple="multiple" name="additionalFields">
                            <c:forEach var="availableField" items="${availableAdditionalFields}">
                                <c:if test="${availableField != 'CHANGE_DATE' and availableField != 'ARCHIVE' and availableField != 'PLAN_DATE'}">
                                    <c:set var="selected" value=""/>
                                    <c:forEach var="additionalField" items="${mailingStatatisticListForm.additionalFields}"
                                               varStatus="rowCounter">
                                        <c:if test="${availableField.sortColumn == additionalField}">
                                            <c:set var="selected" value="selected"/>
                                        </c:if>
                                    </c:forEach>

                                    <option value="${availableField.sortColumn}" ${selected}>
                                        <mvc:message code="${availableField.messageKey}"/>
                                    </option>
                                </c:if>
                            </c:forEach>
                        </select>

                        <button class="btn btn-primary js-dropdown-close" type="button" data-form-change data-form-submit>
                            <i class="icon icon-sync"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </ul>
            </div>
        </c:set>

        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="mailingStat" name="mailingStatisticList" partialList="true"
                                   pagesize="${mailingStatatisticListForm.numberOfRows}"
                                   size="${mailingStatisticList.fullListSize}" excludedParams="*"
                                   requestURI="/statistics/mailing/list.action?__fromdisplaytag=true">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column titleKey="mailing.searchName" sortable="true" sortProperty="shortname" property="shortname" headerClass="js-table-sort"/>
                        <display:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort" property="description"/>

                        <display:column titleKey="Mailinglist" sortable="true" sortProperty="mailinglist" headerClass="js-table-sort js-filter-mailing-list" property="mailinglist"/>

                        <display:column titleKey="mailing.senddate" sortable="true" sortProperty="senddate" headerClass="js-table-sort">
                            <emm:formatDate value="${mailingStat.senddate}" format="${dateTimeFormat}"/>
                        </display:column>

                        <%-- Additional Fiels --%>
                        <c:forEach var="selectedAdditionalField" items="${mailingStatatisticListForm.additionalFields}">
                            <%-- Searching for possible additional field --%>
                            <c:forEach var="availableAdditionalField" items="${availableAdditionalFields}">
                                <c:if test="${availableAdditionalField.sortColumn == selectedAdditionalField}">
                                    <c:choose>
                                        <c:when test="${availableAdditionalField == 'RECIPIENTS_COUNT'}">
                                            <display:column property="recipientsCount" sortable="true" headerClass="js-table-sort"
                                                            titleKey="${availableAdditionalField.messageKey}"
                                                            sortProperty="${availableAdditionalField.sortColumn}"/>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'CREATION_DATE'}">
                                            <display:column titleKey="${availableAdditionalField.messageKey}" sortable="true"
                                                            sortProperty="creation_date" headerClass="js-table-sort">
                                                <emm:formatDate value="${mailingStat.creationdate}" format="${dateTimeFormat}"/>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'TEMPLATE'}">
                                            <display:column titleKey="${availableAdditionalField.messageKey}" sortable="true"
                                                            sortProperty="template_name" headerClass="js-table-sort" property="templateName"/>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'SUBJECT'}">
                                            <display:column titleKey="${availableAdditionalField.messageKey}" sortable="true"
                                                            sortProperty="subject" headerClass="js-table-sort" property="subject"/>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'TARGET_GROUPS'}">
                                            <display:column titleKey="Target-Groups" sortable="true" sortProperty="target_expression" headerClass="js-table-sort">
                                                <div class="d-flex flex-column">
                                                    <c:forEach var="targetgroup" items="${mailingStat.targetgroups}">
                                                        <a href="<c:url value='/target/${targetgroup.target_id}/view.action'/>" class="text-truncate">
                                                            ${targetgroup.target_name}
                                                        </a>
                                                    </c:forEach>
                                                </div>
                                            </display:column>
                                        </c:when>

                                        <c:when test="${availableAdditionalField == 'MAILING_ID'}">
                                            <display:column titleKey="${availableAdditionalField.messageKey}" sortable="true"
                                                            sortProperty="mailing_id" headerClass="js-table-sort" property="mailingid"/>
                                        </c:when>
                                    </c:choose>
                                </c:if>
                            </c:forEach>
                        </c:forEach>

                        <display:column class="fit-content" title="${addAdditionalColumns}" headerClass="additional-columns">
                            <a href='<c:url value="/statistics/mailing/${mailingStat.mailingid}/view.action"/>' class="hidden" data-view-row="page"></a>
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
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/mailing/overview/SearchFor.xml"></a>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse"  data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
