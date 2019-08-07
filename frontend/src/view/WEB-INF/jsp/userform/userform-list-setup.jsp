<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComUserFormEditAction, org.agnitas.web.UserFormEditAction" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= UserFormEditAction.ACTION_LIST %>" scope="request" />
<c:set var="ACTION_VIEW" value="<%= UserFormEditAction.ACTION_VIEW %>" scope="request" />
<c:set var="ACTION_NEW" value="<%= UserFormEditAction.ACTION_NEW %>" scope="request" />
<c:set var="ACTION_IMPORT" value="<%= ComUserFormEditAction.ACTION_IMPORT %>" scope="request" />
<c:set var="ACTION_CONFIRM_DELETE" value="<%= UserFormEditAction.ACTION_CONFIRM_DELETE %>" scope="request" />
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComUserFormEditAction.ACTION_BULK_CONFIRM_DELETE %>" scope="request" />

<emm:CheckLogon/>
<emm:Permission token="forms.show"/>

<c:set var="agnNavigationKey"		value="FormsOverview" 		scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.panel.forms" 				scope="request" />
<c:set var="sidemenu_active" 		value="SiteActions" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="SiteActions" 		scope="request" />
<c:set var="agnHelpKey" 			value="formList" 			scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-list-alt"></i> <bean:message key="workflow.panel.forms"/>
</c:set>

<emm:ShowByPermission token="forms.import">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/userform.do">
            <c:param name="action" value="${ACTION_IMPORT}"/>
        </c:url>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="forms.import"/>
    </c:set>
</emm:ShowByPermission>
<emm:ShowByPermission token="forms.change">
    <c:set var="createNewItemUrl2" scope="request">
        <c:url value="/userform.do">
            <c:param name="action" value="${ACTION_NEW}"/>
        </c:url>
    </c:set>
    <c:set var="createNewItemLabel2" scope="request">
        <bean:message key="New_Form"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.panel.forms"/>
    </emm:instantiate>
</emm:instantiate>
