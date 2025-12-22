<%@ page import="com.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.usergroup.web.UserGroupController" %>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userGroupListForm" type="com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter"--%>
<%--@elvariable id="userGroupList" type="com.agnitas.beans.PaginatedList"--%>
<%--@elvariable id="numberOfRows" type="java.lang.Integer"--%>
<%--@elvariable id="userGroup" type="com.agnitas.emm.core.usergroup.dto.UserGroupDto"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>
<c:set var="ROOT_COMPANY_ID" value="<%= UserGroupController.ROOT_COMPANY_ID %>" scope="request"/>

<c:url var="restoreUrl" value="/administration/usergroup/restore.action"/>
<mvc:message var="restoreMsg" code="default.restore" />

<c:set var="deletePermissionAllowed" value="${emm:permissionAllowed('role.delete', pageContext.request)}"/>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/administration/usergroup/list.action" method="GET"
              modelAttribute="userGroupListForm" data-editable-tile="main">

        <mvc:hidden path="showDeleted" />

        <div class="tile-body">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "user-group-overview": {
                        "rows-count": ${userGroupListForm.numberOfRows}
                    }
                }
            </script>
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deletePermissionAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:choose>
                                        <c:when test="${userGroupListForm.showDeleted}">
                                            <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}" data-form-method="POST" data-form-submit>
                                                <i class="icon icon-redo"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="bulkDeleteUrl" value="/administration/usergroup/delete.action"/>
                                            <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="bulkAction.delete.usergroup" />" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                                <i class="icon icon-trash-alt"></i>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${userGroupList.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${userGroupList.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="userGroup" modelAttribute="userGroupList" cssClass="table table--borderless js-table ${userGroupListForm.showDeleted ? '' : 'table-hover'}">

                        <c:set var="deleteAllowed" value="${deletePermissionAllowed && userGroup.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}"/>

                        <c:if test="${deletePermissionAllowed}">
                            <emm:column title="<input class='form-check-input' type='checkbox' data-bulk-checkboxes autocomplete='off' />" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${userGroup.userGroupId}"
                                       autocomplete="off" ${not deleteAllowed ? 'disabled' : 'data-bulk-checkbox'}>
                            </emm:column>
                        </c:if>

                        <emm:ShowByPermission token="master.show">
                            <emm:column headerClass="fit-content"
                                        property="userGroupId" titleKey="MailinglistID"
                                        sortable="true" sortProperty="admin_group_id" />
                        </emm:ShowByPermission>

                        <emm:column titleKey="default.Name" sortable="true" property="shortname" />

                        <emm:ShowByPermission token="master.show">
                            <emm:column titleKey="settings.Company" sortable="true" sortProperty="company_descr" property="companyDescr" />
                        </emm:ShowByPermission>

                        <emm:column titleKey="Description" sortable="true" property="description" />

                        <emm:column titleKey="settings.admin.number" sortable="true" property="usersCount" sortProperty="users_count" headerClass="fit-content" />

                        <emm:column cssClass="table-actions mobile-hidden ${deletePermissionAllowed ? '' : 'hidden'}" headerClass="mobile-hidden ${deletePermissionAllowed ? '' : 'hidden'}">
                            <c:if test="${not userGroupListForm.showDeleted}">
                                <a href="<c:url value="/administration/usergroup/${userGroup.userGroupId}/view.action"/>" class="hidden" data-view-row="page"></a>
                            </c:if>

                            <c:choose>
                                <c:when test="${userGroupListForm.showDeleted}">
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${restoreMsg}" data-form-url="${restoreUrl}" data-form-method="POST"
                                       data-form-set="bulkIds: ${userGroup.userGroupId}" data-form-submit>
                                        <i class="icon icon-redo"></i>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${deleteAllowed}">
                                        <c:url var="deleteUserGroupLink" value="/administration/usergroup/delete.action?bulkIds=${userGroup.userGroupId}"/>

                                        <a href="${deleteUserGroupLink}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="<mvc:message code="settings.usergroup.delete"/>">
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/usergroup/search.action"
              modelAttribute="userGroupListForm"
              data-toggle-tile=""
              data-form="resource"
              data-resource-selector="#table-tile"
              data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body form-column js-scrollable">
            <div>
                <mvc:message var="groupNameMsg" code="Name"/>
                <label class="form-label" for="group-name">${groupNameMsg}</label>
                <mvc:text id="group-name" path="groupName" cssClass="form-control" placeholder="${groupNameMsg}"/>
            </div>
            <div>
                <mvc:message var="clientNameMsg" code="settings.Company"/>
                <label class="form-label" for="filter-address">${clientNameMsg}</label>
                <mvc:text id="filter-address" path="clientName" cssClass="form-control" placeholder="${clientNameMsg}"/>
            </div>
            <div>
                <mvc:message var="clientIdMsg" code="settings.company.id"/>
                <label class="form-label" for="filter-address">${clientIdMsg}</label>
                <mvc:text id="filter-address" path="clientId" cssClass="form-control" placeholder="0"/>
            </div>
            <div>
                <mvc:message var="descriptionMsg" code="Description"/>
                <label class="form-label" for="description">${descriptionMsg}</label>
                <mvc:text id="description" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
            </div>
            <div class="form-check form-switch mt-1">
                <mvc:checkbox id="filter-show-deleted" path="showDeleted" cssClass="form-check-input" role="switch"/>
                <label class="form-label form-check-label" for="filter-show-deleted">
                    <mvc:message code="default.list.deleted.show"/>
                </label>
            </div>
        </div>
    </mvc:form>
</div>
