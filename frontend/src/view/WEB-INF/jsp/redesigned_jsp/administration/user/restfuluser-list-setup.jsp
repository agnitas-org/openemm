<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="agnNavigationKey" 		value="admins" 				      scope="request" />
<c:set var="agnTitleKey" 			value="settings.RestfulUser" 	  scope="request" />
<c:set var="agnSubtitleKey"	 		value="settings.RestfulUser" 	  scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		      scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 		      scope="request" />
<c:set var="agnHighlightKey" 		value="settings.RestfulUser" 	  scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				      scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		      scope="request" />
<c:set var="agnHelpKey" 			value="User" 				      scope="request" />
<c:set var="agnEditViewKey" 	    value="restful-admins-overview"   scope="request" />

<emm:ShowByPermission token="admin.new">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/restfulUser/create.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.RestfulUser"/>
    </emm:instantiate>
</emm:instantiate>
