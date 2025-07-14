<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.agnitas.emm.core.Permission" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>
<%--@elvariable id="permissionChangeable" type="java.util.Set<java.lang.String>"--%>
<%--@elvariable id="permissionCategories" type="java.util.Set<com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionCategoryEntry>"--%>
<%--@elvariable id="subCategory" type="com.agnitas.emm.core.admin.web.PermissionsOverviewData.PermissionSubCategoryEntry"--%>
<%--@elvariable id="usersCount" type="java.lang.Integer"--%>

<c:set var="allowedUserGroupChange" value="${emm:permissionAllowed('role.change', pageContext.request)}" />

<mvc:form cssClass="tiles-container flex-column" servletRelativeAction="/administration/usergroup/save.action"
          modelAttribute="userGroupForm"
          id="userGroupForm"
          data-form="resource"
          data-form-focus="shortname"
          data-controller="user|groups-permissions"
          data-disable-controls="save"
          data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="id"/>
    <mvc:hidden path="companyId"/>

    <div id="settings-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings"/></h1>
        </div>
        <div class="tile-body row js-scrollable">
            <div class="col">
                <mvc:message var="nameMsg" code="default.Name"/>
                <label class="form-label" for="userGroupShortname">${nameMsg}*</label>
                <mvc:text path="shortname" id="userGroupShortname" size="52" maxlength="99" cssClass="form-control" placeholder="${nameMsg}"/>
            </div>
            <div class="col">
                <mvc:message var="descriptionMsg" code="Description"/>
                <label class="form-label" for="userGroupDescription">${descriptionMsg}</label>
                <mvc:text path="description" id="userGroupDescription" size="52" maxlength="99" cssClass="form-control" placeholder="${descriptionMsg}"/>
            </div>

            <c:if test="${userGroupForm.id gt -1}">
                <emm:ShowByPermission token="master.show">
                    <div class="col">
                        <label class="form-label" for="userGroupCoppany"><mvc:message code="settings.Company" /></label>
                        <input id="userGroupCoppany" type="text" readonly class="form-control" value="${userGroupForm.companyDescr}" />
                    </div>
                </emm:ShowByPermission>

                <div class="col">
                    <label for="usersCount" class="form-label"><mvc:message code="GWUA.numberOfUsers" /></label>
                    <input id="usersCount" type="text" readonly class="form-control" value="${usersCount}" />
                </div>
            </c:if>
        </div>
    </div>

    <div class="tiles-block">
        <%@ include file="../permissions.jspf" %>
    </div>

</mvc:form>
