<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>
<%--@elvariable id="PERMISSIONS_ENABLED" type="java.lang.Boolean"--%>

<mvc:form cssClass="tiles-container flex-column"
          servletRelativeAction="/administration/wsmanager/user/update.action"
          data-form-focus="password"
          id="wsuser-edit-form"
          modelAttribute="webserviceUserForm"
          data-form="resource"
          data-controller="user|groups-permissions" data-editable-view="${agnEditViewKey}">

    <mvc:hidden path="userName"/>
    <mvc:hidden path="companyId"/>

    <script data-initializer="user|groups-permissions" type="application/json">
        {
            "isSoapUser": "true",
            "permissions": ${emm:toJson(PERMISSIONS.allPermissions)},
            "permissionGroups": ${emm:toJson(PERMISSION_GROUPS.allPermissionGroups)}
        }
    </script>

    <div id="edit-settings-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="settings.webservice.user.edit" /></h1>
            <div class="tile-controls">
                <div class="form-check form-switch">
                    <mvc:checkbox path="active" id="active" cssClass="form-check-input" role="switch" />
                    <label class="form-label form-check-label text-capitalize" for="active"><mvc:message code="default.status.active"/></label>
                </div>
            </div>
        </div>

        <div class="tile-body row js-scrollable">
            <div class="col">
                <label for="userNameTitle" class="form-label"><mvc:message code="logon.username" /></label>
                <input type="text" value="${webserviceUserForm.userName}" disabled="disabled" id="userNameTitle" class="form-control">
            </div>
            <div class="col">
                <label class="form-label" for="email"><mvc:message code="settings.Admin.email"/></label>
                <mvc:text path="email" id="email" cssClass="form-control" size="52" maxlength="99"/>
            </div>
            <div class="col" data-field="password">
                <label for="password" class="form-label">
                    <mvc:message code="password.new"/>
                    <a href="#" class="icon icon-question-circle" data-help="webserviceuser/AdminPasswordRules.xml" tabindex="-1" type="button"></a>
                </label>
                <mvc:password path="password" id="password" cssClass="form-control js-password-strength" size="52" maxlength="99" data-rule="${PASSWORD_POLICY}"/>
            </div>
            <div class="col">
                <label for="passwordRepeat" class="form-label"><mvc:message code="settings.admin.Confirm"/> *</label>
                <input type="password" id="passwordRepeat" class="form-control js-password-match" size="52" maxlength="99" readonly />
            </div>
         </div>
    </div>

    <c:if test="${PERMISSIONS_ENABLED}">
        <div class="tiles-block">
            <div id="user-rights-tile" class="tile" data-editable-tile="main" style="flex: 3">
                <div class="tile-header">
                    <h1 class="tile-title text-truncate"><mvc:message code="UserRights.edit"/></h1>
                </div>
                <div class="tile-body" style="display: grid; grid-template-columns: minmax(300px, 1fr) 3fr; gap: 10px">
                    <div class="permission-categories">
                        <input type="radio" class="btn-check" name="view-category" autocomplete="off" id="0-category-btn">
                        <label class="permission-categories__btn" for="0-category-btn" data-category-index="0" data-action="change-category">
                            <span><strong><mvc:message code="webserviceuser.permissionGroups"/></strong></span>
                            <span class="permissions-counter"><%-- load by JS--%></span>
                        </label>
                        <c:forEach items="${PERMISSIONS.categories}" var="category" varStatus="status">
                            <c:if test="${not empty category}">
                                <c:set var="categoryIndex" value="${status.index + 1}"/>
                                <input type="radio" class="btn-check" name="view-category" autocomplete="off" id="${categoryIndex}-category-btn">
                                <label class="permission-categories__btn" for="${categoryIndex}-category-btn" data-category-index="${categoryIndex}" data-action="change-category">
                                    <span><strong><mvc:message code="webservice.permissionCategory.${category}"/></strong></span>
                                    <span class="permissions-counter"><%-- load by JS--%></span>
                                </label>
                            </c:if>
                        </c:forEach>
                    </div>

                    <div id="0-category-permissions" class="tile">
                        <div class="tile-header">
                            <h1 class="tile-title tile-title--grey text-truncate"><mvc:message code="webserviceuser.permissionGroups"/></h1>
                            <div class="tile-controls">
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" role="switch" id="toggle-permissions-0" data-toggle-checkboxes="on">
                                    <label class="form-label form-check-label" for="toggle-permissions-0"><mvc:message code="ToggleOnOff"/></label>
                                </div>
                            </div>
                        </div>
                        <div class="tile-body js-scrollable">
                            <div class="notification-simple no-results-found" style="display: none">
                                <i class="icon icon-info-circle"></i>
                                <span><mvc:message code="noResultsFound"/></span>
                            </div>
                            <ul class="d-flex flex-column gap-3">
                                <c:forEach items="${PERMISSION_GROUPS.allPermissionGroups}" var="group">
                                    <li class="permission">
                                        <div class="form-check form-switch">
                                            <mvc:checkbox path="permissionGroup[${group.id}]" cssClass="form-check-input" role="switch" value="true" id='${group.name}'/>
                                            <label class="form-check-label" for="${group.name}"><mvc:message code="webservice.permissiongroup.${group.name}"/></label>
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>


                    <c:forEach items="${PERMISSIONS.categories}" var="category" varStatus="status">
                        <c:if test="${not empty category}">
                            <c:set var="categoryIndex" value="${status.index + 1}"/>
                            <div id="${categoryIndex}-category-permissions" class="tile">
                                <div class="tile-header">
                                    <h1 class="tile-title tile-title--grey text-truncate"><mvc:message code="webservice.permissionCategory.${category}"/></h1>
                                    <div class="tile-controls">
                                        <div class="form-check form-switch">
                                            <input class="form-check-input" type="checkbox" role="switch" id="toggle-permissions-${categoryIndex}" data-toggle-checkboxes="on">
                                            <label class="form-label form-check-label" for="toggle-permissions-${categoryIndex}"><mvc:message code="ToggleOnOff"/></label>
                                        </div>
                                    </div>
                                </div>
                                <div class="tile-body js-scrollable">
                                    <div class="notification-simple no-results-found" style="display: none">
                                        <i class="icon icon-info-circle"></i>
                                        <span><mvc:message code="noResultsFound"/></span>
                                    </div>
                                    <ul class="d-flex flex-column gap-3">
                                        <c:forEach items="${PERMISSIONS.getPermissionsForCategory(category)}" var="permission">
                                            <li class="permission">
                                                <div class="form-check form-switch">
                                                    <mvc:checkbox path="endpointPermission[${permission.endpointName}]" cssClass="form-check-input" role="switch" value="true" id='${permission.endpointName}'/>
                                                    <label class="form-check-label" for="${permission.endpointName}"><mvc:message code="webservice.permission.${permission.endpointName}" arguments="${[permission.endpointName]}"/></label>
                                                </div>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                        </c:if>
                    </c:forEach>
                </div>
            </div>

            <div id="filter-tile" class="tile" data-action="handle-filter-enterdown" data-editable-tile style="flex: 1">
                <div class="tile-header">
                    <h1 class="tile-title">
                        <i class="icon icon-caret-up mobile-visible"></i>
                        <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
                    </h1>
                    <div class="tile-controls">
                        <a class="btn btn-icon btn-secondary" data-form-clear="#filter-tile" data-action="apply-filter" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                        <a class="btn btn-icon btn-primary" data-action="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
                    </div>
                </div>
                <div class="tile-body js-scrollable">
                    <div class="row g-3">
                        <div class="col-12">
                            <label class="form-label" for="name-filter"><mvc:message code="Name"/></label>
                            <input type="text" id="name-filter" class="form-control" placeholder="<mvc:message code="UserRight.Mailing.mailing.new"/>"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</mvc:form>
