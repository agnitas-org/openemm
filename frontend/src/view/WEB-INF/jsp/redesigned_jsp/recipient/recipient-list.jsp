<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ page import="org.agnitas.util.importvalues.Gender" %>
<%@ page import="org.agnitas.emm.core.recipient.RecipientUtils" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="listForm" type="com.agnitas.emm.core.recipient.forms.RecipientListForm"--%>
<%--@elvariable id="fieldsMap" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="deactivatePagination" type="java.lang.Boolean"--%>
<%--@elvariable id="recipientList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="countOfRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="loadRecipients" type="java.lang.Boolean"--%>

<c:set var="USER_TYPE_ADMIN" value="<%= BindingEntry.UserType.Admin %>"/>
<c:set var="USER_TYPE_TEST" value="<%= BindingEntry.UserType.TestUser %>"/>
<c:set var="USER_TYPE_NORMAL" value="<%= BindingEntry.UserType.World %>"/>
<c:set var="USER_TYPE_TEST_VIP" value="<%= BindingEntry.UserType.TestVIP %>"/>
<c:set var="USER_TYPE_NORMAL_VIP" value="<%= BindingEntry.UserType.WorldVIP %>"/>

<c:set var="GENDER_MALE" value="<%= Gender.MALE %>"/>
<c:set var="GENDER_FEMALE" value="<%= Gender.FEMALE %>"/>
<c:set var="GENDER_UNKNOWN" value="<%= Gender.UNKNOWN %>"/>
<c:set var="GENDER_PRAXIS" value="<%= Gender.PRAXIS %>"/>
<c:set var="GENDER_COMPANY" value="<%= Gender.COMPANY %>"/>

<c:set var="RESET_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESET_PARAM_NAME%>"/>
<c:set var="MAX_SELECTED_FIELDS_COUNT" value="<%= RecipientUtils.MAX_SELECTED_FIELDS_COUNT%>"/>

<c:set var="allowedDeletion" value="false"/>
<emm:ShowByPermission token="recipient.delete">
    <c:set var="allowedDeletion" value="true"/>
</emm:ShowByPermission>

