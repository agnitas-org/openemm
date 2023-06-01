<%@ page import="com.agnitas.emm.core.usergroup.web.UserGroupController" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>
<c:set var="ROOT_COMPANY_ID" value="<%= UserGroupController.ROOT_COMPANY_ID %>" scope="request"/>

<%-- Note: there's a group having id = 0, invalid ids are negative --%>
<c:set var="userGroupIsNew" value="${userGroupForm.id < 0}"/>

<c:set var="isTabsMenuShown" 		value="false" 								scope="request" />
<c:set var="agnNavigationKey" 		value="none" 							    scope="request" />
<c:set var="agnTitleKey" 			value="settings.Usergroup" 					scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.Usergroup" 					scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Usergroups" 				scope="request" />
<c:set var="agnHighlightKey" 		value="settings.NewUsergroup" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 								scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 						scope="request" />
<c:set var="agnHelpKey" 			value="managingUserGroups" 					scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="userGroupId" value="${userGroupForm.id}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Usergroups"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/administration/usergroup/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${userGroupIsNew}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.NewUsergroup"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userGroupForm.shortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${userGroupIsNew}">
            <emm:ShowByPermission token="role.change">
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                    <c:set target="${element}" property="extraAttributes" value="data-form-target='#userGroupForm' data-form-submit"/>
                    <c:set target="${element}" property="iconBefore" value="icon-save"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Create"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:when>
        <c:otherwise>
        <emm:ShowByPermission token="role.change">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
                <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                <c:set target="${element}" property="type" value="href"/>
                <c:set target="${element}" property="url">
                    <c:url value="/administration/usergroup/${userGroupForm.id}/copy.action"/>
                </c:set>
                <c:set target="${element}" property="iconBefore" value="icon-copy"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Copy"/>
                    </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
            <logic:notEqual name="groupID" scope="session" value="${userGroupForm.id}">
            	<c:if test="${userGroupForm.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}">
                	<emm:ShowByPermission token="role.delete">
                    	<emm:instantiate var="element" type="java.util.LinkedHashMap">
                        	<c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                        	<c:set target="${element}" property="btnCls" value="btn btn-regular btn-alert"/>
                        	<c:set target="${element}" property="extraAttributes" value="data-confirm"/>
                            <c:set target="${element}" property="type" value="href"/>
                            <c:set target="${element}" property="url">
                                <c:url value="/administration/usergroup/${userGroupForm.id}/confirmDelete.action"/>
                            </c:set>
                        	<c:set target="${element}" property="iconBefore" value="icon-trash-o"/>
                        	<c:set target="${element}" property="name">
                            	<mvc:message code="button.Delete"/>
                        	</c:set>
                    	</emm:instantiate>
                	</emm:ShowByPermission>
                </c:if>
            </logic:notEqual>

			<c:if test="${userGroupForm.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}">
            	<emm:ShowByPermission token="role.change">
                	<emm:instantiate var="element" type="java.util.LinkedHashMap">
                    	<c:set target="${itemActionsSettings}" property="1" value="${element}"/>

                    	<c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                    	<c:set target="${element}" property="extraAttributes" value="data-form-target='#userGroupForm' data-form-submit"/>
                    	<c:set target="${element}" property="iconBefore" value="icon-save"/>
                    	<c:set target="${element}" property="name">
                        	<mvc:message code="button.Save"/>
                    	</c:set>
                	</emm:instantiate>
            	</emm:ShowByPermission>
            </c:if>
        </c:otherwise>
    </c:choose>
</emm:instantiate>
