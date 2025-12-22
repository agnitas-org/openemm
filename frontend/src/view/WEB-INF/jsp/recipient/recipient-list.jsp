<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="com.agnitas.beans.BindingEntry" %>
<%@ page import="com.agnitas.util.importvalues.Gender" %>
<%@ page import="com.agnitas.web.forms.FormSearchParams" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="listForm" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="deactivatePagination" type="java.lang.Boolean"--%>
<%--@elvariable id="recipientList" type="com.agnitas.beans.PaginatedList"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="loadRecipients" type="java.lang.Boolean"--%>
<%--@elvariable id="forceShowAdvancedSearchTab" type="java.lang.Boolean"--%>

<c:set var="USER_TYPE_ADMIN"      value="<%= BindingEntry.UserType.Admin %>" />
<c:set var="USER_TYPE_TEST"       value="<%= BindingEntry.UserType.TestUser %>" />
<c:set var="USER_TYPE_NORMAL"     value="<%= BindingEntry.UserType.World %>" />
<c:set var="USER_TYPE_TEST_VIP"   value="<%= BindingEntry.UserType.TestVIP %>" />
<c:set var="USER_TYPE_NORMAL_VIP" value="<%= BindingEntry.UserType.WorldVIP %>" />

<c:set var="RESET_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESET_PARAM_NAME%>"/>

<c:set var="allowedDeletion" value="${emm:permissionAllowed('recipient.delete', pageContext.request)}"/>
<c:set var="allowedShow"     value="${emm:permissionAllowed('recipient.show', pageContext.request)}"/>

<c:url var="deleteUrl" value="/recipient/delete.action" />
<mvc:message var="deletionTooltip" code="recipient.RecipientDelete" />

