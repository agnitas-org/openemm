<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		value="admins" 				      scope="request" />
<c:set var="agnTitleKey" 			value="settings.RestfulUser" 	  scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		      scope="request" />
<c:set var="sidemenu_sub_active" 	value="UserActivitylog.Users" 	  scope="request" />
<c:set var="agnHighlightKey" 		value="settings.RestfulUser" 	  scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="UserActivitylog.Users" 	  scope="request" />
<c:set var="agnHelpKey" 			value="User" 				      scope="request" />
<c:set var="agnEditViewKey" 	    value="restful-admins-overview"   scope="request" />

<emm:ShowByPermission token="admin.new">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/restfulUser/create.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="url"><c:url value="/restfulUser/list/export/csv.action"/></c:set>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="name"><mvc:message code="user.export.csv"/></c:set>
            </emm:instantiate>

            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="1" value="${option}"/>
                <c:set target="${option}" property="url"><c:url value="/restfulUser/list/export/pdf.action"/></c:set>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="name"><mvc:message code="user.export.pdf"/></c:set>
            </emm:instantiate>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
