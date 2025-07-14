<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" import="java.util.Locale,org.agnitas.emm.core.logintracking.*" %>
<%@ page import="com.agnitas.beans.AdminPreferences" %>
<%@ page import="com.agnitas.emm.core.admin.enums.UiLayoutType" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="selfForm" type="com.agnitas.emm.core.user.form.UserSelfForm"--%>
<%--@elvariable id="passwordPolicy" type="java.lang.String"--%>
<%--@elvariable id="availableLayouts" type="java.util.List<com.agnitas.beans.EmmLayoutBase>"--%>
<%--@elvariable id="availableAdminGroups" type="java.util.List<com.agnitas.beans.AdminGroup>"--%>
<%--@elvariable id="availableTimezones" type="java.lang.String[]"--%>
<%--@elvariable id="showSupervisorPermissionManagement" type="java.lang.Boolean"--%>

<c:set var="LOGIN_STATUS_SUCCESS" value="<%=LoginStatus.SUCCESS.getStatusCode()%>" />
<c:set var="LOGIN_STATUS_FAIL" value="<%=LoginStatus.FAIL.getStatusCode()%>" />
<c:set var="LOGIN_STATUS_SUCCESS_BUT_BLOCKED" value="<%=LoginStatus.SUCCESS_BUT_BLOCKED.getStatusCode()%>" />

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%=AdminPreferences.STATISTIC_LOADTYPE_ON_CLICK%>"/>       <%-- 0 --%>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%=AdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY%>"/> <%-- 1 --%>

<c:set var="setUserGroupAllowed" value="${emm:permissionAllowed('admin.setgroup', pageContext.request)}" />

