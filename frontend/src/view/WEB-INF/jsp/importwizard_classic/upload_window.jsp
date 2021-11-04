<%@page import="org.agnitas.util.importvalues.ImportModeUpdateHandler"%>
<%@page import="org.agnitas.util.importvalues.ImportModeAddAndUpdateHandler"%>
<%@page import="org.agnitas.util.importvalues.ImportModeAddHandler"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.*, org.agnitas.web.*, com.agnitas.web.*, java.util.*, org.agnitas.beans.*, com.agnitas.beans.*, org.agnitas.util.importvalues.ImportMode"
         errorPage="/error.do" %>
<%@page import="com.agnitas.beans.ComAdmin"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<%@include file="/WEB-INF/jsp/messages.jsp" %>

<emm:Permission token="wizard.importclassic"/>

<% ComImportWizardForm aForm = (ComImportWizardForm) session.getAttribute("importWizardForm");
    int tmpInserted = aForm.getStatus().getInserted();
    int tmpUpdated = aForm.getStatus().getUpdated();
%>

<% // map for the csv download:
    String csvfile = "";
    EmmCalendar my_calendar = new EmmCalendar(TimeZone.getDefault());
    TimeZone zone = TimeZone.getTimeZone(((ComAdmin) session.getAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN)).getAdminTimezone());

    my_calendar.changeTimeWithZone(zone);
    Date my_time = my_calendar.getTime();
    String Datum = my_time.toString();
    String timekey = Long.toString(my_time.getTime());
    pageContext.setAttribute("time_key", timekey);
    Hashtable<String, String> my_map = null;
    if (pageContext.getSession().getAttribute("map") == null) {
        my_map = new Hashtable<>();
        pageContext.getSession().setAttribute("map", my_map);
    } else {
        my_map = (Hashtable<String, String>) pageContext.getSession().getAttribute("map");
    }
    // fill up csv file
    csvfile += SafeString.getLocaleString("import.SubscriberImport", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY));
    csvfile += "\n" + SafeString.getLocaleString("Date", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ": ; \"" + my_time + "\"\n";
%>
<html>
<head></head>
<body>

<logic:equal name="importWizardForm" property="futureIsRunning" value="false" scope="session">
    <div class="hidden" data-load-stop="true"></div>
</logic:equal>

<div class="inline-tile">
	<c:if test="${importIsDone}">
	    <div class="inline-tile-header">
	        <h2 class="headline"><bean:message key="ResultMsg"/></h2>
	        <ul class="inline-tile-header-actions">
	            <li>
	                <p>
	                    <html:link styleClass="btn btn-regular btn-primary" page='<%= "/file_download?key=" + timekey %>'>
	                        <i class="icon icon-download"></i>
	                        <bean:message key="button.Download"/>
	                    </html:link>
	                </p>
	            </li>
	        </ul>
	    </div>
    </c:if>
    <div class="tile-separator"></div>
    <div class="inline-tile-content">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="import.csv_importing_data"/></label>
            </div>
            <c:if test="${not empty importError}">
	            <div class="col-sm-8">
					<ul class="list-group">
						<li class="list-group-item" style="background-color:#DF3939; color:#FFFFFF">${importError}</li>
					</ul>
	            </div>
            </c:if>
            <c:if test="${empty importError}">
	            <div class="col-sm-8">
	                <ul class="list-group">
	                    <logic:greaterThan name="importWizardForm" property="dbInsertStatus" value="100" scope="session">
	                        <logic:iterate name="importWizardForm" property="dbInsertStatusMessagesCopy" scope="session" id="aMsg" type="java.lang.String">
	                            <li class="list-group-item"><bean:message key='<%= (String)pageContext.getAttribute("aMsg") %>'/></li>
	                        </logic:iterate>
	                    </logic:greaterThan>
	                </ul>
	            </div>
            </c:if>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <ul class="list-group">
                    <logic:greaterEqual name="importWizardForm" property="dbInsertStatus" value="1000" scope="session">
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(email)" scope="session"/></span>
                            <bean:message key="import.csv_errors_email"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_email", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("email"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(blacklist)" scope="session"/></span>
                            <bean:message key="import.csv_errors_blacklist"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_blacklist", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("blacklist"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(keyDouble)" scope="session"/></span>
                            <bean:message key="import.csv_errors_double"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_double", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("keyDouble"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(numeric)" scope="session"/></span>
                            <bean:message key="import.csv_errors_numeric"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_numeric", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("numeric"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(mailtype)" scope="session"/></span>
                            <bean:message key="import.csv_errors_mailtype"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_mailtype", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("mailtype"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(gender)" scope="session"/></span>
                            <bean:message key="import.csv_errors_gender"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_gender", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("gender"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(date)" scope="session"/></span>
                            <bean:message key="import.csv_errors_date"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_date", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("date"); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(structure)" scope="session"/></span>
                            <bean:message key="csv_errors_linestructure"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("csv_errors_linestructure", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("structure"); %>
                        
    					<li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.invalidNullValues" scope="session"/></span>
                            <bean:message key="import.csv_errors_invalidNullValues"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.csv_errors_invalidNullValues", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getInvalidNullValues(); %>
                        
    					<li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(valueTooLarge)" scope="session"/></span>
                            <bean:message key="error.import.value.large"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("error.import.value.large", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("valueTooLarge"); %>
                        
    					<li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(numberTooLarge)" scope="session"/></span>
                            <bean:message key="error.import.number.large"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("error.import.number.large", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("numberTooLarge"); %>
                        
    					<li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(invalidFormat)" scope="session"/></span>
                            <bean:message key="error.import.invalidFormat"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("error.import.invalidFormat", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("invalidFormat"); %>
                        
    					<li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.error(missingMandatory)" scope="session"/></span>
                            <bean:message key="error.import.missingMandatory"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("error.import.missingMandatory", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getError("missingMandatory"); %>
                        
                        <% if (aForm.getStatus().getErrorColumns().size() > 0) { %>
	                        <li class="list-group-item">
	                            <span class="badge"><%= StringUtils.join(aForm.getStatus().getErrorColumns(), ", ") %></span>
	                            <bean:message key="error.import.errorColumns"/>
	                        </li>
	                        <% csvfile += "\n" + SafeString.getLocaleString("error.import.errorColumns", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + StringUtils.join(aForm.getStatus().getErrorColumns(), ", "); %>
                        <% } %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.csvLines" scope="session"/></span>
                            <bean:message key="import.result.filedataitems"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.result.filedataitems", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getCsvLines(); %>
                        
                        <li class="list-group-item">
                            <span class="badge"><bean:write name="importWizardForm" property="status.alreadyInDb" scope="session"/></span>
                            <bean:message key="import.RecipientsAllreadyinDB"/>
                        </li>
                        <% csvfile += "\n" + SafeString.getLocaleString("import.RecipientsAllreadyinDB", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getAlreadyInDb(); %>

                        <% if (aForm.getMode() == ImportMode.ADD.getIntValue() || aForm.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) { %>
                            <li class="list-group-item">
                                <span class="badge"><bean:write name="importWizardForm" property="status.inserted" scope="session"/></span>
                                <bean:message key="import.result.imported"/>
                            </li>
                            <% csvfile += "\n" + SafeString.getLocaleString("import.result.imported", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + tmpInserted;
                        } %>
                        
                        <% if (aForm.getMode() == ImportMode.UPDATE.getIntValue() || aForm.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) { %>
                            <li class="list-group-item">
                                <span class="badge"><bean:write name="importWizardForm" property="status.updated" scope="session"/></span>
                                <bean:message key="import.result.updated"/>
                            </li>
                            <% csvfile += "\n" + SafeString.getLocaleString("import.result.updated", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + tmpUpdated;
                        } %>
                        
                        <% if (aForm.getMode() == ImportMode.TO_BLACKLIST.getIntValue()) { %>
                            <li class="list-group-item">
                                <span class="badge"><bean:write name="importWizardForm" property="status.blacklisted" scope="session"/></span>
                                <bean:message key="import.result.blacklisted"/>
                            </li>
                            <% csvfile += "\n" + SafeString.getLocaleString("import.result.blacklisted", (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)) + ":;" + aForm.getStatus().getBlacklisted();
                        } %>
                        
                        <c:forEach var="entry" items="${mailinglists}">
                            <c:set var="mailinglistID" value="${entry.key}"/>
                            <c:set var="mailinglist" value="${entry.value}"/>
                            <li class="list-group-item">
                                <span class="badge">${resultMLAdded[mailinglistID]}</span>
                                ${mailinglist.shortname}
                                <%
                                	ImportMode importMode = ImportMode.getFromInt(aForm.getMode());
                                	if (importMode == ImportMode.ADD
                               			|| importMode == ImportMode.ADD_AND_UPDATE
                               			|| importMode == ImportMode.UPDATE) {
                                %>
                                <bean:message key="import.result.subscribersAdded"/>
                                <%
                                	} else if (importMode == ImportMode.MARK_OPT_OUT) {
                                %>
                                <bean:message key="import.result.subscribersUnsubscribed"/>
                                <%
                                	} else if (importMode == ImportMode.MARK_BOUNCED) {
                                %>
                                <bean:message key="import.result.subscribersBounced"/>
                                <%
                                	} else if (importMode == ImportMode.MARK_SUSPENDED) {
                                %>
                                <bean:message key="import.result.bindingsRemoved"/>
                                <%
                                	} else if (importMode == ImportMode.REACTIVATE_SUSPENDED) {
                                %>
                                <bean:message key="import.result.subscribersReactivated"/>
                                <%
                                	}
                                %>
                            </li>
                        </c:forEach>
                        <% if (aForm.getMode() == ImportMode.ADD.getIntValue() || aForm.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) { %>
                            <li class="list-group-item">
                                <span class="badge"><bean:write name="importWizardForm" property="datasourceID" scope="session"/></span>
                                <bean:message key="import.result.datasourceId"/>
                            </li>
                        <% } %>
                        <%
                            String modeString = SafeString.getLocaleString(ImportMode.getFromInt(aForm.getMode()).getMessageKey(), (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY));
                            csvfile += "\n" + "mode:;" + modeString;
                        %>
                    </logic:greaterEqual>
                </ul>
            </div>
        </div>
            
		<c:if test="${importIsDone}">
			<div class="tile-footer">
                <emm:HideByPermission token="recipient.rollback">
                    <c:url var="recipientUrl" value="/recipient/list.action">
                        <c:param name="latestDataSourceId" value="${aForm.datasourceID}"/>
                    </c:url>
                    <a href="${recipientUrl}" class="btn btn-large btn-primary pull-right">
                        <span><bean:message key="button.Finish"/></span>
                    </a>
                </emm:HideByPermission>
                <emm:ShowByPermission token="recipient.rollback">
                    <html:link page='<%= "/recipient.do?action=" + RecipientAction.ACTION_LIST + "&latestDataSourceId=" + aForm.getDatasourceID() %>' styleClass="btn btn-large btn-primary pull-right">
                        <span><bean:message key="button.Finish"/></span>
                    </html:link>
                </emm:ShowByPermission>
				<span class="clearfix"></span>
			</div>
		</c:if>
	</div>
</div>

<% // put csv file from the form in the hash table:
    my_map.put(timekey, csvfile);
    pageContext.getSession().setAttribute("map", my_map);
%>
</body>
</html>
