<%@page import="org.agnitas.beans.AdminGroup"%>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" import="
	org.agnitas.util.AgnUtils,
	com.agnitas.beans.ComAdmin,
	java.util.Locale,
	java.util.TimeZone,
	java.text.DateFormat,
	java.text.SimpleDateFormat,
	org.agnitas.emm.core.logintracking.*" %>
<%@ page import="com.agnitas.beans.ComAdminPreferences" %>
<%@ page import="org.agnitas.emm.core.logintracking.bean.LoginData" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="adminForm" type="com.agnitas.web.ComAdminForm"--%>

<%
	ComAdmin admin = AgnUtils.getAdmin(request);
%>

<c:set var="LOGIN_STATUS_SUCCESS" value="<%= LoginStatus.SUCCESS.getStatusCode() %>" />
<c:set var="LOGIN_STATUS_FAIL" value="<%= LoginStatus.FAIL.getStatusCode() %>" />
<c:set var="LOGIN_STATUS_SUCCESS_BUT_BLOCKED" value="<%= LoginStatus.SUCCESS_BUT_BLOCKED.getStatusCode() %>" />

<c:set var="MAILING_CONTENT_HTML_EDITOR" value="<%= ComAdminPreferences.MAILING_CONTENT_HTML_EDITOR %>"/>      <%-- 1 --%>
<c:set var="MAILING_CONTENT_HTML_CODE" value="<%= ComAdminPreferences.MAILING_CONTENT_HTML_CODE %>"/>          <%-- 0 --%>

<c:set var="DASHBOARD_MAILINGS_LIST" value="<%= ComAdminPreferences.DASHBOARD_MAILINGS_LIST %>"/>              <%-- 0 --%>
<c:set var="DASHBOARD_MAILINGS_PREVIEW" value="<%= ComAdminPreferences.DASHBOARD_MAILINGS_PREVIEW %>"/>        <%-- 1 --%>

<c:set var="MAILING_SETTINGS_EXPANDED" value="<%= ComAdminPreferences.MAILING_SETTINGS_EXPANDED %>"/>          <%-- 0 --%>
<c:set var="MAILING_SETTINGS_COLLAPSED" value="<%= ComAdminPreferences.MAILING_SETTINGS_COLLAPSED %>"/>        <%-- 1 --%>

<c:set var="LIVE_PREVIEW_RIGHT" value="<%= ComAdminPreferences.LIVE_PREVIEW_RIGHT %>"/>                        <%-- 0 --%>
<c:set var="LIVE_PREVIEW_BOTTOM" value="<%= ComAdminPreferences.LIVE_PREVIEW_BOTTOM %>"/>                      <%-- 1 --%>
<c:set var="LIVE_PREVIEW_DEACTIVATE" value="<%= ComAdminPreferences.LIVE_PREVIEW_DEACTIVATE %>"/>              <%-- 2 --%>

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%= ComAdminPreferences.STATISTIC_LOADTYPE_ON_CLICK %>"/>      <%-- 0 --%>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%= ComAdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY %>"/><%-- 1 --%>

