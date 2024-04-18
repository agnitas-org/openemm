<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>

<c:set var="agnTitleKey" 			        value="recipient.fields" 	                            scope="request" />
<c:set var="agnSubtitleKey" 		        value="recipient.fields" 	                            scope="request" />
<c:set var="sidemenu_active" 		        value="Recipients" 			                            scope="request" />
<c:set var="sidemenu_sub_active" 	        value="recipient.fields" 	                            scope="request" />
<c:set var="isBreadcrumbsShown" 	        value="true" 				                            scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	        value="Recipients" 			                            scope="request" />
<mvc:message var="agnHeadLineFirstCrumb"    code="recipient.fields"                                 scope="request" />
<c:url var="agnBreadcrumbsRootUrl" 	        value="/profiledb/profiledb.action?restoreSort=true"	scope="request" />
<c:set var="agnHelpKey" 			        value="newProfileField" 	                            scope="request" />
<c:set var="agnEditViewKey" 	            value="profile-field-view" 	                            scope="request" />

<c:set var="TMP_FIELDNAME" value="${profileForm.fieldname}"/>

<c:choose>
    <c:when test="${not empty TMP_FIELDNAME}">
        <c:set var="agnHighlightKey" value="settings.EditProfileDB_Field" scope="request" />
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="fieldname" value="${TMP_FIELDNAME}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnHighlightKey" value="settings.NewProfileDB_Field" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${empty TMP_FIELDNAME}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.NewProfileDB_Field"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${TMP_FIELDNAME}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty profileForm.fieldname}">
        <emm:ShowByPermission token="profileField.show">
            <c:url var="confirmDeleteUrl" value="/profiledb/${TMP_FIELDNAME}/delete.action"/>

            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                <c:set target="${element}" property="btnCls" value="btn js-confirm"/>
                <c:set target="${element}" property="type" value="href"/>
                <c:set target="${element}" property="cls" value="mobile-hidden"/>
                <c:set target="${element}" property="url" value="${confirmDeleteUrl}"/>
                <c:set target="${element}" property="iconBefore" value="icon icon-trash-alt"/>
                <c:set target="${element}" property="name">
                    <mvc:message code="button.Delete" />
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>

    <emm:ShowByPermission token="profileField.show">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon icon-save"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#settings-tile' data-form-submit" />
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save" />
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
