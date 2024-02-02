<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="emmLayoutBase" type="com.agnitas.beans.EmmLayoutBase"--%>
<emm:setAbsolutePath var="absoluteCssPath" path="${emmLayoutBase.cssURL}"/>

<link type="text/css" rel="stylesheet" href="${absoluteCssPath}/style.css">
<link type="text/css" rel="stylesheet" href="${absoluteCssPath}/structure.css">
<link type="text/css" rel="stylesheet" href="${absoluteCssPath}/displaytag.css">
<link type="text/css" rel="stylesheet" href="${absoluteCssPath}/ie7.css">

<style type="text/css">
    html, body {
        height: 100%;
        margin: 0 auto;
        padding: 0;
    }

    body {
        position: relative;
    }
</style>
