<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComTargetAction" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_VIEW" value="<%= ComTargetAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="targets.show"/>

<c:set var="agnNavigationKey" 		value="targets" 			scope="request" />
<c:set var="agnTitleKey" 			value="Targetgroups" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="Targets" 			scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true"	 			scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Targetgroups" 		scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 	scope="request" />

<emm:ShowByPermission token="targets.change">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/newTargetGroup.do'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="target.NewTarget"/>
    </c:set>
</emm:ShowByPermission>
