<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

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
    <c:url var="createNewItemUrl" scope="request" value="/target/create.action"/>
    <mvc:message var="createNewItemLabel" code="target.NewTarget" scope="request"/>
</emm:ShowByPermission>
