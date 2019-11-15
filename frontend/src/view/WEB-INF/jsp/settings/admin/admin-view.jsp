<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComAdminAction" %>
<%@ page import="com.agnitas.beans.ComAdminPreferences" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<%--@elvariable id="adminForm" type="com.agnitas.web.ComAdminForm"--%>
<%--@elvariable id="createdCompanies" type="java.util.List"--%>
<%--@elvariable id="adminGroups" type="java.util.List"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="availableTimeZones" type="java.util.List"--%>
<%--@elvariable id="layouts" type="java.util.List"--%>

<c:set var="ACTION_VIEW" value="<%= ComAdminAction.ACTION_VIEW %>"/>
<c:set var="START_PAGE_DASHBOARD" value="<%= ComAdminPreferences.START_PAGE_DASHBOARD %>"/>
<c:set var="START_PAGE_CALENDAR" value="<%= ComAdminPreferences.START_PAGE_CALENDAR %>"/>

<c:set var="MAILING_CONTENT_HTML_EDITOR" value="<%= ComAdminPreferences.MAILING_CONTENT_HTML_EDITOR %>"/>
<c:set var="MAILING_CONTENT_HTML_CODE" value="<%= ComAdminPreferences.MAILING_CONTENT_HTML_CODE %>"/>

<c:set var="DASHBOARD_MAILINGS_LIST" value="<%= ComAdminPreferences.DASHBOARD_MAILINGS_LIST %>"/>
<c:set var="DASHBOARD_MAILINGS_PREVIEW" value="<%= ComAdminPreferences.DASHBOARD_MAILINGS_PREVIEW %>"/>

<c:set var="NAVIGATION_LEFT" value="<%= ComAdminPreferences.NAVIGATION_LEFT %>"/>
<c:set var="NAVIGATION_TOP" value="<%= ComAdminPreferences.NAVIGATION_TOP %>"/>

<c:set var="MAILING_SETTINGS_EXPANDED" value="<%= ComAdminPreferences.MAILING_SETTINGS_EXPANDED %>"/>
<c:set var="MAILING_SETTINGS_COLLAPSED" value="<%= ComAdminPreferences.MAILING_SETTINGS_COLLAPSED %>"/>

<c:set var="LIVE_PREVIEW_RIGHT" value="<%= ComAdminPreferences.LIVE_PREVIEW_RIGHT %>"/>
<c:set var="LIVE_PREVIEW_BOTTOM" value="<%= ComAdminPreferences.LIVE_PREVIEW_BOTTOM %>"/>
<c:set var="LIVE_PREVIEW_DEACTIVATE" value="<%= ComAdminPreferences.LIVE_PREVIEW_DEACTIVATE %>"/>

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%= ComAdminPreferences.STATISTIC_LOADTYPE_ON_CLICK %>"/>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%= ComAdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY %>"/>


