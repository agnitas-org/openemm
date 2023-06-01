<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.*, org.agnitas.web.*, com.agnitas.web.ComImportWizardAction"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<agn:agnForm action="/importwizard">
    <input type="hidden" name="mlists_back" id="mlists_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
            </div>
            <div class="tile-content tile-content-forms">
                <div data-load="<html:rewrite page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_VIEW_STATUS_WINDOW %>'/>" data-load-target="body" data-load-interval="5000"></div>
            </div>
        </div>
    </div>
</agn:agnForm>
<% out.flush(); %>
