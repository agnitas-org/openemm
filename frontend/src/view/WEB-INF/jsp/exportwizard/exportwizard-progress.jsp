<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.AgnUtils, org.agnitas.web.ExportWizardAction, org.agnitas.web.forms.ExportWizardForm"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    ExportWizardForm form = (ExportWizardForm) session.getAttribute("exportWizardForm");
    int barNetto = form.getDbExportStatus();
%>

<logic:equal value="false" name="exportWizardForm" property="error">
    <c:set var="refreshMillis">${exportWizardForm.refreshMillis}</c:set>

    <agn:agnForm action="/exportwizard" data-form="loading" data-polling-interval="${refreshMillis}">
        <html:hidden property="action"/>
        <html:hidden property="error"/>

        <div class="msg-tile msg-tile-primary">
            <div class="msg-tile-header">
                <img alt="" src="assets/core/images/facelift/msgs_msg-uploading.svg" onerror="this.onerror=null; this.src='assets/core/images/facelift/msgs_msg-uploading.png'">
            </div>
            <div class="msg-tile-content">
                <h3><bean:message key="export.data"/></h3>
                <div class="progress">
                    <div class="progress-bar" role="progressbar" aria-valuenow="<%= barNetto %>" aria-valuemin="0" aria-valuemax="100" style="width: <%= barNetto %>%">
                        <bean:message key="export.csv_exporting_data"/> ${exportWizardForm.dbExportStatus}%
                    </div>
                </div>
            </div>
        </div>
    </agn:agnForm>
</logic:equal>
