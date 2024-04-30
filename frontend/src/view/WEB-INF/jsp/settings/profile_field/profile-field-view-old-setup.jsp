<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>

<c:set var="agnTitleKey" 			value="recipient.fields" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.fields" 	scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.fields" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 			scope="request" />
<c:set var="agnHelpKey" 			value="newProfileField" 	scope="request" />

<c:set var="TMP_FIELDNAME" value="${profileForm.fieldname}"/>

<c:choose>
    <c:when test="${not empty TMP_FIELDNAME}">
        <c:set var="agnNavigationKey" 	value="profiledbEdit" 					scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.EditProfileDB_Field" 	scope="request" />
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="fieldname" value="${TMP_FIELDNAME}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="profiledb" 					scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.NewProfileDB_Field" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.fields"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/profiledbold/profiledb.action"/>
        </c:set>
    </emm:instantiate>

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

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty profileForm.fieldname}">
        <emm:ShowByPermission token="profileField.show">
            <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">

                <c:url var="confirmDeleteUrl" value="/profiledbold/${TMP_FIELDNAME}/confirmDelete.action"/>

                <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
                <c:set target="${element0}" property="type" value="href"/>
                <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert js-confirm"/>
                <c:set target="${element0}" property="url" value="${confirmDeleteUrl}"/>
                <c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${element0}" property="name">
                    <mvc:message code="button.Delete"/>
                </c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </c:if>
    <emm:ShowByPermission token="profileField.show">
        <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#profileFieldForm' data-form-submit-event"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </jsp:useBean>
    </emm:ShowByPermission>
</jsp:useBean>
