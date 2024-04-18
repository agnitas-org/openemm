<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>
<%--@elvariable id="adminForm" type="com.agnitas.emm.core.admin.form.AdminForm"--%>
<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="${isRestfulUser ? 'restfulUser' : 'admin'}.show"/>

<c:set var="path" value="${isRestfulUser ? 'restfulUser' : 'admin'}"/>

<c:set var="agnTitleKey" 			value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}"  scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 					                     scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 					                     scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							                     scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 					                     scope="request" />
<c:set var="agnHelpKey" 			value="newUser" 						                     scope="request" />
<c:set var="agnEditViewKey" 	    value="user-view" 	                                         scope="request" />

<emm:sideMenuAdditionalParam name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="adminID" value="${adminForm.adminID}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${adminForm.adminID ne 0}">
        <c:set var="agnNavigationKey"	value="${isRestfulUser ? 'restfulUser' : 'admin'}" 				scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.${isRestfulUser ? 'RestfulUser' : 'admin'}.edit" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="admins" 				scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="${isRestfulUser ? 'settings.RestfulUser' : 'UserActivitylog.Users'}" 		scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/${path}/list.action">
                <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${adminForm.adminID eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.${isRestfulUser ? 'New_RestfulUser' : 'New_Admin'}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${adminForm.username}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${adminForm.adminID eq 0}">
            <emm:ShowByPermission token="admin.new">
                <c:url var="createUrl" value="/${path}/saveNew.action"/>
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn"/>
                    <c:set target="${element}" property="extraAttributes" value="data-form-url='${createUrl}' data-form-target='#user-form' data-form-submit"/>
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

                <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
                <c:set target="${element}" property="cls" value="mobile-hidden"/>
                <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
                <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
                <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

                <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                    <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
                </emm:instantiate>

                <%-- Items for dropdown --%>

                <c:if test="${adminForm.adminID > 0}">
                    <%@ include file="fragments/manage-approval-btn.jspf" %>
                </c:if>

                <emm:ShowByPermission token="admin.delete">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="1" value="${option}"/>
                        <c:set target="${option}" property="url"><c:url value="/${path}/${adminForm.adminID}/confirmDelete.action"/></c:set>
                        <c:set target="${option}" property="extraAttributes" value="data-confirm=''"/>
                        <c:set target="${option}" property="name">
                            <mvc:message code="settings.admin.delete" />
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </emm:instantiate>

            <emm:ShowByPermission token="admin.change">
                <c:url var="saveUrl" value="/${path}/${adminForm.adminID}/save.action"/>
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn"/>
                    <c:set target="${element}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#user-form' data-form-submit"/>
                    <c:set target="${element}" property="iconBefore" value="icon-save"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Save"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:otherwise>
    </c:choose>
</emm:instantiate>
