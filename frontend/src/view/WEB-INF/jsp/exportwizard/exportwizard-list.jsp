<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ExportWizardAction"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<c:set var="ACTION_LIST" value="<%= ExportWizardAction.ACTION_LIST %>" />
<c:set var="ACTION_VIEW" value="<%= ExportWizardAction.ACTION_VIEW %>" />
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ExportWizardAction.ACTION_CONFIRM_DELETE %>" />

<agn:agnForm action="/exportwizard">
    <html:hidden property="action"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="export.Wizard"/>
            </h2>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="index" value="0" scope="request"/>

                <%--@elvariable id="exportWizardForm" type="org.agnitas.web.forms.ExportWizardForm"--%>
                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        pagesize="20"
                        id="exportwizard"
                        list="${exportWizardForm.exportPredefList}"
                        requestURI="/exportwizard.do?action=${ACTION_LIST}"
                        excludedParams="*"
                        length="${exportWizardForm.exportPredefCount}">

                    <display:column headerClass="js-table-sort" sortable="true" titleKey="default.Name"
                                    property="shortname"/>
                    <display:column headerClass="js-table-sort" sortable="true" titleKey="default.description"
                                    property="description"/>

                    <display:column class="table-actions">
                        <html:link styleClass="hidden js-row-show" titleKey="export.ExportEdit"
                                   page="/exportwizard.do?action=${ACTION_VIEW}&exportPredefID=${exportwizard.id}"/>

                        <c:set var="exportDeleteMessage" scope="page">
                            <bean:message key="export.ExportDelete"/>
                        </c:set>
                        <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                     data-tooltip="${exportDeleteMessage}"
                                     page="/exportwizard.do?action=${ACTION_CONFIRM_DELETE}&exportPredefID=${exportwizard.id}">
                            <i class="icon icon-trash-o"></i>
                        </agn:agnLink>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</agn:agnForm>
