<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:choose>
	<c:when test="${SHOW_SUPERVISOR_PERMISSION_MANAGEMENT}">
		<c:set var="agnNavigationKey"		value="userselfservice_with_sv_login"				scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="agnNavigationKey"		value="userselfservice"				scope="request" />
	</c:otherwise>
</c:choose>

<c:set var="agnTitleKey" 			value="settings.Admin" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Welcome" 					scope="request" />
<c:set var="sidemenu_active" 		value="none" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="none"	 					scope="request" />
<c:set var="agnHighlightKey" 		value="settings.admin.edit" 		scope="request" />
<c:set var="agnHelpKey" 			value="user_self-administration" 	scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-cog"></i> ${sessionScope['fullName']}
</c:set>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element1}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#adminForm' data-form-submit"/>
        <c:set target="${element1}" property="iconBefore" value="icon-save"/>
        <c:set target="${element1}" property="name">
            <bean:message key="button.Save"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