<agn:agnForm action="/admin.do" data-form-focus="username" id="adminForm" accept-charset="UTF-8" data-form="resource">
    <html:hidden property="action"/>
    <html:hidden property="adminID"/>
    <html:hidden property="previousAction" value="${ACTION_VIEW}"/>
    <input type="hidden" name="delete" value=""/>
    <input type="hidden" name="save" value=""/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.admin.edit"/></h2>
        </div>
        <div class="tile-content tile-content-forms" data-form-content>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="gender"><bean:message key="recipient.Salutation"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select styleId="empfaenger_detail_anrede" styleClass="form-control js-select" property="gender" size="1">
                        <html:option value="0"><bean:message key="recipient.gender.0.short"/></html:option>
                        <html:option value="1"><bean:message key="recipient.gender.1.short"/></html:option>
                        <emm:ShowByPermission token="recipient.gender.extended">
                            <html:option value="3"><bean:message key="recipient.gender.3.short"/></html:option>
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
                    <html:text styleId="mailing_name" styleClass="form-control" property="title" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="first_name"><bean:message key="Firstname"/> *</label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="first_name" styleClass="form-control" property="firstname" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailing_name"><bean:message key="Lastname"/> *</label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="mailing_name" styleClass="form-control" property="fullname" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="companyName"><bean:message key="settings.Admin.company"/> *</label>
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
                    <c:choose>
                        <c:when test="${adminForm.adminID eq 0}">
                            <html:select styleClass="form-control js-select" property="companyID" size="1">
                                <c:forEach var="company" items="${createdCompanies}">
                                    <html:option value="${company.id}">
                                        ${company.shortname}
                                    </html:option>
                                </c:forEach>
                            </html:select>
                        </c:when>
                        <c:otherwise>
                            <html:hidden property="companyID"/>
                            <html:hidden property="initialCompanyName"/>
                            <input type="text" class="form-control" value="${adminForm.initialCompanyName}" readonly />
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <label class="control-label"><bean:message key="default.mark.required"/></label>
        </div>
    </div>
    <emm:JspExtensionPoint plugin="emm_core" point="admin.view.pos2" />
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.UserSettings"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="username"><bean:message key="logon.username"/> *</label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="username" size="52" maxlength="180" styleId="username"/>
                </div>
            </div>
            <emm:ShowByPermission token="admin.setgroup">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="groupID"><bean:message key="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="groupID" size="1" styleId="groupID">
                            <c:forEach var="adminGroup" items="${adminGroups}">
                                <html:option value="${adminGroup.groupID}">
                                    ${adminGroup.shortname}
                                </html:option>
                            </c:forEach>
                        </html:select>
                    </div>
                </div>
            </emm:ShowByPermission>

            <emm:HideByPermission token="admin.setgroup">
                <html:hidden property="groupID"/>
            </emm:HideByPermission>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="oneTimePassword"><bean:message key="password.once.switch"/></label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <html:checkbox property="oneTimePassword" styleId="oneTimePassword"/>
                            <div class="toggle-control"></div>
                        </label>
                    </div>
                </div>

            <div data-field="password">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="password">
                            <bean:message key="password"/> *
                            <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <html:password property="password" styleId="password" styleClass="form-control js-password-strength" size="52" />
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
                        <label class="control-label" for="repeat"><bean:message key="settings.admin.Confirm"/> *</label>
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
                    <label class="control-label" for="email"><bean:message key="settings.Admin.email"/> *</label>
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
                    <label class="control-label" for="adminPhone"><bean:message key="admin.phone"/></label>
                </div>
                <div class="col-sm-8">
                    <html:text styleClass="form-control" property="adminPhone" size="52" maxlength="99" styleId="adminPhone"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="language"><bean:message key="Language"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="language" size="1" styleId="language" styleClass="form-control js-select">
                        <html:option value="de_DE"><bean:message key="settings.German"/></html:option>
                        <html:option value="en_US"><bean:message key="settings.English"/></html:option>
                        <html:option value="fr_FR"><bean:message key="settings.French"/></html:option>
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
                        <c:forEach var="timeZone" items="${availableTimeZones}">
                            <html:option value="${timeZone}">${timeZone}</html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="layoutBaseId"><bean:message key="Layout"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select styleClass="form-control js-select" property="layoutBaseId" size="1" styleId="layoutBaseId">
                        <c:forEach var="layout" items="${layouts}">
                            <html:option value="${layout.id}">
                                ${layout.shortname}
                            </html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>

            <emm:ShowByPermission token="calendar.show">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="startPage"><bean:message key="start.page.label"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:select styleClass="form-control js-select" property="startPage" styleId="startPage">
                            <html:option value="${START_PAGE_DASHBOARD}"><bean:message key="Dashboard"/></html:option>
                            <html:option value="${START_PAGE_CALENDAR}"><bean:message key="calendar.Calendar"/></html:option>
                        </html:select>
                    </div>
                </div>
            </emm:ShowByPermission>

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
            <label class="control-label"><bean:message key="default.mark.required"/></label>
        </div>
    </div>
</agn:agnForm>
