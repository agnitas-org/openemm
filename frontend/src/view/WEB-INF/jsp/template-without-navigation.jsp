<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<tiles:insertAttribute name="page-setup"/>

<!--[if IE 9 ]> <html class="ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html class=""> <!--<![endif]-->
<tiles:insertAttribute name="head-tag"/>

<body class="${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE ? 'dark-theme' : ''}">

    <div class="loader">
        <i class="icon icon-refresh icon-spin"></i> <mvc:message code="default.loading" />
    </div>

    <main>
        <tiles:insertAttribute name="body"/>
    </main>

    <tiles:insertAttribute name="messages"/>

</body>
</html>