<agn:agnForm id="adminForm" action="selfservice.do?action=save" data-form="search" data-form-focus="username">
    <html:hidden property="action"/>
    <html:hidden property="adminID"/>

    <div class="tile">
        <div class="tile-header">
            <div class="tile-header">
                <h2 class="headline"><bean:message key="settings.admin.edit"/></h2>
            </div>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="gender"><bean:message key="recipient.Salutation"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select styleId="empfaenger_detail_anrede" styleClass="form-control js-select" property="gender" size="1">
                        <html:option value="0"><bean:message key="recipient.gender.0.short"/></html:option>
                        <html:option value="1"><bean:message key="recipient.gender.1.short"/></html:option>
                        <emm:ShowByPermission token="recipient.gender.extended">
                            <html:option value="4"><bean:message key="recipient.gender.4.short"/></html:option>
                            <html:option value="5"><bean:message key="recipient.gender.5.short"/></html:option>
                        </emm:ShowByPermission>
                        <html:option value="2"><bean:message key="recipient.gender.2.short"/></html:option>
                    </html:select>
                </div>
            </div>
             <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="title"><bean:message key="Title"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="title" styleClass="form-control" property="title" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="first_name"><bean:message key="Firstname"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="first_name" styleClass="form-control" property="firstname" maxlength="99" size="32"/>
                </div>
            </div>
             <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name"><bean:message key="Lastname"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="mailing_name" styleClass="form-control" property="fullname" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="companyName"><bean:message key="settings.Admin.company"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="companyName" styleClass="form-control" property="companyName" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="settings.Company"/></label>
                </div>
                <div class="col-sm-8">
                    <html:hidden property="companyID"/>
                    <input type="text" class="form-control disabled" readonly value="${adminForm.initialCompanyName}">
                    <html:hidden property="initialCompanyName"/>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.UserSettings"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="username"><bean:message key="logon.username"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="username" size="52" maxlength="180" styleId="username" readonly="true" />
                </div>
            </div>
            
            <emm:ShowByPermission token="admin.setgroup">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="groupIDs"><bean:message key="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
						<mvc:select path="adminForm.groupIDs" id="groupIDs" cssClass="form-control js-select" multiple="true">
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
                        <label class="control-label" for="groupIDShow"><bean:message key="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
                    	<% for (AdminGroup adminGroup : admin.getGroups()) {%>
                        <input type="text" class="form-control disabled" readonly value="<%= adminGroup.getShortname() %> (<%= adminGroup.getGroupID() %>)">
                        <% } %>
                    </div>
                </div>
            </emm:HideByPermission>
            
            <div data-field="password">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="password">
                            <bean:message key="password"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                            	<input type="password" name="password" id="password" class="form-control js-password-strength" size="52" data-rule="${PASSWORD_POLICY}" />
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
                        <label class="control-label" for="repeat"><bean:message key="settings.admin.Confirm"/></label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <html:password property="passwordConfirm" styleId="repeat" styleClass="form-control js-password-match" size="52" />
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
                    <label class="control-label" for="email"><bean:message key="settings.Admin.email"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="email" size="52" maxlength="99" styleId="email"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statEmail"><bean:message key="stat.email"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="statEmail" size="52" maxlength="99" styleId="statEmail"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="language"><bean:message key="Language"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="language" size="1" styleId="language" styleClass="form-control js-select">
                        <html:option value="<%= Locale.GERMANY.toString() %>"><bean:message key="settings.German"/></html:option>
                        <html:option value="<%= Locale.US.toString() %>"><bean:message key="settings.English"/></html:option>
                        <html:option value="<%= Locale.FRANCE.toString() %>"><bean:message key="settings.French"/></html:option>
                        <html:option value="es_ES"><bean:message key="settings.Spanish"/></html:option>
                        <html:option value="pt_PT"><bean:message key="settings.Portuguese"/></html:option>
                        <html:option value="nl_NL"><bean:message key="settings.Dutch"/></html:option>
                        <html:option value="it_IT"><bean:message key="settings.Italian"/></html:option>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="adminTimezone"><bean:message key="Timezone"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="adminTimezone" size="1" styleId="adminTimezone" styleClass="form-control js-select">
                        <% String allZones[] = TimeZone.getAvailableIDs();
                            int len = allZones.length;
                            TimeZone tmpZone = TimeZone.getDefault();
                            Locale aLoc = (Locale) session.getAttribute("messages_lang");
                            for (int i = 0; i < len; i++) {
                                tmpZone.setID(allZones[i]);
                        %>
                        <html:option value="<%= allZones[i] %>"><%= /* tmpZone.getDisplayName(aLoc) */ allZones[i] %>
                        </html:option>
                        <% } %>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="layoutBaseId"><bean:message key="Layout"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select styleClass="form-control js-select" property="layoutBaseId" size="1" styleId="layoutBaseId">
                        <c:forEach var="layout" items="${availableLayouts}">
                            <html:option value="${layout.id}">
                                ${layout.shortname}
                            </html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statisticLoadType"><bean:message key="statistic.summary"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="statisticLoadType" size="1" styleId="statisticLoadType" styleClass="form-control">
                        <html:option value="${STATISTIC_LOADTYPE_ON_CLICK}"><bean:message key="statistic.summary.load.onclick"/></html:option>
                        <html:option value="${STATISTIC_LOADTYPE_IMMEDIATELY}"><bean:message key="statistic.summary.load.immediately"/></html:option>
                    </html:select>
                </div>
            </div>
        </div>
    </div>
</agn:agnForm>

<agn:agnForm action="/selfservice.do" data-form="search">
    <html:hidden property="action" value="showChangeForm"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="loginLog"/></h2>

            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
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
                        "rows-count": ${adminForm.numberOfRows}
                    }
                }
            </script>

            <div class="table-wrapper">
                <%
                	SimpleDateFormat dateTimeFormat = AgnUtils.getAdmin(request).getDateTimeFormatWithSeconds();
                %>

                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        id="loginData"
                        name="login_tracking_list"
                        pagesize="${adminForm.numberOfRows}"
                        sort="list"
                        requestURI="/selfservice.do?action=showChangeForm&numberOfRows=${adminForm.numberOfRows}&__fromdisplaytag=true"
                        excludedParams="*"
                        >
                    <display:column headerClass="head_action" class="action" titleKey="warning.failed_login.date">
                        <%= dateTimeFormat.format(((LoginData) loginData).getLoginTime()) %>
                        <%--								${loginData.loginTime} --%>
                    </display:column>
                    <display:column headerClass="head_action" class="action" titleKey="statistic.IPAddress">
                        ${loginData.loginIP}
                    </display:column>
                    <display:column headerClass="head_action" class="action" titleKey="warning.failed_login.status">
                        <c:choose>
                            <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_SUCCESS}">
                                <bean:message key="import.csv_successfully" />
                            </c:when>
                            <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_FAIL}">
                                <bean:message key="warning.failed_login.status.failed" />
                            </c:when>
                            <c:when test="${loginData.loginStatus.statusCode == LOGIN_STATUS_SUCCESS_BUT_BLOCKED}">
                                <bean:message key="warning.failed_login.status.blocked" />
                            </c:when>
                        </c:choose>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
