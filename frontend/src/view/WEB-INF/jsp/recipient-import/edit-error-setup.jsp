<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="none"		 				scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="importStep3" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:url var="importViewLink" value="/recipient/import/view.action" />

        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.csv_upload"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${importViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.edit.data"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element0" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-warning"/>
        <c:set target="${element0}" property="extraAttributes" value="data-action='ignore-errors'"/>
        <c:set target="${element0}" property="iconBefore" value="icon-warning"/>
        <c:set target="${element0}" property="name">
            <mvc:message code="Ignore"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="element1" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-secondary btn-regular"/>
        <c:set target="${element1}" property="iconBefore" value="icon-times"/>
        <c:set target="${element1}" property="extraAttributes" value="data-action='cancel'"/>
        <c:set target="${element1}" property="name">
            <mvc:message code="button.Cancel"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="element2" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
        <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element2}" property="extraAttributes" value="data-form-target='#errors-form' data-form-submit-event"/>
        <c:set target="${element2}" property="iconBefore" value="icon-save"/>
        <c:set target="${element2}" property="name">
            <mvc:message code="button.Save"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
