<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="subscriber_list"        scope="request" />
<c:set var="agnTitleKey" 			value="Mailinglists" 	       scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailinglists" 	       scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 		       scope="request" />
<c:set var="sidemenu_sub_active" 	value="Mailinglists" 	       scope="request" />
<c:set var="agnHighlightKey" 		value="Mailinglists" 	       scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			       scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Recipients" 		       scope="request" />
<c:set var="agnHelpKey" 			value="mailinglists"	       scope="request" />
<c:set var="agnEditViewKey" 	    value="mailinglists-overview"  scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Mailinglists"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="mailinglist.delete">
    <emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <%-- Actions dropdown --%>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-action='bulk-delete'"/>
                <c:set target="${option}" property="url">#</c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="bulkAction.delete.mailinglist"/>
                </c:set>
            </emm:instantiate>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:ShowByPermission token="mailinglist.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/mailinglist/create.action"/></c:set>
            <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>
