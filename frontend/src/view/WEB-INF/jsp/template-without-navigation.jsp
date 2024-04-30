<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<tiles:insert attribute="page-setup"/>

<!--[if IE 9 ]> <html class="ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html class=""> <!--<![endif]-->
<tiles:insert attribute="head-tag"/>

<body class="${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE ? 'dark-theme' : ''}">

    <div class="loader">
        <i class="icon icon-refresh icon-spin"></i> <mvc:message code="default.loading" />
    </div>

    <main>
        <tiles:insert attribute="body"/>
    </main>

    <tiles:insert attribute="messages"/>

</body>
</html>