<div class="tiles-container" data-editable-view="${agnEditViewKey}">
    <mvc:form id="settings-tile" servletRelativeAction="/user/self/save.action" cssClass="tile" modelAttribute="selfForm"
              data-editable-tile="" data-form-focus="username" data-form="resource">

        <mvc:hidden path="id"/>

        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-6">
                    <label class="form-label" for="gender"><mvc:message code="recipient.Salutation"/></label>
                    <mvc:select id="gender" path="gender" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="recipient.gender.0.short"/></mvc:option>
                        <mvc:option value="1"><mvc:message code="recipient.gender.1.short"/></mvc:option>
                        <emm:ShowByPermission token="recipient.gender.extended">
                            <mvc:option value="4"><mvc:message code="recipient.gender.4.short"/></mvc:option>
                            <mvc:option value="5"><mvc:message code="recipient.gender.5.short"/></mvc:option>
                        </emm:ShowByPermission>
                        <mvc:option value="2"><mvc:message code="recipient.gender.2.short"/></mvc:option>
                    </mvc:select>
                </div>

                <div class="col-6">
                    <label class="form-label" for="title"><mvc:message code="Title"/></label>
                    <mvc:text id="title" path="title" cssClass="form-control" maxlength="99" size="32"/>
                </div>

                <div class="col-6">
                    <label class="form-label" for="first_name"><mvc:message code="Firstname"/></label>
                    <mvc:text id="first_name" path="firstname" cssClass="form-control" maxlength="99" size="32"/>
                </div>

                <div class="col-6">
                    <label class="form-label" for="mailing_name"><mvc:message code="Lastname"/></label>
                    <mvc:text id="mailing_name" path="fullname" cssClass="form-control" maxlength="99" size="32"/>
                </div>
                <div class="col-6">
                    <label class="form-label" for="companyName"><mvc:message code="settings.Admin.company"/></label>
                    <mvc:text id="companyName" path="companyName" cssClass="form-control" maxlength="99" size="32"/>
                </div>
                <div class="col-6">
                    <label class="form-label"><mvc:message code="settings.Company"/></label>
                    <mvc:hidden path="companyID"/>
                    <mvc:text path="initialCompanyName" cssClass="form-control disabled" readonly="true"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="username"><mvc:message code="logon.username"/></label>
                    <mvc:text id="username" cssClass="form-control" path="username" size="52" maxlength="180" readonly="true"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="groupIDs"><mvc:message code="settings.Usergroup"/></label>
                    <mvc:select path="groupIDs" id="groupIDs" cssClass="form-control js-select" multiple="true" disabled="${not setUserGroupAllowed}">
                        <c:forEach var="adminGroup" items="${availableAdminGroups}">
                            <mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div class="col-6">
                    <label class="form-label" for="email"><mvc:message code="settings.Admin.email"/></label>
                    <mvc:text path="email" cssClass="form-control" id="email" size="52" maxlength="99"/>
                </div>

                <div class="col-6">
                    <label class="form-label" for="statEmail"><mvc:message code="stat.email"/></label>
                    <mvc:text path="statEmail" cssClass="form-control" id="statEmail" size="52" maxlength="99"/>
                </div>

                <div class="col-12">
                    <div class="tile tile--sm">
                        <div class="tile-header border-bottom">
                            <h3 class="tile-title tile-title--grey text-truncate"><mvc:message code="password.change" /></h3>
                        </div>
                        <div class="tile-body row g-3" data-field="password">
                            <div class="col">
                                <label class="form-label" for="current-password">
                                    <mvc:message code="password.current"/>
                                </label>
                                <input id="current-password" type="password" name="currentPassword" class="form-control" size="52" />
                            </div>
                            <div class="col">
                                <label class="form-label" for="password">
                                    <mvc:message code="password.new"/>
                                    <a href="#" class="icon icon-question-circle" data-help="settings/AdminPasswordRules.xml"></a>
                                </label>
                                <input type="password" name="password" id="password" class="form-control js-password-strength" size="52" data-rule="${passwordPolicy}" />
                            </div>
                            <div class="col">
                                <label class="form-label" for="repeat"><mvc:message code="settings.admin.Confirm"/></label>
                                <mvc:password path="passwordConfirm" id="repeat" cssClass="form-control js-password-match" size="52"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-6">
                    <label class="form-label" for="language"><mvc:message code="Language"/></label>
                    <mvc:select path="adminLocale" id="language" cssClass="form-control js-select">
                        <mvc:option value="<%= Locale.GERMANY.toString() %>"><mvc:message code="settings.German"/></mvc:option>
                        <mvc:option value="<%= Locale.US.toString() %>"><mvc:message code="settings.English"/></mvc:option>
                        <mvc:option value="<%= Locale.FRANCE.toString() %>"><mvc:message code="settings.French"/></mvc:option>
                        <mvc:option value="es_ES"><mvc:message code="settings.Spanish"/></mvc:option>
                        <mvc:option value="pt_PT"><mvc:message code="settings.Portuguese"/></mvc:option>
                        <mvc:option value="nl_NL"><mvc:message code="settings.Dutch"/></mvc:option>
                        <mvc:option value="it_IT"><mvc:message code="settings.Italian"/></mvc:option>
                    </mvc:select>
                </div>

                <div class="col-6">
                    <label class="form-label" for="adminTimezone"><mvc:message code="Timezone"/></label>
                    <mvc:select path="adminTimezone" id="adminTimezone" cssClass="form-control js-select">
                        <mvc:options items="${availableTimezones}"/>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="layoutBaseId"><mvc:message code="Layout"/></label>
                    <mvc:select path="layoutBaseId" id="layoutBaseId" cssClass="form-control js-select">
                        <mvc:options items="${availableLayouts}" itemLabel="shortname" itemValue="id"/>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="uiLayoutType"><mvc:message code="gui.layout"/></label>
                    <mvc:select path="uiLayoutType" id="uiLayoutType" cssClass="form-control js-select">
                        <c:forEach var="layoutType" items="${UiLayoutType.values()}">
                            <mvc:option value="${layoutType}"><mvc:message code="${layoutType.messageKey}"/></mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="statisticLoadType"><mvc:message code="statistic.summary"/></label>
                    <mvc:select path="statisticLoadType" id="statisticLoadType" cssClass="form-control js-select">
                        <mvc:option value="${STATISTIC_LOADTYPE_ON_CLICK}"><mvc:message code="statistic.summary.load.onclick"/></mvc:option>
                        <mvc:option value="${STATISTIC_LOADTYPE_IMMEDIATELY}"><mvc:message code="statistic.summary.load.immediately"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </mvc:form>

    <div class="tiles-block flex-column">
        <mvc:form id="login-log-tile" cssClass="tile" cssStyle="flex: 1" servletRelativeAction="/user/self/view.action" modelAttribute="selfForm" data-editable-tile="">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "admin-login-log-overview": {
                        "rows-count": ${selfForm.numberOfRows}
                    }
                }
            </script>

            <div class="tile-body">
                <div class="table-wrapper">
                    <div class="table-wrapper__header">
                        <h1 class="table-wrapper__title"><mvc:message code="loginLog" /></h1>
                        <div class="table-wrapper__controls">
                            <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../common/table/entries-label.jsp">
                                <jsp:param name="totalEntries" value="${fn:length(loginTrackingList)}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <emm:table var="loginData" modelAttribute="loginTrackingList" pageSize="${selfForm.numberOfRows}"
                                   cssClass="table table--borderless table-hover js-table">

                            <%--@elvariable id="loginData" type="org.agnitas.emm.core.logintracking.bean.LoginData"--%>

                            <emm:column titleKey="warning.failed_login.date" property="loginDate" sortable="true" />
                            <emm:column titleKey="statistic.IPAddress" property="loginIP" sortable="true" />

                            <emm:column titleKey="warning.failed_login.status">
                                <span>
                                    <c:choose>
                                        <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_SUCCESS}">
                                            <mvc:message code="import.csv_successfully" />
                                        </c:when>
                                        <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_FAIL}">
                                            <mvc:message code="warning.failed_login.status.failed" />
                                        </c:when>
                                        <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_SUCCESS_BUT_BLOCKED}">
                                            <mvc:message code="warning.failed_login.status.blocked" />
                                        </c:when>
                                    </c:choose>
                                </span>
                            </emm:column>
                        </emm:table>
                    </div>
                </div>
            </div>
        </mvc:form>

        <c:if test="${showSupervisorPermissionManagement}">
            <div id="manage-approval-tile" class="tile" style="flex: 1" data-editable-tile>
                <div class="tile-body" data-load="<c:url value="/user/self/supervisor-permission/show.action" />"></div>
            </div>
        </c:if>
    </div>
</div>
