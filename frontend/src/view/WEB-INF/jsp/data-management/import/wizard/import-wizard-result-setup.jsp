<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			  value="import.Wizard" 			             scope="request" />
<c:set var="sidemenu_active" 		  value="ImportExport" 			                 scope="request" />
<c:set var="sidemenu_sub_active" 	  value="import.csv_upload"		                 scope="request" />
<c:set var="agnHighlightKey" 		  value="import.Wizard" 			             scope="request" />
<c:set var="agnBreadcrumbsRootKey"    value="import"		 		                 scope="request" />
<c:url var="agnBreadcrumbsRootUrl"    value="/recipient/import/chooseMethod.action"  scope="request" />
<c:set var="agnHelpKey"               value="classicImport"                          scope="request" />

<c:set var="status" value="${importWizardSteps.helper.status}" scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.Wizard"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="type" value="href"/>
        <c:set target="${element}" property="url">
            <c:url value='/recipient/import/wizard/downloadCsv.action?downloadName=result'/>
        </c:set>
        <c:set target="${element}" property="extraAttributes" value="data-prevent-load"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Download" />
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="type" value="href"/>
        <c:set target="${element}" property="url">
            <c:url value="/recipient/list.action">
                <c:param name="latestDataSourceId" value="${status.datasourceID}"/>
            </c:url>
        </c:set>
        <c:set target="${element}" property="extraAttributes" value=""/>
        <c:set target="${element}" property="iconBefore" value="icon-check"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Finish" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
