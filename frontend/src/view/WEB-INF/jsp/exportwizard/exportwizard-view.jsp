<%@page import="org.agnitas.dao.UserStatus"%>
<%@page import="org.agnitas.service.RecipientExportWorker"%>
<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.AgnUtils, org.agnitas.web.ExportWizardAction"  errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="NO_MAILINGLIST" value="<%= RecipientExportWorker.NO_MAILINGLIST %>" scope="page" />

<%--@elvariable id="exportWizardForm" type="org.agnitas.web.forms.ExportWizardForm"--%>
<c:set var="localeDatePattern" value="${fn:toLowerCase(exportWizardForm.localeDatePattern)}"/>

<agn:agnForm action="/exportwizard" id="exportWizardForm" data-form="resource" data-form-focus="shortname">
    <html:hidden property="action"/>
    <html:hidden property="exportPredefID"/>

    <html:hidden property="recipientFilterVisible"/>
    <html:hidden property="columnsPanelVisible"/>
    <html:hidden property="mlistsPanelVisible"/>
    <html:hidden property="datesPanelVisible"/>
    <html:hidden property="fileFormatPanelVisible"/>

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
                        <bean:message key="default.Name"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:text styleId="recipient-export-name" styleClass="form-control" property="shortname" maxlength="99" />
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="recipient-export-description" class="control-label">
                        <bean:message key="default.description"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <html:textarea styleId="recipient-export-description" styleClass="form-control v-resizable" property="description" rows="5" />
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

                    <html:select styleClass="form-control js-select" styleId="recipient-export-targetgroup" property="targetID">
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
                <div class="col-sm-8">
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
            <div class="tile">
                <div class="tile-notification tile-notification-info">
                    <bean:message key="export.wizard.hint.export.columns" />
                </div>
            </div>
            <div class="table-responsive">
                <table class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th width="30">
                                <input type="checkbox" data-form-bulk='columns' />
                            </th>
                            <th><bean:message key="export.Column_Name"/></th>
                            <th><bean:message key="default.Type"/></th>
                        </tr>
                    </thead>

                    <tbody>
                        <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>">
                            <c:set var="colName" value='<%=pageContext.getAttribute("_agnTbl_column_name")%>'/>
                            <c:set var="colType" value='<%=pageContext.getAttribute("_agnTbl_data_type")%>'/>
                            <c:set var="shortName" value='<%=pageContext.getAttribute("_agnTbl_shortname")%>'/>
                            <tr>
                                <td>
                                    <html:multibox property="columns" value="${colName}" styleId="checkbox-${colName}"/>
                                </td>
                                <td>
                                    <label for="checkbox-${colName}">
                                    ${shortName}
                                    </label>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test='${colType.toUpperCase().indexOf("CHAR") ne -1}'>
                                            <bean:message key="statistic.alphanumeric"/>
                                        </c:when>
                                        <c:when test='${colType.toUpperCase().indexOf("NUMBER") ne -1}'>
                                            <bean:message key="statistic.numeric"/>
                                        </c:when>
                                        <c:when test='${colType.toUpperCase().indexOf("DOUBLE") ne -1}'>
                                            <bean:message key="statistic.numeric"/>
                                        </c:when>
                                        <c:when test='${colType.toUpperCase().indexOf("TIME") ne -1}'>
                                            <bean:message key="settings.fieldType.DATE"/>
                                        </c:when>
                                        <c:when test='${colType.toUpperCase().indexOf("DATE") ne -1}'>
                                            <bean:message key="settings.fieldType.DATE"/>
                                        </c:when>
                                        <c:otherwise>
                                            ${colType}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </emm:ShowColumnInfo>
                        <c:set var="bounceColumn" value="mailing_bounce"/>
                        <tr id="bounce-export-row">
                            <td>
                                <html:multibox property="columns" value="MAILING_BOUNCE" styleId="checkbox-${bounceColumn}"/>
                            </td>
                            <td>
                                <label for="checkbox-${bounceColumn}">
                                    <bean:message key="report.bounce.reason"/>
                                </label>
                            </td>
                            <td>
                                <bean:message key="statistic.alphanumeric"/>
                            </td>
                        </tr>
                    </tbody>

                </table>
            </div>

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
                    </tr>
                    <tr>
                        <td></td>
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
