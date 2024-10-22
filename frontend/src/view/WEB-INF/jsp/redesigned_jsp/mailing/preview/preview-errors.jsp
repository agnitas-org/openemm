<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.preview.service.MailingWebPreviewService" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="errorReport" type="java.util.List<java.lang.String[]>"--%>

<c:set var="TEMPLATE" value="<%= MailingWebPreviewService.TEMPLATE %>" scope="page"/>
<c:set var="FROM" value="<%= MailingWebPreviewService.FROM %>" scope="page"/>
<c:set var="SUBJECT" value="<%= MailingWebPreviewService.SUBJECT %>" scope="page"/>

<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <jsp:include page="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp"/>
</head>
<body class="flex-center ${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE ? 'dark-theme' : ''}">
<emm:messagesPresent type="error">

    <div class="tile tile--xs tile--alert flex-grow-1" style="max-width: 800px">
        <div class="tile-header">
            <h2 class="tile-title fw-semibold">
                <i class="icon icon-state-alert"></i>
                <emm:messages var="msg_key" type="error">
                    <span class="text-truncate">${msg_key}</span>
                </emm:messages>
            </h2>
        </div>

        <c:if test="${not empty errorReport}">
            <div class="tile-body">
                <div class="tile tile--xs tile--alert">
                    <div class="tile-header">
                        <h3 class="w-50"><mvc:message code="Text_Module" /></h3>
                        <h3 class="w-50"><mvc:message code="mailing.tag" /></h3>
                    </div>

                    <div class="tile-body">
                        <div class="row g-1">
                            <c:forEach var="reportRow" items="${errorReport}">
                                <div class="col-6">
                                    <p class="text-truncate">
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
                                    </p>
                                </div>

                                <div class="col-6">
                                    <p class="text-truncate">
                                        <c:choose>
                                            <c:when test="${not empty reportRow[1]}">
                                                ${reportRow[1]}
                                            </c:when>
                                            <c:otherwise>
                                                ${reportRow[2]}
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
    </div>
</emm:messagesPresent>
</body>
</html>
