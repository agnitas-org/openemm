<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingOverviewForm" type="com.agnitas.emm.core.mailing.forms.MailingOverviewForm"--%>

<emm:CheckLogon/>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="isNewItemAvailable" 	value="false"					scope="request" />

<c:choose>
    <c:when test="${mailingOverviewForm.forTemplates}">
        <c:set var="agnNavigationKey" 		value="templates" 		scope="request" />
        <c:set var="agnTitleKey" 			value="Templates" 		scope="request" />
        <c:set var="agnSubtitleKey" 		value="Templates" 		scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		scope="request" />
        <c:set var="agnHelpKey" 			value="templateList"	scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
            </emm:instantiate>
        </emm:instantiate>

		<emm:ShowByPermission token="mailing.import">
			<c:set var="importItemUrl" value="/import/template.action" scope="request"/>
			<c:set var="importItemLabelKey" value="template.import"    scope="request"/>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="template.change">
            <c:set var="isNewItemAvailable" value="true"/>
            <c:set var="newItemUrl" value="/mailing/new.action?isTemplate=true" scope="request"/>
            <c:set var="newItemLabelKey" value="mailing.New_Template" scope="request"/>
        </emm:ShowByPermission>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="MailingsOverview"                                 scope="request"/>
        <c:set var="agnTitleKey" value="Mailings"                                              scope="request"/>
        <c:set var="agnSubtitleKey" value="Mailings"                                           scope="request"/>
        <c:set var="sidemenu_sub_active" value="default.Overview"                              scope="request"/>
        <c:set var="agnHelpKey" value="mailingList"                                            scope="request"/>
        <emm:ShowByPermission token="mailing.import">
            <c:set var="importItemUrl" value="/import/mailing.action"                          scope="request"/>
            <c:set var="importItemLabelKey" value="mailing.import"                             scope="request"/>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.change">
                <c:set var="isNewItemAvailable" value="true"/>
                    <c:set var="newItemUrl" value="/mailing/create.action"                     scope="request"/>
                <c:set var="newItemLabelKey" value="mailing.New_Mailing"                       scope="request"/>
        </emm:ShowByPermission>
    </c:otherwise>
</c:choose>

<emm:ShowByPermission token="mailing.import">
    <c:url var="createNewItemUrl" value="${importItemUrl}" scope="request"/>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="${importItemLabelKey}"/>
    </c:set>
</emm:ShowByPermission>

<c:if test="${isNewItemAvailable}">
    <c:url var="createNewItemUrl2" value="${newItemUrl}" scope="request"/>
    <c:set var="createNewItemLabel2" scope="request">
        <mvc:message code="${newItemLabelKey}"/>
    </c:set>
</c:if>
