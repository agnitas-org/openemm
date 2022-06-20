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

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>
<c:set var="ROOT_COMPANY_ID" value="<%= UserGroupController.ROOT_COMPANY_ID %>" scope="request"/>

<mvc:form servletRelativeAction="/administration/usergroup/list.action" modelAttribute="userGroupListForm" id="userGroupListForm" data-form="resource">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "user-group-overview": {
                "rows-count": ${userGroupListForm.numberOfRows}
            }
        }
    </script>

    <input type="hidden" name="page" value="${userGroupList.pageNumber}"/>
    <input type="hidden" name="sort" value="${userGroupList.sortCriterion}"/>
    <input type="hidden" name="dir" value="${userGroupList.sortDirection}"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
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
                        </li>

                        <li class="divider"></li>

                        <li><p>
                            <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                            </button>
                        </p></li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="allowedDeleteUserGroup" value="false"/>
                <emm:ShowByPermission token="role.delete">
                    <c:set var="allowedDeleteUserGroup" value="true"/>
                </emm:ShowByPermission>
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="userGroup"
                               name="userGroupList"
                               requestURI="/administration/usergroup/list.action"
                               pagesize="${userGroupListForm.numberOfRows}"
                               excludedParams="*">

                    <%--@elvariable id="userGroup" type="com.agnitas.emm.core.usergroup.dto.UserGroupDto"--%>

                    <%--<!-- Prevent table controls/headers collapsing when the table is empty -->--%>
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <emm:ShowByPermission token="master.show">
                        <display:column headerClass="js-table-sort"
                                    property="userGroupId" titleKey="MailinglistID"
                                    sortable="true" sortProperty="admin_group_id"/>

                        <display:column headerClass="js-table-sort"
                                    property="companyId" titleKey="settings.Company"
                                    sortable="true" sortProperty="company_id"/>
                    </emm:ShowByPermission>

                    <display:column headerClass="js-table-sort"
                                    property="shortname" titleKey="default.Name"
                                    sortable="true" sortProperty="shortname"/>

                    <display:column headerClass="js-table-sort"
                                    property="description" titleKey="Description"
                                    sortable="true" sortProperty="description"/>

                    <display:column class="table-actions ${allowedDeleteUserGroup ? '' : 'hidden'}" headerClass="${allowedDeleteUserGroup ? '' : 'hidden'}">
                        <c:url var="viewNotificationLink" value="/administration/usergroup/${userGroup.userGroupId}/view.action"/>

                        <a href="${viewNotificationLink}" class="hidden js-row-show"></a>

                        <c:if test="${allowedDeleteUserGroup && userGroup.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}">
                            <c:url var="deleteUserGroupLink" value="/administration/usergroup/${userGroup.userGroupId}/confirmDelete.action"/>

                            <c:set var="deleteMessage">
                                <mvc:message code="settings.usergroup.delete"/>
                            </c:set>

                            <a href="${deleteUserGroupLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>
                    </display:column>

                </display:table>
            </div>

        </div>

    </div>
</mvc:form>
