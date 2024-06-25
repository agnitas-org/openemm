<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="FORWARD_MAILING_CREATE" value="<%= WorkflowController.FORWARD_MAILING_CREATE%>" scope="page"/>
<c:set var="FORWARD_MAILING_EDIT" value="<%= WorkflowController.FORWARD_MAILING_EDIT%>" scope="page"/>
<c:set var="FORWARD_MAILING_COPY" value="<%= WorkflowController.FORWARD_MAILING_COPY%>" scope="page"/>
<c:set var="MAILING_TYPE_DATEBASED" value="<%=MailingType.DATE_BASED.getCode()%>" scope="page"/>

<div id="datebased_mailing-editor" data-initializer="date-mailing-initializer">

    <div class="status_error editor-error-messages well" style="display: none;"></div>

    <mvc:form action="" id="datebasedMailingForm" name="datebasedMailingForm">
        <jsp:include page="sort-select-mailing.jsp">
            <jsp:param name="containerId" value="datebased_mailing-editor"/>
            <jsp:param name="selectName" value="mailingId"/>
            <jsp:param name="statusName" value="mailings_status"/>
            <jsp:param name="baseMailingEditor" value="date-mailing-editor-base"/>
            <jsp:param name="status1" value="inactive"/>
            <jsp:param name="status2" value="active"/>
            <jsp:param name="sortByDate" value="active_sort_status asc, active_sort_date"/>
            <jsp:param name="showMailingLinks" value="true"/>
            <jsp:param name="message1" value="autoExport.statusNotActive"/>
            <jsp:param name="message2" value="default.status.active"/>
            <jsp:param name="disabledSelection" value="false"/>
        </jsp:include>
    </mvc:form>

    <hr>
    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular" data-action="editor-cancel">
                    <mvc:message code="button.Cancel"/>
                </a>
                <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="date-mailing-editor-save">
                    <mvc:message code="button.Apply"/>
                </a>
            </div>
        </div>
    </div>

    <script id="config:date-mailing-initializer" type="application/json">
        {
            "form":"datebasedMailingForm",
            "container": "#datebased_mailing-editor",
            "mailingType": "${MAILING_TYPE_DATEBASED}",
            "selectName": "mailingId",
            "mailingStatus": "mailings_status",
            "showCreateEditLinks": ${not emm:permissionAllowed('mailing.content.readonly', pageContext.request)},
            "mailingTypesForLoading": ["${MAILING_TYPE_DATEBASED}"],
            "defaultMailingsSort": "active_sort_status asc, shortname",
            "defaultMailingsOrder": "asc"
        }
    </script>

</div>
