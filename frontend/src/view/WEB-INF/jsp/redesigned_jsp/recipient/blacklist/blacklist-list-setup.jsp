<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			value="recipient.Blacklist"    scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.Blacklist"    scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 			   scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.Blacklist"    scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.Blacklist"    scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 			   scope="request" />
<c:set var="agnHelpKey" 			value="blacklist" 			   scope="request" />
<c:set var="agnNavigationKey"       value="blacklist"              scope="request" />
<c:set var="agnEditViewKey" 	    value="blacklist-overview" 	   scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.Blacklist"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="url"><c:url value="/recipients/blacklist/download.action"/></c:set>
                <c:set target="${option}" property="name"><mvc:message code="BlacklistDownload"/></c:set>
            </emm:instantiate>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
