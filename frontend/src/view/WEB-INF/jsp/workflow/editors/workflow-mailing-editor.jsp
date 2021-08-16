<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.report.enums.fields.MailingTypes" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="FORWARD_MAILING_CREATE" value="<%= WorkflowController.FORWARD_MAILING_CREATE%>" scope="page"/>
<c:set var="FORWARD_MAILING_EDIT" value="<%= WorkflowController.FORWARD_MAILING_EDIT%>" scope="page"/>
<c:set var="FORWARD_MAILING_COPY" value="<%= WorkflowController.FORWARD_MAILING_COPY%>" scope="page"/>
<c:set var="MAILING_TYPE_NORMAL" value="<%=MailingTypes.NORMAL.getCode()%>" scope="page"/>
<c:set var="selectName" value="mailingId" scope="page"/>

<div id="mailing-editor" data-initializer="mailing-editor-initializer">

    <div class="status_error editor-error-messages well" style="display: none;"></div>

    <form action="" id="mailingForm" name="mailingForm">
        <jsp:include page="sort-select-mailing.jsp">
            <jsp:param name="containerId" value="mailing-editor"/>
            <jsp:param name="selectName" value="${selectName}"/>
            <jsp:param name="statusName" value="mailings_status"/>
            <jsp:param name="baseMailingEditor" value="mailing-editor-base"/>
            <jsp:param name="status1" value="unsent"/>
            <jsp:param name="status2" value="sent_scheduled"/>
            <jsp:param name="sortByDate" value="sent_sort_status asc, sent_sort_date"/>
            <jsp:param name="showMailingLinks" value="true"/>
            <jsp:param name="message1" value="mailing.label.unsent"/>
            <jsp:param name="message2" value="mailing.label.sent_scheduled"/>
            <jsp:param name="disabledSelection" value="false"/>
        </jsp:include>

        <jsp:include page="workflow-mailing-delivery-settings.jsp">
            <jsp:param name="editorId" value="normalMailingEditor"/>
        </jsp:include>

    </form>

    <hr>

    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular" data-action="editor-cancel">
                    <bean:message key="button.Cancel"/>
                </a>
                <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="mailing-editor-save">
                    <bean:message key="button.Apply"/>
                </a>
            </div>
        </div>
    </div>

    <jsp:include page="transfer-dialog.jsp">
        <jsp:param name="containerId" value="mailing-editor"/>
        <jsp:param name="baseMailingEditor" value="mailing-editor-base"/>
    </jsp:include>
    <jsp:include page="oneMailinglistWarning-dialog.jsp">
        <jsp:param name="containerId" value="mailing-editor"/>
        <jsp:param name="baseMailingEditor" value="mailing-editor-base"/>
    </jsp:include>

    <script id="config:mailing-editor-initializer" type="application/json">
      {
        "form":"mailingForm",
        "container": "#mailing-editor",
        "mailingType": "${MAILING_TYPE_NORMAL}",
        "selectName": "${selectName}",
        "mailingStatus": "mailings_status",
        "showCreateEditLinks": "true",
        "mailingTypesForLoading": ["${MAILING_TYPE_NORMAL}"],
        "defaultMailingsSort": "sent_sort_status asc, sent_sort_date",
        "defaultMailingsOrder": "desc"
      }
    </script>
</div>
