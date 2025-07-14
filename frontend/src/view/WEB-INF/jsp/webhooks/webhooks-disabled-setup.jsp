<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey"		value="none" 					scope="request" />
<c:set var="agnTitleKey" 			value="webhooks" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="webhooks" 				scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport"	 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="webhooks" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="webhooks" 				scope="request" />
<c:set var="agnHelpKey" 			value="webhooks" 				scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-list-alt far"></i> <mvc:message code="webhooks"/>
</c:set>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>    
    
<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element}" property="extraAttributes" value="data-action='saveUserForm'"/>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Save"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
