<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>

<%@ page import="com.agnitas.reporting.birt.web.ComMailingBIRTStatAction" %>

<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn"     uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST"        value="<%= ComMailingBIRTStatAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW"        value="<%= ComMailingBIRTStatAction.ACTION_VIEW %>"/>
<c:set var="ACTION_MAILINGSTAT" value="<%= ComMailingBIRTStatAction.ACTION_MAILINGSTAT %>"/>

<%--@elvariable id="mailingBIRTStatForm" type="com.agnitas.reporting.birt.web.forms.ComMailingBIRTStatForm"--%>
<%--@elvariable id="mailingStatisticList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="availableAdditionalFields" type="java.util.List"--%>
<%--@elvariable id="availableMailingLists" type="java.util.List"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List"--%>
<%--@elvariable id="mailingStat" type="java.util.Map"--%>
<%--@elvariable id="dateTimeFormat" type="java.text.SimpleDateFormat"--%>

<agn:agnForm action="/mailing_stat">
    <html:hidden property="__STRUTS_CHECKBOX_searchNameChecked" value="false"/>
    <html:hidden property="__STRUTS_CHECKBOX_searchDescriptionChecked" value="false"/>
    <html:hidden property="__STRUTS_MULTIPLE_additionalFields" value=""/>
    <html:hidden property="__STRUTS_MULTIPLE_filteredMailingLists" value=""/>
    <html:hidden property="__STRUTS_MULTIPLE_filteredTargetGroups" value=""/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "mailing-separate-stats-overview": {
                "rows-count": ${mailingBIRTStatForm.numberOfRows},
                "fields": ${emm:toJson(mailingBIRTStatForm.additionalFieldsSet)}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-mailingSearch">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Search"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit="">
                        <i class="icon icon-search"></i>
                        <span class="text"><bean:message key="Search"/></span>
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
                                <label for="searchQueryText"><bean:message key="mailing.searchFor"/></label>
                                <button class="icon icon-help"
                                        data-help="help_${helplanguage}/mailing/overview/SearchFor.xml" tabindex="-1"
                                        type="button"></button>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <html:text styleClass="form-control" property="searchQueryText" styleId="searchQueryText"/>
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="form-group">
                        <div class="col-md-12">
                            <label for="placesToSearch" class="control-label">
                                <bean:message key="mailing.searchIn"/>
                            </label>
                        </div>
                        <div class="col-md-12">
                            <ul class="list-group" style="margin-bottom: 0px;" id="placesToSearch">
                                <li class="list-group-item checkbox">
                                    <label>
                                        <html:checkbox property="searchNameChecked"/>
                                        <bean:message key="mailing.searchName"/>
                                    </label>
                                </li>
                                <li class="list-group-item checkbox">
                                    <label>
                                        <html:checkbox property="searchDescriptionChecked"/>
                                        <bean:message key="mailing.searchDescription"/>
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
                <bean:message key="default.Overview"/>
            </h2>
            <ul class="tile-header-nav">
            </ul>

            <ul class="tile-header-actions">
                <%-- PAGE SIZE --%>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                            <logic:iterate collection="${mailingBIRTStatForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>

                <%-- ADITIONAL FIELDS --%>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-columns"></i>
                        <span class="text"><bean:message key="settings.fields"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="settings.fields"/></li>
                        <li>
                            <select class="form-control js-select" tabindex="-1" multiple="multiple"
                                    name="additionalFields" data-action="change-columns-to-show" style="padding: 5px"
                                    title="additional fields">
                                <c:forEach var="availableField" items="${availableAdditionalFields}">
                                    <c:set var="selected" value=""/>
                                    <c:forEach var="additionalField" items="${mailingBIRTStatForm.additionalFields}"
                                               varStatus="rowCounter">
                                        <c:if test="${availableField.sortColumn == additionalField}">
                                            <c:set var="selected" value="selected"/>
                                        </c:if>
                                    </c:forEach>

                                    <option title="${availableField.messageKey}"
                                            value="${availableField.sortColumn}" ${selected}>
                                        <bean:message key="${availableField.messageKey}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" class="js-dropdown-open" data-form-persist="additionalFields: ''">
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message
                                        key="button.Refresh"/></span>
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
                                               items="${mailingBIRTStatForm.filteredMailingLists}">
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
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message
                                        key="button.Apply"/></span>
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
                                               items="${mailingBIRTStatForm.filteredTargetGroups}">
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
                                <bean:message key="filter.reset"/>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><bean:message key="button.Apply"/></span>
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
                        pagesize="${mailingBIRTStatForm.numberOfRows}"
                        partialList="true"
                        size="${mailingStatisticList.fullListSize}"
                        excludedParams="*"
                        requestURI="/mailing_stat.do?action=${ACTION_LIST}&__fromdisplaytag=true">

                    <%-- Prevent table controls/headers collapsing when the table is empty --%>
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:column titleKey="Mailing"
                                    sortable="true"
                                    sortProperty="shortname"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto"><bean:write name="mailingStat" property="shortname"/></span>
                        <html:link styleClass="hidden js-row-show" titleKey="button.Edit"
                                   page="/mailing_stat.do?action=${ACTION_MAILINGSTAT}&mailingID=${mailingStat.mailingid}"/>
                    </display:column>

                    <display:column titleKey="Description"
                                    sortable="true"
                                    sortProperty="description"
                                    headerClass="js-table-sort">
                        <span class="multiline-auto"><bean:write name="mailingStat" property="description"/></span>
                    </display:column>

                    <display:column titleKey="Mailinglist"
                                    sortable="true"
                                    sortProperty="mailinglist"
                                    headerClass="js-table-sort js-filter-mailing-list">
                        <span class="multiline-auto"><bean:write name="mailingStat" property="mailinglist"/></span>
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
                    <c:forEach var="selectedAdditionalField" items="${mailingBIRTStatForm.additionalFields}">
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
                                            <logic:iterate name="mailingStat" property="targetgroups" id="targetgroup">
                                                <html:link
                                                        page="/target.do?action=${ACTION_VIEW}&targetID=${targetgroup.target_id}">
                                                    <span class="multiline-auto">${targetgroup.target_name}</span>
                                                </html:link>
                                                <br/>
                                            </logic:iterate>
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
</agn:agnForm>
