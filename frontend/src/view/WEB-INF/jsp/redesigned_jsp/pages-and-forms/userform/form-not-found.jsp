<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="SHOW_SUPPORT_BUTTON" type="java.lang.Boolean"--%>
<%--@elvariable id="userFormSupportForm" type="com.agnitas.web.UserFormSupportForm"--%>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <sec:csrfMetaTags />

    <title><mvc:message code="FormNotFoundTitle" /></title>
    <link rel="shortcut icon" href="favicon.ico">

    <c:url var="applicationCssLink" value="/assets/landing.min.css">
        <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
    </c:url>
    <link type="text/css" rel="stylesheet" href="${applicationCssLink}">

    <c:url var="configJsLink" value="/assets/config.js">
        <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
    </c:url>
    <script src="${configJsLink}"></script>

    <c:url var="applicationJsLink" value="/assets/application.redesigned.js">
        <c:param name="ts" value="<%= AgnUtils.getBrowserCacheMarker() %>"/>
    </c:url>
    <script src="${applicationJsLink}"></script>
</head>
    <body class="systempage">
        <div class="tile tile--notification">
            <div class="tile-header">
                <div class="emm-logo">
                    <img class="emm-logo__image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'">
                    <p class="emm-logo__headline"><mvc:message code="default.EMM" /></p>
                    <p class="emm-logo__version"><mvc:message code="default.version" /></p>
                </div>
                <img class="edition-logo" src="${editionLogoSrc}">
            </div>
            <div class="tile-body centered-box">
                <mvc:message code="FormNotFoundMessage" />
            </div>
            <c:if test="${SHOW_SUPPORT_BUTTON}">
                <mvc:form cssClass="tile-footer" servletRelativeAction="/support/sendFormReport.action" modelAttribute="userFormSupportForm">

                    <c:forEach var="parameter" items="${userFormSupportForm.params}">
                        <c:forEach var="parameterValue" items="${parameter.value}">
                            <mvc:hidden path="params['${parameter.key}']" value="${parameterValue}"/>
                        </c:forEach>
                    </c:forEach>
                    <mvc:hidden path="url" />
                    <button type="button" class="btn btn-primary flex-grow-1" data-form-submit>
                        <mvc:message code="button.Send"/>
                    </button>
                </mvc:form>
            </c:if>
        </div>
    </body>
</html>
