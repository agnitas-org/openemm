<%@ page language="java"
         import="org.agnitas.web.ExportWizardAction"
         contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="ACTION_VIEW" value="<%= ExportWizardAction.ACTION_VIEW %>"/>
<c:set var="ACTION_VIEW_STATUS_WINDOW" value="<%= ExportWizardAction.ACTION_VIEW_STATUS_WINDOW %>"/>

<c:url var="statusWindowLink" value="/exportwizard.do">
    <c:param name="action" value="${ACTION_VIEW_STATUS_WINDOW}"/>
</c:url>

<agn:agnForm action="/exportwizard" id="exportWizardForm" data-form="resource">
    <html:hidden styleId="action" property="action" value="${ACTION_VIEW}"/>
    <html:hidden property="exportPredefID"/>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="export.Wizard"/>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div data-load="${statusWindowLink}" data-load-target="body">
                <bean:message key="import.csv_no_iframe"/>
            </div>
        </div>
    </div>
</agn:agnForm>

<!-- implementation unclear begin -->
<% out.flush(); %>
<!-- implementation unclear end -->