<mvc:form cssClass="filter-overview hidden" method="POST" servletRelativeAction="/recipient/search.action" modelAttribute="listForm"
          data-action="search-recipient" data-validator-options="ignore_qb_validation: true, skip_empty: true" data-form="resource"
          data-controller="recipient-list" data-initializer="recipient-list" data-editable-view="${agnEditViewKey}">

    <script id="config:recipient-list" type="application/json">
        {
            "initialRules": ${emm:toJson(listForm.searchQueryBuilderRules)},
            "maxSelectedColumns": ${MAX_SELECTED_FIELDS_COUNT}
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
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <div class="tile-body d-flex flex-column gap-3">
            <c:if test="${recipientList.fullListSize > countOfRecipients}">
                <div class="notification-simple notification-simple--lg notification-simple--info">
                    <span><mvc:message code="recipient.search.max_recipients" arguments="${countOfRecipients}"/></span>
                </div>
            </c:if>

            <div class="table-box ${deactivatePagination ? 'hide-pagination' : ''}">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="recipient" name="recipientList"
                                   sort="external" requestURI="/recipient/list.action?loadRecipients=true"
                                   partialList="true" size="${listForm.numberOfRows}" excludedParams="*">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <display:column headerClass="js-table-sort" titleKey="mailing.MediaType.0"
                                        sortable="true" sortProperty="email">
                            <div class="d-flex align-items-center gap-2">
                                <emm:ShowByPermission token="mailing.encrypted.send">
                                    <i class="icon icon-lock${recipient.encryptedSend ? ' text-success' : '-open text-danger'} fs-3"></i>
                                </emm:ShowByPermission>

                                <span class="text-truncate">${recipient.email}</span>
                            </div>
                        </display:column>

                        <%@include file="fragments/additional-fields.jspf"%>

                        <c:set var="addAdditionalColumns">
                            <div class="dropdown table-header-dropdown">
                                <i class="icon icon-plus" role="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false"></i>

                                <ul class="dropdown-menu">
                                    <div class="d-flex flex-column gap-2">
                                        <mvc:select path="selectedFields" cssClass="form-control dropdown-select" multiple="true" data-action="change-table-columns">
                                            <c:forEach var="field" items="${fieldsMap}" varStatus="rowCounter">
                                                <c:set var="column" value="${field.key}"/>
                                                <c:set var="fieldName" value="${field.value}"/>

                                                <c:set var="isDefaultField" value="${listForm.isDefaultColumn(column)}"/>
                                                <c:set var="fieldSelected" value="${listForm.isSelectedColumn(column)}"/>

                                                <c:if test="${isDefaultField}">
                                                    <option title="${column}" value="${column}" disabled>${fieldName}</option>
                                                </c:if>

                                                <c:if test="${not isDefaultField}">
                                                    <option title="${column}" value="${column}" ${fieldSelected ? 'selected' : ''}>${fieldName}</option>
                                                </c:if>
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

                        <display:column headerClass="${allowedDeletion ? 'additional-columns' : 'hidden'}" sortable="false" title="${addAdditionalColumns}">
                            <emm:ShowByPermission token="recipient.show">
                                <c:url var="viewLink" value="/recipient/${recipient.id}/view.action"/>
                                <a href="${viewLink}" class="hidden" data-view-row="page"></a>
                            </emm:ShowByPermission>

                            <c:if test="${allowedDeletion}">
                                <c:url var="deletionLink" value="/recipient/${recipient.id}/confirmDelete.action"/>
                                <mvc:message var="deletionTooltip" code="recipient.RecipientDelete"/>

                                <a href="${deletionLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deletionTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
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
                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/recipient/SearchMsg.xml"></a>
            </h1>
            <div class="tile-title-controls">
                <input type="checkbox" id="use-advanced-filter" name="recipients-advanced-search" class="icon-switch" data-action="toggle-filter" data-stored-field>
                <label for="use-advanced-filter" class="text-switch__label">
                    <span><mvc:message code="default.basic" /></span>
                    <span><mvc:message code="default.advanced" /></span>
                </label>
            </div>
            <div class="tile-controls">
                <c:url var="resetSearchLink" value="/recipient/search.action">
                    <c:param name="${RESET_SEARCH_PARAM_NAME}" value="true"/>
                </c:url>

                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-url="${resetSearchLink}" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-action="search" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div id="basic-filters-block" class="col-12">
                    <div class="row g-3">
                        <div class="col-12">
                            <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                            <mvc:text path="searchEmail" id="filter-email" cssClass="form-control" placeholder="${emailPlaceholder}"/>
                        </div>

                        <div class="col-12">
                            <label class="form-label" for="filter-gender"><mvc:message code="Gender" /></label>
                            <mvc:message var="allMsg" code="default.All"/>

                            <mvc:select path="filterGender" cssClass="form-control" multiple="false" id="filter-gender">
                                <mvc:option value="">${allMsg}</mvc:option>

                                <mvc:option value="${GENDER_MALE.getStorageValue()}"><mvc:message code="recipient.gender.0.short"/></mvc:option>
                                <mvc:option value="${GENDER_FEMALE.getStorageValue()}"><mvc:message code="recipient.gender.1.short"/></mvc:option>
                                <emm:ShowByPermission token="recipient.gender.extended">
                                    <mvc:option value="${GENDER_PRAXIS.getStorageValue()}"><mvc:message code="recipient.gender.4.short"/></mvc:option>
                                    <mvc:option value="${GENDER_COMPANY.getStorageValue()}"><mvc:message code="recipient.gender.5.short"/></mvc:option>
                                </emm:ShowByPermission>
                                <mvc:option value="${GENDER_UNKNOWN.getStorageValue()}"><mvc:message code="recipient.gender.2.short"/></mvc:option>
                            </mvc:select>
                        </div>

                        <div class="col-12">
                            <mvc:message var="firstnameMsg" code="Firstname" />
                            <label class="form-label" for="filter-firstname">${firstnameMsg}</label>
                            <mvc:text path="searchFirstName" id="filter-firstname" cssClass="form-control" placeholder="${firstnameMsg}"/>
                        </div>

                        <div class="col-12">
                            <mvc:message var="lastnameMsg" code="Lastname" />
                            <label class="form-label" for="filter-lastname">${lastnameMsg}</label>
                            <mvc:text path="searchLastName" id="filter-lastname" cssClass="form-control" placeholder="${lastnameMsg}"/>
                        </div>
                    </div>
                </div>

                <div class="col-12">
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

                <div class="col-12">
                    <label class="form-label" for="filter-target"><mvc:message code="Target" /></label>

                    <mvc:select path="filterTargetId" cssClass="form-control" id="filter-target" multiple="false">
                        <mvc:option value="0">${allMsg}</mvc:option>
                        <c:forEach var="target" items="${targets}">
                            <mvc:option value="${target.id}">${target.targetName}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="filter-type"><mvc:message code="recipient.RecipientType" /></label>
                    <mvc:select path="filterUserTypes" cssClass="form-control" id="filter-type" placeholder="${allMsg}">
                        <mvc:option value="${USER_TYPE_ADMIN.typeCode}"><mvc:message code="recipient.Administrator"/></mvc:option>
                        <mvc:option value="${USER_TYPE_TEST.typeCode}"><mvc:message code="TestSubscriber"/></mvc:option>
                        <%@include file="fragments/recipient-novip-test.jspf" %>
                        <mvc:option value="${USER_TYPE_NORMAL.typeCode}"><mvc:message code="NormalSubscriber"/></mvc:option>
                        <%@include file="fragments/recipient-novip-normal.jspf" %>
                    </mvc:select>
                </div>

                <div class="col-12">
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

                <div class="col-12 d-flex">
                    <button id="advanced-filter-btn" class="btn btn-primary flex-grow-1 fw-normal" type="button" data-action="advanced-filter">
                        <i class="icon icon-external-link-alt"></i>
                        <span><mvc:message code="recipient.AdvancedSearch" /></span>
                    </button>
                </div>

                <div id="filter-query-builder-block" class="col-12 hidden" data-initializer="target-group-query-builder">
                    <div id="targetgroup-querybuilder">
                        <mvc:hidden path="searchQueryBuilderRules" id="queryBuilderRules" />
                    </div>

                    <script id="config:target-group-query-builder" type="application/json">
                        {
                            "mailTrackingAvailable": ${mailTrackingAvailable},
                            "helpLanguage": "${helplanguage}",
                            "queryBuilderRules": ${emm:toJson(listForm.searchQueryBuilderRules)},
                            "queryBuilderFilters": ${queryBuilderFilters}
                        }
                    </script>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<script id="recipient-advanced-filter-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-xl-down modal-xl modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="recipient.AdvancedSearch"/></h1>
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <%-- Loads by JS --%>
                </div>

                <div class="modal-footer">
                    <button id="target-group-save-button" type="button" class="btn btn-primary flex-grow-1 {{- showTargetSaveBtn ? '' : 'hidden' }}" data-action="create-new-target">
                        <i class="icon icon-save"></i>
                        <span class="text"><mvc:message code="recipient.saveSearch"/></span>
                    </button>
                    <button type="button" class="btn btn-primary flex-grow-1" data-action="set-advanced-filter">
                        <i class="icon icon-filter"></i>
                        <span class="text"><mvc:message code="button.recipient.search.filter"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="new-targetgroup-modal" type="text/x-mustache-template">
    <mvc:form servletRelativeAction="/recipient/createTargetGroup.action" cssClass="modal" tabindex="-1" data-form-focus="newTargetName">
        <input type="hidden" name="queryBuilderRules" value="{{- rules}}">

        <div class="modal-dialog modal-fullscreen-xl-down modal-xl modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="recipient.saveSearch"/></h1>
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="newTargetName" class="form-label">
                                <mvc:message var="nameMsg" code="default.Name"/>
                                ${nameMsg}
                            </label>

                            <input type="text" id="newTargetName" name="shortname" maxlength="99" class="form-control"
                                   data-field="required" placeholder="${nameMsg}"/>
                        </div>

                        <div class="col-12">
                            <label for="newTargetDescription" class="form-label">
                                <mvc:message var="descriptionMsg" code="Description"/>
                                ${descriptionMsg}
                            </label>

                            <textarea id="newTargetDescription" name="description" class="form-control v-resizable" rows="5" cols="32" placeholder="${descriptionMsg}"></textarea>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary flex-grow-1 js-confirm-positive" data-bs-dismiss="modal">
                        <i class="icon icon-save"></i>
                        <span class="text"><mvc:message code="button.Save"/></span>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</script>
