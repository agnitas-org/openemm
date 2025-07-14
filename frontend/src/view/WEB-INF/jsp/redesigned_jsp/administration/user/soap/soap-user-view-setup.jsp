<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="webserviceUserForm" type="com.agnitas.emm.core.wsmanager.form.WebserviceUserForm"--%>

<c:set var="agnTitleKey" 			      value="settings.webservice.user" 	                                         scope="request" />
<c:set var="sidemenu_active" 		      value="Administration" 			                                      	 scope="request" />
<c:set var="sidemenu_sub_active" 	      value="UserActivitylog.Users" 			                                 scope="request" />
<c:set var="agnHighlightKey" 		      value="settings.webservice.user" 	                                         scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	      value="UserActivitylog.Users"	 		                                     scope="request" />
<c:url var="agnBreadcrumbsRootUrl"        value="/administration/wsmanager/usersRedesigned.action?restoreSort=true"  scope="request" />
<c:set var="agnEditViewKey" 	          value="soap-user-view" 	                                                 scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${webserviceUserForm.userName}"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="webservice.user.change">
        <emm:instantiate var="saveAction" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${saveAction}"/>

            <c:set target="${saveAction}" property="extraAttributes" value="data-form-target='#wsuser-edit-form' data-form-submit"/>
            <c:set target="${saveAction}" property="iconBefore" value="icon-save"/>
            <c:set target="${saveAction}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
