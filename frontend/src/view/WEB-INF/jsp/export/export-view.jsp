<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@page import="com.agnitas.emm.common.UserStatus"%>
<%@page import="com.agnitas.service.RecipientExportWorker"%>
<%@ page import="com.agnitas.util.DbColumnType" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="timeZones" type="java.lang.String[]"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="profileFields" type="java.util.List<com.agnitas.beans.ProfileField>"--%>
<%--@elvariable id="dateFormats" type="com.agnitas.util.importvalues.DateFormat[]"--%>
<%--@elvariable id="dateTimeFormats" type="com.agnitas.util.importvalues.DateFormat[]"--%>
<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="isManageAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="adminHasDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="isOwnColumnsExportAllowed" type="java.lang.Boolean"--%>

<c:set var="NO_MAILINGLIST" value="<%= RecipientExportWorker.NO_MAILINGLIST %>" scope="page" />
<c:set var="ALL_MAILINGLISTS" value="<%= RecipientExportWorker.ALL_MAILINGLISTS %>" scope="page" />
<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.GENERIC_TYPE_VARCHAR %>" scope="page" />
<c:set var="GENERIC_TYPE_INTEGER" value="<%= DbColumnType.GENERIC_TYPE_INTEGER %>" scope="page" />
<c:set var="GENERIC_TYPE_FLOAT" value="<%= DbColumnType.GENERIC_TYPE_FLOAT %>" scope="page" />
<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.GENERIC_TYPE_DATE %>" scope="page" />
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.GENERIC_TYPE_DATETIME %>" scope="page" />
<c:set var="BOUNCE_USER_STATUS_CODE" value="<%= UserStatus.Bounce.getStatusCode() %>" scope="page" />

<c:set var="localeDatePattern" value="${fn:toLowerCase(localeDatePattern)}"/>

