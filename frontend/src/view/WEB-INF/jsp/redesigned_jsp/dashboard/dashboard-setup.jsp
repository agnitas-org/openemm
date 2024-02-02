<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnTitleKey" 			value="default.A_EMM" 		       scope="request" />
<c:set var="agnSubtitleKey" 		value="Dashboard" 			       scope="request" />
<c:set var="sidemenu_active" 		value="Dashboard" 			       scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview"           scope="request" />
<c:set var="agnHelpKey" 			value="dashboard" 			       scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="icon" value="home"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Dashboard"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-primary rounded-1 header__action"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-pen"/>
        <c:set target="${element}" property="extraAttributes" value="id='dashboard-stop-edit-btn' data-action='stop-editing'" />
        <c:set target="${element}" property="name">
            <mvc:message code="button.Save" />
        </c:set>
    </emm:instantiate>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-primary rounded-1 header__action"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-grip-horizontal"/>
        <c:set target="${element}" property="extraAttributes" value="id='dashboard-select-layout-btn' data-action='select-layout'" />
        <c:set target="${element}" property="name">
            <mvc:message code="dashboard.layout.change" />
        </c:set>
    </emm:instantiate>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-primary rounded-1 header__action"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-pen"/>
        <c:set target="${element}" property="extraAttributes" value="id='dashboard-start-edit-btn' data-action='edit-dashboard'" />
        <c:set target="${element}" property="name">
            <mvc:message code="button.dashboard.change" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
