<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<html>
<tiles:insertAttribute name="head-tag"/>
<body class="${emmLayoutBase.themeType eq DARK_MODE_THEME_TYPE ? 'dark-theme' : ''}">
    <tiles:insertAttribute name="body"/>
</body>
</html>
