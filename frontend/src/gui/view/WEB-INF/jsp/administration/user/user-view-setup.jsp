<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="adminForm" type="com.agnitas.emm.core.admin.form.AdminForm"--%>
<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>

<c:set var="path" value="${isRestfulUser ? 'restfulUser' : 'admin'}" />
<c:url var="overviewLink" value="/${path}/list.action" />

<c:set var="agnTitleKey" 			       value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}"  scope="request" />
<c:set var="sidemenu_active" 		       value="Administration" 					                    scope="request" />
<c:set var="sidemenu_sub_active" 	       value="UserActivitylog.Users" 					            scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	       value="UserActivitylog.Users" 					            scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	       value="${overviewLink}"	                                    scope="request" />
<c:set var="agnHelpKey" 			       value="newUser" 						                        scope="request" />
<c:set var="agnEditViewKey" 	           value="user-view" 	                                        scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="adminID" value="${adminForm.adminID}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${adminForm.adminID ne 0}">
        <c:set var="agnNavigationKey"	value="${isRestfulUser ? 'restfulUser' : 'admin'}" 				scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.${isRestfulUser ? 'RestfulUser' : 'admin'}.edit" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="admins" 				scope="request" />
        <c:set var="agnHighlightKey" 	value="${isRestfulUser ? 'settings.RestfulUser' : 'UserActivitylog.Users'}" 		scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
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

                <c:set target="${element}" property="cls" value="mobile-hidden"/>
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
                        <c:set target="${option}" property="url">
                            <c:url value="/admin/delete.action">
                                <c:param name="bulkIds" value="${adminForm.adminID}" />
                                <c:if test="${isRestfulUser}">
                                    <c:param name="backToUrl" value="/restfulUser/list.action" />
                                </c:if>
                            </c:url>
                        </c:set>
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
