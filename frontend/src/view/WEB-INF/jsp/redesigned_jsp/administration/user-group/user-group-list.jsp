<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.usergroup.web.UserGroupController" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userGroupListForm" type="org.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="userGroupList" type="org.displaytag.pagination.PaginatedList"--%>
<%--@elvariable id="numberOfRows" type="java.lang.Integer"--%>
<%--@elvariable id="userGroup" type="com.agnitas.emm.core.usergroup.dto.UserGroupDto"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>
<c:set var="ROOT_COMPANY_ID" value="<%= UserGroupController.ROOT_COMPANY_ID %>" scope="request"/>

<c:set var="deletePermissionAllowed" value="false"/>
<emm:ShowByPermission token="role.delete">
    <c:set var="deletePermissionAllowed" value="true"/>
</emm:ShowByPermission>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/administration/usergroup/list.action" method="GET"
              modelAttribute="userGroupListForm" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>
        <div class="tile-body">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "user-group-overview": {
                        "rows-count": ${userGroupListForm.numberOfRows}
                    }
                }
            </script>
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="userGroup"
                                   name="userGroupList"
                                   requestURI="/administration/usergroup/list.action"
                                   pagesize="${userGroupListForm.numberOfRows}"
                                   excludedParams="*">
                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <c:set var="deleteAllowed" value="${deletePermissionAllowed && userGroup.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}"/>

                        <display:column title="<input class='form-check-input' type='checkbox' data-form-bulk='bulkIds'/>" sortable="false" class="js-checkable mobile-hidden" headerClass="bulk-ids-column js-table-sort mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${userGroup.userGroupId}" ${not deleteAllowed ? 'disabled' : ''}>
                        </display:column>

                        <emm:ShowByPermission token="master.show">
                            <display:column headerClass="js-table-sort"
                                        property="userGroupId" titleKey="MailinglistID"
                                        sortable="true" sortProperty="admin_group_id"/>
                        </emm:ShowByPermission>

                        <display:column headerClass="js-table-sort"
                                        property="shortname" titleKey="default.Name"
                                        sortable="true" sortProperty="shortname"/>

                        <emm:ShowByPermission token="master.show">
                            <display:column headerClass="js-table-sort"
                                        property="companyDescr" titleKey="settings.Company"
                                        sortable="true" sortProperty="company_descr"/>
                        </emm:ShowByPermission>

                        <display:column headerClass="js-table-sort"
                                        property="description" titleKey="Description"
                                        sortable="true" sortProperty="description"/>

                        <display:column class="table-actions mobile-hidden ${deletePermissionAllowed ? '' : 'hidden'}" headerClass="fit-content mobile-hidden ${deletePermissionAllowed ? '' : 'hidden'}">
                            <a href="<c:url value="/administration/usergroup/${userGroup.userGroupId}/view.action"/>" class="hidden" data-view-row="page"></a>

                            <c:if test="${deleteAllowed}">
                                <c:url var="deleteUserGroupLink" value="/administration/usergroup/deleteRedesigned.action?bulkIds=${userGroup.userGroupId}"/>

                                <a href="${deleteUserGroupLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="<mvc:message code="settings.usergroup.delete"/>">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/usergroup/search.action"
              modelAttribute="userGroupListForm"
              data-toggle-tile="mobile"
              data-form="resource"
              data-resource-selector="#table-tile"
              data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="groupNameMsg" code="Name"/>
                    <label class="form-label" for="group-name">${groupNameMsg}</label>
                    <mvc:text id="group-name" path="groupName" cssClass="form-control" placeholder="${groupNameMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="clientNameMsg" code="settings.Company"/>
                    <label class="form-label" for="filter-address">${clientNameMsg}</label>
                    <mvc:text id="filter-address" path="clientName" cssClass="form-control" placeholder="${clientNameMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="clientIdMsg" code="settings.company.id"/>
                    <label class="form-label" for="filter-address">${clientIdMsg}</label>
                    <mvc:text id="filter-address" path="clientId" cssClass="form-control" placeholder="0"/>
                </div>
                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description">${descriptionMsg}</label>
                    <mvc:text id="description" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
