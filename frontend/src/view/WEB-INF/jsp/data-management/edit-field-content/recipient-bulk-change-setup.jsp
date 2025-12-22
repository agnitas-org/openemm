<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="recipient.change.bulk" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.change.bulk" 	scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.change.bulk" 	scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="ImportExport" 			scope="request" />
<c:set var="agnHelpKey" 			value="Edit_field_content" 		scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}" />
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.change.bulk" />
    </emm:instantiate>
</emm:instantiate>

<c:url var="applyConfirmUrl" value="/recipient/bulkSave/confirm.action" />

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="actionItem" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${actionItem}"/>
		<c:set target="${actionItem}" property="extraAttributes" value="data-form-target='#recipientBulkForm' data-form-url='${applyConfirmUrl}' data-form-confirm"/>
		<c:set target="${actionItem}" property="iconBefore" value="icon-save"/>
		<c:set target="${actionItem}" property="name">
			<mvc:message code="button.Apply"/>
		</c:set>
    </emm:instantiate>
</emm:instantiate>
