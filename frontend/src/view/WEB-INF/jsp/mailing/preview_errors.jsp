<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<%--@elvariable id="errorReport" type="java.util.List<java.lang.String[]>"--%>

<c:set var="TEMPLATE" value="<%= MailingSendAction.TEMPLATE %>" scope="page"/>
<c:set var="FROM" value="<%= MailingSendAction.FROM %>" scope="page"/>
<c:set var="SUBJECT" value="<%= MailingSendAction.SUBJECT %>" scope="page"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <link type="text/css" rel="stylesheet" href="${emmLayoutBase.imagesURL}/stylesheet.css">
    <link type="text/css" rel="stylesheet" href="styles/displaytag.css">
    <link type="text/css" rel="stylesheet" href="styles/tooltiphelp.css">
    <link type="text/css" rel="stylesheet" href="styles/reportstyles.css">
</head>
<body>
<logic:messagesPresent>
    <div style="padding: 10px">
        <div class="error_box">
            <html:messages id="msg_key" message="false">
                <span class="error_message"><bean:write name="msg_key"/></span><br/>
            </html:messages>

            <c:if test="${not empty errorReport}">
                <display:table name="errorReport" id="reportRow" class="errorTable">
                    <display:column headerClass="head_name" class="name" sortable="false" titleKey="Text_Module" group="1">
                        <c:choose>
                            <c:when test="${reportRow[0] eq TEMPLATE}">
                                <bean:message key="Template"/>
                            </c:when>
                            <c:when test="${reportRow[0] eq FROM}">
                                <bean:message key="ecs.From"/>
                            </c:when>
                            <c:when test="${reportRow[0] eq SUBJECT}">
                                <bean:message key="mailing.Subject"/>
                            </c:when>
                            <c:otherwise>
                                ${reportRow[0]}
                            </c:otherwise>
                        </c:choose>
                    </display:column>
                    <display:column headerClass="head_name" class="name" sortable="false" titleKey="tag">
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
</logic:messagesPresent>

</body>
</html>
