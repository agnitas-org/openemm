<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.beans.AdminPreferences" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags"    prefix="s" %>

<%--@elvariable id="adminForm" type="com.agnitas.emm.core.admin.form.AdminForm"--%>
<%--@elvariable id="createdCompanies" type="java.util.List"--%>
<%--@elvariable id="adminGroups" type="java.util.List"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="PASSWORD_POLICY" type="java.lang.String"--%>
<%--@elvariable id="availableTimeZones" type="java.util.List"--%>
<%--@elvariable id="layouts" type="java.util.List"--%>

<c:set var="MAILING_CONTENT_HTML_EDITOR" value="<%=AdminPreferences.MAILING_CONTENT_HTML_EDITOR%>"/>
<c:set var="MAILING_CONTENT_HTML_CODE" value="<%=AdminPreferences.MAILING_CONTENT_HTML_CODE%>"/>

<c:set var="DASHBOARD_MAILINGS_LIST" value="<%=AdminPreferences.DASHBOARD_MAILINGS_LIST%>"/>
<c:set var="DASHBOARD_MAILINGS_PREVIEW" value="<%=AdminPreferences.DASHBOARD_MAILINGS_PREVIEW%>"/>

<c:set var="MAILING_SETTINGS_EXPANDED" value="<%=AdminPreferences.MAILING_SETTINGS_EXPANDED%>"/>
<c:set var="MAILING_SETTINGS_COLLAPSED" value="<%=AdminPreferences.MAILING_SETTINGS_COLLAPSED%>"/>

<c:set var="LIVE_PREVIEW_RIGHT" value="<%=AdminPreferences.LIVE_PREVIEW_RIGHT%>"/>
<c:set var="LIVE_PREVIEW_BOTTOM" value="<%=AdminPreferences.LIVE_PREVIEW_BOTTOM%>"/>
<c:set var="LIVE_PREVIEW_DEACTIVATE" value="<%=AdminPreferences.LIVE_PREVIEW_DEACTIVATE%>"/>

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%=AdminPreferences.STATISTIC_LOADTYPE_ON_CLICK%>"/>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%=AdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY%>"/>

<mvc:form id="admin-form" servletRelativeAction="/admin/${adminForm.adminID}/view.action" data-form-focus="username" modelAttribute="adminForm"
          accept-charset="UTF-8" data-form="resource" data-form-dirty-checking="">
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.admin.edit"/></h2>
        </div>
        <div class="tile-content tile-content-forms" data-form-content>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="empfaenger_detail_anrede"><mvc:message code="recipient.Salutation"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select id="empfaenger_detail_anrede" cssClass="form-control js-select" path="gender" size="1">
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
                    <label class="control-label" for="mailing_name"><mvc:message code="Title"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="mailing_name" cssClass="form-control" path="title" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="first_name"><mvc:message code="Firstname"/> *</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="first_name" cssClass="form-control" path="firstname" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="last_name"><mvc:message code="Lastname"/> *</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="last_name" cssClass="form-control" path="fullname" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="employee_id"><mvc:message code="EmployeeID"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="employee_id" cssClass="form-control" path="employeeID" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="companyName"><mvc:message code="settings.Admin.company"/> *</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text id="companyName" cssClass="form-control" path="companyName" maxlength="99" size="32"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="settings.Company"/></label>
                </div>
                <div class="col-sm-8">
                    <c:choose>
                        <%--if new--%>
                        <c:when test="${adminForm.adminID eq 0}">
                            <mvc:select cssClass="form-control js-select" path="companyID" size="1">
                                <mvc:options itemLabel="shortname" itemValue="id" items="${createdCompanies}"/>
                            </mvc:select>
                        </c:when>
                        <c:otherwise>
                            <mvc:hidden path="companyID"/>
                            <mvc:hidden path="initialCompanyName"/>
                            <input type="text" class="form-control" value="${adminForm.initialCompanyName}" readonly />
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <label class="control-label"><mvc:message code="default.mark.required"/></label>
            <c:if test="${adminForm.adminID > 0}">
                <%@include file="./fragments/approval-mailig-lists-fragment.jspf"%>
            </c:if>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="settings.UserSettings"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="username"><mvc:message code="logon.username"/> *</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text cssClass="form-control" path="username" size="52" maxlength="180" id="username"/>
                </div>
            </div>
            <emm:ShowByPermission token="admin.setgroup">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="groupIDs"><mvc:message code="settings.Usergroup"/></label>
                    </div>
                    <div class="col-sm-8">
                    	<mvc:select path="groupIDs" id="groupIDs" cssClass="form-control js-select-tags" multiple="true" data-sort="alphabetic">
	                        <c:forEach var="adminGroup" items="${adminGroups}">
	                            <mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
	                        </c:forEach>
	                    </mvc:select>
                    </div>
                </div>
            </emm:ShowByPermission>

            <div data-field="password">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="password">
                            <mvc:message code="password"/> *
                            <button class="icon icon-help" data-help="help_${helplanguage}/settings/AdminPasswordRules.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:password path="password" id="password" cssClass="form-control js-password-strength" size="52" data-rule="${PASSWORD_POLICY}"/>
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
                        <label class="control-label" for="repeat"><mvc:message code="settings.admin.Confirm"/> *</label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <mvc:password path="passwordConfirm" id="repeat" cssClass="form-control js-password-match" size="52" />
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
                    <label class="control-label" for="email"><mvc:message code="settings.Admin.email"/> *</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text cssClass="form-control" path="email" size="52" maxlength="99" id="email"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statEmail"><mvc:message code="stat.email"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text cssClass="form-control" path="statEmail" size="52" maxlength="99" id="statEmail"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="adminPhone"><mvc:message code="admin.phone"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text cssClass="form-control" path="adminPhone" size="52" maxlength="99" id="adminPhone"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="language"><mvc:message code="Language"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="language" size="1" id="language" cssClass="form-control js-select">
                        <mvc:option value="de_DE"><mvc:message code="settings.German"/></mvc:option>
                        <mvc:option value="en_US"><mvc:message code="settings.English"/></mvc:option>
                        <mvc:option value="fr_FR"><mvc:message code="settings.French"/></mvc:option>
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
                    <mvc:select path="adminTimezone" size="1" id="adminTimezone" cssClass="form-control js-select">
                        <c:forEach var="timeZone" items="${availableTimeZones}">
                            <mvc:option value="${timeZone}">${timeZone}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="layoutBaseId"><mvc:message code="Layout"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select cssClass="form-control js-select" path="layoutBaseId" size="1" id="layoutBaseId">
                        <mvc:options itemLabel="shortname" itemValue="id" items="${layouts}"/>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="statisticLoadType"><mvc:message code="statistic.summary"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="adminPreferences.statisticLoadType" size="1" id="statisticLoadType" cssClass="form-control">
                        <mvc:option value="${STATISTIC_LOADTYPE_ON_CLICK}"><mvc:message code="statistic.summary.load.onclick"/></mvc:option>
                        <mvc:option value="${STATISTIC_LOADTYPE_IMMEDIATELY}"><mvc:message code="statistic.summary.load.immediately"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            
			<%@include file="./fragments/access-limiting-target-group-input-fragment.jspf"%>
            <label class="control-label"><mvc:message code="default.mark.required"/></label>
        </div>
    </div>
</mvc:form>