<mvc:form id="recipients-overview" cssClass="filter-overview" method="POST" servletRelativeAction="/recipient/list.action" modelAttribute="listForm"
          data-action="search-recipient" data-validator-options="ignore_qb_validation: true, skip_empty: true" data-form="resource"
          data-controller="recipient-list" data-initializer="recipient-list" data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="page" />
    <mvc:hidden path="sort" />
    <mvc:hidden path="dir" />

    <script id="config:recipient-list" type="application/json">
        {
            "initialRules": ${emm:toJson(listForm.searchQueryBuilderRules)},
            "forceShowAdvancedSearchTab": ${forceShowAdvancedSearchTab eq true}
        }
    </script>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "recipient-overview": {
                "rows-count": ${listForm.numberOfRows},
                "fields": ${emm:toJson(listForm.selectedFields)}
            }
        }
    </script>

    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-body vstack gap-3">
            <c:if test="${recipientList.fullListSize > countOfRecipients}">
                <div class="notification-simple notification-simple--lg notification-simple--info">
                    <span><mvc:message code="recipient.search.max_recipients" arguments="${countOfRecipients}"/></span>
                </div>
            </c:if>

            <div class="table-wrapper ${deactivatePagination ? 'table-wrapper--no-pagination' : ''}" data-table-column-manager data-action="save-selected-columns">

                <%@include file="fragments/recipient-field-titles.jspf"%>

                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${allowedDeletion}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deletionTooltip}" data-form-url="${deleteUrl}" data-form-method="GET" data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>

                        <%@include file="../common/table/edit-columns-btn.jspf" %>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>

                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${recipientList.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${recipientList.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="recipient" modelAttribute="recipientList" cssClass="table table-hover table--borderless js-table"
                                   requestUri="/recipient/list.action?loadRecipients=true">

                        <c:if test="${allowedDeletion}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${recipient.id}" autocomplete="off" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="mailing.MediaType.0" sortable="true" sortProperty="email" data-table-column="">
                            <div class="hstack gap-2 overflow-wrap-anywhere">
                                <emm:ShowByPermission token="mailing.encrypted.send">
                                    <c:choose>
                                        <c:when test="${recipient.encryptedSend}">
                                            <span class="icon-badge badge--dark-red">
                                                <i class="icon icon-lock"></i>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="icon-badge badge--green">
                                                <i class="icon icon-lock-open text-white"></i>
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </emm:ShowByPermission>

                                <span class="text-truncate-table">${recipient.email}</span>
                            </div>
                        </emm:column>

                        <%@include file="fragments/additional-fields.jspf"%>

                        <emm:column headerClass="columns-picker">
                            <c:if test="${allowedShow}">
                                <c:url var="viewLink" value="/recipient/${recipient.id}/view.action"/>
                                <a href="${viewLink}" class="hidden" data-view-row="page"></a>
                            </c:if>

                            <c:if test="${allowedDeletion}">
                                <a href="${deleteUrl}?bulkIds=${recipient.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deletionTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
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
                <a href="#" class="icon icon-question-circle" data-help="recipient/SearchMsg.xml"></a>
            </h1>
            <div class="tile-controls flex-grow-1">
                <label class="switch me-auto">
                    <input id="use-advanced-filter" type="checkbox" name="recipients-advanced-search" data-action="toggle-filter"
                           data-stored-field ${forceShowAdvancedSearchTab ? 'checked' : ''}>
                    <span><mvc:message code="default.basic" /></span>
                    <span><mvc:message code="default.advanced" /></span>
                </label>

                <button id="target-group-save-button" type="button" class="btn btn-primary hidden overflow-hidden" data-action="create-new-target">
                    <i class="icon icon-save"></i>
                    <span class="text-truncate"><mvc:message code="recipient.saveSearch"/></span>
                </button>

                <c:url var="resetSearchLink" value="/recipient/search.action">
                    <c:param name="${RESET_SEARCH_PARAM_NAME}" value="true"/>
                </c:url>

                <a class="btn btn-icon btn-secondary" data-form-url="${resetSearchLink}" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-action="search" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body vstack gap-3 js-scrollable">
            <div id="basic-filters-block" class="vstack gap-inherit flex-grow-0">
                <div>
                    <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                    <mvc:text path="searchEmail" id="filter-email" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                </div>

                <div>
                    <label class="form-label" for="filter-gender"><mvc:message code="Gender" /></label>
                    <mvc:message var="allMsg" code="default.All"/>

                    <mvc:select path="filterGender" cssClass="form-control" multiple="false" id="filter-gender">
                        <mvc:option value="">${allMsg}</mvc:option>

                        <mvc:option value="${Gender.MALE.getStorageValue()}"><mvc:message code="recipient.gender.0.short"/></mvc:option>
                        <mvc:option value="${Gender.FEMALE.getStorageValue()}"><mvc:message code="recipient.gender.1.short"/></mvc:option>
                        <emm:ShowByPermission token="recipient.gender.extended">
                            <mvc:option value="${Gender.PRAXIS.getStorageValue()}"><mvc:message code="recipient.gender.4.short"/></mvc:option>
                            <mvc:option value="${Gender.COMPANY.getStorageValue()}"><mvc:message code="recipient.gender.5.short"/></mvc:option>
                        </emm:ShowByPermission>
                        <mvc:option value="${Gender.UNKNOWN.getStorageValue()}"><mvc:message code="recipient.gender.2.short"/></mvc:option>
                    </mvc:select>
                </div>

                <div>
                    <mvc:message var="firstnameMsg" code="Firstname" />
                    <label class="form-label" for="filter-firstname">${firstnameMsg}</label>
                    <mvc:text path="searchFirstName" id="filter-firstname" cssClass="form-control" placeholder="${firstnameMsg}"/>
                </div>

                <div>
                    <mvc:message var="lastnameMsg" code="Lastname" />
                    <label class="form-label" for="filter-lastname">${lastnameMsg}</label>
                    <mvc:text path="searchLastName" id="filter-lastname" cssClass="form-control" placeholder="${lastnameMsg}"/>
                </div>
            </div>

            <div>
                <label class="form-label" for="filter-mailinglist"><mvc:message code="Mailinglist" /></label>
                <mvc:select path="filterMailinglistId" cssClass="form-control" id="filter-mailinglist" data-action="change-mailinglist-id" multiple="false">
                    <mvc:option value="0">${allMsg}</mvc:option>
                    <c:if test="${not hasAnyDisabledMailingLists}">
                        <mvc:option value="-1"><mvc:message code="No_Mailinglist"/></mvc:option>
                    </c:if>
                    <c:forEach var="mailinglist" items="${mailinglists}">
                        <mvc:option value="${mailinglist.id}">${mailinglist.shortname}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-target"><mvc:message code="Target" /></label>

                <mvc:select path="filterTargetId" cssClass="form-control" id="filter-target" multiple="false">
                    <mvc:option value="0">${allMsg}</mvc:option>
                    <c:forEach var="target" items="${targets}">
                        <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>

            <%@include file="fragments/search-altg-select.jspf" %>

            <div>
                <label class="form-label" for="filter-type"><mvc:message code="recipient.RecipientType" /></label>
                <mvc:select path="filterUserTypes" cssClass="form-control" id="filter-type" placeholder="${allMsg}">
                    <mvc:option value="${USER_TYPE_ADMIN.typeCode}"><mvc:message code="recipient.Administrator"/></mvc:option>
                    <mvc:option value="${USER_TYPE_TEST.typeCode}"><mvc:message code="TestSubscriber"/></mvc:option>
                    <%@include file="fragments/recipient-novip-test.jspf" %>
                    <mvc:option value="${USER_TYPE_NORMAL.typeCode}"><mvc:message code="NormalSubscriber"/></mvc:option>
                    <%@include file="fragments/recipient-novip-normal.jspf" %>
                </mvc:select>
            </div>

            <div>
                <label class="form-label" for="filter-status"><mvc:message code="recipient.RecipientStatus" /></label>
                <mvc:select path="filterUserStatus" cssClass="form-control" multiple="false" id="filter-status">
                    <mvc:option value="0">${allMsg}</mvc:option>
                    <mvc:option value="1"><mvc:message code="recipient.MailingState1"/></mvc:option>
                    <mvc:option value="2"><mvc:message code="recipient.MailingState2"/></mvc:option>
                    <mvc:option value="3"><mvc:message code="recipient.OptOutAdmin"/></mvc:option>
                    <mvc:option value="4"><mvc:message code="recipient.OptOutUser"/></mvc:option>
                    <mvc:option value="5"><mvc:message code="recipient.MailingState5"/></mvc:option>
                    <emm:ShowByPermission token="blacklist">
                        <mvc:option value="6"><mvc:message code="recipient.MailingState6"/></mvc:option>
                    </emm:ShowByPermission>
                    <mvc:option value="7"><mvc:message code="recipient.MailingState7"/></mvc:option>
                </mvc:select>
            </div>

            <div id="filter-query-builder-block" data-initializer="target-group-query-builder" data-multi-editor>
                <div class="hstack gap-1 mb-1">
                    <label class="form-label m-0"><mvc:message code="recipient.AdvancedSearch" /></label>
                    <button class="btn btn-icon btn-secondary" type="button" data-enlarged-modal data-tooltip="<mvc:message code="editor.enlargeEditor" />"
                            data-modal-set="title: <mvc:message code="recipient.AdvancedSearch"/>, btnText: <mvc:message code="button.recipient.search.filter"/>">
                        <i class="icon icon-expand-arrows-alt"></i>
                    </button>
                </div>

                <div>
                    <div id="targetgroup-querybuilder" data-enlarge-target>
                        <mvc:hidden path="searchQueryBuilderRules" id="queryBuilderRules" />
                    </div>
                </div>

                <script id="config:target-group-query-builder" type="application/json">
                    {
                        "mailTrackingAvailable": ${mailTrackingAvailable},
                        "queryBuilderRules": ${emm:toJson(listForm.searchQueryBuilderRules)},
                        "queryBuilderFilters": ${queryBuilderFilters}
                    }
                </script>
            </div>
        </div>
    </div>
</mvc:form>

<script id="new-targetgroup-modal" type="text/x-mustache-template">
    <mvc:form servletRelativeAction="/recipient/createTargetGroup.action" cssClass="modal" tabindex="-1" data-form-focus="newTargetName">
        <input type="hidden" name="queryBuilderRules" value="{{- rules}}">

        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="recipient.saveSearch"/></h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body vstack gap-3 js-scrollable">
                    <div>
                        <mvc:message var="nameMsg" code="default.Name"/>
                        <label for="newTargetName" class="form-label">${nameMsg}</label>
                        <input type="text" id="newTargetName" name="shortname" maxlength="99" class="form-control"
                               data-field="required" placeholder="${nameMsg}"/>
                    </div>

                    <div>
                        <mvc:message var="descriptionMsg" code="Description"/>
                        <label for="newTargetDescription" class="form-label">${descriptionMsg}</label>
                        <textarea id="newTargetDescription" name="description" class="form-control" placeholder="${descriptionMsg}" rows="1"></textarea>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary js-confirm-positive" data-bs-dismiss="modal">
                        <i class="icon icon-save"></i>
                        <span class="text"><mvc:message code="button.Save"/></span>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</script>
