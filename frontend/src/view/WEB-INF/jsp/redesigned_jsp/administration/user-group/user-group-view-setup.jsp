<%@ page import="com.agnitas.emm.core.usergroup.web.UserGroupController" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="userGroupForm" type="com.agnitas.emm.core.usergroup.form.UserGroupForm"--%>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>"/>
<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>
<c:set var="ROOT_COMPANY_ID" value="<%= UserGroupController.ROOT_COMPANY_ID %>" scope="request"/>

<%-- Note: there's a group having id = 0, invalid ids are negative --%>
<c:set var="userGroupIsNew" value="${userGroupForm.id < 0}"/>

<c:set var="agnTitleKey" 			value="settings.Usergroup" 					                    scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 						                    scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Usergroups" 				                    scope="request" />
<c:set var="agnHighlightKey" 		value="settings.NewUsergroup" 				                    scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="settings.Usergroups" 		                            scope="request" />
<c:url var="agnBreadcrumbsRootUrl" 	value="/administration/usergroup/list.action?restoreSort=true" 	scope="request" />
<c:set var="agnHelpKey" 			value="managingUserGroups" 					                    scope="request" />
<c:set var="agnEditViewKey" 	    value="usergroup-view" 	                                        scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="userGroupId" value="${userGroupForm.id}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
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
                    <c:set target="${element}" property="extraAttributes" value="data-form-target='#userGroupForm' data-form-submit"/>
                    <c:set target="${element}" property="iconBefore" value="icon-save"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Create"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:when>
        <c:otherwise>
            <%-- Actions dropdown --%>
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

                <c:set target="${element}" property="cls" value="mobile-hidden"/>
                <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
                <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

                <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                    <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
                </emm:instantiate>

                <%-- Items for dropdown --%>
                <emm:ShowByPermission token="role.change">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="0" value="${option}"/>
                        <c:set target="${option}" property="type" value="href"/>
                        <c:set target="${option}" property="url">
                            <c:url value="/administration/usergroup/${userGroupForm.id}/copy.action"/>
                        </c:set>
                        <c:set target="${option}" property="name">
                            <mvc:message code="button.Copy" />
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>

                <c:if test="${userGroupForm.id ne sessionScope['groupID']}">
                    <c:if test="${userGroupForm.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}">
                        <emm:ShowByPermission token="role.delete">
                            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                                <c:set target="${optionList}" property="1" value="${option}"/>
                                <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                                <c:set target="${option}" property="type" value="href"/>
                                <c:set target="${option}" property="url">
                                    <c:url value="/administration/usergroup/deleteRedesigned.action?bulkIds=${userGroupForm.id}"/>
                                </c:set>
                                <c:set target="${option}" property="iconBefore" value="icon-trash-alt"/>
                                <c:set target="${option}" property="name">
                                    <mvc:message code="button.Delete"/>
                                </c:set>
                            </emm:instantiate>
                        </emm:ShowByPermission>
                    </c:if>
                </c:if>
            </emm:instantiate>

            <c:if test="${userGroupForm.companyId eq admin.companyID or admin.companyID eq ROOT_COMPANY_ID}">
                <emm:ShowByPermission token="role.change">
                    <emm:instantiate var="element" type="java.util.LinkedHashMap">
                        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

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
