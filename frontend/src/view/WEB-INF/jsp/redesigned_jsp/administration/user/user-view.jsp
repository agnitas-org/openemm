<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.beans.AdminPreferences" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="adminForm" type="com.agnitas.emm.core.admin.form.AdminForm"--%>
<%--@elvariable id="createdCompanies" type="java.util.List"--%>
<%--@elvariable id="adminGroups" type="java.util.List"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="PASSWORD_POLICY" type="java.lang.String"--%>
<%--@elvariable id="availableTimeZones" type="java.util.List"--%>
<%--@elvariable id="layouts" type="java.util.List"--%>
<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>

<c:set var="STATISTIC_LOADTYPE_ON_CLICK" value="<%=AdminPreferences.STATISTIC_LOADTYPE_ON_CLICK%>"/>
<c:set var="STATISTIC_LOADTYPE_IMMEDIATELY" value="<%=AdminPreferences.STATISTIC_LOADTYPE_IMMEDIATELY%>"/>

<mvc:form cssClass="tiles-container" id="user-form" servletRelativeAction="/${isRestfulUser ? 'restfulUser' : 'admin'}/${adminForm.adminID}/view.action"
          data-form-focus="username" modelAttribute="adminForm" accept-charset="UTF-8"
          data-form="resource" data-form-dirty-checking="" data-editable-view="${agnEditViewKey}">
    <div id="general-info-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="settings.general.information"/></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-6">
                    <label class="form-label" for="empfaenger_detail_anrede"><mvc:message code="recipient.Salutation"/></label>
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
                <div class="col-6">
                    <label class="form-label" for="mailing_name"><mvc:message code="Title"/></label>
                    <mvc:text id="mailing_name" cssClass="form-control" path="title" maxlength="99" size="32"/>
                </div>
                <div class="col-6">
                    <label class="form-label" for="first_name"><mvc:message code="Firstname"/> *</label>
                    <mvc:text id="first_name" cssClass="form-control" path="firstname" maxlength="99" size="32"/>
                </div>
                <div class="col-6">
                    <label class="form-label" for="last_name"><mvc:message code="Lastname"/> *</label>
                    <mvc:text id="last_name" cssClass="form-control" path="fullname" maxlength="99" size="32"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="username"><mvc:message code="logon.username"/> *</label>
                    <mvc:text cssClass="form-control" path="username" size="52" maxlength="180" id="username"/>
                </div>
                <div class="col-12" data-field="password">
                    <div class="row">
                        <div class="col">
                            <label class="form-label" for="password">
                                <mvc:message code="password"/> *
                                <a href="#" class="icon icon-question-circle" data-help="help_${helplanguage}/${isRestfulUser ? 'restfuluser' : 'settings'}/AdminPasswordRules.xml" tabindex="-1" type="button"></a>
                            </label>
                            <mvc:password path="password" id="password" cssClass="form-control js-password-strength" size="52" data-rule="${PASSWORD_POLICY}"/>
                        </div>
                        <div class="col">
                            <label class="form-label" for="repeat"><mvc:message code="settings.admin.Confirm"/> *</label>
                            <mvc:password path="passwordConfirm" id="repeat" cssClass="form-control js-password-match" size="52" readonly="true" />
                        </div>
                        <c:if test="${not isRestfulUser}">
                            <%@include file="./fragments/update-password-reminder-switch.jspf"%>
                        </c:if>
                    </div>
                </div>
                <emm:ShowByPermission token="admin.setgroup">
                    <div class="col-12">
                        <label class="form-label" for="groupIDs"><mvc:message code="settings.Usergroup"/></label>
                        <mvc:select path="groupIDs" id="groupIDs" cssClass="form-control" multiple="true" data-sort="alphabetic">
                            <c:forEach var="adminGroup" items="${adminGroups}">
                                <mvc:option value="${adminGroup.groupID}">${fn:escapeXml(adminGroup.shortname)}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </emm:ShowByPermission>
                <div class="col-12">
                    <div class="row">
                        <c:if test="${not isRestfulUser}">
                            <div class="col">
                                <label class="form-label" for="employee_id"><mvc:message code="EmployeeID"/></label>
                                <mvc:text id="employee_id" cssClass="form-control" path="employeeID" maxlength="99" size="32"/>
                            </div>
                        </c:if>
                        <div class="col">
                            <label class="form-label" for="companyName"><mvc:message code="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}.company"/> *</label>
                            <mvc:text id="companyName" cssClass="form-control" path="companyName" maxlength="99" size="32"/>
                        </div>
                        <div class="col">
                            <label class="form-label"><mvc:message code="settings.Company"/></label>
                            <c:choose>
                                <%--if new--%>
                                <c:when test="${adminForm.adminID eq 0}">
                                    <mvc:select cssClass="form-control js-select" path="companyID">
                                        <c:forEach var="createdCompany" items="${createdCompanies}">
                                            <mvc:option value="${createdCompany.id}">${createdCompany.shortname} (${createdCompany.id})</mvc:option>
                                        </c:forEach>
                                    </mvc:select>
                                </c:when>
                                <c:otherwise>
                                    <mvc:hidden path="companyID"/>
                                    <mvc:hidden path="initialCompanyName"/>
                                    <input type="text" class="form-control" value="${adminForm.initialCompanyName} (${adminForm.companyID})" readonly />
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
                <div class="col-6">
                    <label class="form-label" for="email"><mvc:message code="mailing.MediaType.email"/> *</label>
                    <mvc:text cssClass="form-control" path="email" size="52" maxlength="99" id="email"/>
                </div>
                <div class="col-6">
                    <label class="form-label" for="statEmail"><mvc:message code="stat.email"/></label>
                    <mvc:text cssClass="form-control" path="statEmail" size="52" maxlength="99" id="statEmail"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="adminPhone"><mvc:message code="upload.view.phone"/></label>
                    <mvc:text cssClass="form-control" path="adminPhone" size="52" maxlength="99" id="adminPhone"/>
                </div>
            </div>
        </div>
    </div>
    <div id="user-settings-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="settings.UserSettings"/></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-6">
                    <label class="form-label" for="language"><mvc:message code="Language"/></label>
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
                <div class="col-6">
                    <label class="form-label" for="adminTimezone"><mvc:message code="Timezone"/></label>
                    <mvc:select path="adminTimezone" size="1" id="adminTimezone" cssClass="form-control js-select">
                        <c:forEach var="timeZone" items="${availableTimeZones}">
                            <mvc:option value="${timeZone}">${timeZone}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div class="col-12">
                    <label class="form-label" for="layoutBaseId"><mvc:message code="Layout"/></label>
                    <mvc:select cssClass="form-control js-select" path="layoutBaseId" size="1" id="layoutBaseId">
                        <mvc:options itemLabel="shortname" itemValue="id" items="${layouts}"/>
                    </mvc:select>
                </div>
                <div class="col-12">
                    <label class="form-label" for="statisticLoadType"><mvc:message code="statistic.summary"/></label>
                    <mvc:select path="adminPreferences.statisticLoadType" size="1" id="statisticLoadType" cssClass="form-control js-select">
                        <mvc:option value="${STATISTIC_LOADTYPE_ON_CLICK}"><mvc:message code="statistic.summary.load.onclick"/></mvc:option>
                        <mvc:option value="${STATISTIC_LOADTYPE_IMMEDIATELY}"><mvc:message code="statistic.summary.load.immediately"/></mvc:option>
                    </mvc:select>
                </div>
                <%@include file="./fragments/altg-select.jspf"%>
            </div>
        </div>
    </div>
</mvc:form>
