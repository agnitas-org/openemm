<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="showSupervisorPermissionManagement" type="java.lang.Boolean"--%>
<%--@elvariable id="supervisorGrantLoginPermissionForm" type="com.agnitas.emm.core.user.form.SupervisorGrantLoginPermissionForm"--%>
<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="departmentList" type="java.util.List<com.agnitas.emm.core.departments.beans.Department>"--%>
<%--@elvariable id="allDepartmentsId" type="java.lang.Integer"--%>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<c:if test="${showSupervisorPermissionManagement}">
    <mvc:form servletRelativeAction="/user/self/supervisor-permission/grant.action" modelAttribute="supervisorLoginForm" data-form="resource">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="settings.supervisor.manageLoginPermissions"/></h2>

                <ul class="tile-header-actions">
                    <li>
                        <button type="button" class="btn btn-regular btn-primary" data-form-persist="isShowStatistic: true" data-form-submit-static>
                            <i class="icon icon-plus"></i>
                            <span class="text"><mvc:message code="settings.supervisor.grantLoginPermission"/></span>
                        </button>
                    </li>
                </ul>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="departmentID"><mvc:message code="supervisor.department"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="departmentID" id="departmentID" cssClass="form-control js-select">
                            <mvc:option value="0"><mvc:message code="settings.supervisor.chooseDepartment"/></mvc:option>
                            <mvc:option value="${allDepartmentsId}"><mvc:message code="settings.supervisor.allDepartments"/></mvc:option>
                            <c:forEach var="department" items="${departmentList}">
                                <mvc:option value="${department.id}"><mvc:message code="department.slugs.${department.slug}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="settings.supervisor.loginPermission.validity"/></label>
                    </div>
                    <div class="col-sm-8">
                        <ul class="list-group">
                            <li class="list-group-item">
                                <div class="form-vertical">
                                    <div class="row">
                                        <div class="col-sm-4">
                                            <label class="radio-inline">
                                                <mvc:radiobutton path="limit" value="LIMITED" cssStyle="limited"/>
                                                <mvc:message code="settings.supervisor.loginPermission.grantedUntilIncluding"/>
                                            </label>
                                        </div>
                                        <div class="col-sm-8">
                                            <div class="control">
                                                <div class="input-group">
                                                    <div class="input-group-controls">
                                                        <mvc:text path="expireDateLocalized" data-value="${supervisorGrantLoginPermissionForm.expireDateLocalized}"
                                                                  cssClass="form-control datepicker-input js-datepicker"
                                                                  data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}', formatSubmit: '${fn:toLowerCase(localeDatePattern)}'"/>
                                                    </div>

                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                            <i class="icon icon-calendar-o"></i>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <label class="radio-inline">
                                    <mvc:radiobutton path="limit" value="UNLIMITED" cssStyle="limited"/>
                                    <mvc:message code="settings.supervisor.loginPermission.grantedUnlimited"/>
                                </label>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="tile-content">
                <div class="table-wrapper table-overflow-visible">
                    <display:table class="table table-bordered table-striped table-hover js-table" id="permissionData" name="activeLoginPermissions"
                            sort="list" excludedParams="*">

                        <%--@elvariable id="permissionData" type="com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem"--%>

                        <display:column headerClass="head_action" class="action" titleKey="CreationDate">
                        <fmt:formatDate value="${permissionData.granted}" pattern="${adminDateTimeFormatWithSeconds}" timeZone="${adminTimeZone}" /></display:column>
                        <display:column headerClass="head_action" class="action" titleKey="date.expiry">
                            <c:choose>
                                <c:when test="${not empty permissionData.expireDate}">
                                    <fmt:formatDate value="${permissionData.expireDate}" pattern="${adminDateTimeFormatWithSeconds}" timeZone="${adminTimeZone}" />
                                </c:when>
                                <c:otherwise>
                                    <mvc:message code="settings.supervisor.loginPermission.grantedUnlimited" />
                                </c:otherwise>
                            </c:choose>
                        </display:column>

                        <c:choose>
                            <c:when test="${permissionData.allDepartmentsPermission}">
                                <display:column headerClass="head_action" class="action" titleKey="supervisor.department"><mvc:message code="settings.supervisor.allDepartments"/></display:column>
                            </c:when>
                            <c:otherwise>
                                <display:column headerClass="head_action" class="action" titleKey="supervisor.department"><mvc:message code="department.slugs.${permissionData.departmentSlugOrNull}" /></display:column>
                            </c:otherwise>
                        </c:choose>

                        <c:url value="/user/self/supervisor-permission/${permissionData.permissionID}/revoke.action" var="revocationLink"/>

                        <display:column headerClass="table-actions" class="action" titleKey="settings.supervisor.revokeLoginPermission">
                            <a href="${revocationLink}" class="btn btn-regular btn-alert js-row-delete">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
</c:if>