<mvc:form servletRelativeAction="/export/${id}/save.action" id="exportForm" modelAttribute="exportForm" data-form="resource"
          data-form-focus="shortname"
          data-controller="export-view"
          data-initializer="export-view"
          data-validator="export/form"
          data-editable="${isManageAllowed}">
    
    <script id="config:export-view" type="application/json">
        {
            "adminHasOwnColumnPermission": ${isOwnColumnsExportAllowed},
            "bounceUserStatusCode": ${BOUNCE_USER_STATUS_CODE},
            "customColumns": ${emm:toJson(exportForm.customColumns)}
        }
    </script>
    
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="export.ExportDefinition"/>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-name" class="control-label">
                        <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" id="recipient-export-name" cssClass="form-control" maxlength="99" placeholder="${nameMsg}"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-description" class="control-label">
                        <c:set var="descriptionMsg"><mvc:message code="default.description"/></c:set>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="recipient-export-description" cssClass="form-control v-resizable" rows="5" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-selection">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="export.selection"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-selection">
            <c:set var="mailinglistSelect">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="recipient-export-mailinglist" class="control-label">
                            <mvc:message code="Mailinglist"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="mailinglistId" id="recipient-export-mailinglist" cssClass="js-select form-control">
                            <c:if test="${not adminHasDisabledMailingLists}">
                                <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                                <mvc:option value="${NO_MAILINGLIST}"><mvc:message code="No_Mailinglist"/></mvc:option>
                            </c:if>
                            <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                        </mvc:select>
                    </div>
                </div>
            </c:set>
            
            <%@ include file="fragments/export-mailinglist-select.jspf" %>
            
            ${mailinglistSelect}

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-targetgroup" class="control-label">
                        <mvc:message code="target.Target"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="targetId" id="recipient-export-targetgroup" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                        <mvc:options items="${targetGroups}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-recipienttype" class="control-label">
                        <mvc:message code="recipient.RecipientType"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="userType" cssClass="form-control" id="recipient-export-recipienttype">
                        <c:forEach var="userType" items="${availableUserTypeOptions}">
                            <mvc:option value="${userType.typeCode}">
                                <mvc:message code="${userType.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-recipientstatus" class="control-label">
                        <mvc:message code="recipient.RecipientStatus"/>
                    </label>
                </div>
                <div class="col-sm-8" data-action="add-bounce-col-to-choose">
                    <mvc:select path="userStatus" cssClass="form-control" id="recipient-export-recipientstatus">
                        <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                        <c:forEach var="userStatus" items="${availableUserStatusOptions}">
                            <mvc:option value="${userStatus.statusCode}">
                                <mvc:message code="${userStatus.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-columns">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="export.columns"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-columns">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="notification-simple notification-info">
                        <mvc:message code="export.wizard.hint.export.columns"/>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="user-columns"><mvc:message code="export.columns"/></label>
                </div>
                <div class="col-sm-8">
                    <c:set var="allColumnsStr"><mvc:message code="export.columns.all"/></c:set>
                    <mvc:select path="userColumns" id="user-columns" multiple="true" cssClass="form-control js-select" data-placeholder="${allColumnsStr}">
                        <c:forEach var="profileField" items="${profileFields}">
                            <c:set var="colType">
                                <c:choose>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_VARCHAR}'>
                                        <mvc:message code='statistic.alphanumeric'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_INTEGER or profileField.dataType == GENERIC_TYPE_FLOAT}'>
                                        <mvc:message code='statistic.numeric'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_DATETIME}'>
                                        <mvc:message code='settings.fieldType.DATETIME'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_DATE}'>
                                        <mvc:message code='settings.fieldType.DATE'/>
                                    </c:when>
                                    <c:otherwise>
                                        ${profileField}
                                    </c:otherwise>
                                </c:choose>
                            </c:set>
                            <mvc:option value="${fn:toLowerCase(profileField.column)}">${fn:escapeXml(profileField.shortname)} (${colType})</mvc:option>
                        </c:forEach>
                        <mvc:option id="mailing-bounce-column-option" value="mailing_bounce"><mvc:message code="report.bounce.reason"/> (<mvc:message code="statistic.alphanumeric"/>)</mvc:option>
                    </mvc:select>
                </div>
            </div>
            <c:if test="${isOwnColumnsExportAllowed}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="columnMappings"><mvc:message code="export.columns.add.own"/></label>
                    </div>
                    <div id="columnMappings" class="col-sm-8">
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th><mvc:message code="export.Column_Name"/></th>
                                        <th><mvc:message code="default.value.optional"/></th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%-- this block load by JS--%>
                                    <%@ include file="fragments/export-column-mapping-row-template.jspf" %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </c:if>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-mailingslists">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="export.mailinglist.status"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-mailingslists">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="export.mailinglist.status"/>
                        <button class="icon icon-help" data-help="help_${helplanguage}/export/Export_Mailinglist_Status.xml" tabindex="-1" type="button">
                        </button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <ul class="list-group">
                        <c:forEach var="mailinglist" items="${mailinglists}">
                            <li class="list-group-item">
                                <label class="checkbox-inline">
                                    <mvc:checkbox path="mailinglists" value='${mailinglist.id}'/>
                                    ${mailinglist.shortname}
                                </label>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-fileformats">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="export.file_format"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-fileformats">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-separator" class="control-label">
                        <mvc:message code="import.Separator"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="separator" cssClass="form-control" id="recipient-export-format-separator">
                        <mvc:option value=";">;</mvc:option>
                        <mvc:option value=",">,</mvc:option>
                        <mvc:option value="|">|</mvc:option>
                        <mvc:option value="t">Tab</mvc:option>
                        <mvc:option value="^">^</mvc:option>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="import.Delimiter"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="delimiter" cssClass="form-control" id="recipient-export-format-delimiter">
                        <mvc:option value='"'><mvc:message code="delimiter.doublequote"/></mvc:option>
                        <mvc:option value="'"><mvc:message code="delimiter.singlequote"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="csv.alwaysQuote"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="alwaysQuote" cssClass="form-control" id="recipient-export-format-delimiter">
                        <mvc:option value="false"><mvc:message code="delimiter.ifneeded"/></mvc:option>
                        <mvc:option value="true"><mvc:message code="delimiter.always"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-charset" class="control-label">
                        <mvc:message code="mailing.Charset"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="charset" cssClass="form-control" id="recipient-export-format-charset">
                        <c:forEach var="charset" items="${availableCharsetOptions}">
                            <mvc:option value="${charset.charsetName}">
                                <mvc:message code="${charset.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="csv.DateFormat"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="dateFormat" cssClass="form-control js-select" id="recipient-export-format-delimiter">
						<c:forEach var="dateFormat" items="${dateFormats}">
							<mvc:option value="${dateFormat}">${dateFormat.publicValue}</mvc:option>
						</c:forEach>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="csv.DateTimeFormat"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="dateTimeFormat" cssClass="form-control js-select" id="recipient-export-format-delimiter">
						<c:forEach var="dateTimeFormat" items="${dateTimeFormats}">
							<mvc:option value="${dateTimeFormat}">${dateTimeFormat.publicValue}</mvc:option>
						</c:forEach>
                    </mvc:select>
                </div>
            </div>
		
			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="locale"><mvc:message code="import.report.locale" /></label>
				</div>
				<div class="col-sm-8">
					<mvc:select path="locale" cssClass="form-control js-select" id="locale">
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
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="Timezone"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="timezone" cssClass="form-control js-select" id="recipient-export-format-delimiter">
						<c:forEach var="availableTimeZone" items="${timeZones}">
							<mvc:option value="${availableTimeZone}">${availableTimeZone}</mvc:option>
						</c:forEach>
                    </mvc:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <mvc:message code="csv.DecimalSeparator"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="decimalSeparator" cssClass="form-control" id="recipient-export-format-delimiter">
						<mvc:option value=".">.</mvc:option>
						<mvc:option value=",">,</mvc:option>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label for="export-decoded-values" class="control-label checkbox-control-label">
                        <mvc:message code="export.decode.values"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="useDecodedValues" id="export-decoded-values"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-datelimits">
                <i class="tile-toggle icon icon-angle-up"></i>
                <mvc:message code="export.dates.limits"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-datelimits">
            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th><mvc:message code="Start"/></th>
                        <th><mvc:message code="report.stopDate"/></th>
                        <th><mvc:message code="lastDays"/></th>
                        <%@ include file="fragments/export-date-limits-currentdate-column.jspf" %>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><mvc:message code="export.dates.timestamp"/></td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="timestampStart" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="timestampEnd" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="timestampLastDaysStr" type="number" cssClass="form-control"
                                              value="${exportForm.timestampLastDays == 0 ? '' : exportForm.timestampLastDays}"/>
                                </div>
                            </div>
                        </td>
                        <c:set var="includeCurentDayInputName" value="timestampIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-curent-day-switch.jspf" %>
                    </tr>
                    <tr>
                        <td><mvc:message code="export.dates.creation_date"/></td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="creationDateStart" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="creationDateEnd" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="creationDateLastDaysStr" type="number" cssClass="form-control"
                                        value="${exportForm.creationDateLastDays == 0 ? '' : exportForm.creationDateLastDays}"/>
                                </div>
                            </div>
                        </td>
                        <c:set var="includeCurentDayInputName" value="creationDateIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-curent-day-switch.jspf" %>
                    </tr>
                    <tr>
                        <td><mvc:message code="export.dates.mailinglists"/>*</td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="mailinglistBindStart" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="mailinglistBindEnd" cssClass="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${localeDatePattern}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <mvc:text path="mailinglistBindLastDaysStr" type="number" cssClass="form-control"
                                              value="${exportForm.mailinglistBindLastDays == 0 ? '' : exportForm.mailinglistBindLastDays}"/>
                                </div>
                            </div>
                        </td>
                        <c:set var="includeCurentDayInputName" value="mailinglistBindIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-curent-day-switch.jspf" %>
                    </tr>
                    <tr>
                        <td>
                            <div class="input-group">
                                <label class="toggle" style="padding-right: 5px">
                                    <mvc:checkbox path="timeLimitsLinkedByAnd"/>
                                    <div class="toggle-control"></div>
                                </label>
                                <mvc:message code="export.timeLimitsLinkedByAnd"/>
                            </div>
                        </td>
                        <td colspan="3">* <mvc:message code="export.dates.mailinglists.hint"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</mvc:form>
