<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="agnTitleKey" 			value="Targetgroups" 		     scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 		     scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	     scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	     scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Targetgroups" 		     scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 	     scope="request" />
<c:set var="agnEditViewKey" 	    value="target-groups-overview"   scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="targets.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/target/create.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>
