<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" import="
	java.util.Locale,
	org.agnitas.emm.core.logintracking.*" %>
<%@ page import="com.agnitas.beans.AdminPreferences" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="selfForm" type="com.agnitas.emm.core.user.form.UserSelfForm"--%>
<%--@elvariable id="passwordPolicy" type="java.lang.String"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="availableLayouts" type="java.util.List<com.agnitas.beans.EmmLayoutBase>"--%>
<%--@elvariable id="availableAdminGroups" type="java.util.List<org.agnitas.beans.AdminGroup>"--%>
<%--@elvariable id="availableTimezones" type="java.lang.String[]"--%>

<c:set var="LOGIN_STATUS_SUCCESS" value="<%=LoginStatus.SUCCESS.getStatusCode()%>" />
<c:set var="LOGIN_STATUS_FAIL" value="<%=LoginStatus.FAIL.getStatusCode()%>" />
<c:set var="LOGIN_STATUS_SUCCESS_BUT_BLOCKED" value="<%=LoginStatus.SUCCESS_BUT_BLOCKED.getStatusCode()%>" />

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%=AdminPreferences.STATISTIC_LOADTYPE_ON_CLICK%>"/>       <%-- 0 --%>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%=AdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY%>"/> <%-- 1 --%>

<mvc:form servletRelativeAction="/user/self/save.action" data-form="search" data-form-focus="username" modelAttribute="selfForm">
    <mvc:hidden path="id"/>

    <div class="tile">
        <div class="tile-header">
            <div class="tile-header">
                <h2 class="headline"><mvc:message code="settings.admin.edit"/></h2>
            </div>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="gender"><mvc:message code="recipient.Salutation"/></label>
                </div>
                <div class="col-sm-8">
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
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="title"><mvc:message code="Title"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="title" path="title" cssClass="form-control" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="first_name"><mvc:message code="Firstname"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="first_name" path="firstname" cssClass="form-control" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name"><mvc:message code="Lastname"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="mailing_name" path="fullname" cssClass="form-control" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="companyName"><mvc:message code="settings.Admin.company"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="companyName" path="companyName" cssClass="form-control" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="settings.Company"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:hidden path="companyID"/>
                    <mvc:text path="initialCompanyName" cssClass="form-control disabled" readonly="true"/>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.UserSettings"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="username"><mvc:message code="logon.username"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="username" cssClass="form-control" path="username" size="52" maxlength="180" readonly="true"/>
                </div>
            </div>

            <emm:ShowByPermission token="admin.setgroup">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="groupIDs"><mvc:message code="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="groupIDs" id="groupIDs" cssClass="form-control js-select" multiple="true">
                            <c:forEach var="adminGroup" items="${availableAdminGroups}">
                                <mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="admin.setgroup">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="groupIDs"><mvc:message code="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select id="groupIDs" path="groupIDs" cssClass="form-control js-select" size="1" disabled="true">
                            <c:forEach var="adminGroup" items="${availableAdminGroups}">
                                <mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            </emm:HideByPermission>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="current-password">
                        <mvc:message code="password.current"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <input id="current-password" type="password" name="currentPassword" class="form-control" size="52" />
                </div>
            </div>

            <div data-field="password">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="password">
                            <mvc:message code="password"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input type="password" name="password" id="password" class="form-control js-password-strength" size="52" data-rule="${passwordPolicy}" />
                            </div>
                            <div class="input-group-addon">
                            <span class="addon js-password-strength-indicator hidden">
                                <i class="icon icon-check"></i>
                             </span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="repeat"><mvc:message code="settings.admin.Confirm"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:password path="passwordConfirm" id="repeat" cssClass="form-control js-password-match" size="52"/>
                            </div>
                            <div class="input-group-addon">
                            <span class="addon js-password-match-indicator hidden">
                                <i class="icon icon-check"></i>
                             </span>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="email"><mvc:message code="settings.Admin.email"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="email" cssClass="form-control" id="email" size="52" maxlength="99"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statEmail"><mvc:message code="stat.email"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="statEmail" cssClass="form-control" id="statEmail" size="52" maxlength="99"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="language"><mvc:message code="Language"/></label>
                </div>
                <div class="col-sm-8">
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
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="adminTimezone"><mvc:message code="Timezone"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="adminTimezone" id="adminTimezone" cssClass="form-control js-select">
                        <mvc:options items="${availableTimezones}"/>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="layoutBaseId"><mvc:message code="Layout"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="layoutBaseId" id="layoutBaseId" cssClass="form-control js-select">
                        <mvc:options items="${availableLayouts}" itemLabel="shortname" itemValue="id"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statisticLoadType"><mvc:message code="statistic.summary"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="statisticLoadType" id="statisticLoadType" cssClass="form-control js-select">
                        <mvc:option value="${STATISTIC_LOADTYPE_ON_CLICK}"><mvc:message code="statistic.summary.load.onclick"/></mvc:option>
                        <mvc:option value="${STATISTIC_LOADTYPE_IMMEDIATELY}"><mvc:message code="statistic.summary.load.immediately"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<mvc:form servletRelativeAction="/user/self/view.action" data-form="search" modelAttribute="selfForm">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="loginLog"/></h2>

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
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content" data-form-content="">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "admin-login-log-overview": {
                        "rows-count": ${selfForm.numberOfRows}
                    }
                }
            </script>

            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table" id="loginData" name="loginTrackingList"
                               pagesize="${selfForm.numberOfRows}" sort="list" requestURI="/user/self/view.action" excludedParams="*">

                    <%--@elvariable id="loginData" type="org.agnitas.emm.core.logintracking.bean.LoginData"--%>

                    <display:column headerClass="head_action" class="action" titleKey="warning.failed_login.date" property="loginDate"/>
                    <display:column headerClass="head_action" class="action" titleKey="statistic.IPAddress" property="loginIP"/>

                    <display:column headerClass="head_action" class="action" titleKey="warning.failed_login.status">
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
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
