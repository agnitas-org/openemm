<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="showSupervisorPermissionManagement" type="java.lang.Boolean"--%>

<c:choose>
    <c:when test="${showSupervisorPermissionManagement}">
        <c:set var="agnNavigationKey" value="userselfservice_with_sv_login" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="userselfservice" scope="request" />
    </c:otherwise>
</c:choose>

<c:set var="agnTitleKey" 			value="settings.Admin" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Welcome" 					scope="request" />
<c:set var="sidemenu_active" 		value="none" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="none"	 					scope="request" />
<c:set var="agnHighlightKey" 		value="settings.admin.edit" 		scope="request" />
<c:set var="agnHelpKey" 			value="user_self-administration" 	scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-cog"></i> ${sessionScope['userName']}
</c:set>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#selfForm' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Save"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
