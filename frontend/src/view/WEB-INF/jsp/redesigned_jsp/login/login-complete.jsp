<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%--@elvariable id="webStorageBundleNames" type="java.util.List"--%>
<%--@elvariable id="isFrameShown" type="java.lang.Boolean"--%>
<%--@elvariable id="adminId" type="java.lang.Integer"--%>

<!DOCTYPE html>
<html>
    <head>
        <c:url var="startPageLink" value="/startRedesigned.action"/>
        <%-- Proceed in 3 seconds anyway (even if JS code failed) --%>
        <meta http-equiv="refresh" content="3; URL='${startPageLink}'">

        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <sec:csrfMetaTags />

        <title><mvc:message code="logon.title"/></title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insertTemplate template="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp"/>
    </head>
    <body>
        <mvc:form action="${startPageLink}" data-form="static" method="POST" data-initializer="logon-complete">
            <%-- To be populated by JS code --%>
            <input type="hidden" name="webStorageJson" value=""/>
            <script id="config:logon-complete" type="application/json">
                {
                    "webStorageBundleNames": ${emm:toJson(webStorageBundleNames)},
                    "isFrameShown": ${isFrameShown},
                    "adminId": ${adminId}
                }
            </script>
        </mvc:form>
    </body>
</html>