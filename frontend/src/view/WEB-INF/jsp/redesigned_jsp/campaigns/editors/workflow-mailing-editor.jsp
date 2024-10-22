<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="MAILING_TYPE_NORMAL" value="<%=MailingType.NORMAL.getCode()%>" scope="page"/>
<c:set var="selectName" value="mailingId" scope="page"/>

<div id="mailing-editor" data-initializer="mailing-editor-initializer">
    <script id="config:mailing-editor-initializer" type="application/json">
      {
        "form":"mailingForm",
        "container": "#mailing-editor",
        "mailingType": "${MAILING_TYPE_NORMAL}",
        "selectName": "${selectName}",
        "mailingStatus": "mailings_status",
        "showCreateEditLinks": ${not emm:permissionAllowed('mailing.content.readonly', pageContext.request)},
        "mailingTypesForLoading": ["${MAILING_TYPE_NORMAL}"],
        "defaultMailingsSort": "sent_sort_status asc, sent_sort_date",
        "defaultMailingsOrder": "desc"
      }
    </script>
    
    <mvc:form action="" id="mailingForm" name="mailingForm">
        <jsp:include page="workflow-editor-mailing-select.jsp">
            <jsp:param name="containerId" value="mailing-editor"/>
            <jsp:param name="selectName" value="${selectName}"/>
            <jsp:param name="statusName" value="mailings_status"/>
            <jsp:param name="baseMailingEditor" value="mailing-editor-base"/>
            <jsp:param name="status1" value="unsent"/>
            <jsp:param name="status2" value="sent_scheduled"/>
            <jsp:param name="showMailingLinks" value="true"/>
            <jsp:param name="message1" value="mailing.label.unsent"/>
            <jsp:param name="message2" value="mailing.label.sent_scheduled"/>
            <jsp:param name="disabledSelection" value="false"/>
        </jsp:include>

        <jsp:include page="workflow-mailing-delivery-settings.jsp">
            <jsp:param name="editorId" value="normalMailingEditor"/>
        </jsp:include>
    </mvc:form>
</div>
