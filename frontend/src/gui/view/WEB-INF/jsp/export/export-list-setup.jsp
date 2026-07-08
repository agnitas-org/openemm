<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			       value="export" 				          scope="request"/>
<c:set var="sidemenu_active" 		       value="ImportExport" 		          scope="request"/>
<c:set var="sidemenu_sub_active" 	       value="export" 				          scope="request"/>
<c:set var="agnHighlightKey" 		       value="export" 				          scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	       value="manage.tables.exportProfiles"   scope="request"/>
<c:set var="agnHelpKey" 			       value="export" 				          scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="export.change">
	<emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
		<emm:instantiate var="element" type="java.util.LinkedHashMap">
			<c:set target="${newResourceSettings}" property="0" value="${element}"/>
			<c:set target="${element}" property="url"><c:url value="/export/0/view.action"/></c:set>
		</emm:instantiate>
	</emm:instantiate>
</emm:ShowByPermission>
