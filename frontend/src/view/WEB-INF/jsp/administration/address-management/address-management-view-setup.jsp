<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action"%>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="categories" type="java.util.List<com.agnitas.emm.core.address_management.enums.AddressManagementCategory>"--%>
<%--@elvariable id="entries" type="java.util.Map<com.agnitas.emm.core.address_management.enums.AddressManagementCategory, java.util.List>"--%>

<c:set var="agnNavigationKey" 		  value="admins" 				    scope="request" />
<c:set var="agnTitleKey" 			  value="user.adressmanagement" 	scope="request" />
<c:set var="sidemenu_active" 		  value="Administration" 			scope="request" />
<c:set var="sidemenu_sub_active" 	  value="settings.Admin" 			scope="request" />
<c:set var="agnHighlightKey" 		  value="user.adressmanagement" 	scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	  value="Administration" 			scope="request" />
<c:set var="agnEditViewKey" 	      value="address-management" 		scope="request" />

<c:set var="entriesExists" value="false" scope="request" />
<c:forEach var="category" items="${categories}">
    <c:if test="${fn:length(entries.get(category)) gt 0}">
        <c:set var="entriesExists" value="true" scope="request" />
    </c:if>
</c:forEach>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="user.adressmanagement"/>
    </emm:instantiate>
</emm:instantiate>

<c:if test="${entriesExists}">
    <emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="extraAttributes" value="data-action='update-all' data-form-target='#address-management-form'"/>
            <c:set target="${element}" property="iconBefore" value="icon-pen"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.update.entries"/>
            </c:set>
        </emm:instantiate>

        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            <c:set target="${element}" property="extraAttributes" value="data-action='delete-all' data-form-target='#address-management-form'"/>
            <c:set target="${element}" property="iconBefore" value="icon-trash-alt"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.delete.entries"/>
            </c:set>
        </emm:instantiate>
    </emm:instantiate>
</c:if>
