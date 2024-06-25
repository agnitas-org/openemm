<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.preview.service.MailingWebPreviewService" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="errorReport" type="java.util.List<java.lang.String[]>"--%>

<c:set var="TEMPLATE" value="<%= MailingWebPreviewService.TEMPLATE %>" scope="page"/>
<c:set var="FROM" value="<%= MailingWebPreviewService.FROM %>" scope="page"/>
<c:set var="SUBJECT" value="<%= MailingWebPreviewService.SUBJECT %>" scope="page"/>
<emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <c:url var="displayTagUrl" value="/styles/displaytag.css"/>
    <c:url var="tooltiphelpUrl" value="/styles/tooltiphelp.css"/>
    <c:url var="reportstylesUrl" value="/styles/reportstyles.css"/>

    <link type="text/css" rel="stylesheet" href="${absoluteImagePath}/stylesheet.css">
    <link type="text/css" rel="stylesheet" href="${displayTagUrl}">
    <link type="text/css" rel="stylesheet" href="${tooltiphelpUrl}">
    <link type="text/css" rel="stylesheet" href="${reportstylesUrl}">
</head>
<body>
<emm:messagesPresent type="error">
    <div style="padding: 10px">
        <div class="error_box">
            <emm:messages var="msg_key" type="error">
                <span class="error_message">${msg_key}</span><br/>
            </emm:messages>

            <c:if test="${not empty errorReport}">
                <display:table name="errorReport" id="reportRow" class="errorTable">
                    <%--@elvariable id="reportRow" type="java.lang.String[]"--%>

                    <display:column headerClass="head_name" class="name" sortable="false" titleKey="Text_Module" group="1">
                        <c:choose>
                            <c:when test="${reportRow[0] eq TEMPLATE}">
                                <mvc:message code="Template"/>
                            </c:when>
                            <c:when test="${reportRow[0] eq FROM}">
                                <mvc:message code="ecs.From"/>
                            </c:when>
                            <c:when test="${reportRow[0] eq SUBJECT}">
                                <mvc:message code="mailing.Subject"/>
                            </c:when>
                            <c:otherwise>
                                ${reportRow[0]}
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="head_name" class="name" sortable="false" titleKey="mailing.tag">
                        <c:choose>
                            <c:when test="${not empty reportRow[1]}">
                                ${reportRow[1]}
                            </c:when>
                            <c:otherwise>
                                ${reportRow[2]}
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                </display:table>
            </c:if>
        </div>
    </div>
</emm:messagesPresent>

</body>
</html>
