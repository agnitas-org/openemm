<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILING_TYPE_DATEBASED" value="<%=MailingType.DATE_BASED.getCode()%>" scope="page"/>

<div id="datebased_mailing-editor" data-initializer="date-based-mailing-editor-initializer">

    <mvc:form action="" id="datebasedMailingForm" name="datebasedMailingForm">
        <jsp:include page="workflow-editor-mailing-select.jsp">
            <jsp:param name="containerId" value="datebased_mailing-editor"/>
            <jsp:param name="selectName" value="mailingId"/>
            <jsp:param name="statusName" value="mailings_status"/>
            <jsp:param name="baseMailingEditor" value="date-mailing-editor-base"/>
            <jsp:param name="status1" value="inactive"/>
            <jsp:param name="status2" value="active"/>
            <jsp:param name="showMailingLinks" value="true"/>
            <jsp:param name="message1" value="autoExport.statusNotActive"/>
            <jsp:param name="message2" value="default.status.active"/>
            <jsp:param name="disabledSelection" value="false"/>
        </jsp:include>
    </mvc:form>

    <script id="config:date-based-mailing-editor-initializer" type="application/json">
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
