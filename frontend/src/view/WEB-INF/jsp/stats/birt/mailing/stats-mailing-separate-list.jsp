<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingStatatisticListForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.mailingStatatisticListForm"--%>
<%--@elvariable id="mailingStatisticList" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="availableAdditionalFields" type="java.util.List"--%>
<%--@elvariable id="availableMailingLists" type="java.util.List"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List"--%>
<%--@elvariable id="mailingStat" type="java.util.Map"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<mvc:form servletRelativeAction="/statistics/mailing/list.action" modelAttribute="mailingStatatisticListForm">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-separate-stats-overview": {
                "rows-count": ${mailingStatatisticListForm.numberOfRows},
                "fields": ${emm:toJson(mailingStatatisticListForm.additionalFieldsSet)}
            }
        }
    </script>

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
                                <button class="icon icon-help"
                                        data-help="help_${helplanguage}/mailing/overview/SearchFor.xml" tabindex="-1"
                                        type="button"></button>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <mvc:text path="searchQueryText" id="searchQueryText" cssClass="form-control" />
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="placesToSearch" class="control-label">
                                <mvc:message code="mailing.searchIn"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <ul class="list-group" style="margin-bottom: 0px;" id="placesToSearch">
                                <li class="list-group-item checkbox">
                                    <label>
                                        <mvc:checkbox path="searchNameChecked"/>
                                        <mvc:message code="mailing.searchName"/>
                                    </label>
                                </li>
                                <li class="list-group-item checkbox">
                                    <label>
                                        <mvc:checkbox path="searchDescriptionChecked"/>
                                        <mvc:message code="mailing.searchDescription"/>
                                    </label>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.Overview"/>
            </h2>
            <ul class="tile-header-nav">
            </ul>

            <ul class="tile-header-actions">
                <%-- PAGE SIZE --%>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
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
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>

                <%-- ADITIONAL FIELDS --%>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-columns"></i>
                        <span class="text"><mvc:message code="settings.fields"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="settings.fields"/></li>
                        <li>
                            <select class="form-control js-select" tabindex="-1" multiple="multiple"
                                    name="additionalFields" data-action="change-columns-to-show" style="padding: 5px"
                                    title="additional fields">
                                <c:forEach var="availableField" items="${availableAdditionalFields}">
                                    <c:if test="${availableField != 'CHANGE_DATE' and availableField != 'ARCHIVE' and availableField != 'PLAN_DATE'}">
                                        <c:set var="selected" value=""/>
                                        <c:forEach var="additionalField" items="${mailingStatatisticListForm.additionalFields}"
                                                   varStatus="rowCounter">
                                            <c:if test="${availableField.sortColumn == additionalField}">
                                                <c:set var="selected" value="selected"/>
                                            </c:if>
                                        </c:forEach>

                                        <option title="${availableField.messageKey}" value="${availableField.sortColumn}" ${selected}>
                                            <mvc:message code="${availableField.messageKey}"/>
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="additionalFields: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text">
                                        <mvc:message code="button.Refresh"/>
                                    </span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content hidden" data-initializer="mailing-overview-table">
                <%-- filters --%>
            <div class="hidden">
                <%-- filter for mailing lists--%>
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-mailing-list">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu">
                        <li>
                            <select class="form-control js-select" name="filteredMailingLists" multiple="multiple"
                                    data-field-filter=""
                                    style="padding: 5px" title="mailing list filter">
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
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filteredMailingLists: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text">
                                        <mvc:message code="button.Apply"/>
                                    </span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>

                <%-- filter for mailing lists --%>
                <div class="dropdown filter" data-field="filter" data-filter-target=".js-filter-target-group">
                    <button class="btn btn-regular btn-filter dropdown-toggle" type="button" data-toggle="dropdown">
                        <i class="icon icon-filter"></i>
                    </button>
                    <ul class="dropdown-menu">
                        <li>
                            <select class="form-control js-select" name="filteredTargetGroups" multiple="multiple"
                                    data-field-filter=""
                                    style="padding: 5px" title="target group filter">
                                <c:forEach var="availableTargetGroup" items="${availableTargetGroups}">
                                    <c:set var="filteredTargetGroupSelected" value=""/>
                                    <c:forEach var="filteredTargetGroupId"
                                               items="${mailingStatatisticListForm.filteredTargetGroups}">
                                        <c:if test="${availableTargetGroup.id == filteredTargetGroupId}">
                                            <c:set var="filteredTargetGroupSelected" value="selected"/>
                                        </c:if>
                                    </c:forEach>
                                    <option value="${availableTargetGroup.id}" ${filteredTargetGroupSelected}>
                                        <c:out value="${availableTargetGroup.targetName}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="filteredTargetGroups: ''">
                                <mvc:message code="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="table-wrapper table-overflow-visible">
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        style="display: table"
                        id="mailingStat"
                        name="mailingStatisticList"
                        pagesize="${mailingStatatisticListForm.numberOfRows}"
                        partialList="true"
                        size="${mailingStatisticList.fullListSize}"
                        excludedParams="*"
                        requestURI="/statistics/mailing/list.action?__fromdisplaytag=true">

                    <%-- Prevent table controls/headers collapsing when the table is empty --%>
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:column titleKey="Mailing"
                                    sortable="true"
                                    sortProperty="shortname"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto">${mailingStat.shortname}</span>
                        <c:url var="mailingSatatUrl" value="/statistics/mailing/${mailingStat.mailingid}/view.action"/>
                        <a href="${mailingSatatUrl}" class="hidden js-row-show" title="<mvc:message code='button.Edit'/> "/>
                    </display:column>

                    <display:column titleKey="Description"
                                    sortable="true"
                                    sortProperty="description"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto">${mailingStat.description}</span>
                    </display:column>

                    <display:column titleKey="Mailinglist"
                                    sortable="true"
                                    sortProperty="mailinglist"
                                    headerClass="js-table-sort js-filter-mailing-list">
                        <span class="multiline-auto">${mailingStat.mailinglist}</span>
                    </display:column>

                    <display:column titleKey="mailing.senddate"
                                    sortable="true"
                                    sortProperty="senddate"
                                    headerClass="js-table-sort js-filter-send-date">
                        <span class="multiline-auto">
                            <emm:formatDate value="${mailingStat.senddate}" format="${dateTimeFormat}"/>
                        </span>
                    </display:column>
                    <%-- Additional Fiels --%>
                    <c:forEach var="selectedAdditionalField" items="${mailingStatatisticListForm.additionalFields}">
                        <%-- Searching for possible additional field --%>
                        <c:forEach var="availableAdditionalField" items="${availableAdditionalFields}">
                            <c:if test="${availableAdditionalField.sortColumn == selectedAdditionalField}">
                                <c:choose>
                                    <c:when test="${availableAdditionalField == 'RECIPIENTS_COUNT'}">
                                        <display:column property="recipientsCount"
                                                        titleKey="${availableAdditionalField.messageKey}"
                                                        sortable="true"
                                                        sortProperty="${availableAdditionalField.sortColumn}"
                                                        headerClass="js-table-sort"/>
                                    </c:when>

                                    <c:when test="${availableAdditionalField == 'CREATION_DATE'}">
                                        <display:column titleKey="${availableAdditionalField.messageKey}"
                                                        sortable="true"
                                                        sortProperty="creation_date"
                                                        headerClass="js-table-sort">
                                            <span class="multiline-auto">
                                                <emm:formatDate value="${mailingStat.creationdate}" format="${dateTimeFormat}"/>
                                            </span>
                                        </display:column>
                                    </c:when>

                                    <c:when test="${availableAdditionalField == 'TEMPLATE'}">
                                        <display:column titleKey="${availableAdditionalField.messageKey}"
                                                        sortable="true"
                                                        sortProperty="template_name"
                                                        headerClass="js-table-sort">
                                            <span class="multiline-auto">${mailingStat.templateName}</span>
                                        </display:column>
                                    </c:when>

                                    <c:when test="${availableAdditionalField == 'SUBJECT'}">
                                        <display:column titleKey="${availableAdditionalField.messageKey}"
                                                        sortable="true"
                                                        sortProperty="subject"
                                                        headerClass="js-table-sort">
                                            <span class="multiline-auto">${mailingStat.subject}</span>
                                        </display:column>
                                    </c:when>

                                    <c:when test="${availableAdditionalField == 'TARGET_GROUPS'}">
                                        <display:column titleKey="Target-Groups"
                                                        sortable="true"
                                                        sortProperty="target_expression"
                                                        headerClass="js-table-sort js-filter-target-group">
                                            <c:forEach var="targetgroup" items="${mailingStat.targetgroups}">
                                                <c:url var="viewTargetUrl" value="/target/${targetgroup.target_id}/view.action"/>
                                                <a href="${viewTargetUrl}">
                                                    <span class="multiline-auto">${targetgroup.target_name}</span>
                                                </a>
                                                <br/>
                                            </c:forEach>
                                        </display:column>
                                    </c:when>

                                    <c:when test="${availableAdditionalField == 'MAILING_ID'}">
                                        <display:column
                                                titleKey="${availableAdditionalField.messageKey}"
                                                sortable="true"
                                                sortProperty="mailing_id"
                                                headerClass="js-table-sort">
                                            <span class="multiline-auto">${mailingStat.mailingid}</span>
                                        </display:column>
                                    </c:when>
                                </c:choose>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
