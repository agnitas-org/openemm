<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileForm"--%>
<%--@elvariable id="isUserHasPermissionForSelectedMode" type="java.lang.Boolean"--%>

<c:set var="profileExists" value="${form.id != 0}" />

<c:set var="sidemenu_active" 		 value="ImportExport" 			                       scope="request" />
<c:set var="sidemenu_sub_active" 	 value="manage.tables.importProfiles"	               scope="request" />
<c:set var="agnBreadcrumbsRootKey"	 value="manage.tables.importProfiles" 			       scope="request" />
<c:set var="agnHelpKey"			     value="newImportProfile" 		                       scope="request" />
<c:set var="agnEditViewKey" 	     value="import-profile-view"                           scope="request" />
<c:url var="agnBreadcrumbsRootUrl"   value="/import-profile/list.action?restoreSort=true"  scope="request" />

<c:choose>
    <c:when test="${profileExists}">
        <c:set var="agnTitleKey" value="import.ImportProfile" scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnTitleKey" value="import.NewImportProfile" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${profileExists}">
                <c:set target="${agnBreadcrumb}" property="text" value="${form.name}" />
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="import.NewImportProfile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
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
            <c:if test="${profileExists}">
                <emm:ShowByPermission token="import.change">
                    <%@include file="fragments/create-auto-import-option.jspf" %>

                    <c:url var="saveAndStartUrl" value="/import-profile/save.action">
                        <c:param name="start" value="true" />
                    </c:url>

                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="1" value="${option}"/>

                        <c:set target="${option}" property="extraAttributes" value="data-form-target='#import-profile-view' data-form-url='${saveAndStartUrl}' data-action='save'"/>
                        <c:set target="${option}" property="name"><mvc:message code="button.Import_Start"/></c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>

                <emm:ShowByPermission token="import.delete">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap">
                        <c:set target="${optionList}" property="2" value="${option}"/>

                        <c:set target="${option}" property="extraAttributes" value="data-confirm=''"/>
                        <c:set target="${option}" property="url"><c:url value="/import-profile/delete.action?ids=${form.id}"/></c:set>
                        <c:set target="${option}" property="name"><mvc:message code="button.Delete"/></c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>
        </emm:instantiate>
    </emm:instantiate>

    <emm:ShowByPermission token="import.change">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

            <c:set target="${element}" property="extraAttributes" value="data-form-target='#import-profile-view' data-action='save'"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
