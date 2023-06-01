<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@page import="org.agnitas.dao.UserStatus"%>
<%@page import="org.agnitas.service.RecipientExportWorker"%>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="org.agnitas.util.DbColumnType" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ page import="org.agnitas.web.ExportWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="NO_MAILINGLIST" value="<%= RecipientExportWorker.NO_MAILINGLIST %>" scope="page" />

<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.GENERIC_TYPE_VARCHAR %>" scope="page" />
<c:set var="GENERIC_TYPE_INTEGER" value="<%= DbColumnType.GENERIC_TYPE_INTEGER %>" scope="page" />
<c:set var="GENERIC_TYPE_FLOAT" value="<%= DbColumnType.GENERIC_TYPE_FLOAT %>" scope="page" />
<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.GENERIC_TYPE_DATE %>" scope="page" />
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.GENERIC_TYPE_DATETIME %>" scope="page" />
<c:set var="BOUNCE_USER_STATUS_CODE" value="<%= UserStatus.Bounce.getStatusCode() %>" scope="page" />

<%--@elvariable id="exportWizardForm" type="org.agnitas.web.forms.ExportWizardForm"--%>
<c:set var="localeDatePattern" value="${fn:toLowerCase(exportWizardForm.localeDatePattern)}"/>

<agn:agnForm action="/exportwizard" id="exportWizardForm" data-form="resource" data-form-focus="shortname"
             data-controller="exportwizard-view"
             data-initializer="exportwizard-view"
             data-validator="export/form">
    <html:hidden property="action"/>
    <html:hidden property="exportPredefID"/>

    <html:hidden property="recipientFilterVisible"/>
    <html:hidden property="columnsPanelVisible"/>
    <html:hidden property="mlistsPanelVisible"/>
    <html:hidden property="datesPanelVisible"/>
    <html:hidden property="fileFormatPanelVisible"/>

    <emm:ShowByPermission token="export.ownColumns">
        <c:set var="adminHasOwnColumnPermission" value="${true}"/>
    </emm:ShowByPermission>
    <emm:HideByPermission token="export.ownColumns">
        <c:set var="adminHasOwnColumnPermission" value="${false}"/>
    </emm:HideByPermission>
    
    <script id="config:exportwizard-view" type="application/json">
        {
            "adminHasOwnColumnPermission": ${adminHasOwnColumnPermission},
            "bounceUserStatusCode": ${BOUNCE_USER_STATUS_CODE},
            "columnMappings": ${emm:toJson(exportWizardForm.customColumnMappings)}
        }
    </script>
    
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="export.ExportDefinition"/>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-name" class="control-label">
                        <c:set var="nameMsg"><bean:message key="default.Name"/></c:set>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <agn:agnText styleId="recipient-export-name" styleClass="form-control" property="shortname" maxlength="99" placeholder="${nameMsg}"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-description" class="control-label">
                        <c:set var="descriptionMsg"><bean:message key="default.description"/></c:set>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <agn:agnTextarea styleId="recipient-export-description" styleClass="form-control v-resizable" property="description" rows="5" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-selection">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="export.selection"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-selection">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-mailinglist" class="control-label">
                        <bean:message key="Mailinglist"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select styleClass="js-select form-control" styleId="recipient-export-mailinglist" property="mailinglistID" >
                        <html:option value="0" key="default.All" />
                        <html:option value="${NO_MAILINGLIST}" key="No_Mailinglist" />
                        <c:forEach var="mailinglist" items="${exportWizardForm.mailinglistObjects}">
                            <html:option value="${mailinglist.id}">
                                ${mailinglist.shortname}
                            </html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-targetgroup" class="control-label">
                        <bean:message key="target.Target"/>
                    </label>
                </div>
                <div class="col-sm-8">

                    <html:select styleClass="form-control js-select"
                                 styleId="recipient-export-targetgroup"
                                 property="targetID">
                        <html:option value="0" key="default.All" />
                        <c:forEach var="target" items="${exportWizardForm.targetGroups}">
                            <html:option value="${target.id}">
                                ${target.targetName}
                            </html:option>
                        </c:forEach>
                    </html:select>

                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-recipienttype" class="control-label">
                        <bean:message key="recipient.RecipientType"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select styleClass="form-control" styleId="recipient-export-recipienttype" property="userType">
                        <html:option value="<%= RecipientExportWorker.ALL_BINDING_TYPES %>" key="default.All"/>
                        <html:option value="A" key="recipient.Administrator" />
                        <html:option value="T" key="recipient.TestSubscriber" />
                        <%@include file="exportwizard-view-novip-test.jspf" %>
                        <html:option value="W" key="recipient.NormalSubscriber"/>
                        <%@include file="exportwizard-view-novip-normal.jspf" %>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-recipientstatus" class="control-label">
                        <bean:message key="recipient.RecipientStatus"/>
                    </label>
                </div>
                <div class="col-sm-8" data-action="add-bounce-col-to-choose">
                    <html:select styleClass="form-control" styleId="recipient-export-recipientstatus" property="userStatus">
                        <html:option value="0" key="default.All" />
                            <html:option value="1" key="recipient.MailingState1" />
                            <html:option value="2" key="recipient.MailingState2" />
                            <html:option value="3" key="recipient.OptOutAdmin" />
                            <html:option value="4" key="recipient.OptOutUser" />
                            <html:option value="5" key="recipient.MailingState5"/>
                            <emm:ShowByPermission token="blacklist">
                                <html:option value="6" key="recipient.MailingState6"/>
                            </emm:ShowByPermission>
                            <html:option value="7" key="recipient.MailingState7"/>
                    </html:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-columns">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="export.columns"/>
            </a>
        </div>


        <div class="tile-content tile-content-forms" id="recipient-export-tile-columns">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <div class="notification-simple notification-info">
                        <bean:message key="export.wizard.hint.export.columns"/>
                    </div>
                </div>
            </div>                
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="chooseColumns"><bean:message key="export.columns"/></label>
                </div>
                <div id="chooseColumns" class="col-sm-8">
                    <c:set var="allColumnsStr"><bean:message key="export.columns.all"/></c:set>
                    <agn:agnSelect property="columns"
                                   styleClass="form-control js-select"
                                   data-placeholder="${allColumnsStr}"
                                   multiple="true">
                        <c:forEach var="column" items="${availableColumns}">
                            <c:choose>
                                <c:when test='${column.dataType.indexOf(GENERIC_TYPE_VARCHAR) ne -1}'>
                                    <c:set var="colType"><bean:message key='statistic.alphanumeric'/></c:set>
                                </c:when>
                                <c:when test='${column.dataType.toUpperCase().indexOf(GENERIC_TYPE_INTEGER) ne -1}'>
                                    <c:set var="colType"><bean:message key='statistic.numeric'/></c:set>
                                </c:when>
                                <c:when test='${column.dataType.toUpperCase().indexOf(GENERIC_TYPE_FLOAT) ne -1}'>
                                    <c:set var="colType"><bean:message key='statistic.numeric'/></c:set>
                                </c:when>
                                <c:when test='${column.dataType.toUpperCase().indexOf(GENERIC_TYPE_DATETIME) ne -1}'>
                                    <c:set var="colType"><bean:message key='settings.fieldType.DATETIME'/></c:set>
                                </c:when>
                                <c:when test='${column.dataType.toUpperCase().indexOf(GENERIC_TYPE_DATE) ne -1}'>
                                    <c:set var="colType"><bean:message key='settings.fieldType.DATE'/></c:set>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="colType">${column}</c:set>
                                </c:otherwise>
                            </c:choose>
                            
                            <agn:agnOption value="${fn:toLowerCase(column.column)}">${fn:escapeXml(column.shortname)} (${colType})</agn:agnOption>
                        </c:forEach>
                        <agn:agnOption styleId="mailing-bounce-column-option" value="MAILING_BOUNCE"><bean:message key="report.bounce.reason"/> (<bean:message key="statistic.alphanumeric"/>)</agn:agnOption>
                    </agn:agnSelect>
                    ${exportWizardForm.customColumnMappings.clear()}
                </div>
            </div>
            <emm:ShowByPermission token="export.ownColumns">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="columnMappings"><bean:message key="export.columns.add.own"/></label>
                </div>
                <div id="columnMappings" class="col-sm-8">
                    <div class="table-responsive">
                        <table class="table table-bordered table-striped">
                            <thead>
                                <tr>
                                    <th><bean:message key="export.Column_Name"/></th>
                                    <th><bean:message key="default.value.optional"/></th>
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
            </emm:ShowByPermission>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-mailingslists">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="export.mailinglist.status"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-mailingslists">
            <div class="form-group">
 	          <div class="col-sm-4">
                    <label class="control-label">
                        <bean:message key="export.mailinglist.status"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <ul class="list-group">
                        <c:forEach var="mailinglist" items="${exportWizardForm.mailinglistObjects}">
                            <li class="list-group-item">
                                <label class="checkbox-inline">
                                    <html:multibox property="mailinglists" value='${mailinglist.id}'/>
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
                <bean:message key="export.file_format"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-fileformats">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-separator" class="control-label">
                        <bean:message key="import.Separator"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="separator" styleClass="form-control" styleId="recipient-export-format-separator">
                        <html:option value=";">;</html:option>
                        <html:option value=",">,</html:option>
                        <html:option value="|">|</html:option>
                        <html:option value="t">Tab</html:option>
                        <html:option value="^">^</html:option>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="import.Delimiter"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="delimiter" styleClass="form-control" styleId="recipient-export-format-delimiter">
                        <html:option value="&#34;"><bean:message key="delimiter.doublequote"/></html:option>
                        <html:option value="'"><bean:message key="delimiter.singlequote"/></html:option>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="csv.alwaysQuote"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="alwaysQuote" styleClass="form-control" styleId="recipient-export-format-delimiter">
                        <html:option value="0"><bean:message key="delimiter.ifneeded"/></html:option>
                        <html:option value="1"><bean:message key="delimiter.always"/></html:option>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-charset" class="control-label">
                        <bean:message key="mailing.Charset"/>
                    </label>
                </div>
                <div class="col-sm-8">
                	<%@include file="exportwizard-charsets.jspf" %>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="csv.DateFormat"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="dateFormat" styleClass="form-control js-select" styleId="recipient-export-format-delimiter">
						<c:forEach var="availableDateFormat" items="${availableDateFormats}">
							<html:option value="${availableDateFormat.intValue}">${availableDateFormat.publicValue}</html:option>
						</c:forEach>
                    </html:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="csv.DateTimeFormat"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="dateTimeFormat" styleClass="form-control js-select" styleId="recipient-export-format-delimiter">
						<c:forEach var="availableDateTimeFormat" items="${availableDateTimeFormats}">
							<html:option value="${availableDateTimeFormat.intValue}">${availableDateTimeFormat.publicValue}</html:option>
						</c:forEach>
                    </html:select>
                </div>
            </div>
		
			<div class="form-group">
				<div class="col-sm-4">
					<label class="control-label" for="locale"><bean:message key="import.report.locale" /></label>
				</div>
				<div class="col-sm-8">
					<agn:agnSelect property="localeString" styleClass="form-control js-select" styleId="profile">
						<html:option value="de_DE"><bean:message key="settings.German" /></html:option>
						<html:option value="en_US"><bean:message key="settings.English" /></html:option>
						<html:option value="fr_FR"><bean:message key="settings.French" /></html:option>
						<html:option value="es_ES"><bean:message key="settings.Spanish" /></html:option>
						<html:option value="pt_PT"><bean:message key="settings.Portuguese" /></html:option>
						<html:option value="nl_NL"><bean:message key="settings.Dutch" /></html:option>
						<html:option value="it_IT"><bean:message key="settings.Italian" /></html:option>
					</agn:agnSelect>
				</div>
			</div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="Timezone"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="timezone" styleClass="form-control js-select" styleId="recipient-export-format-delimiter">
						<c:forEach var="availableTimeZone" items="${availableTimeZones}">
							<html:option value="${availableTimeZone}">${availableTimeZone}</html:option>
						</c:forEach>
                    </html:select>
                </div>
            </div>
            
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-format-delimiter" class="control-label">
                        <bean:message key="csv.DecimalSeparator"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:select property="decimalSeparator" styleClass="form-control" styleId="recipient-export-format-delimiter">
						<html:option value=".">.</html:option>
						<html:option value=",">,</html:option>
                    </html:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#recipient-export-tile-datelimits">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="export.dates.limits"/>
            </a>
        </div>
        <div class="tile-content tile-content-forms" id="recipient-export-tile-datelimits">


            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <th></th>
                        <th><bean:message key="Start"/></th>
                        <th><bean:message key="report.stopDate"/></th>
                        <th><bean:message key="lastDays"/></th>
                        <th><bean:message key="export.dates.includeCurrentDay"/></th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td><bean:message key="export.dates.timestamp"/></td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="timestampStart" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="timestampEnd" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control" property="timestampLastDays"/>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnCheckbox property="timestampIncludeCurrentDay"/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td><bean:message key="export.dates.creation_date"/></td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="creationDateStart" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="creationDateEnd" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control" property="creationDateLastDays"/>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnCheckbox property="creationDateIncludeCurrentDay"/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td><bean:message key="export.dates.mailinglists"/>*</td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="mailinglistBindStart" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control datepicker-input js-datepicker" property="mailinglistBindEnd" data-datepicker-options="format: '${localeDatePattern}'"/>
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
                                    <agn:agnText styleClass="form-control" property="mailinglistBindLastDays"/>
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <agn:agnCheckbox property="mailinglistBindIncludeCurrentDay"/>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td><agn:agnCheckbox property="timeLimitsLinkedByAnd"/> <bean:message key="export.timeLimitsLinkedByAnd"/></td>
                        <td colspan="3">* <bean:message key="export.dates.mailinglists.hint"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <script type="text/javascript">
        updateBouncesRowVisibility();

        $('#recipient-export-recipientstatus').change(updateBouncesRowVisibility);

        function updateBouncesRowVisibility() {
            var selectedRecipientStatus = $('#recipient-export-recipientstatus').find(':selected').val();
            if (selectedRecipientStatus == <%= UserStatus.Bounce.getStatusCode() %>) {
                $('#bounce-export-row').show();
            } else {
                $('#bounce-export-row').hide();
            }
        }
    </script>

</agn:agnForm>
