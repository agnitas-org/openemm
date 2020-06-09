<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.core.report.enums.fields.MailingTypes" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>

<c:set var="MAILING_TYPE_ACTIONBASED" value="<%=MailingTypes.ACTION_BASED.getCode()%>" scope="page"/>

<div id="actionbased_mailing-editor" data-initializer="action-mailing-editor-initializer">

    <div class="status_error editor-error-messages well" style="display: none;"></div>

        <form action="" id="actionbasedMailingForm" name="actionbasedMailingForm">
            <jsp:include page="sort-select-mailing.jsp">
                <jsp:param name="containerId" value="actionbased_mailing-editor"/>
                <jsp:param name="selectName" value="mailingId"/>
                <jsp:param name="statusName" value="mailings_status"/>
                <jsp:param name="baseMailingEditor" value="action-mailing-editor-base"/>
                <jsp:param name="status1" value="inactive"/>
                <jsp:param name="status2" value="active"/>
                <jsp:param name="sortByDate" value="active_sort_status asc, active_sort_date"/>
                <jsp:param name="showMailingLinks" value="true"/>
                <jsp:param name="message1" value="autoExport.statusNotActive"/>
                <jsp:param name="message2" value="default.status.active"/>
                <jsp:param name="disabledSelection" value="false"/>
            </jsp:include>
        </form>

        <hr>

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <bean:message key="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="action-mailing-editor-save">
                        <bean:message key="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>

    <jsp:include page="transfer-dialog.jsp">
        <jsp:param name="containerId" value="actionbased_mailing-editor"/>
        <jsp:param name="baseMailingEditor" value="action-mailing-editor-base"/>
    </jsp:include>

<script id="action-mailing-editor-data" type="application/json">
    {
        "form":"actionbasedMailingForm",
        "container": "#actionbased_mailing-editor",
        "mailingType": "${MAILING_TYPE_ACTIONBASED}",
        "selectName": "mailingId",
        "mailingStatus": "mailings_status",
        "showCreateEditLinks": "true",
        "mailingTypesForLoading": ["${MAILING_TYPE_ACTIONBASED}"],
        "defaultMailingsSort": "active_sort_status asc, shortname",
        "defaultMailingsOrder": "asc"
    }
</script>
</div>
